package services

import DTOs.UserDTO
import DTOs.UserDTO.encode
import models.UsersTable
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import repositories.UsersRepository
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class UsersService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends UsersRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def validateUser(email: String, password: String): Future[Option[(UUID, String)]] = {
    db.run(
      UsersTable.query.filter(_.email === email).result.headOption
    ).map {
      case Some(userDTO) if BCrypt.checkpw(password, userDTO.password) =>
        userDTO.id.map((_, userDTO.role))
      case _ =>
        None
    }
  }

  override def createUser(user: UserDTO): Future[Option[UUID]] = {
    val hashedUser = user.copy(
      password = BCrypt.hashpw(user.password, BCrypt.gensalt())
    )

    db.run(
      (UsersTable.query returning UsersTable.query.map(_.id)) += hashedUser
    ).map(userId => Some(userId))
  }

  override def readUser(id: UUID): Future[Option[UserDTO]] = db.run(
    UsersTable.query.filter(_.id === id).result.headOption
  )

  override def readUser(email: String): Future[Option[UserDTO]] = db.run(
    UsersTable.query.filter(_.email === email).result.headOption
  )

  override def fetchUsers(offset: Long, role: String): Future[Option[Seq[UserDTO]]] = db
    .run(UsersTable.query.filter(_.role === role).drop(offset).take(offset + 20).result)
    .map(users => if (users.isEmpty) None else Some(users))

  private def getUpdated(dbUser: UserDTO, newUser: UserDTO): UserDTO = {
    val email = if (newUser.email == "") dbUser.email else newUser.email
    val password = if (newUser.password == "") dbUser.password else newUser.password
    dbUser.copy(
      email = email,
      password = BCrypt.hashpw(password, BCrypt.gensalt())
    )
  }

  override def updateUser(id: UUID, user: UserDTO): Future[Boolean] =
    db.run(UsersTable.query.filter(_.id === id).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated = getUpdated(existingUser, user)
        db.run(UsersTable.query.filter(_.id === id).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def updateUser(email: String, user: UserDTO): Future[Boolean] =
    db.run(UsersTable.query.filter(_.email === email).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated = getUpdated(existingUser, user)
        db.run(UsersTable.query.filter(_.email === email).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def changeRole(email: String): Future[Option[UUID]] = {
    db.run(UsersTable.query.filter(_.email === email).result.headOption).flatMap {
      case Some(existingUser) =>
        val updated =
          if (existingUser.role == "client")
            existingUser.copy(
              role = "admin"
            )
          else existingUser.copy(role = "client")
        db.run(UsersTable.query.filter(_.email === email).update(updated)).map {
          rowsAffected =>
            if (rowsAffected > 0)
              existingUser.id
            else
              None
        }
      case None =>
        Future.successful(None)
    }
  }

  override def deleteUser(id: UUID): Future[Boolean] = db.run(
    (for {
      deleted <- UsersTable.query.filter(_.id === id).delete
    } yield deleted > 0).transactionally
  )

  override def deleteUser(email: String): Future[Boolean] = db.run(
    (for {
      deleted <- UsersTable.query.filter(_.email === email).delete
    } yield deleted > 0).transactionally
  )

}
