package controllers.crud

import DTOs.UserDTO
import controllers.client.ClientImpl
import play.api.libs.json._
import play.api.mvc._
import services.{TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UsersController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    tokensService: TokensService,
    ci: ClientImpl
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def readUser(): Action[AnyContent] = Action.async {
    implicit request =>
      validateToken(request).flatMap {
        case Right(userId) =>
          usersService.readUser(userId).map {
            case Some(userData) =>
              Ok(UserDTO.encode(userData.copy(id = None, password = "")))
            case None =>
              InternalServerError(Json.obj("message" -> "An error occurred during user reading"))
          }
        case Left(errorResult) => Future.successful(errorResult)
      }
  }

  def updateUser(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeUserData = extractJsonData(request, "newEmail", "newPassword")

      maybeUserData match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right(userId) =>
              usersService
                .updateUser(userId, UserDTO(None, data("newEmail"), data("newPassword")))
                .flatMap {
                  case true =>
                    if (data("newEmail") != "") {
                      ci.streamUpdatedUser(userId, data("newEmail")).map {
                        case 200 => Ok(Json.obj("message" -> "User updated successfully"))
                        case _ =>
                          InternalServerError(
                            Json.obj("message" -> "An error occurred during user update"))
                      }
                    } else {
                      Future.successful(Ok(Json.obj("message" -> "User updated successfully")))
                    }
                  case false =>
                    Future.successful(InternalServerError(
                      Json.obj("message" -> "An error occurred during user update")))
                }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def deleteUser(): Action[AnyContent] = Action.async {
    implicit request =>
      validateToken(request).flatMap {
        case Right(userId) =>
          usersService.deleteUser(userId).flatMap {
            case true =>
              ci.streamDeletedUser(userId).map {
                case 200 => Ok(Json.obj("message" -> "User deleted successfully"))
                case _ =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during user deletion"))
              }
            case false =>
              Future.successful(InternalServerError(
                Json.obj("message" -> "An error occurred during user deletion")))
          }
        case Left(errorResult) => Future.successful(errorResult)
      }
  }

  private def validateToken(request: Request[AnyContent]): Future[Either[Result, UUID]] = {
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.stripPrefix("Bearer ").trim
        tokensService.validateToken(token).map {
          case Some((userId, _)) => Right(userId)
          case None => Left(Unauthorized(Json.obj("message" -> "Token validation failed")))
        }
      case Some(_) =>
        Future.successful(
          Left(BadRequest(Json.obj("message" -> "Incorrect token format"))))
      case None =>
        Future.successful(
          Left(BadRequest(Json.obj("message" -> "Missing token in request header"))))
    }
  }

  private def extractJsonData(
      request: Request[AnyContent],
      fields: String*): Option[Map[String, String]] = {
    for {
      json <- request.body.asJson
      jsonData <- JsonOP.parseString(json.toString())
      data = fields.flatMap(field => jsonData(field).flatMap(_.asString).map(field -> _)).toMap
      if data.size == fields.size
    } yield data
  }
}
