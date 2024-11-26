package models

import DTOs.UserDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import java.util.UUID

class UsersTable(tag: Tag) extends Table[UserDTO](tag, "users") {
  def id: Rep[UUID] =
    column[UUID]("id", O.PrimaryKey, O.SqlType("UUID DEFAULT uuid_generate_v4()"), O.AutoInc)

  def email: Rep[String] = column[String]("email", O.Unique)

  def password: Rep[String] = column[String]("password")

  def role: Rep[String] = column[String]("role")

  def createdAt: Rep[Timestamp] =
    column[Timestamp]("created_at", O.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"), O.AutoInc)

  def * : ProvenShape[UserDTO] =
    (id.?, email, password, role, createdAt.?) <> ((UserDTO.apply _).tupled, UserDTO.unapply)
}

object UsersTable {
  val query = new TableQuery(tag => new UsersTable(tag))
}
