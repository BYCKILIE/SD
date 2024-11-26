package controllers.security

import controllers.client.ClientImpl
import play.api.libs.json._
import play.api.mvc._
import services.{TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject() (
    cc: ControllerComponents,
    dc: ClientImpl,
    usersService: UsersService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def authenticate(): Action[AnyContent] = Action.async {
    implicit request =>
      extractCredentials(request) match {
        case Some((email, password)) =>
          validateUserAndGenerateToken(email, password)

        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def authorize(): Action[AnyContent] = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authHeader) if authHeader.startsWith("Bearer ") =>
          val token = authHeader.stripPrefix("Bearer ").trim
          validateToken(token, isAdmin = false)

        case Some(_) => Future.successful(BadRequest(Json.obj("message" -> "Incorrect token format")))
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Missing token in request header")))
      }
  }

  def admin(): Action[AnyContent] = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authHeader) if authHeader.startsWith("Bearer ") =>
          val token = authHeader.stripPrefix("Bearer ").trim
          validateToken(token, isAdmin = true)

        case Some(_) => Future.successful(BadRequest(Json.obj("message" -> "Incorrect token format")))
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Missing token in request header")))
      }
  }

  private def extractCredentials(request: Request[AnyContent]): Option[(String, String)] = {
    for {
      json <- request.body.asJson
      userJson <- JsonOP.parseString(json.toString())
      email <- userJson("email").flatMap(_.asString)
      password <- userJson("password").flatMap(_.asString)
    } yield (email, password)
  }

  private def validateUserAndGenerateToken(email: String, password: String): Future[Result] = {
    usersService.validateUser(email, password).flatMap {
      case Some((userId, role)) =>
        tokensService.updateToken(userId, role).flatMap {
          case Some(token) =>
            dc.streamToken(token).map {
              case 200 /*OK*/ =>
                Ok(Json.obj("message" -> "User authenticated", "token" -> token, "role" -> role))
              case _ => InternalServerError(s"Unexpected response")
            }
          case None =>
            Future.successful(
              InternalServerError(Json.obj("message" -> "Failed to generate token")))
        }

      case None =>
        Future.successful(Unauthorized(Json.obj("message" -> "User validation failed")))
    }
  }

  private def validateToken(token: String, isAdmin: Boolean): Future[Result] = {
    tokensService.validateToken(token).map {
      case Some((_, role)) =>
        if (isAdmin) {
          role match {
            case "admin" => Ok(Json.obj("message" -> "Token authorised", "role" -> "admin"))
            case _ => Unauthorized(Json.obj("message" -> "Token not found or invalid"))
          }
        } else {
          role match {
            case "client" => Ok(Json.obj("message" -> "Token authorised", "role" -> "client"))
            case _ => Unauthorized(Json.obj("message" -> "Token not found or invalid"))
          }
        }
      case None =>
        Unauthorized(Json.obj("message" -> "Token not found or invalid"))
    }
  }

}
