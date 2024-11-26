package services

import DTOs.DeviceDTO
import models.DevicesTable
import play.api.db.slick.DatabaseConfigProvider
import repositories.DevicesRepository
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DevicesService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    ex: ExecutionContext)
  extends DevicesRepository {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  override def createDevice(device: DeviceDTO): Future[Boolean] = {
    db.run(
      DevicesTable.query += device
    ).map(_ > 0)
  }

  override def readDevice(deviceId: UUID): Future[Option[DeviceDTO]] =
    db.run(DevicesTable.query.filter(_.id === deviceId).result.headOption)

  override def fetchOffsetDevices(offset: Long): Future[Option[Seq[DeviceDTO]]] = db
    .run(DevicesTable.query.drop(offset).take(offset + 20).result)
    .map(ownerships => if (ownerships.isEmpty) None else Some(ownerships))

  override def fetchMatchingDevices(partialName: String): Future[Option[Seq[DeviceDTO]]] = {
    val patterns = partialName.toLowerCase.split("\\s+").map(word => s"%$word%")

    val query = DevicesTable.query.filter { device =>
      patterns.map(pattern => device.name.toLowerCase.like(pattern)).reduceLeft(_ || _)
    }

    db.run(query.result).map { devices =>
      if (devices.isEmpty) None else Some(devices)
    }
  }

  override def updateDevice(deviceId: UUID, newDevice: DeviceDTO): Future[Boolean] =
    db.run(DevicesTable.query.filter(_.id === deviceId).result.headOption).flatMap {
      case Some(existingDevice) =>
        val name = if (newDevice.name == "") existingDevice.name else newDevice.name
        val description = if (newDevice.description == "") existingDevice.description else newDevice.description
        val address = if (newDevice.address == "") existingDevice.address else newDevice.address
        val energyConsumption = if (newDevice.energyConsumption == 0) existingDevice.energyConsumption else newDevice.energyConsumption

        val updated = existingDevice.copy(
          name = name,
          description = description,
          address = address,
          energyConsumption = energyConsumption
        )
        db.run(DevicesTable.query.filter(_.id === deviceId).update(updated)).map(_ > 0)
      case None =>
        Future.successful(false)
    }

  override def deleteDevice(deviceId: UUID): Future[Boolean] = db.run(
    (for {
      deleted <- DevicesTable.query.filter(_.id === deviceId).delete
    } yield deleted > 0).transactionally
  )


}
