package services

import DTOs.UserDTO
import models.UsersTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.UsersRepository
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UsersService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends UsersRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def createUser(user: UserDTO): Future[Boolean] = {
    db.run(
      UsersTable.query += user
    ).map(_ > 0)
  }

  override def retrieveId(email: String): Future[Option[UUID]] = {
    db.run(
      UsersTable.query.filter(_.email === email).result.headOption
    ).map(_.map(_.id))
  }

  override def updateUser(userId: UUID, newEmail: String): Future[Boolean] =
    db.run(UsersTable.query.filter(_.id === userId).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated = existingUser.copy(
          email = newEmail
        )
        db.run(UsersTable.query.filter(_.id === userId).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def updateUser(oldEmail: String, newEmail: String): Future[Boolean] =
    db.run(UsersTable.query.filter(_.email === oldEmail).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated = existingUser.copy(
          email = newEmail
        )
        db.run(UsersTable.query.filter(_.email === oldEmail).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def deleteUser(userId: UUID): Future[Boolean] = db.run(
    (for {
      deleted <- UsersTable.query.filter(_.id === userId).delete
    } yield deleted > 0).transactionally
  )

  override def deleteUser(email: String): Future[Boolean] = db.run(
    (for {
      deleted <- UsersTable.query.filter(_.email === email).delete
    } yield deleted > 0).transactionally
  )

}
