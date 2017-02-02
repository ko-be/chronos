package org.apache.mesos.chronos.scheduler.mesos

import java.nio.charset.StandardCharsets
import mesosphere.mesos.protos._
import mesosphere.mesos.util.FrameworkIdUtil
import com.google.common.cache.Cache
import org.apache.mesos.Protos.Offer
import org.apache.mesos.chronos.ChronosTestHelper._
import org.apache.mesos.chronos.scheduler.jobs.{ BaseJob, JobScheduler, TaskManager }
import org.apache.mesos.chronos.scheduler.state.PersistenceStore
import org.apache.mesos.{ Protos, SchedulerDriver }
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

import scala.collection.mutable

class MesosJobFrameworkSpec extends SpecificationWithJUnit with Mockito {
  "MesosJobFramework" should {
    "Revive offers when registering" in {
      val mockMesosOfferReviver = mock[MesosOfferReviver]

      val mesosJobFramework = new MesosJobFramework(
        mock[MesosDriverFactory],
        mock[JobScheduler],
        mock[TaskManager],
        makeConfig(),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mockMesosOfferReviver)

      mesosJobFramework.registered(mock[SchedulerDriver], Protos.FrameworkID.getDefaultInstance,
        Protos.MasterInfo.getDefaultInstance)

      there was one(mockMesosOfferReviver).reviveOffers()
    }

    "Revive offers when re-registering" in {
      val mockMesosOfferReviver = mock[MesosOfferReviver]

      val mesosJobFramework = new MesosJobFramework(
        mock[MesosDriverFactory],
        mock[JobScheduler],
        mock[TaskManager],
        makeConfig(),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mockMesosOfferReviver)

      mesosJobFramework.reregistered(mock[SchedulerDriver], Protos.MasterInfo.getDefaultInstance)

      there was one(mockMesosOfferReviver).reviveOffers()
    }

    "Reject unused offers with the default " in {
      import mesosphere.mesos.protos.Implicits._

      import scala.collection.JavaConverters._

      val mesosDriverFactory = mock[MesosDriverFactory]
      val schedulerDriver = mock[SchedulerDriver]
      mesosDriverFactory.get().returns(schedulerDriver)

      val mesosJobFramework = spy(
        new MesosJobFramework(
          mesosDriverFactory,
          mock[JobScheduler],
          mock[TaskManager],
          makeConfig(),
          mock[FrameworkIdUtil],
          mock[MesosTaskBuilder],
          mock[MesosOfferReviver]))

      val tasks = mutable.Buffer[(String, BaseJob, Offer)]()
      doReturn(tasks).when(mesosJobFramework).generateLaunchableTasks(any)
      doNothing.when(mesosJobFramework).reconcile(any)

      val offer: Offer = makeBasicOffer
      mesosJobFramework.resourceOffers(mock[SchedulerDriver], Seq[Protos.Offer](offer).asJava)

      there was one (schedulerDriver).declineOffer(OfferID("1"), Protos.Filters.getDefaultInstance)
    }

    "Reject unused offers with default RefuseSeconds if --decline_offer_duration is not set" in {
      import mesosphere.mesos.protos.Implicits._

      import scala.collection.JavaConverters._

      val mesosDriverFactory = mock[MesosDriverFactory]
      val schedulerDriver = mock[SchedulerDriver]
      mesosDriverFactory.get().returns(schedulerDriver)

      val mesosJobFramework = spy(
        new MesosJobFramework(
          mesosDriverFactory,
          mock[JobScheduler],
          mock[TaskManager],
          makeConfig(),
          mock[FrameworkIdUtil],
          mock[MesosTaskBuilder],
          mock[MesosOfferReviver]))

      val tasks = mutable.Buffer[(String, BaseJob, Offer)]()
      doReturn(tasks).when(mesosJobFramework).generateLaunchableTasks(any)
      doNothing.when(mesosJobFramework).reconcile(any)

      val offer: Offer = makeBasicOffer
      mesosJobFramework.resourceOffers(mock[SchedulerDriver], Seq[Protos.Offer](offer).asJava)

      there was one (schedulerDriver).declineOffer(OfferID("1"), Protos.Filters.getDefaultInstance)
    }
  }

  "Reject unused offers with the configured value of RefuseSeconds if --decline_offer_duration is set" in {
    import mesosphere.mesos.protos.Implicits._

    import scala.collection.JavaConverters._

    val mesosDriverFactory = mock[MesosDriverFactory]
    val schedulerDriver = mock[SchedulerDriver]
    mesosDriverFactory.get().returns(schedulerDriver)

    val mesosJobFramework = spy(
      new MesosJobFramework(
        mesosDriverFactory,
        mock[JobScheduler],
        mock[TaskManager],
        makeConfig("--decline_offer_duration", "3000"),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mock[MesosOfferReviver]))

    val tasks = mutable.Buffer[(String, BaseJob, Offer)]()
    doReturn(tasks).when(mesosJobFramework).generateLaunchableTasks(any)
    doNothing.when(mesosJobFramework).reconcile(any)

    val offer: Offer = makeBasicOffer
    mesosJobFramework.resourceOffers(mock[SchedulerDriver], Seq[Protos.Offer](offer).asJava)

    val filters = Protos.Filters.newBuilder().setRefuseSeconds(3).build()
    there was one (schedulerDriver).declineOffer(OfferID("1"), filters)
  }

