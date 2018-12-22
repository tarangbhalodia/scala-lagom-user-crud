//package com.lagom.user.infrastructure.impl
//
//import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
//import com.lightbend.lagom.scaladsl.testkit.ServiceTest
//import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
//import com.demo.login.lagomlogin.api._
//import com.lagom.login.api.GreetingMessage
//import com.lagom.user.api.UserService
//
//class UserServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
//
//  private val server = ServiceTest.startServer(
//    ServiceTest.defaultSetup
//  ) { ctx =>
//    new LagomloginApplication(ctx) with LocalServiceLocator
//  }
//
//  val client = server.serviceClient.implement[UserService]
//
//  override protected def afterAll() = server.stop()
//
//  "lagom-login service" should {
//
//    "say hello" in {
//      client.hello("Alice").invoke().map { answer =>
//        answer should ===("Hello, Alice!")
//      }
//    }
//
//    "allow responding with a custom message" in {
//      for {
//        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
//        answer <- client.hello("Bob").invoke()
//      } yield {
//        answer should ===("Hi, Bob!")
//      }
//    }
//  }
//}
