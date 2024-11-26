package DTOs

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, parser}
import play.api.libs.json.{JsValue, Json => PlayJson}

import java.util.UUID

case class OwnershipDTO(ownershipId: Option[UUID], deviceId: UUID, userId: Option[UUID])

object OwnershipDTO {
  implicit val usersDecoder: Decoder[OwnershipDTO] = deriveDecoder[OwnershipDTO]
  implicit val usersEncoder: Encoder[OwnershipDTO] = deriveEncoder[OwnershipDTO]

  def decode(jsonString: String): OwnershipDTO = {
    parser.decode[OwnershipDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: OwnershipDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
