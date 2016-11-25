package com.github.dnvriend

import java.util.UUID

import akka.Done
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.persistence.query.scaladsl.{CurrentEventsByPersistenceIdQuery, EventsByPersistenceIdQuery, ReadJournal}
import akka.stream.Materializer
import akka.testkit.TestProbe
import akka.util.Timeout
import com.github.dnvriend.actor.Entity
import org.scalatest._
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.WsTestClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import akka.routing.RoundRobinPool
import akka.stream.scaladsl.{Sink, Source}
import akka.pattern.ask

import scala.collection.immutable.Seq

class IntegrationSpec extends FlatSpec
  with Matchers
  with GivenWhenThen
  with OptionValues
  with TryValues
  with ScalaFutures
  with WsTestClient
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Eventually
  with GuiceOneServerPerSuite {

  def getComponent[A: ClassTag] = app.injector.instanceOf[A]

  // set the port number of the HTTP server
  override lazy val port: Int = 9001
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 30.seconds, interval = 300.millis)
  implicit val system: ActorSystem = getComponent[ActorSystem]
  implicit val ec: ExecutionContext = getComponent[ExecutionContext]
  implicit val mat: Materializer = getComponent[Materializer]
  implicit val readJournal = getComponent[ReadJournal].asInstanceOf[ReadJournal with EventsByPersistenceIdQuery with CurrentEventsByPersistenceIdQuery]
  implicit val timeout: Timeout = 10.seconds

  final val NumberOfActors = 50

  def randomId = UUID.randomUUID.toString

  def persistEvents(pid: String, entity: ActorRef, event: Any, numberOfEvents: Int = 10000, parallelism: Int = 1, logEach: Int = 1000): Future[Done] =
    Source.repeat(event)
      .mapAsync(parallelism)(msg => entity ? msg)
      .zipWithIndex
      .map {
        case (x, index) =>
          if (index !=0 && index % logEach == 0)
            println(s"writing: pid: '$pid' - $index")

          x
      }
      .take(numberOfEvents)
      .runWith(Sink.ignore)

  def readEvents(pid: String, numberOfEvents: Int = 10000, logEach: Int = 1000): Future[Seq[_]] =
    readJournal.eventsByPersistenceId(pid, 0, Long.MaxValue)
      .zipWithIndex
      .map {
        case (ev, index) =>
          if(index !=0 && index % logEach == 0)
            println(s"READING: pid: '${ev.persistenceId}' - $index")

          ev
      }
      .take(numberOfEvents)
      .runWith(Sink.seq)

  def readEventsNumberOfActors(pid: String, numberOfEvents: Int = 10000): Future[Seq[_]] =
    readJournal.eventsByPersistenceId(s"pid$NumberOfActors", 0, Long.MaxValue)
      .take(numberOfEvents / NumberOfActors)
      .runWith(Sink.seq)


  def withSingleEntity[A](f: String => ActorRef => A): A = {
    val pid: String = randomId
    val ref = system.actorOf(Props(classOf[Entity], pid))
    try f(pid)(ref) finally killActors(ref)
  }

  def withRoundRobinEntity[A](f: String => ActorRef => A): A = {
    val pid: String = randomId
    val ref = system.actorOf(RoundRobinPool(NumberOfActors).props(Props(classOf[Entity], pid)))
    try f(pid)(ref) finally killActors(ref)
  }

  def killActors(actors: ActorRef*): Unit = {
    val tp = TestProbe()
    actors.foreach { (actor: ActorRef) =>
      tp watch actor
      actor ! PoisonPill
      tp.expectTerminated(actor)
    }
  }
}
