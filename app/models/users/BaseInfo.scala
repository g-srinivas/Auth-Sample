package models.users

case class BaseInfo(
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  gender: Option[String])
