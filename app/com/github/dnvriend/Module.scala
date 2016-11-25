package com.github.dnvriend

import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.scaladsl.ReadJournal
import com.google.inject.{ AbstractModule, Inject, Provider }

class Module extends AbstractModule {
  protected def configure(): Unit = {
    bind(classOf[ReadJournal])
      .toProvider(classOf[CassandraReadJournalProvider])
  }
}

class CassandraReadJournalProvider @Inject() (system: ActorSystem) extends Provider[ReadJournal] {
  override def get(): ReadJournal =
    PersistenceQuery.apply(system).readJournalFor(CassandraReadJournal.Identifier)
}