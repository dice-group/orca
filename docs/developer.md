# Developer Information
[HOBBIT documentation](https://hobbit-project.github.io/)
describes general information about HOBBIT platform architecture.

## Building and testing
`mvn verify` builds all jar files and Docker images and runs all tests.

Tests include running the benchmark initialization process twice,
in multi-threaded Java environment and in Docker environment.
The corresponding test methods are
`BenchmarkTest#executeBenchmark` and `BenchmarkIT#executeBenchmark`.
`make test-benchmark` and `make test-benchmark-dockerized`
are shortcuts for running those tests individually.

## Deploying images
The HOBBIT platform uses Docker images to run benchmarks and systems.

`make push-images` pushes already built Docker images to the HOBBIT repository.

## Updating benchmark metadata in the HOBBIT Platform
The HOBBIT platform uses the metadata file (`benchmark.ttl`)
to retrieve the information about the benchmark.
File should be in a repository on the HOBBIT git server.

`make push-hobbit` adds the corresponding git remote
and pushes your `master` branch there.
