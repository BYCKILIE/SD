package controllers.security

import play.api.mvc._
import services.{TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminServerController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  private val apiKey = "veryStrongApiKey1"

  def updateUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "oldEmail", "newEmail") match {
        case Some((requestApiKey, token, data)) =>
          if (requestApiKey == apiKey) {
            tokensService.validateToken(token).flatMap {
              case Some((_, "admin")) =>
                usersService.updateUser(data("oldEmail"), data("newEmail")).map {
                  case true => Ok
                  case false => InternalServerError
                }
              case _ => Future.successful(InternalServerError)
            }
          } else {
            Future.successful(InternalServerError)
          }
        case None => Future.successful(InternalServerError)
      }
  }

  def deleteUser(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "email") match {
        case Some((requestApiKey, token, data)) =>
          if (requestApiKey == apiKey) {
            tokensService.validateToken(token).flatMap {
              case Some((_, "admin")) =>
                usersService.deleteUser(data("email")).map {
                  case true => Ok
                  case false => InternalServerError
                }
              case _ => Future.successful(InternalServerError)
            }
          } else {
            Future.successful(InternalServerError)
          }
        case None => Future.successful(InternalServerError)
      }
  }

  def setNullToken(): Action[AnyContent] = Action.async {
    implicit request =>
      extractJson(request, "userId") match {
        case Some((requestApiKey, token, data)) =>
          if (requestApiKey == apiKey) {
            tokensService.validateToken(token).flatMap {
              case Some((_, "admin")) =>
                tokensService.setNull(UUID.fromString(data("userId"))).map {
                  case true => Ok
                  case false => InternalServerError
                }
              case _ => Future.successful(InternalServerError)
            }
          } else {
            Future.successful(InternalServerError)
          }
        case None => Future.successful(InternalServerError)
      }
  }

  private def extractJson(
      request: Request[AnyContent],
      jsonFields: String*): Option[(String, String, Map[String, String])] = {
    for {
      apiKey <- request.headers.get("ApiKey")

      tokenHeader <- request.headers.get("Authorization")
      token = tokenHeader.stripPrefix("Bearer ").trim

      json <- request.body.asJson
      jsonData <- JsonOP.parseString(json.toString())
      extractedFields = jsonFields
        .flatMap(field => jsonData(field).flatMap(_.asString).map(field -> _))
        .toMap
      if extractedFields.size == jsonFields.size
    } yield (apiKey, token, extractedFields)
  }

}
