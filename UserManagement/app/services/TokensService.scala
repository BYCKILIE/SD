package services

import DTOs.TokenDTO
import models.TokensTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.TokensRepository
import slick.jdbc.JdbcProfile
import utils.TokenOP

import javax.inject.Inject
import javax.inject.Singleton

import java.time.Instant
import java.util.UUID

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

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
  override def updateToken(userId: UUID, role: String): Future[Option[String]] =
    db.run(TokensTable.query.filter(_.userId === userId).result.headOption).flatMap {
      case Some(existingUser) =>
        val newToken = Option.apply[String](TokenOP.generateToken(userId, role))
        val updated = existingUser.copy(
          token = newToken
        )
        db.run(TokensTable.query.filter(_.userId === userId).update(updated)).map(_ => newToken)
      case None =>
        Future.successful(None)
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
