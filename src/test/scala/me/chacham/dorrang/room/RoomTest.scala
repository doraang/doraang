package me.chacham.dorrang.room

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import me.chacham.dorrang.session.{Joined, SessionMessage}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.util.UUID

class RoomTest extends AnyFlatSpec with BeforeAndAfterAll with should.Matchers {
  val testKit: ActorTestKit = ActorTestKit()

  "A Room" should "broadcast join message to sessions if receive join message" in {
    val roomService = testKit.spawn(RoomService())
    val room = testKit.spawn(Room("testRoomId", roomService))
    val session1 = testKit.createTestProbe[SessionMessage]()
    val session2 = testKit.createTestProbe[SessionMessage]()
    val session3 = testKit.createTestProbe[SessionMessage]()
    val session1Id = UUID.randomUUID().toString
    val session2Id = UUID.randomUUID().toString
    val session3Id = UUID.randomUUID().toString
    room ! Join(session1Id, session1.ref)
    room ! Join(session2Id, session2.ref)
    room ! Join(session3Id, session3.ref)
    session1.expectMessage(Joined(session2Id))
    session1.expectMessage(Joined(session3Id))
    session2.expectMessage(Joined(session3Id))
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()
}
