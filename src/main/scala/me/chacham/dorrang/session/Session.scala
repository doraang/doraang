package me.chacham.dorrang.session

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

sealed trait SessionMessage
final case class Joined(sessionId: String) extends SessionMessage
final case class FromClient(body: String) extends SessionMessage
object Finished extends SessionMessage
final case class Failed(e: Throwable) extends SessionMessage

sealed trait ClientMessage
final case class NormalMessage(body: String) extends ClientMessage

object Session {
  def apply(id: String, client: ActorRef[ClientMessage]): Behavior[SessionMessage] = {
    Behaviors.receiveMessage[SessionMessage] {
      case FromClient(body) =>
        client ! NormalMessage(body)
        Behaviors.same
      case _ => Behaviors.same
    }
  }
}
