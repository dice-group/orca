# Adding crawlers
There are two decisions to make when adding a crawler:

- how the crawler (or an adapter for it) should communicate with the HOBBIT platform:
    - use [HOBBIT core library](https://github.com/hobbit-project/core) (recommended)
    - write your own HOBBIT API client
- how to organize the HOBBIT system project:
    - actual crawler will be started separately in a Docker container (recommended)
    - the same process will communicate with HOBBIT and do the crawling

## Metadata file
The system resource in your system metadata file (`system.ttl`)
should have a triple with the property [`hobbit:implementsAPI`](http://w3id.org/hobbit/vocab#implementsAPI)
and the object `https://github.com/dice-group/ldcbench#Api` (defined in ORCA's `benchmark.ttl`)
to appear in the list of systems compatible with ORCA.

### Example metadata file
```turtle
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix hobbit: <http://w3id.org/hobbit/vocab#> .

<system_resource_URI_here> a hobbit:SystemInstance;
  rdfs:label "System name here"@en;
  rdfs:comment "System description here."@en;
  hobbit:implementsAPI <https://github.com/dice-group/ldcbench#Api>;
  hobbit:imageName "system_docker_image_name_here" .
```

## Using the HOBBIT core library
- [Integrating a system into the HOBBIT platform (tutorial)](https://hobbit-project.github.io/system_integration.html)

The HOBBIT core library provides several base classes which simplify
the interaction with HOBBIT platform.

Your implementation should extend
[`AbstractSystemAdapter`](https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/AbstractSystemAdapter.java).
Override `init` method if you need to initialize anything before benchmarking starts.
Override `receiveGeneratedData` method to receive SPARQL endpoint URI
and seed URIs and start the crawling.

### Crawler as a separate Docker container

In this case, the system would be just an adapter
which will receive the required information from ORCA
and start the crawler in a Docker container
using the HOBBIT API.
Additionally, the system needs to watch for container termination events
to terminate the system after the crawling is done or the crawler crashed.

```java
protected String instance;

@Override
public void init() throws Exception {
  super.init();
}

@Override
public void receiveGeneratedData(byte[] data) {
  ByteBuffer buffer = ByteBuffer.wrap(data);
  String sparqlUrl = RabbitMQUtils.readString(buffer);
  String sparqlUser = RabbitMQUtils.readString(buffer);
  String sparqlPwd = RabbitMQUtils.readString(buffer);
  List<String> seedURIs = new ArrayList<>(Arrays.asList(RabbitMQUtils.readString(buffer).split("\n")));

  String[] env = new String[]{ /* pass the arguments here */ };

  instance = createContainer("crawler-docker-image-name-here", Constants.CONTAINER_TYPE_SYSTEM, env);
}

@Override
public void receiveCommand(byte command, byte[] data) {
  if (command == Commands.DOCKER_CONTAINER_TERMINATED) {
    ByteBuffer buffer = ByteBuffer.wrap(data);
    String containerName = RabbitMQUtils.readString(buffer);
    int exitCode = buffer.get();
    containerStopped(containerName, exitCode);
  }
  super.receiveCommand(command, data);
}

public void containerStopped(String containerName, int exitCode) {
  if (instance != null && instance.equals(containerName)) {
    Exception e = null;
    if (exitCode != 0) {
      e = new IllegalStateException("Crawler terminated with exit code " + exitCode + ".");
    }
    ldSpiderInstance = null;
    terminate(e);
  } 
}

/* ORCA does not use this feature of the HOBBIT platform */
@Override
public void receiveGeneratedTask(String taskId, byte[] data) {
  throw new IllegalStateException();
}
```

### Crawling in the same process

In this case, the same process which receives information from ORCA
will do the crawling.
Typically some existing crawler classes would be called there.

```java
@Override
public void init() throws Exception {
  super.init();

  /* initialize your crawler here, if possible */
}

@Override
public void receiveGeneratedData(byte[] data) {
  ByteBuffer buffer = ByteBuffer.wrap(data);
  String sparqlUrl = RabbitMQUtils.readString(buffer);
  String sparqlUser = RabbitMQUtils.readString(buffer);
  String sparqlPwd = RabbitMQUtils.readString(buffer);
  List<String> seedURIs = new ArrayList<>(Arrays.asList(RabbitMQUtils.readString(buffer).split("\n")));

  /* start the crawling here */

  /* terminate when the crawling is finished (can be called from a different method) */
  terminate(null);
}

/* ORCA does not use this feature of the HOBBIT platform */
@Override
public void receiveGeneratedTask(String taskId, byte[] data) {
  throw new IllegalStateException();
}
```

### HOBBIT/ORCA API details (for custom implementations)
ORCA uses [generic HOBBIT API](https://hobbit-project.github.io/system_integration_api.html) with some ORCA-specific parts.

To receive the ORCA-specific message with the data for the crawler, use the RabbitMQ queue with the name built as a result of appending the value of the environment variable `HOBBIT_SESSION_ID` to the string `hobbit.datagen-system.`.
The message has the following structure:

| offset (bytes) | length (bytes) | description
|---|---|---
| 0 | 4 | `e` = length of SPARQL endpoint
| 4 | `e` | SPARQL endpoint (UTF-8 bytes)
| `e` + 4 | 4 | `u` = length of SPARQL username
| `e` + 8 | `u` | SPARQL username (UTF-8 bytes)
| `e` + `u` + 8 | 4 | `p` = length of SPARQL password
| `e` + `u` + 12 | `p` | SPARQL password (UTF-8 bytes)
| `e` + `u` + `p` + 12 | 4 | `s` = length of seed URIs
| `e` + `u` + `p` + 16 | `s` | newline-separated seed URIs (UTF-8 bytes)

The crawled data should be stored in the specified SPARQL endpoint.
As soon as the crawling is finished, the system [should terminate the same way as any system for HOBBIT](https://hobbit-project.github.io/benchmark_integration_api.html#container-termination).
