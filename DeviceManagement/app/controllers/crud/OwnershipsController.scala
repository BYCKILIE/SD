package controllers.crud

import DTOs.OwnershipDTO
import play.api.libs.json._
import play.api.mvc._
import services.{OwnershipsService, TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OwnershipsController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    ownershipsService: OwnershipsService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def createOwnership(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeRequestData = extractJsonData(request, "email", "deviceId")

      maybeRequestData match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right(_) =>
              usersService.retrieveId(data("email")).flatMap {
                case Some(userId) =>
                  ownershipsService
                    .createOwnership(
                      OwnershipDTO(None, UUID.fromString(data("deviceId")), Some(userId)))
                    .map {
                      case true => Ok(Json.obj("message" -> "Device added successfully"))
                      case false =>
                        InternalServerError(Json.obj("message" -> "Server could not add bound"))
                    }
                case None =>
                  Future.successful(
                    InternalServerError(Json.obj("message" -> "User does not exist")))
              }
            case Left(errorResult) =>
              Future.successful(errorResult)
          }

        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def deleteOwnership(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeOwnershipId = extractJsonData(request, "ownershipId")

      maybeOwnershipId match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right(_) =>
              ownershipsService.deleteOwnership(UUID.fromString(data("ownershipId"))).map {
                case true =>
                  Ok(Json.obj("message" -> "Ownership deleted successfully"))
                case false =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during ownership deletion"))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  private def validateToken(request: Request[AnyContent]): Future[Either[Result, Unit]] = {
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.stripPrefix("Bearer ").trim

        tokensService.validateToken(token).map {
          case Some((_, "admin")) => Right(())
          case Some(_) => Left(Unauthorized(Json.obj("message" -> "Token validation failed")))
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
