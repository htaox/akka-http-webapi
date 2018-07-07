package com.javacodegeeks.http

import scala.concurrent.duration._
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class OAuth2RouteSpec extends WordSpec
  with Matchers with ScalatestRouteTest with BeforeAndAfterEach
  with OAuth2Route {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  val timeout = 500 milliseconds
  val badToken = "XYZ"
  val goodToken = "ABC123"

  "OAuth2 Web API" should {
    "return health check" in {
      Get("/preferences/health") ~> route ~> check {
        responseAs[String] should be ("OK")
        // responseEntity shouldEqual HttpEntity(ContentTypes.`text/plain(UTF-8)`, "OK")
      }
    }

    "should fail with bad token" in {
      val auth = Authorization(OAuth2BearerToken(badToken))
      Get("/preferences/frequency/33/frequency-preference") ~> addHeader(auth) ~> route ~>
        check {
          responseAs[String] should be ("The supplied authentication is invalid")
          status shouldEqual Unauthorized
        }
    }

    "should succeed with good token" in {
      val auth = Authorization(OAuth2BearerToken(goodToken))
      Get("/preferences/frequency/33/frequency-preference") ~> addHeader(auth) ~> route ~>
        check {
          responseAs[String] should be ("33")
          status shouldEqual OK
        }

      Get("/preferences/pause/44/pause-info") ~> addHeader(auth) ~> route ~>
        check {
          responseAs[String] should be ("44")
          status shouldEqual OK
        }
    }

  }

}
