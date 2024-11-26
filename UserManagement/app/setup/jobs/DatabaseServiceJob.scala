package setup.jobs

import models._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatabaseServiceJob @Inject() (
    dbConfigProvider: DatabaseConfigProvider,
    implicit val ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private val logger = Logger(this.getClass)

  def createSchema(): Future[Unit] = {
    db
      .run(
        DBIO.seq(
          UsersTable.query.schema.createIfNotExists,
          ProfilesTable.query.schema.createIfNotExists,
          TokensTable.query.schema.createIfNotExists
        ))
      .map(_ => logger.info("Database schema created"))
  }

  def closeDB(): Future[Unit] = {
    Future.successful(db.close())
  }
}
