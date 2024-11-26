package services

import DTOs.TokenDTO
import models.TokensTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.TokensRepository
import slick.jdbc.JdbcProfile
import utils.TokenOP

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TokensService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends TokensRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def createToken(userId: UUID): Future[Boolean] = {
    db.run(
      TokensTable.query += TokenDTO(userId)
    ).map(_ > 0)
  }

  override def validateToken(token: String): Future[Option[(UUID, String)]] = {
    TokenOP.parseToken(token) match {
      case Some(tokenData) =>
        val currentTime = Instant.now().getEpochSecond
        db.run(TokensTable.query.filter(_.userId === tokenData.userId).result.headOption).map {
          case Some(dbToken)
              if dbToken.token.contains(token) && tokenData.expiration.exists(_ > currentTime) =>
            Some((tokenData.userId, tokenData.role))
          case _ =>
            None
        }

      case None =>
        Future.successful(None)
    }
  }

  override def updateToken(token: String): Future[Boolean] = {
    TokenOP.parseToken(token) match {
      case Some(tokenOP) =>
        db.run(TokensTable.query.filter(_.userId === tokenOP.userId).result.headOption).flatMap {
          case Some(existingToken) =>
            val updated = existingToken.copy(token = Option.apply(token))
            db.run(TokensTable.query.filter(_.userId === tokenOP.userId).update(updated)).map(_ > 0)
          case None =>
            Future.successful(false)
        }
      case None => Future.successful(false)
    }
  }

  override def setNull(userId: UUID): Future[Boolean] =
    db.run(TokensTable.query.filter(_.userId === userId).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated = existingUser.copy(
          token = None
        )
        db.run(TokensTable.query.filter(_.userId === userId).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

}
