package models

import DTOs.ProfileDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class ProfilesTable(tag: Tag) extends Table[ProfileDTO](tag, "profiles") {
  def userId: Rep[UUID] =
    column[UUID]("user_id", O.PrimaryKey)

  def firstName: Rep[String] = column[String]("first_name")

  def lastName: Rep[String] = column[String]("last_name")

  def profilePicUrl: Rep[String] = column[String]("profile_pic_url")

  def userFk = foreignKey("user_fk", userId, UsersTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def * : ProvenShape[ProfileDTO] =
    (userId.?, firstName, lastName, profilePicUrl) <> ((ProfileDTO.apply _).tupled, ProfileDTO.unapply)
}

object ProfilesTable {
  lazy val query = new TableQuery(tag => new ProfilesTable(tag))
}
