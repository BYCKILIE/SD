package models

import DTOs.ConversationDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import java.util.UUID

class ConversationsTable(tag: Tag) extends Table[ConversationDTO](tag, "conversations") {
  def conversationId: Rep[UUID] =
    column[UUID](
      "conversation_id",
      O.PrimaryKey,
      O.SqlType("UUID DEFAULT uuid_generate_v4()"),
      O.AutoInc)

  def isGroup: Rep[Boolean] = column[Boolean]("is_group")

  def conversationName: Rep[Option[String]] = column[Option[String]]("conversation_name")

  def createdAt: Rep[Timestamp] =
    column[Timestamp]("created_at", O.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"), O.AutoInc)

  def * : ProvenShape[ConversationDTO] =
    (conversationId.?, isGroup, conversationName, createdAt.?) <> (
      (ConversationDTO.apply _).tupled,
      ConversationDTO.unapply)
}

object ConversationsTable {
  lazy val query = new TableQuery(tag => new ConversationsTable(tag))
}
