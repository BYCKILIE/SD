package repositories

import DTOs.DeviceDTO

import java.util.UUID
import scala.concurrent.Future

trait DevicesRepository {

  def createDevice(device: DeviceDTO): Future[Boolean]

  def readDevice(deviceId: UUID): Future[Option[DeviceDTO]]

  def fetchOffsetDevices(offset: Long): Future[Option[Seq[DeviceDTO]]]

  def fetchMatchingDevices(partialName: String): Future[Option[Seq[DeviceDTO]]]

  def updateDevice(deviceId: UUID, newDevice: DeviceDTO): Future[Boolean]

  def deleteDevice(deviceId: UUID): Future[Boolean]

}
