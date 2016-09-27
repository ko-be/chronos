package org.apache.mesos.chronos.scheduler.jobs

import org.apache.mesos.chronos.schedule.{Schedule, ISO8601Schedule, CronSchedule}

/**
 * A stream of schedules.
 * Calling tail will return a clipped schedule.
 * Each unit in the stream consists of a Schedule, a job name, and a schedule Time Zone (TODO: remove schedule time zone)
 * @author Florian Leibert (flo@leibert.de)
 */
class ScheduleStream(val schedule: Schedule, val jobName: String, val scheduleTimeZone: String = "") {

  def head: (Schedule, String, String) = (schedule, jobName, scheduleTimeZone)

  def tail: Option[ScheduleStream] =
    schedule match {
      case iso8601: ISO8601Schedule => iso8601.recurrences match {
        case -1 => Some(new ScheduleStream(new ISO8601Schedule(iso8601.recurrences, iso8601.start.plus(iso8601.period), iso8601.period), jobName,
            scheduleTimeZone))
        case 0 => None
        case _ => Some(new ScheduleStream(new ISO8601Schedule(iso8601.recurrences - 1, iso8601.start.plus(iso8601.period), iso8601.period), jobName,
            scheduleTimeZone))
        
      }
      case cron: CronSchedule => Some(new ScheduleStream(cron, jobName, scheduleTimeZone))
    }
}
