package services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.{JsValue, JsNull}
import scala.concurrent.Future
import security.models._
import models.users._
import com.mohiva.play.silhouette.api.services.AuthInfo
trait UserService extends IdentityService[User] {

  def create(loginInfo: LoginInfo, signUp: SignUp, avatarUrl: Option[String] = None, json: JsValue = JsNull): Future[User]

  def save(user: User): Future[User]

  def save[A <: AuthInfo](profile: CommonSocialProfile): Future[User]

  def link[A <: AuthInfo](user: User, profile: CommonSocialProfile): Future[User]
}
