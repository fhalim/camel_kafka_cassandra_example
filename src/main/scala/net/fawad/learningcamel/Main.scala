package net.fawad.learningcamel

import java.util.concurrent.CountDownLatch

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.{Exchange, Processor}
import org.apache.log4j.{ConsoleAppender, LogManager, PatternLayout}

class KV {
  @JsonProperty
  var name: String = _
  @JsonProperty
  var value: String = _
}

object Main extends App {
  // kafka-topics.sh -zookeeper localhost:2181 --topic learningcamel.in --partitions 12 --replication-factor 1 -create
  // while true;do kafka-console-producer -brokers localhost:9092 -topic "learningcamel.in" -key $RANDOM -value "{\"name\":\"foo$RANDOM\", \"value\": \"Value$RANDOM\"}";sleep 1;done;
  // cqlsh> CREATE KEYSPACE dev WITH replication= {'class': 'SimpleStrategy', 'replication_factor': 1};
  // cqlsh> USE dev;
  // cqlsh:dev> CREATE TABLE messages (k TEXT PRIMARY KEY, v TEXT);

  LogManager.getRootLogger.addAppender(new ConsoleAppender(new PatternLayout()))
  val ctx = new DefaultCamelContext()
  ctx.addRoutes(new RouteBuilder {
    override def configure() {
      from("kafka:localhost:9092?zookeeperHost=localhost&topic=learningcamel.in&groupId=learningcamel")
        .unmarshal().json(JsonLibrary.Jackson, classOf[KV])
        .process(new Processor {
          override def process(exchange: Exchange): Unit = {
            val body = exchange.getIn.getBody.asInstanceOf[KV]
            val headers = exchange.getIn.getHeaders
            //println(s"Body: ${body}, headers: ${headers}")
            exchange.getOut.setBody(Array(body.name, body.value))
          }
        })
        .to("cql:localhost/dev?cql=INSERT INTO messages (k, v) VALUES(?, ?)")
    }
  })
  ctx.start()
  println("Please press enter to stop")
  new CountDownLatch(1).await()
  ctx.shutdown()
}
