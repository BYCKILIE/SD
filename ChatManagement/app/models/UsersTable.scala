package models

import DTOs.UserDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class UsersTable(tag: Tag) extends Table[UserDTO](tag, "users") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)

  def email: Rep[String] = column[String]("email", O.Unique)

  def * : ProvenShape[UserDTO] =
    (id, email) <> ((UserDTO.apply _).tupled, UserDTO.unapply)
}

object UsersTable {
  lazy val query = new TableQuery(tag => new UsersTable(tag))
}
