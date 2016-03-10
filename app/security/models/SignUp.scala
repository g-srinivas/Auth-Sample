package security.models

case class SignUp(
  password: String,
  identifier: String,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String])
