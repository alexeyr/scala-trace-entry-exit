package com.github.alexeyr.trace.entry_exit

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender

import scala.collection.mutable.ArrayBuffer

class Log4jBufferAppender extends AbstractAppender("", null, null) {
  val events = ArrayBuffer.empty[LogEvent]

  override def append(event: LogEvent): Unit =
    events += event
}
