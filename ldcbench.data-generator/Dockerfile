FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/lib /usr/src/app/lib
ADD log4j.properties /usr/src/app
ADD target/ldcbench.data-generator.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.controller.jar:/usr/src/app:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.benchmark.DataGenerator"]
