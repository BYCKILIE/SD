package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.sql.Timestamp
import java.util.UUID

case class ParticipantDTO(
    participantId: Option[UUID],
    conversationId: UUID,
    userId: UUID,
    joinedAt: Option[Timestamp] = None)

object ParticipantDTO {
  import DTOs.circe.TimestampCirce._

  implicit lazy val usersDecoder: Decoder[ParticipantDTO] = deriveDecoder[ParticipantDTO]
  implicit lazy val usersEncoder: Encoder[ParticipantDTO] = deriveEncoder[ParticipantDTO]

  def decode(jsonString: String): ParticipantDTO = {
    parser.decode[ParticipantDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: ParticipantDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
