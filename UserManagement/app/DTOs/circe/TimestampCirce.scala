package DTOs.circe

import io.circe.{Decoder, Encoder}

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeParseException

object TimestampCirce {
  implicit lazy val timestampDecoder: Decoder[Timestamp] = Decoder.decodeString.emap {
    str =>
      try {
        Right(Timestamp.from(Instant.parse(str)))
      } catch {
        case _: DateTimeParseException => Left(s"Invalid date format: $str")
      }
  }

  implicit lazy val timestampEncoder: Encoder[Timestamp] = Encoder.encodeString.contramap[Timestamp] {
    ts => ts.toInstant.toString
  }
}
