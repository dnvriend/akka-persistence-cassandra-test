package com.github.dnvriend.actor

import akka.persistence.{ PersistentActor, Recovery }

class Entity(override val persistenceId: String) extends PersistentActor {
  override def recovery: Recovery = Recovery.none
  override def receiveRecover: Receive = PartialFunction.empty
  override def receiveCommand: Receive = {
    case msg => persist(msg)(sender() ! _)
  }
}
