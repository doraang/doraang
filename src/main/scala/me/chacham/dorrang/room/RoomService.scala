package me.chacham.dorrang.room

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import me.chacham.dorrang.external.ExternalSvcMessage

sealed trait RoomSvcMessage
final case class CreateRoom(externalService: ActorRef[ExternalSvcMessage]) extends RoomSvcMessage
final case class TerminateRoom(roomId: String) extends RoomSvcMessage

object RoomService {
  def apply(): Behavior[RoomSvcMessage] = {
    Behaviors.receiveMessage[RoomSvcMessage] {
      case CreateRoom(externalService) => Behaviors.same
      case TerminateRoom(roomId)       => Behaviors.same
    }
  }
}
