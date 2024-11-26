package models

import DTOs.MessageDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import java.util.UUID

class MessagesTable(tag: Tag) extends Table[MessageDTO](tag, "messages") {
  def messageId: Rep[UUID] =
    column[UUID](
      "message_id",
      O.PrimaryKey,
      O.SqlType("UUID DEFAULT uuid_generate_v4()"),
      O.AutoInc)

  def conversationId: Rep[UUID] = column[UUID]("conversation_id")

  def senderId: Rep[UUID] = column[UUID]("sender_id")

  def messageText: Rep[Option[String]] = column[Option[String]]("message_text")

  def mediaUrl: Rep[Option[String]] = column[Option[String]]("media_url")

  def createdAt: Rep[Timestamp] =
    column[Timestamp]("created_at", O.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"), O.AutoInc)

  def isDeleted: Rep[Boolean] = column[Boolean]("is_deleted")

  def conversationFk = foreignKey("conversation_fk", conversationId, ConversationsTable.query)(
    _.conversationId,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def senderFk = foreignKey("sender_fk", senderId, UsersTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def * : ProvenShape[MessageDTO] =
    (messageId.?, conversationId, senderId, messageText, mediaUrl, createdAt.?, isDeleted) <> (
      (MessageDTO.apply _).tupled,
      MessageDTO.unapply)
}

object MessagesTable {
  lazy val query = new TableQuery(tag => new MessagesTable(tag))
}
