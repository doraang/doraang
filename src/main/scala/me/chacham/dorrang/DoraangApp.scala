package me.chacham.dorrang

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, concat, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import me.chacham.dorrang.api.ws.WebSocketRoute

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object DoraangApp {
  def route(context: ActorContext[_]): Route = {
    concat(
      path("room") {
        post {
          complete("DONE")
        }
      },
      pathPrefix("ws") { WebSocketRoute(context) }
    )
  }

  private def startHttpServer(implicit context: ActorContext[_]): Unit = {
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val executionContext: ExecutionContextExecutor = context.executionContext

    val futureBinding = Http()(system).newServerAt("localhost", 8080).bind(route(context))
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      startHttpServer(context)
      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "DoraangHttpServer")
  }
}
