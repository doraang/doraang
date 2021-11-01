package me.chacham.dorrang.api.ws

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path, pathPrefix}
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import me.chacham.dorrang.session._

import java.util.UUID

object WebSocketRoute {
  private implicit val actorSystem: akka.actor.ActorSystem =
    akka.actor.ActorSystem()

  val route: Route = pathPrefix("ws") {
    path(Segment) { roomId: String =>
      handleWebSocketMessages(newSession(roomId))
    }
  }

  def newSession: String => Flow[Message, Message, Any] =
    (roomId: String) => {
      val (sourceActor: ActorRef[ClientMessage], rawSessionSource: Source[ClientMessage, NotUsed]) =
        ActorSource
          .actorRef(
            PartialFunction.empty,
            PartialFunction.empty,
            1000,
            OverflowStrategy.dropNew
          )
          .preMaterialize()
      val source: Source[Message, NotUsed] = rawSessionSource.via(Flow.fromFunction[ClientMessage, Message] {
        case NormalMessage(body) => TextMessage(body)
      })

      val sessionId = UUID.randomUUID().toString
      val session = ActorSystem(Session(sessionId, sourceActor), sessionId)

      val sinkActor: ActorRef[SessionMessage] = ActorSystem(
        Behaviors.receive[SessionMessage]((_, message) => {
          println(s"Received $message")
          session ! message
          Behaviors.same
        }),
        sessionId
      )

      val sink: Sink[Message, NotUsed] =
        Flow[Message]
          .filter(_.isInstanceOf[TextMessage])
          .flatMapConcat(message => message.asInstanceOf[TextMessage].textStream)
          .map(message => FromClient(message))
          .to(
            ActorSink.actorRef[SessionMessage](
              sinkActor,
              Finished,
              e => Failed(e)
            )
          )

      Flow.fromSinkAndSource(sink, source)
    }
}
