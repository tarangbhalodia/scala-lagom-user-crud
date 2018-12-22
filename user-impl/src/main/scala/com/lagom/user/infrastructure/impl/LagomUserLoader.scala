package com.lagom.user.infrastructure.impl

import com.lagom.user.api.UserService
import com.lagom.user.infrastructure.ElasticsearchClient
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContext

trait LoginComponent extends LagomServerComponents {
  implicit def executionContext: ExecutionContext
  def environment: Environment
}

class LagomUserLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomloginApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomloginApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[UserService])
}

abstract class LagomloginApplication(context: LagomApplicationContext) extends LagomApplication(context) with LoginComponent with AhcWSComponents {
  lazy val elasticSearch: ElasticsearchClient = wire[ElasticsearchClient]

  override lazy val lagomServer: LagomServer = serverFor[UserService](wire[UserServiceImpl])
}
