package com.github.dnvriend.perf

import com.github.dnvriend.IntegrationSpec

import scala.compat.Platform

class FastSpec extends IntegrationSpec {

  def writeEvents(numberOfEvents: Int): Unit = withRoundRobinEntity { pid => entity =>
      val eventsSender = persistEvents(pid, entity, "FastSpec", numberOfEvents)
      val eventsReader = readEvents(pid, numberOfEvents)

      val start = Platform.currentTime
      eventsSender.futureValue
      println(s"Writing events took: ${Platform.currentTime - start} ms")
      eventsReader.futureValue.size shouldBe numberOfEvents
      println(s"Reading events took: ${Platform.currentTime - start} ms")
  }

  it should "perform with type 'String'" in {
    (1 to 5).foreach( _ => writeEvents(10000))
  }
}
