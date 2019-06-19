FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/ldcbench.http-node-shaded.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.http-node-shaded.jar", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.nodes.http.simple.SimpleHttpServerComponent"]
