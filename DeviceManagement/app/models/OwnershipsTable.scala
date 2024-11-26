package models

import DTOs.OwnershipDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class OwnershipsTable(tag: Tag) extends Table[OwnershipDTO](tag, "ownerships") {
  def ownershipId: Rep[UUID] = column[UUID]("ownership_id", O.PrimaryKey, O.SqlType("UUID DEFAULT uuid_generate_v4()"), O.AutoInc)

  def deviceId: Rep[UUID] = column[UUID]("device_id")

  def userId: Rep[Option[UUID]] = column[Option[UUID]]("user_id")

  def deviceFk = foreignKey("device_fk", deviceId, DevicesTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def userFk = foreignKey("user_fk", userId, UsersTable.query)(
    _.id.?,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.SetNull)

  def * : ProvenShape[OwnershipDTO] =
    (ownershipId.?, deviceId, userId) <> ((OwnershipDTO.apply _).tupled, OwnershipDTO.unapply)
}

object OwnershipsTable {
  lazy val query = new TableQuery(tag => new OwnershipsTable(tag))
}
