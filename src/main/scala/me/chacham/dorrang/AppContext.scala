package me.chacham.dorrang

import akka.actor.typed.scaladsl.ActorContext
import com.typesafe.config.ConfigFactory
import me.chacham.dorrang.external.ManagementGateway
import me.chacham.dorrang.external.ManagementGateway.ManagementGateway
import me.chacham.dorrang.messagebroker.MessageBroker.MessageBroker
import me.chacham.dorrang.messagebroker.{InitMessageBroker, MessageBroker}
import me.chacham.dorrang.room.RoomService.RoomService
import me.chacham.dorrang.room.{InitRoomService, RoomService}
import org.slf4j.{Logger, LoggerFactory}

object AppContext {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(context: ActorContext[_]): AppContext = {
    val config = ConfigFactory.load()
    val managementBaseUrl = config.getString("dorrang.management.base-url")

    val roomService = context.spawn(RoomService(), RoomService.getClass.getSimpleName)
    val messageBroker = context.spawn(MessageBroker(), MessageBroker.getClass.getSimpleName)
    val managementGateway =
      context.spawn(ManagementGateway(roomService, Option(managementBaseUrl)), ManagementGateway.getClass.getSimpleName)

    roomService ! InitRoomService(messageBroker)
    messageBroker ! InitMessageBroker(roomService)

    logger.info("Initializing AppContext managementBaseUrl:{}", managementBaseUrl)
    AppContext(roomService, messageBroker, managementGateway)
  }
}

case class AppContext(roomService: RoomService, messageBroker: MessageBroker, managementGateway: ManagementGateway)
