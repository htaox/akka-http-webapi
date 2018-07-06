package com.javacodegeeks.http

import scala.concurrent.duration._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class OAuth2RouteSpec extends WordSpec
  with Matchers with ScalatestRouteTest with BeforeAndAfterEach
  with OAuth2Route {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  val timeout = 500 milliseconds

  "OAuth2 Web API" should {
    "return health check" in {
      Get("/health") ~> route ~> check {
        response should be ("OK")
      }
    }

  }

}
