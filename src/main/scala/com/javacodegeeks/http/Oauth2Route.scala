package com.javacodegeeks.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{authenticateOAuth2, authorize, path, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.stream.{ActorMaterializer, Materializer}

// import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.derivation._
import io.circe.{Decoder, ObjectEncoder, derivation}
import io.circe.jawn.decode

import scala.language.postfixOps

case class OAuthInfo (audience: String, expiresIn: Long = 0)
case class InvalidTokenError(error: String="invalid_token")

trait FakeAuthServer {
  implicit val encoder: ObjectEncoder[OAuthInfo] = deriveEncoder(derivation.renaming.snakeCase)

  private val storage = Map[String, OAuthInfo]("ABC123" -> OAuthInfo(audience = "client:1", expiresIn = 1530992266969L) )

  private val error = InvalidTokenError()

  def tokenInfo(token: String) =
    storage.get(token) match {
      case Some(a) => a.asJson.noSpaces
      case None => error.asJson.noSpaces
    }
}

object FakeRemoteAuthServer extends FakeAuthServer

object FakeLocalSessionServer extends FakeAuthServer

trait OAuth2Route {
  def frequencyPreference(in: Long) = (path("frequency-preference") & get) {
    complete {
      in.toString
    }
  }

  def addFreqPref(in: Long) = (path("add-preference") & post) {
    entity(as[String]) { body =>
      complete {
        body.toString
      }
    }
  }

  def pauseInfo(in: Long) = (path("pause-info") & get) {
    complete {
      in.toString
    }
  }

  def addPauseInfo(in: Long) = (path("add-pause-info") & post) {
    entity(as[String]) { body =>
      complete {
        body.toString
      }
    }
  }

  def unPauseInfo(in: Long) = (path("un-pause-info") & get) {
    complete {
      in.toString
    }
  }

  def healthRoute = complete {
    "OK"
  }


  // References
  // https://stackoverflow.com/questions/46840206/unable-to-authenticate-oauth2-with-akka-http
  // https://github.com/akka/akka-http/blob/master/docs/src/test/java/docs/http/javadsl/server/OAuth2AuthenticatorExample.java
  // https://github.com/akka/akka-http/blob/master/docs/src/test/scala/docs/http/scaladsl/server/directives/SecurityDirectivesExamplesSpec.scala
  // https://github.com/jw3/example-akka-oauth/blob/master/src/main/scala/com/github/jw3/oauth/Client.scala
  // https://github.com/akka/akka-http/blob/master/akka-http-testkit/src/test/scala/akka/http/scaladsl/testkit/ScalatestRouteTestSpec.scala
  def route(implicit system: ActorSystem, mat: ActorMaterializer): Route =
    Route.seal {
      pathPrefix("preferences") {
        authenticateOAuth2(realm = "Secure site", authenticator) { authInfo =>
          pathPrefix("frequency" / LongNumber) { custNum =>
            authorize(hasScopes(authInfo)) {
              frequencyPreference(custNum) ~ addFreqPref(custNum)
            }
          } ~ pathPrefix("pause" / LongNumber) { custNum =>
            authorize(hasScopes(authInfo)) {
              pauseInfo(custNum) ~ addPauseInfo(custNum) ~ unPauseInfo(custNum)
            }
          }
        } ~
          (path("health") & (get | put)) {
            healthRoute
          }
      }
    }

  private def unmarshallTokenResponse(json: String) = {
    implicit val decoder: Decoder[OAuthInfo] = deriveDecoder(derivation.renaming.snakeCase)
    val resp = decode[OAuthInfo](json)

    resp match {
      case Right(a) => Some(a)
      case Left(a) => None
    }
  }

  def hasScopes(authInfo: OAuthInfo): Boolean = true

  def authenticator(credentials: Credentials)(implicit system: ActorSystem, mat: ActorMaterializer): Option[OAuthInfo] = {
    credentials match {
      case p @ Credentials.Provided(token) =>
        // val id = p.identifier
        // val same = token == id

        // Check local.
        val json1 = FakeLocalSessionServer.tokenInfo(token)
        val r = unmarshallTokenResponse(json1)

        r match {
          case Some(a) => Some(a)
          case None =>
            // Check remote.
            val json = FakeRemoteAuthServer.tokenInfo(token)
            unmarshallTokenResponse(json1)
        }
      case _ => None
    }
  }
}

class OAuth2Api()(implicit val materializer: Materializer) extends OAuth2Route {

}
