package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import scala.collection.mutable
import scala.concurrent.Future
import OAuth2InfoDAO._

class OAuth2InfoDAO extends DelegableAuthInfoDAO[OAuth2Info] {


  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }


  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(data.get(loginInfo))
  }
}

object OAuth2InfoDAO {
  var data: mutable.HashMap[LoginInfo, OAuth2Info] = mutable.HashMap()
}
