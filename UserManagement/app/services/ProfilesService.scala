package services

import DTOs.ProfileDTO
import models.ProfilesTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.ProfilesRepository
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import javax.inject.Singleton

import java.util.UUID

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ProfilesService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends ProfilesRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def createProfile(profile: ProfileDTO): Future[Boolean] = {
    db.run(
      ProfilesTable.query += profile
    ).map(_ > 0)
  }

  override def readProfile(userId: UUID): Future[Option[ProfileDTO]] = db
    .run(
      ProfilesTable.query.filter(_.userId === userId).result.headOption
    )

  def fetchMatchingProfiles(partialName: String): Future[Option[Seq[ProfileDTO]]] = {
    val patterns = partialName.toLowerCase.split("\\s+").map(word => s"%$word%")

    val query = ProfilesTable.query.filter { profile =>
      val firstNameCondition = patterns.map(pattern => profile.firstName.toLowerCase.like(pattern)).reduceLeft(_ || _)
      val lastNameCondition = patterns.map(pattern => profile.lastName.toLowerCase.like(pattern)).reduceLeft(_ || _)

      firstNameCondition || lastNameCondition
    }

    db.run(query.result).map { profiles =>
      if (profiles.isEmpty) None else Some(profiles)
    }
  }

  override def updateProfile(userId: UUID, profile: ProfileDTO): Future[Boolean] =
    db.run(ProfilesTable.query.filter(_.userId === userId).result.headOption).flatMap {
      case Some(existingUser) =>
        val firstName = if (profile.firstName == "") existingUser.firstName else profile.firstName
        val lastName = if (profile.lastName == "") existingUser.lastName else profile.lastName


        val updated = existingUser.copy(
          firstName = firstName,
          lastName = lastName
        )
        db.run(ProfilesTable.query.filter(_.userId === userId).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def deleteProfile(userId: UUID): Future[Boolean] = db.run(
    (for {
      //      _ <- Credentials.filter(_.userId === id).delete
      deleted <- ProfilesTable.query.filter(_.userId === userId).delete
    } yield deleted > 0).transactionally
  )

}
