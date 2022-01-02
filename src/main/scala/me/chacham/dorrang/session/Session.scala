package me.chacham.dorrang.session

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.room.Room.Room
import me.chacham.dorrang.room.UpstreamRoomMessage

sealed trait SessionMessage
final case class InitSession(room: Room) extends SessionMessage
final case class Joined(sessionId: String) extends SessionMessage
final case class UpstreamSessionMessage(body: String) extends SessionMessage
final case class DownstreamSessionMessage(body: String) extends SessionMessage
object Finished extends SessionMessage
final case class Failed(e: Throwable) extends SessionMessage

sealed trait ClientMessage
final case class ChatMessage(body: String) extends ClientMessage

object Session {
  type Session = ActorRef[SessionMessage]
  type Client = ActorRef[ClientMessage]

  def apply(id: String, client: Client): Behavior[SessionMessage] =
    Behaviors.receive {
      case (_, InitSession(room)) => session(id, client)(room)
      case (context, msg) =>
        context.log.error("Session not initialized yet, but received message: {}", msg)
        Behaviors.same
    }

  def session(id: String, client: Client)(implicit room: Room): Behavior[SessionMessage] = {
    Behaviors.receiveMessage[SessionMessage] {
      case UpstreamSessionMessage(body) =>
        room ! UpstreamRoomMessage(id, body)
        Behaviors.same
      case DownstreamSessionMessage(body) =>
        client ! ChatMessage(body)
        Behaviors.same
      case _ => Behaviors.same
    }
  }
}
