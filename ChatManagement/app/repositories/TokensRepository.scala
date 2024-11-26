package repositories

import java.util.UUID
import scala.concurrent.Future

trait TokensRepository {

  def createToken(userId: UUID): Future[Boolean]

  def validateToken(token: String): Future[Option[(UUID, String)]]

  def updateToken(token: String): Future[Boolean]

  def setNull(userId: UUID): Future[Boolean]

}
