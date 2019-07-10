FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/ldcbench.sparql-node-shaded.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.sparql-node-shaded.jar", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.nodes.sparql.simple.SimpleSparqlComponent"]
