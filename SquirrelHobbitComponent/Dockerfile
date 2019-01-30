FROM java

ADD target/squirrel-adapter-1.0.0.jar /system/squirrel-adapter-1.0.0.jar

WORKDIR /system

CMD java -cp squirrel-adapter-1.0.0.jar org.hobbit.core.run.ComponentStarter org.dice_research.squirrel.adapter.SquirrelAdapter