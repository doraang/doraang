package me.chacham.dorrang.external

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Put
import me.chacham.dorrang.room.CreateRoom
import me.chacham.dorrang.room.RoomService.RoomService
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

sealed trait ManagementGatewayMessage
final case class CreateRoomRequest(roomId: String) extends ManagementGatewayMessage
final case class RoomCreated(roomId: String) extends ManagementGatewayMessage
final case class RoomTerminated(roomId: String) extends ManagementGatewayMessage

object ManagementGateway {
  type ManagementGateway = ActorRef[ManagementGatewayMessage]

  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  def apply(roomService: RoomService, callbackBaseUrl: Option[String]): Behavior[ManagementGatewayMessage] = {
    Behaviors.setup(context => {
      val http = Http(context.system)
      Behaviors.receiveMessage {
        case CreateRoomRequest(roomId) =>
          roomService ! CreateRoom(context.self, Some(roomId))
          Behaviors.same
        case msg @ RoomCreated(roomId) =>
          context.log.info("RoomCreated: {} {}", roomId, callbackBaseUrl)
          callbackBaseUrl.foreach(baseUrl => {
            http.singleRequest(Put(s"$baseUrl/room").withEntity(Serialization.write(msg)))
          })
          Behaviors.same
        case RoomTerminated(roomId) =>
          context.log.info("RoomTerminated: {}", roomId)
          Behaviors.same
      }
    })
  }
}
