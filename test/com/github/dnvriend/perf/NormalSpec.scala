package com.github.dnvriend.perf

import com.github.dnvriend.IntegrationSpec

import scala.compat.Platform

class NormalSpec extends IntegrationSpec {

  it should "perform with type 'String'" in withSingleEntity { pid => entity =>
    val numberOfEvents = 10000
    val eventsSender = persistEvents(pid, entity, "NormalSpec", numberOfEvents)
    val eventsReader = readEvents(pid, numberOfEvents)

    val start = Platform.currentTime
    eventsSender.futureValue
    println(s"Writing events took: ${Platform.currentTime - start} ms")
    eventsReader.futureValue.size shouldBe numberOfEvents
    println(s"Reading events took: ${Platform.currentTime - start} ms")
  }
}
