package repositories

import DTOs.ProfileDTO

import java.util.UUID
import scala.concurrent.Future

trait ProfilesRepository {

  def createProfile(profile: ProfileDTO): Future[Boolean]

  def readProfile(userId:  UUID): Future[Option[ProfileDTO]]

  def fetchMatchingProfiles(partialName: String): Future[Option[Seq[ProfileDTO]]]

  def updateProfile(userId: UUID, profile: ProfileDTO): Future[Boolean]

  def deleteProfile(userId: UUID): Future[Boolean]

}
