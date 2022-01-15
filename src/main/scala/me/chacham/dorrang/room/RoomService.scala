package me.chacham.dorrang.room

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.external.{ManagementGatewayMessage, RoomCreated, RoomTerminated}
import me.chacham.dorrang.messagebroker.{BrokerMessage, UpstreamBrokerMessage}
import me.chacham.dorrang.session.InitSession
import me.chacham.dorrang.session.Session.Session

import java.util.UUID

sealed trait RoomSvcMessage
final case class InitRoomService(messageBroker: ActorRef[BrokerMessage]) extends RoomSvcMessage
final case class CreateRoom(replyTo: ActorRef[ManagementGatewayMessage], roomId: Option[String] = None)
    extends RoomSvcMessage
final case class TerminateRoom(replyTo: ActorRef[ManagementGatewayMessage], roomId: String) extends RoomSvcMessage
final case class UpstreamRoomSvcMessage(roomId: String, roomMessage: RoomMessage) extends RoomSvcMessage
final case class DownstreamRoomSvcMessage(roomId: String, roomMessage: RoomMessage) extends RoomSvcMessage
final case class JoinRoomRequest(roomId: String, sessionId: String, session: Session) extends RoomSvcMessage

object RoomService {
  type RoomService = ActorRef[RoomSvcMessage]

  def apply(): Behavior[RoomSvcMessage] =
    Behaviors.receive[RoomSvcMessage] {
      case (_, InitRoomService(messageBroker)) => roomService(Map.empty)(messageBroker)
      case (context, msg: RoomSvcMessage) =>
        context.log.error("RoomService not initialized yet, but received message: {}", msg)
        Behaviors.same
    }

  private def roomService(
      roomMap: Map[String, ActorRef[RoomMessage]]
  )(implicit messageBroker: ActorRef[BrokerMessage]): Behavior[RoomSvcMessage] = {
    Behaviors.receive[RoomSvcMessage] {
      case (context, _: InitRoomService) =>
        context.log.warn("Already Initialized RoomService")
        Behaviors.same
      case (context, CreateRoom(replyTo, roomIdOption)) =>
        val roomId: String = roomIdOption.getOrElse(UUID.randomUUID().toString)
        val newRoom = context.spawn(Room(roomId, context.self), roomId)
        val newRoomMap = roomMap + (roomId -> newRoom)
        newRoom ! InitRoom(context.self)
        replyTo ! RoomCreated(roomId)
        roomService(newRoomMap)
      case (_, TerminateRoom(replyTo, roomId)) =>
        val newRoomMap = roomMap - roomId
        replyTo ! RoomTerminated(roomId)
        roomService(newRoomMap)
      case (_, UpstreamRoomSvcMessage(roomId, roomMessage)) =>
        messageBroker ! UpstreamBrokerMessage(roomId, roomMessage)
        Behaviors.same
      case (_, DownstreamRoomSvcMessage(roomId, roomMessage)) =>
        roomMap.get(roomId).foreach(_ ! roomMessage)
        Behaviors.same
      case (_, JoinRoomRequest(roomId, sessionId, session)) =>
        roomMap
          .get(roomId)
          .foreach(room => {
            room ! Join(sessionId, session)
            session ! InitSession(room)
          })
        Behaviors.same
    }
  }
}
