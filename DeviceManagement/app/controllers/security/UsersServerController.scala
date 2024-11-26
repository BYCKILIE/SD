package controllers.security

import DTOs.UserDTO
import play.api.libs.json.Json
import play.api.mvc._
import services.{TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UsersServerController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  private val apiKey = "veryStrongApiKey1"

  def createUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "userId", "email") match {
        case Some((sentApiKey, data)) =>
          if (sentApiKey == apiKey) {
            handleUserCreation(UUID.fromString(data("userId")), data("email"))
          } else {
            Future.successful(InternalServerError)
          }
        case _ =>
          Future.successful(InternalServerError)
      }
  }

  def updateUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "userId", "newEmail") match {
        case Some((requestApiKey, data)) =>
          if (requestApiKey == apiKey) {
            usersService.updateUser(UUID.fromString(data("userId")), data("newEmail")).map {
              case true => Ok
              case false => InternalServerError
            }
          } else {
            Future.successful(InternalServerError)
          }
        case None => Future.successful(InternalServerError)
      }
  }

  def deleteUser(): Action[AnyContent] = Action.async {
    implicit request =>
    extractJson(request, "userId") match {
      case Some((requestApiKey, data)) =>
        if (requestApiKey == apiKey) {
          usersService.deleteUser(UUID.fromString(data("userId"))).map {
            case true => Ok
            case false => InternalServerError
          }
        } else {
          Future.successful(InternalServerError)
        }
      case None => Future.successful(InternalServerError)
    }
  }

  def updateToken(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "token") match {
        case Some((requestApiKey, data)) =>
          if (requestApiKey == apiKey) {
            tokensService.updateToken(data("token")).map {
              case true => Ok
              case false => InternalServerError
            }
          } else {
            Future.successful(InternalServerError)
          }
        case None => Future.successful(InternalServerError)
      }
  }

  private def extractJson(
      request: Request[AnyContent],
      jsonFields: String*): Option[(String, Map[String, String])] = {
    for {
      apiKey <- request.headers.get("ApiKey")
      json <- request.body.asJson
      jsonData <- JsonOP.parseString(json.toString())
      extractedFields = jsonFields
        .flatMap(field => jsonData(field).flatMap(_.asString).map(field -> _))
        .toMap
      if extractedFields.size == jsonFields.size
    } yield (apiKey, extractedFields)
  }

  private def handleUserCreation(userId: UUID, email: String): Future[Result] = {
    usersService.createUser(UserDTO(userId, email)).flatMap {
      case true =>
        for {
          tokenSuccess <- tokensService.createToken(userId)
          result <- processCreationResult(userId, tokenSuccess)
        } yield result
      case false =>
        Future.successful(InternalServerError)
    }
  }

  private def processCreationResult(userId: UUID, tokenSuccess: Boolean): Future[Result] = {
    if (tokenSuccess) {
      Future.successful(Ok(Json.obj("message" -> "User created successfully")))
    } else {
      rollbackUserCreation(userId)
    }
  }

  private def rollbackUserCreation(userId: UUID): Future[Result] = {
    usersService.deleteUser(userId).map {
      _ => InternalServerError(Json.obj("message" -> "Failed to create user profile or token"))
    }
  }

}
