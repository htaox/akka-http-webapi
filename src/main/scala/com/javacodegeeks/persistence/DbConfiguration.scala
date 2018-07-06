package com.javacodegeeks.persistence

import slick.driver.JdbcProfile
import slick.backend.DatabaseConfig

trait DbConfiguration {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("db")
}