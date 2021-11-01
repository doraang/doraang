package me.chacham.dorrang.room

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.session.{Joined, SessionMessage}

sealed trait RoomMessage
final case class Join(sessionId: String, session: ActorRef[SessionMessage]) extends RoomMessage
final case class Exit(sessionId: String) extends RoomMessage

object Room {
  def apply(): Behavior[RoomMessage] = apply(Map.empty)

  private def apply(
      sessionMap: Map[String, ActorRef[SessionMessage]]
  ): Behavior[RoomMessage] = {
    Behaviors.receiveMessage[RoomMessage] {
      case Join(newSessionId, newSession) =>
        val newSessionMap = sessionMap + (newSessionId -> newSession)
        newSessionMap.foreachEntry((_, session) => session ! Joined(newSessionId))
        Room(newSessionMap)
      case Exit(sessionId) => Room(sessionMap - sessionId)
    }
  }
}
