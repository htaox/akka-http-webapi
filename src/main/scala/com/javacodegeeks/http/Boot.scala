// Courtesy: https://www.javacodegeeks.com/2016/11/developing-modern-applications-scala-web-apis-akka-http.html

package com.javacodegeeks.http

import akka.http.scaladsl.Http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.javacodegeeks.persistence.DbConfiguration
import com.javacodegeeks.persistence.UsersRepository

object Boot extends App with DbConfiguration with SslSupport {
  implicit val system = ActorSystem("akka-http-webapi")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val repository = new UsersRepository(config)
  val routes2 = new UserApi(repository).route
  val routes = new OAuth2Api().route
  
  Http().bindAndHandle(routes, "0.0.0.0", 58083, connectionContext = https)
  
  Http().bindAndHandle(routes, "0.0.0.0", 58080)
    .flatMap(r => repository.init())
}
