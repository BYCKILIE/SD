package models

import DTOs.TokenDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class TokensTable(tag: Tag) extends Table[TokenDTO](tag, "tokens") {
  def userId: Rep[UUID] =
    column[UUID]("user_id", O.PrimaryKey)

  def token: Rep[Option[String]] = column[Option[String]]("token")

  def userFk = foreignKey("user_fk", userId, UsersTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def * : ProvenShape[TokenDTO] =
    (userId, token) <> ((TokenDTO.apply _).tupled, TokenDTO.unapply)
}

object TokensTable {
  lazy val query = new TableQuery(tag => new TokensTable(tag))
}
