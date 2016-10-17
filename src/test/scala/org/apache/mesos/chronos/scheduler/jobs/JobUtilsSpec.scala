package org.apache.mesos.chronos.scheduler.jobs

import org.apache.mesos.chronos.scheduler.config.SchedulerConfiguration
import org.apache.mesos.chronos.scheduler.state.MesosStatePersistenceStore
import org.apache.curator.framework.CuratorFramework

import org.specs2.mock._
import org.specs2.mutable._
import org.apache.mesos.chronos.schedule.{ISO8601Parser, Schedule, ISO8601Schedule, AdjustedForStartDate}

class JobUtilsSpec extends SpecificationWithJUnit with Mockito {

  "Save a ScheduleBasedJob job correctly and be able to load it" in {
    val schedule = ISO8601Parser("R1/2012-01-01T00:00:01.000Z/PT1M").get
    val mockZKClient = mock[CuratorFramework]
    val config = new SchedulerConfiguration {}
    val store = new MesosStatePersistenceStore(mockZKClient, config)
    val job = new ScheduleBasedJob(schedule, "sample-name", "sample-command")
    val mockScheduler = mock[JobScheduler]

    store.persistJob(job)
    JobUtils.loadJobs(mockScheduler, store)

    there was one(mockScheduler).registerJob(List(job), persist = true)
  }

  "Can get job with arguments" in {
    val schedule = ISO8601Parser("R/2012-01-01T00:00:01.000Z/PT1M").get
    val arguments = "--help"
    val command = "sample-command"
    val commandWithArguments = command + " " + arguments

    val scheduledJob = new ScheduleBasedJob(schedule, "sample-name", command = command)
    val dependencyJob = new DependencyBasedJob(parents = Set("sample-name"), "sample-name2", command = command)
    val scheduledJobWithArguments = JobUtils.getJobWithArguments(scheduledJob, arguments)
    val dependencyJobWithArguments = JobUtils.getJobWithArguments(dependencyJob, arguments)

    scheduledJobWithArguments.command.toString must_== commandWithArguments
    dependencyJobWithArguments.command.toString must_== commandWithArguments
  }

  "Accepts a job name with periods" in {
    val jobName = "sample.name"

    JobUtils.isValidJobName(jobName)
  }
}
