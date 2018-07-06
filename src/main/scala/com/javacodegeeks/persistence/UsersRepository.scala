package com.javacodegeeks.persistence

import com.javacodegeeks.model.User

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import slick.backend.DatabaseConfig
import slick.dbio.DBIOAction
import slick.driver.JdbcProfile

class UsersRepository(val config: DatabaseConfig[JdbcProfile]) extends Db with UsersTable {
  import config.driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def init() = db.run(DBIOAction.seq(users.schema.create))
  def drop() = db.run(DBIOAction.seq(users.schema.drop))

  def insert(user: User) = db
    .run(users returning users.map(_.id) += user)
    .map(id => user.copy(id = Some(id)))

  def find(id: Int) = db.run((for (user <- users if user.id === id) yield user).result.headOption)
  def findByEmail(email: String) = db.run((for (user <- users if user.email === email) yield user).result.headOption)
  def findAll() = db.run(users.result)

  def update(id: Int, firstName: Option[String], lastName: Option[String]) = {
    val query = for (user <- users if user.id === id) yield (user.firstName, user.lastName)
    db.run(query.update(firstName, lastName)) map { _ > 0 }
  }

  def delete(id: Int) =
    db.run(users.filter(_.id === id).delete) map { _ > 0 }

  def getNames(id: Int) =
    db.run(sql"select user_first_name, user_last_name from users where user_id = #$id"
      .as[(String, String)].headOption)

 def stream(implicit materializer: Materializer) = Source
   .fromPublisher(db.stream(users.result.withStatementParameters(fetchSize = 10)))
}
