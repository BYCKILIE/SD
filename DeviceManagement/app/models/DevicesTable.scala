package models

import DTOs.DeviceDTO
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class DevicesTable(tag: Tag) extends Table[DeviceDTO](tag, "devices") {
  def id: Rep[UUID] =
    column[UUID]("id", O.PrimaryKey, O.SqlType("UUID DEFAULT uuid_generate_v4()"), O.AutoInc)

  def name: Rep[String] = column[String]("name")

  def description: Rep[String] = column[String]("description")

  def address: Rep[String] = column[String]("address")

  def energyConsumption: Rep[Double] = column[Double]("energy_consumption")

  def * : ProvenShape[DeviceDTO] =
    (id.?, name, description, address, energyConsumption) <> ((DeviceDTO.apply _).tupled, DeviceDTO.unapply)
}

object DevicesTable {
  val query = new TableQuery(tag => new DevicesTable(tag))
}
