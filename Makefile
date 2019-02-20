images:
	mvn -DskipTests package
	mvn -DfailIfNoTests=false -Dtest=BenchmarkTest#buildImages test
