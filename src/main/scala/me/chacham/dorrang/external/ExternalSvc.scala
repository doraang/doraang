package me.chacham.dorrang.external

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

sealed trait ExternalSvcMessage
final case class RoomCreated(roomId: String) extends ExternalSvcMessage
final case class RoomTerminated(roomId: String) extends ExternalSvcMessage

object ExternalService {
  def apply(): Behavior[ExternalSvcMessage] = {
    Behaviors.receiveMessage[ExternalSvcMessage] {
      case _ => Behaviors.same
    }
  }
}
