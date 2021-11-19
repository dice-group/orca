FROM openjdk:8-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY microformats.tests/tests /usr/src/ldcbench.microformat-gen/microformats.tests/tests
COPY target/lib /usr/src/app/lib
COPY target/ldcbench.microformat-gen.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.microformat-gen.jar:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.microformat.gen.MicroformatGenerator"]
