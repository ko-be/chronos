package org.apache.mesos.chronos.scheduler.jobs

import org.apache.mesos.chronos.schedule.Scheduling.Nextable

/**
 * A stream of schedules.
 * Calling tail will return an Option[ScheduleStream], where the job at the head of the stream contains the next run.
 * Each unit in the stream consists of a Schedule, a job name, and a schedule Time Zone (TODO: remove schedule time zone)
 * @author Florian Leibert (flo@leibert.de)
 */
class ScheduleStream[T](val schedule: T, val jobName: String, val scheduleTimeZone: String = "")(implicit nextable: Nextable[T]) {

  def head: (T, String, String) = (schedule, jobName, scheduleTimeZone)

  def tail() : Option[ScheduleStream[T]] = {
    nextable.next(schedule).map(schedule => new ScheduleStream(schedule, jobName, scheduleTimeZone))
  }
}
