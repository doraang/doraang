package me.chacham.dorrang

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.http.scaladsl.Http
import me.chacham.dorrang.api.HttpRoute

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object DoraangApp {
  private def startHttpServer(implicit context: ActorContext[_]): Unit = {
    implicit val system: ActorSystem[Nothing] = context.system
    implicit val executionContext: ExecutionContextExecutor = context.executionContext

    val appContext = AppContext(context)

    Http()
      .newServerAt("localhost", 8080)
      .bind(HttpRoute(context, appContext))
      .onComplete {
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
