package com.javacodegeeks.http

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.Matchers
import org.scalatest.WordSpec
import com.javacodegeeks.persistence.DbConfiguration
import com.javacodegeeks.persistence.UsersRepository
import com.javacodegeeks.model.User
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.BeforeAndAfterEach
import scala.language.postfixOps
import akka.http.scaladsl.model.headers.Location
import com.javacodegeeks.model.UserUpdate
import akka.http.scaladsl.model.headers.BasicHttpCredentials

class UserRouteSpec extends WordSpec
    with Matchers with ScalatestRouteTest with BeforeAndAfterEach
      with DbConfiguration with UserRoute {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  val timeout = 500 milliseconds
  override val repository = new UsersRepository(config)

  override def beforeEach(): Unit = {
    Await.result(repository.init(), timeout)
  }

  override def afterEach(): Unit = {
    Await.result(repository.drop(), timeout)
  }

  "Users Web API" should {
    "return all users" in {
      Get("/users") ~> route ~> check {
        responseAs[Seq[User]] shouldEqual Seq()
      }
    }

    "create new user" in {
      Post("/users", User(None, "a@b.com", None, None)) ~> route ~> check {
        status shouldEqual Created
        header[Location] shouldBe defined
      }
    }

    "cannot create new user with the same email twice" in {
      Post("/users", User(None, "a@b.com", None, None)) ~> route ~> check {
        status shouldEqual Created
        header[Location] shouldBe defined
      }

      Post("/users", User(None, "a@b.com", None, None)) ~> route ~> check {
        status shouldEqual Conflict
        header[Location] shouldBe empty
      }
    }

    "create and update user" in {
      Post("/users", User(None, "a@b.com", None, None)) ~> route ~> check {
        status shouldEqual Created
        header[Location] map { location =>
          val credentials = BasicHttpCredentials("a@b.com", "password")
          Put(location.uri, UserUpdate(Some("John"), Some("Smith"))) ~> addCredentials(credentials) ~> route ~> check {
            status shouldEqual OK
            responseAs[User] should have {
              'firstName (Some("John"))
              'lastName (Some("Smith"))
            }
          }
        }
      }
    }
  }
}
