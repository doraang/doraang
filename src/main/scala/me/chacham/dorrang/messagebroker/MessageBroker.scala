package me.chacham.dorrang.messagebroker

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.room.{DownstreamRoomSvcMessage, RoomMessage, RoomSvcMessage}

sealed trait BrokerMessage
final case class InitMessageBroker(roomService: ActorRef[RoomSvcMessage]) extends BrokerMessage
final case class UpstreamBrokerMessage(roomId: String, message: RoomMessage) extends BrokerMessage
final case class DownstreamBrokerMessage(roomId: String, message: RoomMessage) extends BrokerMessage

object MessageBroker {
  type MessageBroker = ActorRef[BrokerMessage]

  def apply(): Behavior[BrokerMessage] =
    Behaviors.setup(context => {
      Behaviors.receiveMessage[BrokerMessage] {
        case InitMessageBroker(roomService) => standaloneMessageBroker(roomService)
        case msg: BrokerMessage =>
          context.log.error("MessageBroker NOT Initialized yet, but received message: {}", msg)
          Behaviors.same
      }
    })

  private def standaloneMessageBroker(implicit roomService: ActorRef[RoomSvcMessage]): Behavior[BrokerMessage] = {
    Behaviors.setup(context => {
      Behaviors.receiveMessage[BrokerMessage] {
        case UpstreamBrokerMessage(roomId, message) =>
          context.self ! DownstreamBrokerMessage(roomId, message)
          Behaviors.same
        case DownstreamBrokerMessage(roomId, message) =>
          roomService ! DownstreamRoomSvcMessage(roomId, message)
          Behaviors.same
      }
    })
  }
}
