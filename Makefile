IMAGE_BASE = git.project-hobbit.eu:4567/ldcbench/ldcbench/

images:
	mvn -DskipTests clean package
	mvn -DfailIfNoTests=false -Dtest=BenchmarkTest#buildImages test
	docker build --tag $(IMAGE_BASE)simple-http-node ldcbench.http-node/

