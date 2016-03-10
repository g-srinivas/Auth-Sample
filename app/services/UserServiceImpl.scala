package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.AuthInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.libs.json.{JsNull, JsValue}
import scala.collection.mutable.HashMap
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import security.models._
import models.users._

class UserServiceInMemory extends UserService {
  def create(loginInfo: LoginInfo, signUp: SignUp, avatarUrl: Option[String] = None, json: JsValue = JsNull): Future[User] = {
    val fullName = signUp.fullName.getOrElse(signUp.firstName.getOrElse("None") + " " + signUp.lastName.getOrElse("None"))
    val info = BaseInfo(
      firstName = signUp.firstName,
      lastName = signUp.lastName,
      fullName = Some(fullName),
      gender = None)
    val user = User(
      loginInfo = loginInfo,
      email = Some(signUp.identifier),
      username = None,
      avatarUrl = avatarUrl,
      info = info)
    Future.successful {
      User(
        loginInfo = loginInfo,
        email = Some(signUp.identifier),
        username = None,
        avatarUrl = avatarUrl,
        info = info)
    }
  }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
      Future.successful {
        UserServiceImpl.users.find {
          case (id, user) => user.loginInfo == loginInfo || user.socials.map(_.find(li => li == loginInfo)).isDefined
        }.map(_._2)
      }
  }

  def save(user: User) = {
      UserServiceImpl.users += (user.loginInfo.toString -> user)
      Future.successful(user)
  }

  def save[A <: AuthInfo](profile: CommonSocialProfile) = {

  retrieve(profile.loginInfo).flatMap {
    case Some(user) => // Update user with profile
      val u = user.copy(info = BaseInfo(
        firstName = profile.firstName,
        lastName = profile.lastName,
        fullName = profile.fullName,
        gender = None),
        email = profile.email,
        avatarUrl = profile.avatarURL)
      save(u)
    case None => // Insert a new user
      val u = User(
        loginInfo = profile.loginInfo,
        username = None,
        info = BaseInfo(
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          gender = None),
        email = profile.email,
        avatarUrl = profile.avatarURL)
      save(u)
  }
}


def link[A <: AuthInfo](user: User, profile: CommonSocialProfile): Future[User] = {
  val s = user.socials.getOrElse(Seq())
  val u = user.copy(socials = Some(s :+ profile.loginInfo))
  save(u)
}
}



object UserServiceImpl extends UserServiceInMemory {
  val users: HashMap[String, User] = HashMap()
}
