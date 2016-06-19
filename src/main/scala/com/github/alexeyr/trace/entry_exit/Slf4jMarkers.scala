package com.github.alexeyr.trace.entry_exit

import org.slf4j.MarkerFactory

object Slf4jMarkers {
  val ENTRY_MARKER = MarkerFactory.getMarker("ENTRY")
  val EXIT_MARKER = MarkerFactory.getMarker("EXIT")
  val CATCHING_MARKER = MarkerFactory.getMarker("CATCHING")
  val THROWING_MARKER = MarkerFactory.getMarker("THROWING")
  val FLOW_MARKER = MarkerFactory.getMarker("FLOW")

  Seq(ENTRY_MARKER, EXIT_MARKER, CATCHING_MARKER, THROWING_MARKER).foreach(FLOW_MARKER.add)
}
