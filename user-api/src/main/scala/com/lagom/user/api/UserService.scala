package com.lagom.user.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json._

trait UserService extends Service {

  def getUsers(pageNumber: Int, pageSize: Int): ServiceCall[SearchRequest, SearchResponse]

  def findBy(userId: UUID): ServiceCall[NotUsed, User]

  def create: ServiceCall[CreateUser, User]

  def update(userId: UUID): ServiceCall[User, User]

  def delete(userId: UUID): ServiceCall[NotUsed, Done]

  override final def descriptor: Descriptor = {
    import Service._
    named("lagom-login")
      .withCalls(
        pathCall("/api/users?pageNumber&pageSize", getUsers _),
        pathCall("/api/users/:userId", findBy _),
        pathCall("/api/user/create", create _),
        pathCall("/api/user/:userId/update", update _),
        pathCall("/api/user/:userId/delete", delete _)
      )
      .withAutoAcl(true)
  }
}

case class CreateUser(firstName: String, lastName: String, email: String) {
  def getUser = User(UUID.randomUUID(), firstName, lastName, email)
}
object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}

case class User(id: UUID = UUID.randomUUID(), firstName: String, lastName: String, email: String)

object User {
  implicit val format: Format[User] = Json.format
}

case class SearchRequest(keyword: Option[String])

object SearchRequest {
  implicit val format: Format[SearchRequest] = Json.format
}

case class SearchResponse(result: Seq[User], pageNumber: Int, pageSize: Int, totalRecords: Long)

object SearchResponse {
  implicit val format: Format[SearchResponse] = Json.format
}
