package me.chacham.dorrang.api.ws

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import me.chacham.dorrang.room.JoinRoomRequest
import me.chacham.dorrang.room.RoomService.RoomService
import me.chacham.dorrang.session.Session.Client
import me.chacham.dorrang.session._

import java.util.UUID

object WebSocketHandlers {

  def clientSessionHandler(
      roomId: String
  )(context: ActorContext[_], roomService: RoomService): Flow[Message, Message, NotUsed] = {
    implicit val actorSystem: ActorSystem[Nothing] = context.system

    val sessionId = UUID.randomUUID().toString

    val (client: Client, rawSessionSource: Source[ClientMessage, NotUsed]) =
      ActorSource
        .actorRef(
          PartialFunction.empty,
          PartialFunction.empty,
          1000,
          OverflowStrategy.dropNew
        )
        .preMaterialize()
    val source: Source[Message, NotUsed] = rawSessionSource.via(Flow.fromFunction[ClientMessage, Message] {
      case ChatMessage(body) => TextMessage(body)
    })

    val session = ActorSystem(Session(sessionId, client), sessionId)

    val sink: Sink[Message, NotUsed] =
      Flow[Message]
        .filter(_.isInstanceOf[TextMessage])
        .flatMapConcat(message => message.asInstanceOf[TextMessage].textStream)
        .map(message => UpstreamSessionMessage(message))
        .to(
          ActorSink.actorRef[SessionMessage](
            session,
            Finished,
            e => Failed(e)
          )
        )

    roomService ! JoinRoomRequest(roomId, sessionId, session)

    Flow.fromSinkAndSource(sink, source)
  }
}
