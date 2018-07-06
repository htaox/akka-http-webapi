package com.javacodegeeks.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

case class GoogleTokenInfo(aud: String, sub: String, email: String)

sealed trait TokenType
case object IdToken     extends TokenType
case object AccessToken extends TokenType

class GoogleOauthService()(implicit system: ActorSystem, executor: ExecutionContextExecutor, materializer: ActorMaterializer)
  extends DefaultJsonProtocol {

  implicit val googleTokenInfoFormat = jsonFormat3(GoogleTokenInfo)
  val config                         = ConfigFactory.load()

  val fetchAndValidateTokenInfo: (String, TokenType) => Future[GoogleTokenInfo] = (tokenValue, tokenType) => {
    val tokenQuery = tokenType match {
      case IdToken     => s"id_token=$tokenValue"
      case AccessToken => s"access_token=$tokenValue"
    }

    for {
      response <- Http().singleRequest(
        HttpRequest(
          method = HttpMethods.GET,
          uri = s"https://www.googleapis.com/oauth2/v3/tokeninfo?$tokenQuery"
        ))
      tokenInfo <- Unmarshal(response.entity).to[GoogleTokenInfo]
      validatedTokenInfo <- if (tokenInfo.aud == config.getString("googleClientId")) Future.successful(tokenInfo)
      else Future.failed(new RuntimeException("Invalid sub"))
    } yield validatedTokenInfo
  }
}
