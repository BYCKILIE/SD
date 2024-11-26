package setup

import play.api.inject.ApplicationLifecycle
import setup.jobs.DatabaseServiceJob

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

@Singleton
class Executor @Inject() (
    lifecycle: ApplicationLifecycle,
    databaseServiceJob: DatabaseServiceJob
) {

  databaseServiceJob.createSchema()

  lifecycle.addStopHook(() => Future.successful(databaseServiceJob.closeDB()))

}
