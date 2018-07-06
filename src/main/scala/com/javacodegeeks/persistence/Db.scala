package com.javacodegeeks.persistence

import slick.driver.JdbcProfile
import slick.backend.DatabaseConfig

trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db
}