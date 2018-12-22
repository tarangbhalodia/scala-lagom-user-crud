package com.lagom.user.infrastructure.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.lagom.user.api
import com.lagom.user.api._
import com.lagom.user.infrastructure.ElasticsearchClient
import com.lagom.user.infrastructure.util.Converters._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import com.sksamuel.elastic4s.analyzers.{AsciiFoldingTokenFilter, LowercaseTokenFilter}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.{ElasticDsl, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.searches.queries.BoolQuery
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl(elasticsearch: ElasticsearchClient)(implicit executionContext: ExecutionContext) extends UserService {

  private lazy val logger = Logger(classOf[UserServiceImpl])
  private val INDEX_NAME = "user"
  private val TYPE_NAME = "entry"
  private val NORMALIZED_FIELD = "normalized-field"

  createIndex()

  private def createIndex() = {
    for {
      _ <- elasticsearch
        .execute {
          createIndexTemplate("generic-template", "*")
            .settings(Map("number_of_shards" -> 5))
            .normalizers(customNormalizer("lowercase-normalizer", LowercaseTokenFilter, AsciiFoldingTokenFilter))
            .mappings(
              mapping("entry") templates (
                dynamicTemplate("longs")
                  .mapping(dynamicLongField().fields(longField(NORMALIZED_FIELD))) matchMappingType "long",
                dynamicTemplate("doubles")
                  .mapping(dynamicDoubleField().fields(doubleField(NORMALIZED_FIELD))) matchMappingType "double",
                dynamicTemplate("dates")
                  .mapping(dynamicDateField().fields(dateField(NORMALIZED_FIELD))) matchMappingType "date",
                dynamicTemplate("booleans")
                  .mapping(dynamicBooleanField().fields(booleanField(NORMALIZED_FIELD))) matchMappingType "boolean",
                dynamicTemplate("strings")
                  .mapping(dynamicTextField().fields(keywordField(NORMALIZED_FIELD).normalizer("lowercase-normalizer"))) matchMappingType "string"
              )
            )
        }
      _ <- elasticsearch.execute(ElasticDsl.createIndex(INDEX_NAME))
    } yield ()
  }

  override def getUsers(pageNumber: Int, pageSize: Int): ServiceCall[SearchRequest, SearchResponse] = ServiceCall { request =>
    val searchQuery =
      request.keyword.fold[BoolQuery](boolQuery().must(matchAllQuery())) { serchTerm =>
        boolQuery()
          .should {
            Seq(
              matchQuery("firstName", serchTerm),
              matchQuery(s"firstName.$NORMALIZED_FIELD", serchTerm),
              matchQuery("lastName", serchTerm),
              matchQuery(s"lastName.$NORMALIZED_FIELD", serchTerm),
              matchQuery("email", serchTerm),
              matchQuery(s"email.$NORMALIZED_FIELD", serchTerm)
            )
          }
          .minimumShouldMatch(1)
      }
    if (pageNumber < 1) throw BadRequest(s"PageNumber: $pageNumber must be greater or equal to 1")
    if (pageSize < 1) throw BadRequest(s"PageSize: $pageSize must be greater or equal to 1")
    executeRequest(searchQuery, pageSize, pageNumber)
  }

  private def findOptionalBy(id: UUID): Future[Option[User]] = {
    val booleanIdsQuery = boolQuery().must(idsQuery(id))
    executeRequest(booleanIdsQuery).map { result =>
      result.result.headOption
    }
  }

  override def findBy(id: UUID): ServiceCall[NotUsed, User] = ServiceCall { _ =>
    findOptionalBy(id).map {
      case None       => throw NotFound(s"User with id: $id not found")
      case Some(user) => user
    }
  }

  private def index(user: User): Future[User] = {
    val indexRequest = ElasticDsl.indexInto(INDEX_NAME / TYPE_NAME).doc(user).id(user.id.toString)
    elasticsearch.execute(indexRequest).map {
      case RequestFailure(_, _, _, error) =>
        logger.error(s"Error while indexing $user: $user - ${error.reason}")
        throw new RuntimeException(s"Error while index $user: $user - ${error.reason}")

      case RequestSuccess(_, _, _, _) => user
    }
  }

  private def findOptionalByEmail(email: String): Future[Option[User]] = {
    val emailQuery = boolQuery().must(matchQuery(s"email.$NORMALIZED_FIELD", email))
    for {
      result <- executeRequest(emailQuery)
    } yield result.result.headOption
  }

  override def create: ServiceCall[CreateUser, User] = ServiceCall { createUser =>
    for {
      existingUser <- findOptionalByEmail(createUser.email)
      result <- existingUser match {
        case None => index(createUser.getUser)
        case _    => throw BadRequest(s"User with email: ${createUser.email} already exists")
      }
    } yield result
  }

  override def update(userId: UUID): ServiceCall[User, User] = ServiceCall { user =>
    for {
      _ <- findBy(userId).invoke()
      result <- index(user.copy(id = userId))
    } yield result
  }

  override def delete(userId: UUID): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    for {
      _ <- findBy(userId).invoke()
      _ <- elasticsearch.execute(deleteById(INDEX_NAME, TYPE_NAME, userId.toString))
    } yield Done
  }

  private def executeRequest(query: BoolQuery, pageSize: Int = 10, pageNumber: Int = 1): Future[SearchResponse] = {
    val request = search(INDEX_NAME)
      .query(query)
      .from((pageNumber - 1) * pageSize)
      .size(pageSize)

    if (logger.isDebugEnabled) logger.debug(s"Search request is: $request")
    elasticsearch
      .execute(request)
      .map {
        case RequestFailure(_, _, _, error) =>
          logger.error(s"""Error while performing search request: ${request.show}:
                        																												|Error: ${error.reason}
                        																												|""".stripMargin)

          throw new RuntimeException(s"Failed to execute search request: ${error.reason}")
        case RequestSuccess(_, _, _, result) => api.SearchResponse(result.hits.toUsers, pageNumber, pageSize, result.totalHits)
      }
  }
}