  private[this] def makeBasicOffer: Offer = {
    import mesosphere.mesos.protos.Implicits._

    Protos.Offer.newBuilder()
      .setId(OfferID("1"))
      .setFrameworkId(FrameworkID("chronos"))
      .setSlaveId(SlaveID("slave0"))
      .setHostname("localhost")
      .addResources(ScalarResource(Resource.CPUS, 1, "*"))
      .addResources(ScalarResource(Resource.MEM, 100, "*"))
      .addResources(ScalarResource(Resource.DISK, 100, "*"))
      .build()
  }

  "Ignore invalid status updates about tasks we don't understand" in {
    import mesosphere.mesos.protos.Implicits._
    import scala.collection.JavaConverters._

    val taskManager = mock[TaskManager]

    val taskCache = mock[Cache[String, Protos.TaskState]]
    doNothing.when(taskCache).put(any, any)

    taskManager.taskCache returns taskCache

    val mesosDriverFactory = mock[MesosDriverFactory]
    val schedulerDriver = mock[SchedulerDriver]
    mesosDriverFactory.get().returns(schedulerDriver)

    val mesosJobFramework = spy(
      new MesosJobFramework(
        mesosDriverFactory,
        mock[JobScheduler],
        taskManager,
        makeConfig("--decline_offer_duration", "3000"),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mock[MesosOfferReviver]))
    val status = Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue("BLAHBLAHBLAH")).setState(Protos.TaskState.TASK_RUNNING).build()
    mesosJobFramework.statusUpdate(schedulerDriver, status) must not(throwA[MatchError])
  }

  "Ignore TASK_LOST for jobs that we don't believe to be running" in {
    import mesosphere.mesos.protos.Implicits._
    import scala.collection.JavaConverters._

    val taskManager = mock[TaskManager]
    val persistenceStore = mock[PersistenceStore]
    val fakeTasks = collection.immutable.HashMap[String, Array[Byte]]()
    persistenceStore.getTasks returns fakeTasks
    taskManager.persistenceStore returns persistenceStore
    val jobScheduler = mock[JobScheduler]

    val taskCache = mock[Cache[String, Protos.TaskState]]
    doNothing.when(taskCache).put(any, any)

    taskManager.taskCache returns taskCache

    val mesosDriverFactory = mock[MesosDriverFactory]
    val schedulerDriver = mock[SchedulerDriver]
    mesosDriverFactory.get().returns(schedulerDriver)

    val mesosJobFramework = spy(
      new MesosJobFramework(
        mesosDriverFactory,
        jobScheduler,
        taskManager,
        makeConfig("--decline_offer_duration", "3000"),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mock[MesosOfferReviver]))
    val status = Protos.TaskStatus.newBuilder()
      .setTaskId(Protos.TaskID.newBuilder()
      .setValue("mytask"))
      .setState(Protos.TaskState.TASK_LOST)
      .build()

    mesosJobFramework.statusUpdate(schedulerDriver, status)
    there was no(jobScheduler).handleFailedTask(status)
  }

  "Treat TASK_LOST as failed when the task is in task manager" in {
    import mesosphere.mesos.protos.Implicits._
    import scala.collection.JavaConverters._

    val taskManager = mock[TaskManager]
    val persistenceStore = mock[PersistenceStore]
    val fakeTasks = collection.immutable.HashMap("ct:0000:1:foo" -> "foo".getBytes(StandardCharsets.UTF_8) )
    persistenceStore.getTasks returns fakeTasks
    taskManager.persistenceStore returns persistenceStore
    val jobScheduler = mock[JobScheduler]

    val taskCache = mock[Cache[String, Protos.TaskState]]
    doNothing.when(taskCache).put(any, any)

    taskManager.taskCache returns taskCache

    val mesosDriverFactory = mock[MesosDriverFactory]
    val schedulerDriver = mock[SchedulerDriver]
    mesosDriverFactory.get().returns(schedulerDriver)

    val mesosJobFramework = spy(
      new MesosJobFramework(
        mesosDriverFactory,
        jobScheduler,
        taskManager,
        makeConfig("--decline_offer_duration", "3000"),
        mock[FrameworkIdUtil],
        mock[MesosTaskBuilder],
        mock[MesosOfferReviver]))
    val status = Protos.TaskStatus.newBuilder()
      .setTaskId(Protos.TaskID.newBuilder()
      .setValue("ct:0000:1:foo"))
      .setState(Protos.TaskState.TASK_LOST)
      .build()
    mesosJobFramework.statusUpdate(schedulerDriver, status)
    there was one(jobScheduler).handleFailedTask(status)
  }
}
