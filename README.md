[![Build Status](https://travis-ci.org/dice-group/ldcbench.svg?branch=master)](https://travis-ci.org/dice-group/ldcbench) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/6030450cade64e259f69dddbd4a17c14)](https://www.codacy.com/app/MichaelRoeder/ldcbench?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/ldcbench&amp;utm_campaign=Badge_Grade)

# Linked Data Crawler Benchmark

Benchmark linked data crawlers with [the HOBBIT platform](http://project-hobbit.eu/).

# Parameters

## Seed

## Number of nodes

## Triples per node

## Average node delay

## Average node graph degree

## Average RDF graph degree

# API

System needs to declare that it
[implements](http://w3id.org/hobbit/vocab#implementsAPI)
[`ldcbench:api`](https://github.com/dice-group/ldcbench#api).

If the system extends
[`AbstractSystemAdapter`](https://github.com/hobbit-project/core/blob/master/src/main/java/org/hobbit/core/components/AbstractSystemAdapter.java),
it should overload `receiveGeneratedData` method to receive SPARQL endpoint URI
and `receiveGeneratedTask` method to receive seed URI.

System should start crawling by retrieving provided seed URI
and use use SPARQL endpoint to store crawled data.

# Benchmark maintenance

## Building and testing

Use `mvn verify` to run build and all available tests.

Use `make test-benchmark` to run tests with main components running without docker.

Use `make test-benchmark-dockerized` to run tests with docker images.

## Building and pushing images

Use `make images push-images` to build docker images and push them to the HOBBIT repository.

## Updating benchmark metadata in the HOBBIT Platform

Updated files should be in the `master` branch.
Use `make push-hobbit` to push to HOBBIT git.
