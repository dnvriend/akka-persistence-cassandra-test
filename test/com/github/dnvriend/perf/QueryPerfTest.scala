package com.github.dnvriend.perf

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.{ Sink, Source }
import com.github.dnvriend.IntegrationSpec

import scala.collection.immutable.Seq
import scala.compat.Platform
import scala.concurrent.Future

class QueryPerfTest extends IntegrationSpec {
  def persistEvents(entity: ActorRef, event: Any, numberOfEvents: Int = 10000, parallelism: Int = 1): Future[Done] =
    Source.repeat(event)
      .mapAsync(parallelism)(msg => entity ? msg)
      .take(numberOfEvents)
      .runWith(Sink.ignore)

  def readEvents(numberOfEvents: Int = 10000): Future[Seq[_]] =
    readJournal.eventsByPersistenceId("pid", 0, Long.MaxValue)
      .take(numberOfEvents)
      .runWith(Sink.seq)

  it should "perform with type 'String'" in withEntity() { entity =>
    val numberOfEvents = 10000
    val eventsSender = persistEvents(entity, "pid", numberOfEvents)
    val eventsReader = readEvents(numberOfEvents)

    val start = Platform.currentTime
    eventsSender.futureValue
    println(s"sending events took: ${Platform.currentTime - start} ms")
    eventsReader.futureValue
    println(s"reading events took: ${Platform.currentTime - start} ms")
  }
}
