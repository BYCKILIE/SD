package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.util.UUID

case class DeviceDTO(
    id: Option[UUID] = None,
    name: String,
    description: String,
    address: String,
    energyConsumption: Double)

object DeviceDTO {
  implicit lazy val usersDecoder: Decoder[DeviceDTO] = deriveDecoder[DeviceDTO]
  implicit lazy val usersEncoder: Encoder[DeviceDTO] = deriveEncoder[DeviceDTO]

  def decode(jsonString: String): DeviceDTO = {
    parser.decode[DeviceDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: DeviceDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
