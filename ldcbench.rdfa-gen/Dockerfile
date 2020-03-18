FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD rdfa.github.io/test-suite/test-cases /usr/src/ldcbench.rdfa-gen/rdfa.github.io/test-suite/test-cases
ADD target/lib /usr/src/app/lib
ADD target/ldcbench.rdfa-gen.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.rdfa-gen.jar:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.rdfa.gen.RDFaDataGenerator"]
