package me.chacham.dorrang.external

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.room.CreateRoom
import me.chacham.dorrang.room.RoomService.RoomService

sealed trait ExternalSvcMessage
final case class CreateRoomRequest(roomId: String) extends ExternalSvcMessage
final case class RoomCreated(roomId: String) extends ExternalSvcMessage
final case class RoomTerminated(roomId: String) extends ExternalSvcMessage

object ExternalService {
  type ExternalService = ActorRef[ExternalSvcMessage]

  def apply(roomService: RoomService): Behavior[ExternalSvcMessage] = {
    Behaviors.receive {
      case (context, CreateRoomRequest(roomId)) =>
        roomService ! CreateRoom(context.self, Some(roomId))
        Behaviors.same
      case (context, RoomCreated(roomId)) =>
        context.log.info("RoomCreated: {}", roomId)
        Behaviors.same
      case (context, RoomTerminated(roomId)) =>
        context.log.info("RoomTerminated: {}", roomId)
        Behaviors.same
    }
  }
}
