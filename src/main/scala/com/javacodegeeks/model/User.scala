package com.javacodegeeks.model

case class User(id: Option[Int], email: String, firstName: Option[String], lastName: Option[String])
case class UserUpdate(firstName: Option[String], lastName: Option[String])