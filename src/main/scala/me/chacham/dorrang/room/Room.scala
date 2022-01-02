package me.chacham.dorrang.room

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.room.RoomService.RoomService
import me.chacham.dorrang.session.Session.Session
import me.chacham.dorrang.session.{DownstreamSessionMessage, Joined}

sealed trait RoomMessage
final case class InitRoom(roomService: RoomService) extends RoomMessage
final case class Join(sessionId: String, session: Session) extends RoomMessage
final case class Exit(sessionId: String) extends RoomMessage
final case class UpstreamRoomMessage(sessionId: String, body: String) extends RoomMessage
final case class DownstreamRoomMessage(sessionId: String, body: String) extends RoomMessage

object Room {
  type Room = ActorRef[RoomMessage]

  def apply(id: String, roomService: RoomService): Behavior[RoomMessage] =
    Behaviors.receive[RoomMessage] {
      case (_, InitRoom(roomService)) => room(Map.empty)(id, roomService)
      case (context, msg) =>
        context.log.error("Room not initialized yet, but received message: {}", msg)
        Behaviors.same
    }

  private def room(
      sessionMap: Map[String, Session]
  )(implicit id: String, roomService: RoomService): Behavior[RoomMessage] = {
    Behaviors.receiveMessage[RoomMessage] {
      case Join(newSessionId, newSession) =>
        val newSessionMap = sessionMap + (newSessionId -> newSession)
        newSessionMap.foreachEntry((_, session) => session ! Joined(newSessionId))
        room(newSessionMap)
      case Exit(sessionId) => room(sessionMap - sessionId)
      case UpstreamRoomMessage(sessionId, body) =>
        roomService ! UpstreamRoomSvcMessage(id, DownstreamRoomMessage(sessionId, body))
        Behaviors.same
      case DownstreamRoomMessage(sessionId, body) =>
        sessionMap.foreachEntry((_, session) => session ! DownstreamSessionMessage(body))
        Behaviors.same
    }
  }
}
