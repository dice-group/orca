FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/ldcbench.ckan-node-shaded.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.ckan-node-shaded.jar", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.nodes.ckan.simple.SimpleCkanComponent"]
