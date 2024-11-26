package DTOs

import io.circe.{parser, Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import play.api.libs.json.{Json => PlayJson, JsValue}

import java.sql.Timestamp
import java.util.UUID

case class ReadReceiptDTO(
    receiptId: Option[UUID],
    messageId: UUID,
    userId: UUID,
    readAt: Option[Timestamp])

object ReadReceiptDTO {
  import DTOs.circe.TimestampCirce._

  implicit lazy val usersDecoder: Decoder[ReadReceiptDTO] = deriveDecoder[ReadReceiptDTO]
  implicit lazy val usersEncoder: Encoder[ReadReceiptDTO] = deriveEncoder[ReadReceiptDTO]

  def decode(jsonString: String): ReadReceiptDTO = {
    parser.decode[ReadReceiptDTO](jsonString) match {
      case Left(_) =>
        throw new IllegalArgumentException(s"invalid JSON object")
      case Right(dtoObject) => dtoObject
    }
  }

  def encode(dtoObject: ReadReceiptDTO): JsValue = {
    PlayJson.parse(dtoObject.asJson.toString)
  }
}
