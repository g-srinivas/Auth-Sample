package models.users

import com.mohiva.play.silhouette.api.Identity
import com.mohiva.play.silhouette.api.LoginInfo
import java.util.UUID
import models.authorizations._

case class User(
  id: String = UUID.randomUUID.toString,
  loginInfo: LoginInfo,
  socials: Option[Seq[LoginInfo]] = None,
  email: Option[String],
  username: Option[String],
  avatarUrl: Option[String],
  info: BaseInfo,
  roles: Set[Role] = Set(SimpleUser)) extends Identity {

}

object User {

}
