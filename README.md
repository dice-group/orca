[![Build Status](https://travis-ci.org/dice-group/ldcbench.svg?branch=master)](https://travis-ci.org/dice-group/ldcbench) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/6030450cade64e259f69dddbd4a17c14)](https://www.codacy.com/app/MichaelRoeder/ldcbench?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/ldcbench&amp;utm_campaign=Badge_Grade)

# Benchmark for Data Web Crawlers

Benchmark Data Web Crawlers on [the HOBBIT platform](http://project-hobbit.eu/).

## Parameters

### Number of nodes
The number of nodes in the synthetic graph.
### Average node degree
The average degree of the nodes in the generated graph.
### RDF dataset size
Average number of triples of the generated RDF graphs.
### Average resource degree
The average degree of the resources in the RDF graphs.
### Node type amounts
For each node type, the user can define the proportion of nodes that should have this type.
### Dump file serialisations
For each available dump file serialisation, a boolean flag can be set.
### Dump file compression ratio
Proportion of dump files that are compressed.
### Average ratio of disallowed resources
Proportion of resources that are generated within a node and marked as disallowed for crawling.
### Average crawl delay
The crawl delay of the node's `robots.txt` file.
### Seed
A seed value for initialising random number generators is used to ensure the repeatability of experiments.

## Key performance indicators

### Recall
Number of true positives divided by the number of checked triples.
### Runtime
The time it takes from starting the crawling process to termination.
### Requested disallowed resources
The number of forbidden resources crawled by the crawler, divided by the number of all resources forbidden by the `robots.txt` file.
### Crawl delay fulfilment
The average measured delay between the requests received by a single node divided by the delay defined in the `robots.txt` file. If the measure is below 1.0 the crawler does not strictly follow the delay instruction.
### Consumed hardware resources
The RAM and CPU consumption of the benchmarked crawler.
### Triples over time:
The number of triples in the sink over time.
### Cloud graph visualisation
A visualisation of the generated synthetic Linked Data web.

## API

System needs to declare that it
[implements](http://w3id.org/hobbit/vocab#implementsAPI)
[`api`](https://github.com/dice-group/ldcbench#Api).

If the system extends
[`AbstractSystemAdapter`](https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/AbstractSystemAdapter.java),
it should overload `receiveGeneratedData` method to receive SPARQL endpoint URI
and `receiveGeneratedTask` method to receive seed URI.

System should start crawling by retrieving provided seed URI
and use use SPARQL endpoint to store crawled data.

## Benchmark maintenance

### Building and testing

Use `mvn verify` to run build and all available tests.

Use `make test-benchmark` to run tests with main components running without docker only.

Use `make test-benchmark-dockerized` to run tests with docker images only.

### Building and pushing images

Use `make images push-images` to build docker images and push them to the HOBBIT repository.

### Updating benchmark metadata in the HOBBIT Platform

Updated files should be in the `master` branch.
Use `make push-hobbit` to push to HOBBIT git.
