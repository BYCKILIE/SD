package repositories

import DTOs.UserDTO

import java.util.UUID
import scala.concurrent.Future

trait UsersRepository {

  def validateUser(email: String, password: String): Future[Option[(UUID, String)]]

  def createUser(user: UserDTO): Future[Option[UUID]]

  def readUser(id: UUID): Future[Option[UserDTO]]

  def readUser(email: String): Future[Option[UserDTO]]

  def fetchUsers(offset: Long, role: String): Future[Option[Seq[UserDTO]]]

  def updateUser(id: UUID, user: UserDTO): Future[Boolean]

  def updateUser(email: String, user: UserDTO): Future[Boolean]

  def changeRole(email: String): Future[Option[UUID]]

  def deleteUser(id: UUID): Future[Boolean]

  def deleteUser(email: String): Future[Boolean]
}
