FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

# Copy data
ADD data/lemming /usr/src/app/data

ADD target/lib /usr/src/app/lib
ADD log4j.properties /usr/src/app
ADD target/ldcbench.lemming.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.data-generator.jar:/usr/src/app:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.benchmark.LemmingBasedBenchmarkController"]
