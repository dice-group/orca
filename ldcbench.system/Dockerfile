FROM maven

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD target/lib /usr/src/app/lib
ADD log4j.properties /usr/src/app
ADD target/ldcbench.system.jar /usr/src/app

CMD ["java", "-cp", "ldcbench.system.jar:/usr/src/app/lib/*", "org.hobbit.core.run.ComponentStarter", "org.dice_research.ldcbench.system.SystemAdapter"]
