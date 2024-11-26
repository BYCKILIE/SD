package controllers.admin

import DTOs.ProfileDTO
import play.api.libs.json._
import play.api.mvc._
import services.{ProfilesService, TokensService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfilesController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    profilesService: ProfilesService,
    tokensService: TokensService)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def readProfile(): Action[AnyContent] = Action.async {
    implicit request =>
      extractTokenAndEmail(request) match {
        case Some((token, email)) =>
          validateAdminToken(token).flatMap {
            case Right(_) =>
              extractUserIdByEmail(email).flatMap {
                case Some(userId) =>
                  profilesService.readProfile(userId).map {
                    case Some(profileData) =>
                      Ok(ProfileDTO.encode(profileData.copy(userId = None)))
                    case None =>
                      InternalServerError(
                        Json.obj("message" -> "An error occurred during profile reading"))
                  }
                case None =>
                  Future.successful(Unauthorized(Json.obj("message" -> "User not found")))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None =>
          Future.successful(
            BadRequest(Json.obj("message" -> "Missing or invalid token or email in request")))
      }
  }

  def updateProfile(): Action[AnyContent] = Action.async {
    implicit request =>
      extractProfileUpdateData(request) match {
        case Some((token, email, firstName, lastName)) =>
          validateAdminToken(token).flatMap {
            case Right(_) =>
              extractUserIdByEmail(email).flatMap {
                case Some(userId) =>
                  profilesService.updateProfile(userId, ProfileDTO(None, firstName, lastName)).map {
                    case true => Ok(Json.obj("message" -> "Profile updated successfully"))
                    case false =>
                      InternalServerError(
                        Json.obj("message" -> "An error occurred during profile update"))
                  }
                case None =>
                  Future.successful(Unauthorized(Json.obj("message" -> "User not found")))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  private def extractUserIdByEmail(email: String): Future[Option[UUID]] =
    usersService.readUser(email).map(_.flatMap(_.id))

  private def validateAdminToken(token: String): Future[Either[Result, Unit]] =
    tokensService.validateToken(token).map {
      case Some((_, "admin")) => Right(())
      case Some(_) => Left(Unauthorized(Json.obj("message" -> "Token unauthorised")))
      case None => Left(Unauthorized(Json.obj("message" -> "Token validation failed")))
    }

  private def extractTokenAndEmail(request: Request[AnyContent]): Option[(String, String)] = for {
    tokenHeader <- request.headers.get("Authorization")
    token = tokenHeader.stripPrefix("Bearer ").trim

    json <- request.body.asJson
    jsonData <- JsonOP.parseString(json.toString())
    email <- jsonData("email").flatMap(_.asString)
  } yield (token, email)

  private def extractProfileUpdateData(
      request: Request[AnyContent]): Option[(String, String, String, String)] = for {
    tokenHeader <- request.headers.get("Authorization")
    token = tokenHeader.stripPrefix("Bearer ").trim

    json <- request.body.asJson
    jsonData <- JsonOP.parseString(json.toString())
    email <- jsonData("email").flatMap(_.asString)
    firstName <- jsonData("firstName").flatMap(_.asString)
    lastName <- jsonData("lastName").flatMap(_.asString)
  } yield (token, email, firstName, lastName)

}
