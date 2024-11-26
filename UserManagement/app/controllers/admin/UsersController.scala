package controllers.admin

import DTOs.UserDTO
import controllers.client.ClientImpl
import play.api.libs.json._
import play.api.mvc._
import services.{TokensService, UsersService}
import utils.JsonOP

import javax.inject._
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
      extractTokenAndJson(request, "wantedEmail") match {
        case Some((token, data)) =>
          validateAdminToken(token).flatMap {
            case Ok =>
              usersService.readUser(data("wantedEmail")).map {
                case Some(userData) => Ok(UserDTO.encode(userData.copy(id = None, password = "")))
                case None =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during user reading"))
              }
            case unauthorizedResult => Future.successful(unauthorizedResult)
          }

        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Missing token or email in request")))
      }
  }

  def updateUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractTokenAndJson(request, "email", "newEmail", "newPassword") match {
        case Some((token, data)) =>
          validateAdminToken(token).flatMap {
            case Ok =>
              usersService
                .updateUser(data("email"), UserDTO(None, data("newEmail"), data("newPassword")))
                .flatMap {
                  case true =>
                    ci.streamUpdatedUser(data("email"), data("newEmail")).map {
                      case 200 => Ok(Json.obj("message" -> "Email updated successfully"))
                      case _ =>
                        InternalServerError(
                          Json.obj("message" -> "An error occurred during email update"))
                    }
                  case false =>
                    Future.successful(InternalServerError(
                      Json.obj("message" -> "An error occurred during email update")))
                }
            case unauthorizedResult => Future.successful(unauthorizedResult)
          }

        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def changeRole(): Action[AnyContent] = Action.async {
    implicit request =>
      extractTokenAndJson(request, "email") match {
        case Some((token, data)) =>
          validateAdminToken(token).flatMap {
            case Ok =>
              usersService.changeRole(data("email")).flatMap {
                case Some(userId) =>
                  tokensService.setNull(userId).flatMap {
                    case true =>
                      ci.setNullToken(userId).map {
                        case 200 => Ok(Json.obj("message" -> "Role updated successfully"))
                        case _ =>
                          InternalServerError(
                            Json.obj("message" -> "An error occurred during role update"))
                      }
                    case false =>
                      Future.successful(InternalServerError(Json.obj("message" -> "Unable to reset token")))
                  }
                case None =>
                  Future.successful(InternalServerError(
                    Json.obj("message" -> "An error occurred during role update")))
              }
            case unauthorizedResult => Future.successful(unauthorizedResult)
          }

        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def deleteUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractTokenAndJson(request, "email") match {
        case Some((token, data)) =>
          validateAdminToken(token).flatMap {
            case Ok =>
              usersService.deleteUser(data("email")).flatMap {
                case true =>
                  ci.streamDeletedUser(data("email")).map {
                    case 200 => Ok(Json.obj("message" -> "User deleted successfully"))
                    case _ =>
                      InternalServerError(
                        Json.obj("message" -> "An error occurred during user deletion"))
                  }
                case false =>
                  Future.successful(InternalServerError(
                    Json.obj("message" -> "An error occurred during user deletion")))
              }
            case unauthorizedResult => Future.successful(unauthorizedResult)
          }

        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  private def extractTokenAndJson(
      request: Request[AnyContent],
      jsonFields: String*): Option[(String, Map[String, String])] = {
    for {
      tokenHeader <- request.headers.get("Authorization")
      token = tokenHeader.stripPrefix("Bearer ").trim

      json <- request.body.asJson
      jsonData <- JsonOP.parseString(json.toString())
      extractedFields = jsonFields
        .flatMap(field => jsonData(field).flatMap(_.asString).map(field -> _))
        .toMap
      if extractedFields.size == jsonFields.size
    } yield (token, extractedFields)
  }

  private def validateAdminToken(token: String): Future[Result] = {
    tokensService.validateToken(token).flatMap {
      case Some((_, "admin")) => Future.successful(Ok)
      case Some((_, _)) =>
        Future.successful(Unauthorized(Json.obj("message" -> "Token unauthorised")))
      case None => Future.successful(Unauthorized(Json.obj("message" -> "Token validation failed")))
    }
  }
}
