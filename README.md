## Build and run

Start a Neo4j Docker instance

```
docker run --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/secret' -e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes neo4j/neo4j-experimental:4.0.0-rc01
```

Goto 

https://github.com/oracle/graal/releases

Download 
https://github.com/oracle/graal/releases/tag/vm-19.2.1

Follow instructions to install
Execute  

`gu install native-image`

to get the native image tool

Build and run this project with

```
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-19.2.0.1/Contents/Home
export JAVA_HOME=$GRAALVM_HOME
git checkout withGraalVm192
./mvnw clean package
./target/org.neo4j.examples.drivernative.drivernativeapplication
```

Should print movies or empty list. 
Fails with no connection if there's no database.

Repeat with Graal 19.3, download is here: https://github.com/oracle/graal/releases/tag/vm-19.3.0

```
export GRAALVM_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-java8-19.3.0/Contents/Home
export JAVA_HOME=$GRAALVM_HOME
git checkout withGraalVm193
./mvnw clean package
./target/org.neo4j.examples.drivernative.drivernativeapplication 
```

The different commit only changes the native image maven plugin.
The error however is reproducable when compiled with 19.3 and the program will fail with


```
WARNING: [0x8b94b506][localhost:7687][] Fatal error occurred in the pipeline
org.neo4j.driver.internal.shaded.io.netty.handler.codec.DecoderException: Failed to read inbound message:
70a286736572766572d0104e656f346a2f342e302e302d726330318d636f6e6e656374696f6e5f696486626f6c742d30

	at org.neo4j.driver.internal.async.inbound.InboundMessageHandler.channelRead0(InboundMessageHandler.java:87)
	at org.neo4j.driver.internal.async.inbound.InboundMessageHandler.channelRead0(InboundMessageHandler.java:35)
	at org.neo4j.driver.internal.shaded.io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:105)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at org.neo4j.driver.internal.shaded.io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:328)
	at org.neo4j.driver.internal.shaded.io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:302)
	at org.neo4j.driver.internal.async.inbound.MessageDecoder.channelRead(MessageDecoder.java:47)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at org.neo4j.driver.internal.shaded.io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:328)
	at org.neo4j.driver.internal.shaded.io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:302)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at org.neo4j.driver.internal.shaded.io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1422)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at org.neo4j.driver.internal.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at org.neo4j.driver.internal.shaded.io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:931)
	at org.neo4j.driver.internal.shaded.io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:163)
	at org.neo4j.driver.internal.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:700)
	at org.neo4j.driver.internal.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeysPlain(NioEventLoop.java:600)
	at org.neo4j.driver.internal.shaded.io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:554)
	at org.neo4j.driver.internal.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:514)
	at org.neo4j.driver.internal.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$6.run(SingleThreadEventExecutor.java:1044)
	at org.neo4j.driver.internal.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at org.neo4j.driver.internal.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:834)
	at com.oracle.svm.core.thread.JavaThreads.threadStartRoutine(JavaThreads.java:479)
	at com.oracle.svm.core.posix.thread.PosixJavaThreads.pthreadStartRoutine(PosixJavaThreads.java:193)
Caused by: org.neo4j.driver.internal.packstream.PackStream$Unexpected: Expected a struct, but got: ffffffb1
	at org.neo4j.driver.internal.packstream.PackStream$Unpacker.unpackStructHeader(PackStream.java:421)
	at org.neo4j.driver.internal.messaging.v1.ValueUnpackerV1.unpackStructHeader(ValueUnpackerV1.java:62)
	at org.neo4j.driver.internal.messaging.v1.MessageReaderV1.read(MessageReaderV1.java:51)
	at org.neo4j.driver.internal.async.inbound.InboundMessageHandler.channelRead0(InboundMessageHandler.java:83)
	... 31 more
```