# Adding Key Performance Indicators

## Adding RDF definition

Define the new KPI as a resource in `benchmark.ttl`.
It should be of type `hobbit:KPI`,
have `rdfs:domain` of both `hobbit:Experiment` and `hobbit:Challenge`,
and have `rdfs:label`, `rdfs:comment` and `rdfs:range`
([HOBBIT documentation](https://hobbit-project.github.io/benchmark_integration_api.html#kpis)).

ORCA's `hobbit:measuresKPI` property should be extended to include the new KPI.

### Example

```turtle
:runtime a hobbit:KPI ;
  rdfs:label "Runtime (in ms)"@en;
  rdfs:comment "The overall runtime of the crawler in milliseconds."@en;
  rdfs:domain hobbit:Experiment, hobbit:Challenge;
  rdfs:range xsd:long .

:Benchmark hobbit:measuresKPI :runtime .
```

## Adding value to experiment results

At some point, the benchmark creates the result model,
which includes KPI values and other information about the experiment.
Add a triple to that model with the subject of the experiment,
property of your KPI as described in `benchmark.ttl`
and object of the value you computed for the KPI.

It can be done in either of the following classes:
- `EvalModule`, in the `summarizeEvaluation` method; the result model is available as `model` variable
- `BenchmarkController`, in the `executeBenchmark` method; the result model is available as `resultModel`

In both cases, the experiment resource is available as the local variable `experimentResource`.

### Example

```java
long runtime = endTimeStamp - startTimeStamp;
if (runtime > 0) {
    model.add(model.createLiteralStatement(experimentResource, LDCBench.runtime, runtime));
}
```
