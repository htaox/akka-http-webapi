package com.javacodegeeks.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{authenticateOAuth2, authorize, path, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.stream.{ActorMaterializer, Materializer}
import scala.language.postfixOps

case class OAuthInfo (audience: String)

trait OAuth2Route {
  def frequencyPreference(in: Long) = complete {
    in.toString
  }

  def addFreqPref(in: Long) = complete {
    in.toString
  }

  def pauseInfo(in: Long) = complete {
    in.toString
  }

  def addPauseInfo(in: Long) = complete {
    in.toString
  }

  def unPauseUser(in: Long) = complete {
    in.toString
  }

  def healthRoute = complete {
    "OK"
  }


  // Oauth2 test
  // https://stackoverflow.com/questions/46840206/unable-to-authenticate-oauth2-with-akka-http
  // https://github.com/akka/akka-http/blob/master/docs/src/test/java/docs/http/javadsl/server/OAuth2AuthenticatorExample.java
  // https://github.com/akka/akka-http/blob/master/docs/src/test/scala/docs/http/scaladsl/server/directives/SecurityDirectivesExamplesSpec.scala
  def route(implicit system: ActorSystem, mat: ActorMaterializer): Route =
    Route.seal {
      pathPrefix("newsletter-preferences") {
        authenticateOAuth2(realm = "Secure site", authenticator) { authInfo =>
          path("frequency" / LongNumber) { custNum =>
            authorize(hasScopes(authInfo)) {
              frequencyPreference(custNum) ~ addFreqPref(custNum)
            }
          } ~ path("pause" / LongNumber) { custNum =>
            authorize(hasScopes(authInfo)) {
              pauseInfo(custNum) ~ addPauseInfo(custNum) ~ unPauseUser(custNum)
            }
          }
        } ~
          (path("health") & (get | put)) {
            healthRoute
          }
      }
    }

  def hasScopes(authInfo: OAuthInfo): Boolean = true

  def authenticator(credentials: Credentials)(implicit system: ActorSystem, mat: ActorMaterializer): Option[OAuthInfo] = {
    credentials match {
      case p @ Credentials.Provided(token) =>
        // p.verify(secret = "password")
        val id = p.identifier
        // Check local session store, if not found, check external.
        Some(OAuthInfo(audience = "ABC"))
      case _ => None
    }
  }
}

class OAuth2Api()(implicit val materializer: Materializer) extends OAuth2Route {

}