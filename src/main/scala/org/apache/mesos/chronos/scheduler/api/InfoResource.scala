package org.apache.mesos.chronos.scheduler.api

import org.apache.mesos.chronos.scheduler.config.SchedulerConfiguration
import org.apache.curator.framework.recipes.leader.{LeaderLatch, Participant}
import mesosphere.chaos.http.HttpConf
import mesosphere.mesos.util.FrameworkIdUtil
import org.apache.mesos.Protos.FrameworkID

import java.util.logging.{Level, Logger}
import javax.ws.rs._
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.{MediaType, Response}
import scala.util.Try
import com.google.inject.Inject

@Path(PathConstants.infoPath)
class InfoResource @Inject()(
  val schedulerConfiguration: SchedulerConfiguration,
  val frameworkIdUtil: FrameworkIdUtil,
  val leaderLatch: LeaderLatch
) {
  private val log = Logger.getLogger(getClass.getName)

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def info(): Response = {

    import scala.concurrent.ExecutionContext.Implicits.global
    import mesosphere.util.BackToTheFuture.Implicits.defaultTimeout
    val frameworkId = frameworkIdUtil.fetch

    val chronosConfiguration = Map(
        "user" -> schedulerConfiguration.user(),
        "failover_timeout" -> schedulerConfiguration.failoverTimeoutSeconds(),
        "scheduler_horizon" -> schedulerConfiguration.scheduleHorizonSeconds(),
        "cluster_name" -> schedulerConfiguration.clusterName.get.getOrElse("None"),
        "hostname" -> schedulerConfiguration.hostname(),
        "leader_max_idle_time" -> schedulerConfiguration.leaderMaxIdleTimeMs(),
        "min_revive_offers_interval" -> schedulerConfiguration.minReviveOffersInterval(),
        "decline_offer_duration" -> schedulerConfiguration.declineOfferDuration.get.getOrElse("None"),
        "reconciliation_interval" -> schedulerConfiguration.reconciliationInterval(),
        "default_task_epsilon" -> schedulerConfiguration.taskEpsilon.get.getOrElse("defaultTaskEpsilon"),
        "failure_retry_delay" -> schedulerConfiguration.failureRetryDelayMs(),
        "disable_after_failures" -> schedulerConfiguration.disableAfterFailures(),
        "task_epsilon" -> schedulerConfiguration.taskEpsilon(),
        "reconciliation_interval" -> schedulerConfiguration.reconciliationInterval()
    )
    val mesosConfig = Map(
        "master" -> schedulerConfiguration.master(),
        "mesos_task_mem" -> schedulerConfiguration.mesosTaskMem(),
        "mesos_task_cpu" -> schedulerConfiguration.mesosTaskCpu(),
        "mesos_task_disk" -> schedulerConfiguration.mesosTaskDisk(),
        "mesos_checkpoint" -> schedulerConfiguration.mesosCheckpoint(),
        "mesos_role" -> schedulerConfiguration.mesosRole(),
        "mesos_framework_name" -> schedulerConfiguration.mesosFrameworkName(),
        "mesos_authentication_principal" -> schedulerConfiguration.mesosAuthenticationPrincipal.get.getOrElse("None")
    )

    val mailConfig = Map(
      "mail_server" -> schedulerConfiguration.mailServer.get.getOrElse("None"),
      "mail_user" -> schedulerConfiguration.mailUser.get.getOrElse("None"),
      "mail_from" -> schedulerConfiguration.mailFrom.get.getOrElse("None"),
      "mail_ssl_on" -> schedulerConfiguration.mailFrom.get.getOrElse("None")
    )
    val zookeeperConfig = Map(
      "zk_hosts" -> schedulerConfiguration.zookeeperServersString(),
      "zookeeper_timeout" -> schedulerConfiguration.zooKeeperTimeout(),
      "zookeeper_path" -> schedulerConfiguration.zooKeeperPath()
    )
    Response.ok(
       Map(
         "version" -> schedulerConfiguration.version,
         "leader" -> getCurrentLeader().getOrElse("Unknown"),
         "frameworkId" -> frameworkId.get.getValue,
         "mail_config" -> mailConfig,
         "zookeeper_config" -> zookeeperConfig,
         "mesos_config" -> mesosConfig,
         "chronos_configuration" -> chronosConfiguration
       )
    ).build
  }

  private def getCurrentLeader(): Try[String] = Try(leaderLatch.getLeader.getId)
}
