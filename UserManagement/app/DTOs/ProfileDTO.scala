package DTOs

import io.circe.{Decoder, Encoder, parser}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{JsValue, Json => PlayJson}

import java.util.UUID

case class ProfileDTO(userId: Option[UUID], firstName: String, lastName: String, profilePicUrl: String = "/default.png")

object ProfileDTO {
  implicit lazy val usersDecoder: Decoder[ProfileDTO] = deriveDecoder[ProfileDTO]
  implicit lazy val usersEncoder: Encoder[ProfileDTO] = deriveEncoder[ProfileDTO]

  def decode(jsonString: String): ProfileDTO = {
    parser.decode[ProfileDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: ProfileDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
