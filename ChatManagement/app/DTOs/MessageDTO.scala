package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.sql.Timestamp
import java.util.UUID

case class MessageDTO(
    messageId: Option[UUID] = None,
    conversationId: UUID,
    senderId: UUID,
    messageText: Option[String],
    mediaUrl: Option[String],
    createdAt: Option[Timestamp] = None,
    isDeleted: Boolean = false)

object MessageDTO {
  import DTOs.circe.TimestampCirce._

  implicit lazy val usersDecoder: Decoder[MessageDTO] = deriveDecoder[MessageDTO]
  implicit lazy val usersEncoder: Encoder[MessageDTO] = deriveEncoder[MessageDTO]

  def decode(jsonString: String): MessageDTO = {
    parser.decode[MessageDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: MessageDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}

