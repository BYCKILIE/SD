package services

import DTOs.OwnershipDTO
import models.OwnershipsTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.OwnershipsRepository
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OwnershipsService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends OwnershipsRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def createOwnership(ownership: OwnershipDTO): Future[Boolean] = {
    db.run(
      OwnershipsTable.query += ownership
    ).map(_ > 0)
  }

  override def readOwnerships(userId: UUID, offset: Long): Future[Option[Seq[OwnershipDTO]]] = db
    .run(OwnershipsTable.query.filter(_.userId === userId).drop(offset).take(offset + 20).result)
    .map(ownerships => if (ownerships.isEmpty) None else Some(ownerships))

  override def deleteOwnership(ownershipID: UUID): Future[Boolean] = db.run(
    (for {
      deleted <- OwnershipsTable.query
        .filter(ownership => ownership.ownershipId === ownershipID)
        .delete
    } yield deleted > 0).transactionally
  )

}
