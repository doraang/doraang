package me.chacham.dorrang.external

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.room.CreateRoom
import me.chacham.dorrang.room.RoomService.RoomService

sealed trait ManagementGatewayMessage
final case class CreateRoomRequest(roomId: String) extends ManagementGatewayMessage
final case class RoomCreated(roomId: String) extends ManagementGatewayMessage
final case class RoomTerminated(roomId: String) extends ManagementGatewayMessage

object ManagementGateway {
  type ManagementGateway = ActorRef[ManagementGatewayMessage]

  def apply(roomService: RoomService): Behavior[ManagementGatewayMessage] = {
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
