package me.chacham.dorrang.api

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives.{complete, concat, handleWebSocketMessages, path, pathPrefix, post}
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import me.chacham.dorrang.AppContext
import me.chacham.dorrang.api.ws.WebSocketHandlers
import me.chacham.dorrang.external.CreateRoomRequest
import org.slf4j.LoggerFactory

object HttpRoute {
  def apply(context: ActorContext[_], appContext: AppContext): Route = {
    val logger = LoggerFactory.getLogger(this.getClass)
    concat(
      pathPrefix("room") {
        path(Segment) { (roomId: String) =>
          post {
            appContext.externalService ! CreateRoomRequest(roomId)
            complete("OK")
          }
        }
      },
      pathPrefix("ws") {
        path(Segment) { (roomId: String) =>
          val clientSessionHandler = WebSocketHandlers.clientSessionHandler(roomId)(context, appContext.roomService)
          handleWebSocketMessages(clientSessionHandler)
        }
      }
    )
  }
}
