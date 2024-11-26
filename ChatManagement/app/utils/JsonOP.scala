package utils

import io.circe._
import io.circe.parser._

object JsonOP {

  def parseString(data: String): Option[JsonObject] = {
    parse(data) match {
      case Left(_)       => None
      case Right(json)   => json.asObject
    }
  }

}