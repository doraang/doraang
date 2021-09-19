package me.chacham

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}

import java.util.UUID

object WebSocketHandler {
  private implicit val actorSystem: akka.actor.ActorSystem =
    akka.actor.ActorSystem()

  def echo: Flow[Message, Message, Any] = {
    val (sourceActor: ActorRef[Message], source: Source[Message, NotUsed]) =
      ActorSource
        .actorRef(
          PartialFunction.empty,
          PartialFunction.empty,
          1000,
          OverflowStrategy.dropTail
        )
        .preMaterialize()

    val sinkActor: ActorRef[Message] = ActorSystem(
      Behaviors.receive[Message]((_, message) => {
        println(message)
        sourceActor ! message
        Behaviors.same
      }),
      UUID.randomUUID().toString
    )
    val sink: Sink[Message, NotUsed] = ActorSink.actorRef(
      sinkActor,
      TextMessage("FINISH"),
      PartialFunction.empty
    )

    Flow.fromSinkAndSource(sink, source)
  }
}
