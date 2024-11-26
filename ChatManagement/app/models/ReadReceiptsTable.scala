package models

import DTOs.ReadReceiptDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import java.util.UUID

class ReadReceiptsTable(tag: Tag) extends Table[ReadReceiptDTO](tag, "read_receipts") {
  def receiptId: Rep[UUID] =
    column[UUID](
      "receipt_id",
      O.PrimaryKey,
      O.SqlType("UUID DEFAULT uuid_generate_v4()"),
      O.AutoInc)

  def messageId: Rep[UUID] = column[UUID]("message_id")

  def userId: Rep[UUID] = column[UUID]("user_id")

  def readAt: Rep[Timestamp] =
    column[Timestamp]("read_at", O.SqlType("TIMESTAMP DEFAULT CURRENT_TIMESTAMP"), O.AutoInc)

  def messageFk = foreignKey("message_fk", messageId, MessagesTable.query)(
    _.messageId,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def userFk = foreignKey("user_fk", userId, UsersTable.query)(
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade)

  def * : ProvenShape[ReadReceiptDTO] =
    (receiptId.?, messageId, userId, readAt.?) <> (
      (ReadReceiptDTO.apply _).tupled,
      ReadReceiptDTO.unapply)
}

object ReadReceiptsTable {
  lazy val query = new TableQuery(tag => new ReadReceiptsTable(tag))
}
