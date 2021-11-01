package me.chacham.dorrang.api

import akka.http.scaladsl.server.Directives.{complete, concat, handleWebSocketMessages, path, pathPrefix, post}
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import me.chacham.dorrang.api.ws.WebSocketRoute

object RootRoute {
  def apply(): Route = {
    concat(
      path("room") {
        post {
          complete("DONE")
        }
      },
      pathPrefix("ws") {
        path(Segment) { roomId =>
          handleWebSocketMessages(WebSocketRoute.newSession(roomId))
        }
      }
    )
  }
}
