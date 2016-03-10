package controllers

import scala.concurrent.Future
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ConfigurationException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.api.util.Credentials
import modules.HeaderEnvironmentModule
import security.models._
import models.users.User


object Application extends Silhouette[User, JWTAuthenticator] with HeaderEnvironmentModule{
  implicit val restFormat = formatters.json.UserFormats.restFormat
	implicit val signUpFormat = Json.format[SignUp]
	implicit val restCredentialFormat = security.formatters.json.CredentialFormat.restFormat

	def index =  UserAwareAction.async { request =>
	    request.identity match {
	      case Some(user) => Future.successful(Ok("Loggedin"))
	      case None => Future.successful(Ok("you are not logged! Login man!"))
	    }
	}


	def signUp = Action.async(parse.json) { implicit request =>
		request.body.validate[SignUp].map { signUp =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.identifier)
      (userService.retrieve(loginInfo).flatMap {
        case None => /* user not already exists */
          val authInfo = passwordHasher.hash(signUp.password)
          for {
            userToSave <- userService.create(loginInfo, signUp, None)
            user <- userService.save(userToSave)
            authInfo <- authInfoService.save(loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(loginInfo)
            token <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(token, Future.successful {
              Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDate)))
            })
          } yield {
            env.eventBus.publish(SignUpEvent(user, request, request2lang))
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            result
          }
        case Some(u) => /* user already exists! */
          Future.successful(BadRequest("user already exists"))
      })
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest("user already exists"))
    }

	}

	def authenticate = Action.async(parse.json[Credentials]) { implicit request =>
    (env.providers.get(CredentialsProvider.ID) match {
      case Some(p: CredentialsProvider) => p.authenticate(request.body)
      case _                            => Future.failed(new ConfigurationException(s"Cannot find credentials provider"))
    }).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) => env.authenticatorService.create(user.loginInfo).flatMap { authenticator =>
          env.eventBus.publish(LoginEvent(user, request, request2lang))
          env.authenticatorService.init(authenticator).flatMap { token =>
            env.authenticatorService.embed(token, Future.successful {
              Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDate)))
            })
          }
        }
        case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }
    }.recover{case e => InternalServerError("sdf")}
  }
}
