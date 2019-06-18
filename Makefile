IMAGE_BASE = git.project-hobbit.eu:4567/ldcbench/ldcbench/

test:
	mvn verify

publish: images push-images push-hobbit

images:
	mvn -DskipDefaultTest -DfailIfNoTests=false -Dtest=ImageBuilder package

push-images:
	docker push $(IMAGE_BASE)benchmark-controller
	docker push $(IMAGE_BASE)datagen
	docker push $(IMAGE_BASE)eval-module
	docker push $(IMAGE_BASE)system-adapter
	docker push $(IMAGE_BASE)empty-server
	docker push $(IMAGE_BASE)simple-http-node
	docker push $(IMAGE_BASE)ckan-node
	docker push $(IMAGE_BASE)sparql-node

add-hobbit-remote:
	git remote |grep hobbit ||git remote --verbose add hobbit https://git.project-hobbit.eu/ldcbench/ldcbench

push-hobbit: add-hobbit-remote
	git push --verbose hobbit master:master

test-benchmark:
	mvn -DfailIfNoTests=false -Dtest=BenchmarkTest#checkHealth test

test-benchmark-dockerized:
	mvn -DskipDefaultTest -DfailIfNoTests=false -Dtest=ImageBuilder verify
