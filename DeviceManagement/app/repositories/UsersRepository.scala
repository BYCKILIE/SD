package repositories

import DTOs.UserDTO

import java.util.UUID
import scala.concurrent.Future

trait UsersRepository {

  def createUser(user: UserDTO): Future[Boolean]

  def retrieveId(email: String): Future[Option[UUID]]

  def updateUser(userId: UUID, newEmail: String): Future[Boolean]

  def updateUser(oldEmail: String, newEmail: String): Future[Boolean]

  def deleteUser(userId: UUID): Future[Boolean]

  def deleteUser(email: String): Future[Boolean]

}
