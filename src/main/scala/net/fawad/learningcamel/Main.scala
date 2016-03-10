package net.fawad.learningcamel

import java.util.concurrent.CountDownLatch

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.log4j.{PatternLayout, ConsoleAppender, LogManager}

object Main extends App {
  LogManager.getRootLogger.addAppender(new ConsoleAppender(new PatternLayout()))
  val ctx = new DefaultCamelContext()
  ctx.addRoutes(new RouteBuilder {
    override def configure() {
      from("file:/tmp/camelin").to("file:/tmp/camelout")
    }
  })
  ctx.start()
  println("Please press enter to stop")
  new CountDownLatch(1).await()
  ctx.shutdown()
}
