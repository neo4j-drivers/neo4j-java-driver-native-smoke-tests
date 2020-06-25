## Build and run

### Start a Neo4j Docker instance

```
docker run --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/secret' -e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes neo4j:4.0
```

### Install GraalVM

Download the JDK 11 version for your operating system from
https://github.com/oracle/graal/releases

All community downloads are available on
https://github.com/graalvm/graalvm-ce-builds/releases

Here we used version 20.1.0

Exports should be as follows:

```
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-java11-20.1.0/Contents/Home
export PATH=$PATH:$GRAALVM_HOME/bin
export JAVA_HOME=$GRAALVM_HOME
```

Then install the native image tool

```
gu install native-image
gu list
```

### Build the project

```
./mvnw clean package 
```

Run against the above database

```
./target/org.neo4j.examples.drivernative.drivernativeapplication
```

No movies will be printed as long as you didn't install the movie graph, but a query will have been made.

Run against a different database:

```
./target/org.neo4j.examples.drivernative.drivernativeapplication <URL> <USERNAME> <PASSWORD> <LOGLEVEL> 
```

