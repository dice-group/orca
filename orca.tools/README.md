This directory contains some classes which could be helpful when working with ORCA.

## LODStatsAnalyser

This class gathers the [`dcat:downloadURL`](http://www.w3.org/ns/dcat#downloadURL) triples of the LODStats dataset and assigns them to classes following simple rules:
* dump file
  * `".*\\.ttl$"`
  * `".*\\.n3$"`
  * `".*\\.nt$"`
  * `".*\\.nq$"`
  * `".*\\.xml$"`
  * `".*\\.turtle$"`
  * `".*\\.owl$"`
  * `".*\\.rdf$"`
* Compressed dump file
  * `".*\\.gz$"`
  * `".*\\.zip$"`
  * `".*\\.tgz$"`
  * `".*\\.bz2$"`
  * `".*\\.7z$"`
  * `".*\\.xz$"`
* SPARQL endpoint
  * `".*/[sS]parql[/]?.*"`
* HTML
  * `".*\\.html$"`
  * `".*\\.htm$"`


