package com.javacodegeeks.persistence

import slick.lifted.ProvenShape.proveShapeOf
import com.javacodegeeks.model.User

trait UsersTable { this: Db =>
  import config.driver.api._
  
  private[persistence] class Users(tag: Tag) extends Table[User](tag, "USERS") {
    // Columns
    def id = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("USER_EMAIL", O.Length(512))
    def firstName = column[Option[String]]("USER_FIRST_NAME", O.Length(64)) 
    def lastName = column[Option[String]]("USER_LAST_NAME", O.Length(64))
    
    // Indexes
    def emailIndex = index("USER_EMAIL_IDX", email, true)
    
    // Select
    def * = (id.?, email, firstName, lastName) <> (User.tupled, User.unapply)
  }
  
  val users = TableQuery[Users]
}

