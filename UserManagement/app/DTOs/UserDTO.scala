package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.sql.Timestamp
import java.util.UUID

case class UserDTO(
    id: Option[UUID] = None,
    email: String,
    password: String,
    role: String = "client",
    createdAt: Option[Timestamp] = Option.empty[Timestamp])

object UserDTO {
  import DTOs.circe.TimestampCirce._

  implicit lazy val usersDecoder: Decoder[UserDTO] = deriveDecoder[UserDTO]
  implicit lazy val usersEncoder: Encoder[UserDTO] = deriveEncoder[UserDTO]

  def decode(jsonString: String): UserDTO = {
    parser.decode[UserDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: UserDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
