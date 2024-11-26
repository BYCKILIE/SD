package repositories

import DTOs.OwnershipDTO

import java.util.UUID
import scala.concurrent.Future

trait OwnershipsRepository {

  def createOwnership(ownership: OwnershipDTO): Future[Boolean]

  def readOwnerships(userId: UUID, offset: Long): Future[Option[Seq[OwnershipDTO]]]

  def deleteOwnership(ownershipId: UUID): Future[Boolean]

}