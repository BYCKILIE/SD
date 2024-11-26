package utils

import org.mindrot.jbcrypt.BCrypt

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.imageio.ImageIO
import scala.util.{Failure, Success, Try}

object ImageOP {

  private val fixedSalt = "$2a$10$"

  private val profilePicPath = "public/images/profile_pictures"
  private val profilePicBackupPath = "public/images/profile_pictures/backup"

  def saveImage(image: BufferedImage, userId: UUID): Option[String] = {
    val savePath: String = "/" + BCrypt.hashpw(userId.toString, fixedSalt + userId.toString)

    val outputFile = new File(profilePicPath + savePath)

    moveToBackupIfExists(outputFile, savePath)

    writeImage(image, outputFile).map(_ => savePath)
  }

  private def moveToBackupIfExists(file: File, backupDir: String): Unit = {
    if (file.exists()) {
      val backupPath = Paths.get(
        profilePicBackupPath + backupDir,
        DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
      val backupFile = new File(backupPath.toString)

      createDirectory(profilePicBackupPath + backupDir).map {
        _ => moveFile(file, backupFile)
      }
    }
  }

  private def moveFile(source: File, destination: File): Unit = {
    Try {
      Files.move(source.toPath, destination.toPath, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  private def writeImage(image: BufferedImage, file: File): Option[Unit] = {
    Try {
      ImageIO.write(image, "jpg", file)
    } match {
      case Success(_) => Some(())
      case Failure(_) => None
    }
  }

  private def createDirectory(dirPath: String): Option[Unit] = {
    Try {
      val dir = new File(dirPath)
      if (!dir.exists()) dir.mkdirs()
    } match {
      case Success(_) => Some(())
      case Failure(_) => None
    }
  }

}
