package models

import DTOs.ParticipantDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import java.util.UUID

class ParticipantsTable(tag: Tag) extends Table[ParticipantDTO](tag, "participants") {
  def participantId: Rep[UUID] =
    column[UUID](
      "participant_id",
      O.PrimaryKey,
      O.SqlType("UUID DEFAULT uuid_generate_v4()"),
      O.AutoInc)

  def conversationId: Rep[UUID] =
    column[UUID]("conversation_id")

  def userId: Rep[UUID] =
    column[UUID]("user_id")

  def joinedAt: Rep[Timestamp] =
    column[Timestamp]("joined_at", O.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"), O.AutoInc)

  def conversationFk = foreignKey("conversation_fk", conversationId, ConversationsTable.query)(
    _.conversationId,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def userFk = foreignKey("user_fk", userId, UsersTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def * : ProvenShape[ParticipantDTO] =
    (participantId.?, conversationId, userId, joinedAt.?) <> (
      (ParticipantDTO.apply _).tupled,
      ParticipantDTO.unapply)
}

object ParticipantsTable {
  lazy val query = new TableQuery(tag => new ParticipantsTable(tag))
}
