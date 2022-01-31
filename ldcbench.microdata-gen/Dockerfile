FROM openjdk:8-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY w3c.github.io_microdata-rdf/tests /usr/src/ldcbench.microdata-gen/w3c.github.io_microdata-rdf/tests
COPY target/lib /usr/src/app/lib
COPY target/ldcbench.microdata-gen.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.microdata-gen.jar:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.microdata.gen.MicrodataGenerator"]
