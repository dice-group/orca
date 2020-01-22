[![Build Status](https://travis-ci.org/dice-group/ldcbench.svg?branch=master)](https://travis-ci.org/dice-group/ldcbench)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6030450cade64e259f69dddbd4a17c14)](https://www.codacy.com/app/MichaelRoeder/ldcbench?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/ldcbench&amp;utm_campaign=Badge_Grade)

# Benchmark for Data Web Crawlers

Benchmark Data Web Crawlers on [the HOBBIT platform](http://project-hobbit.eu/).

## System adapter implementations

* [LDSpider](https://github.com/dice-group/ldcbench-ldspider-adapter)
* [Squirrel](https://github.com/dice-group/ldcbench-squirrel-adapter)

## Parameters
Parameter | Description | Ontology resources
--- | --- | ---
Number of nodes | The number of nodes in the synthetic graph. | [orca:numberOfNodes](http://w3id.org/dice-research/orca/ontology#numberOfNodes)
Average node degree | The average degree of the nodes in the generated graph. | [orca:averageNodeGraphDegree](http://w3id.org/dice-research/orca/ontology#averageNodeGraphDegree)
RDF dataset size | Average number of triples of the generated RDF graphs. | [orca:averageTriplesPerNode](http://w3id.org/dice-research/orca/ontology#averageTriplesPerNode)
Average resource degree | The average degree of the resources in the RDF graphs. | [orca:averageRdfGraphDegree](http://w3id.org/dice-research/orca/ontology#averageRdfGraphDegree)
Node type amounts | For each node type, the user can define the proportion of nodes that should have this type. | [orca:httpDumpNodeWeight](http://w3id.org/dice-research/orca/ontology#httpDumpNodeWeight) [orca:dereferencingHttpNodeWeight](http://w3id.org/dice-research/orca/ontology#dereferencingHttpNodeWeight) [orca:sparqlNodeWeight](http://w3id.org/dice-research/orca/ontology#sparqlNodeWeight) [orca:ckanNodeWeight](http://w3id.org/dice-research/orca/ontology#ckanNodeWeight)
Dump file serialisations | For each available dump file serialisation, a boolean flag can be set. | [orca:useNtDumps](http://w3id.org/dice-research/orca/ontology#useNtDumps) [orca:useN3Dumps](http://w3id.org/dice-research/orca/ontology#useN3Dumps) [orca:useRdfXmlDumps](http://w3id.org/dice-research/orca/ontology#useRdfXmlDumps) [orca:useTurtleDumps](http://w3id.org/dice-research/orca/ontology#useTurtleDumps)
Dump file compression ratio | Proportion of dump files that are compressed. | [orca:httpDumpNodeCompressedRatio](http://w3id.org/dice-research/orca/ontology#httpDumpNodeCompressedRatio)
Average ratio of disallowed resources | Proportion of resources that are generated within a node and marked as disallowed for crawling. | [orca:averageDisallowedRatio](http://w3id.org/dice-research/orca/ontology#averageDisallowedRatio)
Average crawl delay | The crawl delay of the node's `robots.txt` file. | [orca:averageCrawlDelay](http://w3id.org/dice-research/orca/ontology#averageCrawlDelay)
Seed | A seed value for initialising random number generators is used to ensure the repeatability of experiments. | [orca:seed](http://w3id.org/dice-research/orca/ontology#seed)

## Key performance indicators
KPI | Description | Ontology resources
--- | --- | ---
Recall | Number of true positives divided by the number of checked triples. | [orca:microRecall](http://w3id.org/dice-research/orca/ontology#microRecall) [orca:macroRecall](http://w3id.org/dice-research/orca/ontology#macroRecall)
Runtime | The time it takes from starting the crawling process to termination. | [orca:runtime](http://w3id.org/dice-research/orca/ontology#runtime)
Requested disallowed resources | The number of forbidden resources crawled by the crawler, divided by the number of all resources forbidden by the `robots.txt` file. | [orca:ratioOfRequestedDisallowedResources](http://w3id.org/dice-research/orca/ontology#ratioOfRequestedDisallowedResources)
Crawl delay fulfilment | The average measured delay between the requests received by a single node divided by the delay defined in the `robots.txt` file. If the measure is below 1.0 the crawler does not strictly follow the delay instruction. | [orca:minAverageCrawlDelayFulfillment](http://w3id.org/dice-research/orca/ontology#minAverageCrawlDelayFulfillment) [orca:maxAverageCrawlDelayFulfillment](http://w3id.org/dice-research/orca/ontology#maxAverageCrawlDelayFulfillment) [orca:macroAverageCrawlDelayFulfillment](http://w3id.org/dice-research/orca/ontology#macroAverageCrawlDelayFulfillment)
Consumed hardware resources | The RAM and CPU consumption of the benchmarked crawler. | [orca:totalCpuUsage](http://w3id.org/dice-research/orca/ontology#totalCpuUsage) [orca:averageDiskUsage](http://w3id.org/dice-research/orca/ontology#averageDiskUsage) [orca:averageMemoryUsage](http://w3id.org/dice-research/orca/ontology#averageMemoryUsage)
Triples over time | The number of triples in the sink over time. | [orca:tripleCountOverTime](http://w3id.org/dice-research/orca/ontology#tripleCountOverTime)

## Maintenance

### Building and testing
Use `mvn verify` to build all jar files, build docker images and run all tests.

Tests include running the benchmark initialization process twice,
once in multi-threaded Java environment
and once in Docker environment.
The corresponding tests are
`BenchmarkTest#executeBenchmark` and `BenchmarkIT#executeBenchmark`.
You can use `test-benchmark` and `test-benchmark-dockerized` targets to run them.

### Building and pushing images
The HOBBIT platform needs a Docker image to run the benchmark.

Use `make push-images` to push Docker images to the HOBBIT repository.

### Updating benchmark metadata in the HOBBIT Platform
The HOBBIT platform needs a repository with the benchmark metadata (`benchmark.ttl`)
in HOBBIT git instance.

Use `make push-hobbit` to push your `master` branch to HOBBIT git
for updating the `benchmark.ttl` file.

## Developer information
Refer to [HOBBIT documentation](https://hobbit-project.github.io/)
for general information about HOBBIT platform architecture
and how it works with benchmarks and benchmarked systems.

### Forking this benchmark
When you fork a HOBBIT benchmark to create your own benchmark,
you should change the following things to avoid conflicts with the original benchmark:
- URIs of RDF resources in `benchmark.ttl` and your code
- labels of RDF resources in `benchmark.ttl`
- Docker image names

### Adding crawlers
The general description how to integrate a system into the HOBBIT platform can be found in [the HOBBIT documentation](https://hobbit-project.github.io/system_integration.html).

The crawling process can be integrated in [one of the following ways](https://hobbit-project.github.io/system_integration_api.html):
- the system for the HOBBIT platform does both the communication and the crawling
- the system for the HOBBIT platform is just a system adapter which starts another Docker container with the actual crawler

#### API
The system resource in your system metadata file (`system.ttl`)
should have a triple with the property [`hobbit:implements`](http://w3id.org/hobbit/vocab#implementsAPI)
and the object `https://github.com/dice-group/ldcbench#Api` (described in `benchmark.ttl`)
to appear in the list of systems compatible with ORCA.

##### Generic API
API consists of [generic HOBBIT API](https://hobbit-project.github.io/system_integration_api.html) and some ORCA-specific parts.

To receive the ORCA-specific message, use the RabbitMQ queue with the name built as a result of appending the value of the environment variable `HOBBIT_SESSION_ID` to the string `hobbit.datagen-system.`.
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
As soon as the crawling is finished, the system [should terminate](https://hobbit-project.github.io/benchmark_integration_api.html#container-termination).

##### Java API based on the HOBBIT core library
The HOBBIT core library provides several base classes which simplify
the interaction with HOBBIT platform.

Your implementation should extend
[`AbstractSystemAdapter`](https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/AbstractSystemAdapter.java).
Override `init` method if you need to initialize anything before benchmarking starts.
Override `receiveGeneratedData` method to receive SPARQL endpoint URI
and seed URIs and start the crawling:
```java
@Override
public void init() throws Exception {
  super.init();

  /* initialize */
}

@Override
public void receiveGeneratedData(byte[] data) {
  ByteBuffer buffer = ByteBuffer.wrap(data);
  String sparqlUrl = RabbitMQUtils.readString(buffer);
  String sparqlUser = RabbitMQUtils.readString(buffer);
  String sparqlPwd = RabbitMQUtils.readString(buffer);
  List<String> seedURIs = new ArrayList<>(Arrays.asList(RabbitMQUtils.readString(buffer).split("\n")));

  /* crawl the "cloud" here and store the data in the specified SPARQL endpoint */

  terminate(null); // terminate the system after crawling is finished
}

/* ORCA does not use this feature of the HOBBIT platform */
@Override
public void receiveGeneratedTask(String taskId, byte[] data) {
  throw new IllegalStateException();
}
```

It's also possible to [start another Docker container with the actual crawler](https://hobbit-project.github.io/platform_api.html).

Refer to existing system adapter implementations for examples.

### Adding key performance indicators
Describe the new KPI as a resource in `benchmark.ttl`.
It should be of type `hobbit:KPI`,
have `rdfs:domain` of both `hobbit:Experiment` and `hobbit:Challenge`,
and have `rdfs:label`, `rdfs:comment` and `rdfs:range`
as described at https://hobbit-project.github.io/benchmark_integration_api.html#kpis.

At some point, the benchmark creates the result model,
which includes KPI values and other information about the experiment.
Add a triple to that model with the subject of the experiment,
property of your KPI as described in `benchmark.ttl`
and object of the value you computed for the KPI.
It can be done in either of the following classes:
- [`EvalModule`](blob/master/ldcbench.eval-module/src/main/java/org/dice_research/ldcbench/benchmark/EvalModule.java), in the `summarizeEvaluation` method; the result model is available as `model` variable
- [`BenchmarkController`](blob/master/ldcbench.controller/src/main/java/org/dice_research/ldcbench/benchmark/BenchmarkController.java), in the `executeBenchmark` method; the result model is available as `resultModel`

In both cases, the experiment resource is available as `experimentResource`.

Refer to the existing KPIs for examples.
