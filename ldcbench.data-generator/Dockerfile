FROM maven

ARG CLASS=org.dice_research.ldcbench.benchmark.DataGenerator
ARG JAR_FILE

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/lib /usr/src/app/lib
ADD log4j.properties /usr/src/app
ADD target/${JAR_FILE} /usr/src/app

ENTRYPOINT ["java", "-cp", "${JAR_FILE}:/usr/src/app:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "${CLASS}"]
