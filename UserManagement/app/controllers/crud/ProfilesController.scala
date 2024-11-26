package controllers.crud

import DTOs.ProfileDTO
import play.api.libs.Files.TemporaryFile
import play.api.libs.json._
import play.api.mvc._
import services.{ProfilesService, TokensService}
import utils.{ImageOP, JsonOP}

import javax.imageio.ImageIO
import javax.inject._

import java.awt.image.BufferedImage
import java.nio.file.{Files, Path}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ProfilesController @Inject() (
    cc: ControllerComponents,
    profilesService: ProfilesService,
    tokensService: TokensService)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def readProfile(): Action[AnyContent] = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authHeader) if authHeader.startsWith("Bearer ") =>
          val token = authHeader.stripPrefix("Bearer ").trim
          validateUserToken(token).flatMap {
            case Right(userId) =>
              profilesService.readProfile(userId).map {
                case Some(profileData) =>
                  Ok(ProfileDTO.encode(profileData.copy(userId = None)))
                case None =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during profile reading"))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case Some(_) =>
          Future.successful(
            BadRequest(Json.obj("message" -> "Incorrect token format")))
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Missing token in request header")))
      }
  }

  def updateProfile(): Action[AnyContent] = Action.async {
    implicit request =>
      extractProfileUpdateData(request) match {
        case Some((token, firstName, lastName)) =>
          validateUserToken(token).flatMap {
            case Right(userId) =>
              profilesService.updateProfile(userId, ProfileDTO(None, firstName, lastName)).map {
                case true => Ok(Json.obj("message" -> "Profile updated successfully"))
                case false =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during profile update"))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def uploadImage(): Action[MultipartFormData[TemporaryFile]] = Action.async(parse.multipartFormData) { request =>
    val maybeToken: Option[String] = request.headers.get("Token")

    maybeToken match {
      case Some(token) =>
        validateUserToken(token).flatMap {
          case Right(userId) =>
            request.body.file("image") match {
              case Some(image) =>
                processImage(image, userId).map {
                  case Some(savePath) => Ok(s"Image uploaded and saved successfully at: $savePath")
                  case None => InternalServerError("Failed to save image.")
                }
              case None =>
                Future.successful(BadRequest("Image file is missing."))
            }
          case Left(errorResult) =>
            Future.successful(errorResult)
        }

      case None =>
        Future.successful(BadRequest("Missing Token in headers."))
    }
  }

  private def processImage(image: MultipartFormData.FilePart[TemporaryFile], userId: UUID): Future[Option[String]] = {
    val path: Path = image.ref.path
    val inputStream = Files.newInputStream(path)

    val bufferedImageOpt: Option[BufferedImage] = Try {
      ImageIO.read(inputStream)
    }.toOption

    inputStream.close()

    bufferedImageOpt match {
      case Some(bufferedImage) =>
        Future.successful(ImageOP.saveImage(bufferedImage, userId))
      case None =>
        Future.successful(None)
    }
  }

  private def validateUserToken(token: String): Future[Either[Result, UUID]] =
    tokensService.validateToken(token).map {
      case Some((userId, _)) => Right(userId)
      case None => Left(Unauthorized(Json.obj("message" -> "Token validation failed")))
    }

  private def extractProfileUpdateData(
      request: Request[AnyContent]): Option[(String, String, String)] = for {
    tokenHeader <- request.headers.get("Authorization")
    token = tokenHeader.stripPrefix("Bearer ").trim

    json <- request.body.asJson
    jsonData <- JsonOP.parseString(json.toString())
    firstName <- jsonData("firstName").flatMap(_.asString)
    lastName <- jsonData("lastName").flatMap(_.asString)
  } yield (token, firstName, lastName)

}
