package controllers.crud

import DTOs.{DeviceDTO, OwnershipDTO}
import play.api.libs.json._
import play.api.mvc._
import services.{DevicesService, TokensService}
import services.{OwnershipsService, UsersService}
import utils.JsonOP

import javax.inject._

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

@Singleton
class FetchController @Inject() (
    cc: ControllerComponents,
    usersService: UsersService,
    devicesService: DevicesService,
    ownershipsService: OwnershipsService,
    tokensService: TokensService
)(implicit
    ec: ExecutionContext
) extends AbstractController(cc) {

  private def deviceToXml(device: DeviceDTO, ownership: Option[UUID] = None): Elem = {
    <device>
        {
          if (ownership.isDefined)
            <ownershipId>
              {ownership.get}
            </ownershipId>
        }
      <deviceId>
        {device.id.get}
      </deviceId>
      <name>
        {device.name}
      </name>
      <description>
        {device.description}
      </description>
      <address>
        {device.address}
      </address>
      <energyConsumption>
        {device.energyConsumption}
      </energyConsumption>
    </device>
  }

  def fetchAvailableDevices(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeOffset = extractJsonData(request, "partialName", "offset")

      maybeOffset match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right((_, role)) =>
              role match {
                case "admin" =>
                  handleFetching(data("partialName"), data("offset").toLong)
                case _ =>
                  Future.successful(Unauthorized(Json.obj("message" -> "Token validation failed")))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def fetchUserDevices(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeOffset = extractJsonData(request, "offset")

      maybeOffset match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right((userId, role)) =>
              role match {
                case "client" => fetchOwnerships(userId, data("offset").toLong)
                case _ =>
                  Future.successful(
                    Unauthorized(Json.obj("message" -> "Unauthorized request for non client user")))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  def fetchAdminDevices(): Action[AnyContent] = Action.async {
    implicit request =>
      val maybeUserData = extractJsonData(request, "email", "offset")

      maybeUserData match {
        case Some(data) =>
          validateToken(request).flatMap {
            case Right((_, role)) =>
              role match {
                case "admin" =>
                  usersService.retrieveId(data("email")).flatMap {
                    case Some(userId) => fetchOwnerships(userId, data("offset").toLong, admin = true)
                    case None =>
                      Future.successful(InternalServerError(
                        Json.obj("message" -> "An error occurred during device reading")))
                  }
                case _ =>
                  Future.successful(Unauthorized(Json.obj("message" -> "Token validation failed")))
              }
            case Left(errorResult) => Future.successful(errorResult)
          }
        case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid request data")))
      }
  }

  private def fetchOwnerships(userId: UUID, offset: Long, admin: Boolean = false): Future[Result] = {
    ownershipsService.readOwnerships(userId, offset).flatMap {
      case Some(ownerships) => fetchOwnedDevices(ownerships, admin)
      case None =>
        Future.successful(NotFound(Json.obj("message" -> "No devices were found")))
    }
  }

  private def handleFetching(partialName: String, offset: Long): Future[Result] = {
    if (offset == -1) {
      fetchByName(partialName)
    } else {
      fetchByOffset(offset)
    }
  }

  private def fetchByName(partialName: String): Future[Result] = {
    devicesService.fetchMatchingDevices(partialName).flatMap {
      case Some(devices) => extractDevicesSequence(devices)
      case None =>
        Future.successful(
          InternalServerError(Json.obj("message" -> "An error occurred during devices reading")))
    }
  }

  private def fetchByOffset(offset: Long): Future[Result] = {
    devicesService.fetchOffsetDevices(offset).flatMap {
      case Some(devices) => extractDevicesSequence(devices)
      case None =>
        Future.successful(
          InternalServerError(Json.obj("message" -> "An error occurred during devices reading")))
    }
  }

  private def fetchOwnedDevices(ownerships: Seq[OwnershipDTO], admin: Boolean): Future[Result] = {
    val devices = ownerships.map {
      ownershipDTO =>
        devicesService.readDevice(ownershipDTO.deviceId).map {
          case Some(deviceDTO) =>
            if (admin) {
              Some(deviceToXml(deviceDTO, ownershipDTO.ownershipId))
            } else {
              Some(deviceToXml(deviceDTO))
            }
          case None => None
        }
    }

    makeDevicesResponse(devices)
  }

  private def extractDevicesSequence(devicesDTO: Seq[DeviceDTO]): Future[Result] = {
    val devices = devicesDTO.map(device => Future.successful(Some(deviceToXml(device))))

    makeDevicesResponse(devices)
  }

  private def makeDevicesResponse(devices: Seq[Future[Option[Elem]]]): Future[Result] = {
    Future.sequence(devices).map {
      device =>
        val validDevice = device.flatten
        val xmlResponse = <devices>
          {validDevice}
        </devices>
        Ok(xmlResponse).as("text/xml")
    }
  }

  private def validateToken(
      request: Request[AnyContent]): Future[Either[Result, (UUID, String)]] = {
    request.headers.get("Authorization") match {
      case Some(authHeader) if authHeader.startsWith("Bearer ") =>
        val token = authHeader.stripPrefix("Bearer ").trim

        tokensService.validateToken(token).map {
          case Some((userId, role)) => Right((userId, role))
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
