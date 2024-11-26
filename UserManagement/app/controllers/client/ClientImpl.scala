package controllers.client

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc.Headers

import javax.inject._
import javax.inject.Inject
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ClientImpl @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {

  private val apiKey = "veryStrongApiKey1"
  private val clients: Seq[String] = Seq("devices", "chat")

  def streamUser(userId: UUID, email: String): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/user/create"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("userId" -> userId.toString, "email" -> email))
    }
  }

  def streamUpdatedUser(userId: UUID, newEmail: String): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/user/update"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("userId" -> userId.toString, "newEmail" -> newEmail))
    }
  }

  def streamUpdatedUser(oldEmail: String, newEmail: String): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/admin-user/update"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("oldEmail" -> oldEmail, "newEmail" -> newEmail))
    }
  }

  def streamDeletedUser(userId: UUID): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/user/delete"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("userId" -> userId.toString))
    }
  }

  def streamDeletedUser(email: String): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/admin-user/delete"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("email" -> email))
    }
  }

  def streamToken(token: String): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/token/update"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("token" -> token))
    }
  }

  def setNullToken(userId: UUID): Future[Int] = getStatus {
    clients.map { client =>
      val url = f"https://sd.$client.bchportal.net/token/set-null"
      makeRequest(url, Headers("ApiKey" -> apiKey), Seq("userId" -> userId.toString))
    }
  }

  private def getStatus(requests: Seq[Future[Int]]): Future[Int] = Future.sequence(requests).map { statuses =>
      if (statuses.forall(_ == 200)) 200 else 500
    }

  private def makeRequest(url: String, headers: Headers, values: Seq[(String, JsValueWrapper)]): Future[Int] = {
    ws.url(url)
      .withHttpHeaders(headers.headers: _*)
      .post(Json.obj(values: _*))
      .map(_.status)
  }

}
