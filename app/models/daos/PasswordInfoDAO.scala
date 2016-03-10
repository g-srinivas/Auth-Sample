package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import scala.collection.mutable
import scala.concurrent.Future
import PasswordInfoDAO._


class PasswordInfoDAO extends DelegableAuthInfoDAO[PasswordInfo] {


  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    data += (loginInfo -> authInfo)
    Future.successful(authInfo)
  }


  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    Future.successful(data.get(loginInfo))
  }
}

object PasswordInfoDAO {

  var data: mutable.HashMap[LoginInfo, PasswordInfo] = mutable.HashMap()
}
