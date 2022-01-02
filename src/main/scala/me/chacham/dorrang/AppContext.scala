package me.chacham.dorrang

import akka.actor.typed.scaladsl.ActorContext
import me.chacham.dorrang.external.ExternalService
import me.chacham.dorrang.external.ExternalService.ExternalService
import me.chacham.dorrang.messagebroker.MessageBroker.MessageBroker
import me.chacham.dorrang.messagebroker.{InitMessageBroker, MessageBroker}
import me.chacham.dorrang.room.RoomService.RoomService
import me.chacham.dorrang.room.{InitRoomService, RoomService}

object AppContext {
  def apply(context: ActorContext[_]): AppContext = {
    val roomService = context.spawn(RoomService(), "roomService")
    val messageBroker = context.spawn(MessageBroker(), "messageBroker")
    val externalService = context.spawn(ExternalService(roomService), "externalService")

    roomService ! InitRoomService(messageBroker)
    messageBroker ! InitMessageBroker(roomService)

    AppContext(roomService, messageBroker, externalService)
  }
}

case class AppContext(roomService: RoomService, messageBroker: MessageBroker, externalService: ExternalService)
