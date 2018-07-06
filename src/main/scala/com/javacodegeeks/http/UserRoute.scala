package com.javacodegeeks.http

import com.javacodegeeks.persistence.UsersRepository
import com.javacodegeeks.model.User
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.server.directives.Credentials

import scala.concurrent.Future
import com.javacodegeeks.model.UserUpdate
import akka.http.scaladsl.model.headers.Location
import java.sql.SQLException

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.language.postfixOps

trait UserRoute {
  val repository: UsersRepository
  implicit val materializer: Materializer

  import scala.concurrent.ExecutionContext.Implicits.global
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  implicit val userFormat = jsonFormat4(User)
  implicit val userUpdateFormat = jsonFormat2(UserUpdate)
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  val exceptionHandler = ExceptionHandler {
    case ex: SQLException => complete(Conflict, ex.getMessage)
  }

  def authenticator(credentials: Credentials): Future[Option[User]] = {
    credentials match {
      case p @ Credentials.Provided(email) if p.verify("password") =>
        repository.findByEmail(email)
      case _ => Future.successful(None)
    }
  }

  val route: Route = logRequestResult("user-routes") {
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          complete(repository.findAll)
        } ~
        post {
          withRequestTimeout(5 seconds) {
            handleExceptions(exceptionHandler) {
              extractUri { uri =>
                entity(as[User]) { user =>
                  onSuccess(repository.insert(user)) { u =>
                    respondWithHeader(Location(uri.withPath(uri.path / u.id.mkString))) {
                      complete(Created, u)
                    }
                  }
                }
              }
            }
          }
        }
      } ~
      path(IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(repository.find(id))
          }
        } ~
        put {
          authenticateBasicAsync(realm = "Users", authenticator) { user =>
            rejectEmptyResponse {
              entity(as[UserUpdate]) { user =>
                complete(
                  repository.update(id, user.firstName, user.lastName) map {
                    case true => repository.find(id)
                    case _=> Future.successful(None)
                  }
                )
              }
            }
          }
        }
      } ~
      path("stream") {
        get {
          complete(repository.stream)
        }
      }
    }
  }

}

class UserApi(val repository: UsersRepository)(implicit val materializer: Materializer) extends UserRoute {

}
