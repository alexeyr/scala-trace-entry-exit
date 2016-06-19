package com.github.alexeyr.trace.entry_exit

import scala.collection.JavaConverters._
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.scalatest.{Matchers, fixture}
import org.slf4j.{LoggerFactory, Logger => SlfLogger}

import scala.collection.mutable

class TraceEntryExitTests extends fixture.FunSpec with Matchers {
  val slf4jLogger = LoggerFactory.getLogger(SlfLogger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
  slf4jLogger.setLevel(ch.qos.logback.classic.Level.TRACE)

  val log4jLogger = LogManager.getRootLogger.asInstanceOf[org.apache.logging.log4j.core.Logger]
  log4jLogger.setLevel(org.apache.logging.log4j.Level.TRACE)

  case class FixtureParam(slf4j: mutable.Buffer[ILoggingEvent], log4j: mutable.Buffer[LogEvent])

  def withFixture(test: OneArgTest) = {
    val slf4jAppender = new ListAppender[ILoggingEvent]
    slf4jAppender.start()
    slf4jAppender.setContext(slf4jLogger.getLoggerContext)
    slf4jLogger.addAppender(slf4jAppender)

    val log4jAppender = new Log4jBufferAppender
    log4jAppender.start()
    log4jLogger.addAppender(log4jAppender)

    val param = FixtureParam(slf4jAppender.list.asScala, log4jAppender.events)
    try {
      withFixture(test.toNoArgTest(param))
    } finally {
      slf4jLogger.detachAppender(slf4jAppender)
      log4jLogger.removeAppender(log4jAppender)
    }
  }

  def assertMessages(param: FixtureParam, expected: String*) = {
    val slf4j = param.slf4j.map(_.getMessage)
    val log4j = param.log4j.map(_.getMessage)
    slf4j should equal(expected)
    log4j should equal(expected)
  }

  it("the simplest case works") { param =>
    class Slf4jSimpleTestClass {
      private val logger = LoggerFactory.getLogger(getClass)

      @TraceEntryExit
      def foo(x: Int, y: String) = y
    }

//    class Log4jSimpleTestClass {
//      private val logger = LogManager.getLogger(getClass)
//
//      @TraceEntryExit(library = LoggingLibrary.Log4j2)
//      def foo(x: Int, y: String) = y
//    }

    (new Slf4jSimpleTestClass).foo(1, "ab")
//    (new Log4jSimpleTestClass).foo(1, "ab")
    assertMessages(param,
      "> Entering method foo",
      "< Exiting method foo; returning ab"
    )
  }
}
