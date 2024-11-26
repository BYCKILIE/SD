package utils

import io.circe.Json
import pdi.jwt._

import java.time.Instant
import java.util.UUID

import scala.util.{Failure, Success}

case class TokenOP(userId: UUID, role: String, expiration: Option[Long])

object TokenOP {

  private val secretKey = "SarmauaAiaBuna"
  private val algo = JwtAlgorithm.HS512

  def generateToken(
      userId: UUID,
      role: String
  ): String = {
    val claim = JwtClaim(
      content = Json
        .obj(
          ("userId", Json.fromString(userId.toString)),
          ("role", Json.fromString(role))
        )
        .noSpaces,
      expiration = Some(Instant.now.plusSeconds(2592000).getEpochSecond)
    )
    Jwt.encode(claim, secretKey, algo)
  }

  def parseToken(token: String): Option[TokenOP] = Jwt.decode(token, secretKey, Seq(algo)) match {
    case Success(claim) =>
      for {
        jsonData <- JsonOP.parseString(claim.content)

        userId <- jsonData("userId").flatMap(_.asString)
        role <- jsonData("role").flatMap(_.asString)
      } yield {
        TokenOP(userId = UUID.fromString(userId), role = role, expiration = claim.expiration)
      }

    case Failure(_) => None
  }
}
