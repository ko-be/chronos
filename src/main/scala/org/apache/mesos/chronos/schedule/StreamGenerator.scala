package org.apache.mesos.chronos.schedule

import org.apache.mesos.chronos.scheduler.jobs.ScheduleBasedJob
import org.apache.mesos.chronos.scheduler.jobs.ScheduleStream
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Period, Seconds}

object StreamGenerator {
  trait StreamGenerator[T] {
    def streamGenerator(job: ScheduleBasedJob, dateTime: DateTime): Option[ScheduleStream[T]]
  }

  implicit object ISO8601StreamGenerator extends StreamGenerator[ISO8601Schedule] {
    def streamGenerator(job: ScheduleBasedJob, dateTime: DateTime): Option[ScheduleStream[ISO8601Schedule]] = {
      if(job.schedule.start.plus(job.epsilon).isBefore(dateTime)) {
        AdjustedForStartDate(job.schedule.asInstanceOf[ISO8601Schedule], dateTime).map(schedule => new ScheduleStream[ISO8601Schedule](schedule, job.name, job.scheduleTimeZone))
      } else {
        Some(new ScheduleStream(job.schedule.asInstanceOf[ISO8601Schedule], job.name, job.scheduleTimeZone))
      }
    }
  }

  implicit object CronScheduleStreamGenerator extends StreamGenerator[CronSchedule] {
    def streamGenerator(job: ScheduleBasedJob, dateTime: DateTime): Option[ScheduleStream[CronSchedule]] = {
      Some(new ScheduleStream[CronSchedule](job.schedule.asInstanceOf[CronSchedule], job.name, job.scheduleTimeZone))
    }
  }
}