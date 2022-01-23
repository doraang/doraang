package me.chacham.dorrang.api.rest

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import me.chacham.dorrang.AppContext
import me.chacham.dorrang.external.CreateRoomRequest

object ManagementRoute {
  def apply(context: ActorContext[_], appContext: AppContext): Route = {
    pathPrefix("manage") {
      pathPrefix("room" / Segment) { (roomId: String) =>
        post {
          appContext.managementGateway ! CreateRoomRequest(roomId)
          complete("OK")
        }
      }
    }
  }
}
