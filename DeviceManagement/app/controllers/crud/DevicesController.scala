package controllers.crud

import DTOs.DeviceDTO
import play.api.libs.json._
import play.api.mvc._
import services.{DevicesService, TokensService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DevicesController @Inject() (
    cc: ControllerComponents,
    devicesService: DevicesService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  def createDevice(): Action[AnyContent] = Action.async {
    implicit request =>
      val deviceDTO = DeviceDTO.decode(request.body.asJson.get.toString)

      validateToken(request).flatMap {
        case Right(_) =>
          devicesService
            .createDevice(
              DeviceDTO(
                None,
                deviceDTO.name,
                deviceDTO.description,
                deviceDTO.address,
                deviceDTO.energyConsumption))
            .map {
              case true => Ok(Json.obj("message" -> "Device added successfully"))
              case false =>
                InternalServerError(Json.obj("message" -> "Server could not add the device"))
            }
        case Left(errorResult) => Future.successful(errorResult)
      }
  }

  def readDevice(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeDeviceId = extractJsonData(request, "deviceId")

      maybeDeviceId match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right(_) =>
              devicesService.readDevice(UUID.fromString(data("deviceId"))).map {
                case Some(deviceDTO) => Ok(DeviceDTO.encode(deviceDTO))
                case None =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during device reading"))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }

        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def updateDevice(): Action[AnyContent] = Action.async {
    implicit request =>
      val deviceDTO = DeviceDTO.decode(request.body.asJson.get.toString)
      validateToken(request).flatMap {
        case Right(_) =>
          devicesService
            .updateDevice(
              deviceDTO.id.getOrElse(UUID.fromString("")),
              DeviceDTO(
                None,
                deviceDTO.name,
                deviceDTO.description,
                deviceDTO.address,
                deviceDTO.energyConsumption))
            .map {
              case true =>
                Ok(Json.obj("message" -> "Device updated successfully"))
              case false =>
                InternalServerError(Json.obj("message" -> "An error occurred during device update"))
            }
        case Left(errorResult) => Future.successful(errorResult)
      }
  }

  def deleteDevice(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeDeviceId = extractJsonData(request, "deviceId")

      maybeDeviceId match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right(_) =>
              devicesService.deleteDevice(UUID.fromString(data("deviceId"))).map {
                case true =>
                  Ok(Json.obj("message" -> "Device deleted successfully"))
                case false =>
                  InternalServerError(
                    Json.obj("message" -> "An error occurred during device deletion"))
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
