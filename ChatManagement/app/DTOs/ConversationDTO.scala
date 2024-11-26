package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.sql.Timestamp
import java.util.UUID

case class ConversationDTO(
    conversationId: Option[UUID] = None,
    isGroup: Boolean,
    conversationName: Option[String] = None,
    createdAt: Option[Timestamp] = None)

object ConversationDTO {
  import DTOs.circe.TimestampCirce._

  implicit lazy val usersDecoder: Decoder[ConversationDTO] = deriveDecoder[ConversationDTO]
  implicit lazy val usersEncoder: Encoder[ConversationDTO] = deriveEncoder[ConversationDTO]

  def decode(jsonString: String): ConversationDTO = {
    parser.decode[ConversationDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: ConversationDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
