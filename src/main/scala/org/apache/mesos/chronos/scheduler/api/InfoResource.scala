package org.apache.mesos.chronos.scheduler.api

import org.apache.mesos.chronos.scheduler.config.SchedulerConfiguration
import org.apache.mesos.chronos.scheduler.config.SchedulerConfiguration
import mesosphere.chaos.http.HttpConf
import mesosphere.mesos.util.FrameworkIdUtil
import org.apache.mesos.Protos.FrameworkID

import java.util.logging.{Level, Logger}
import javax.ws.rs._
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response}
import com.google.inject.Inject

@Path(PathConstants.infoPath)
class InfoResource @Inject()(
  val schedulerConfiguration: SchedulerConfiguration,
  val frameworkIdUtil: FrameworkIdUtil
) {
  private val log = Logger.getLogger(getClass.getName)

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def info(): Response = {

    import scala.concurrent.ExecutionContext.Implicits.global
    import mesosphere.util.BackToTheFuture.Implicits.defaultTimeout
    val frameworkId = frameworkIdUtil.fetch

    val chronosConfiguration = Map(
        "master" -> schedulerConfiguration.master(),
        "user" -> schedulerConfiguration.user(),
        "failoverTimeoutSeconds" -> schedulerConfiguration.failoverTimeoutSeconds(),
        "schedulerHorizonSeconds" -> schedulerConfiguration.scheduleHorizonSeconds(),
        "clusterName" -> schedulerConfiguration.clusterName.get.getOrElse("None"),
        "hostname" -> schedulerConfiguration.hostname(),
        "leaderMaxIdleTime" -> schedulerConfiguration.leaderMaxIdleTimeMs(),
        "minReviveOffersInterval" -> schedulerConfiguration.minReviveOffersInterval(),
        "declineOfferDuration" -> schedulerConfiguration.declineOfferDuration.get.getOrElse("None"),
        "reconciliationInterval" -> schedulerConfiguration.reconciliationInterval(),
        "defaultTaskEpsilon" -> schedulerConfiguration.taskEpsilon.get.getOrElse("defaultTaskEpsilon")
    )
    val mailConfig = Map(
      "mail_server" -> schedulerConfiguration.mailServer.get.getOrElse("None"),
      "mail_user" -> schedulerConfiguration.mailUser.get.getOrElse("None"),
      "mail_from" -> schedulerConfiguration.mailFrom.get.getOrElse("None"),
      "mail_ssl_on" -> schedulerConfiguration.mailFrom.get.getOrElse("None")
    )
    val zookeeperConfig = Map(
      "zookeeperTimeout" -> schedulerConfiguration.zooKeeperTimeout(),
      "zookeeperPath" -> schedulerConfiguration.zooKeeperPath()
    )
    Response.ok(
       Map(
         "version" -> schedulerConfiguration.version,
         "frameworkId" -> frameworkId.get.getValue,
         "mail_config" -> mailConfig,
         "zookeeper_config" -> zookeeperConfig,
         "schedulerConfiguration" -> chronosConfiguration
       )
    ).build
  }
}
