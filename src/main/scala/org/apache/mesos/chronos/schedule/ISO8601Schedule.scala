package org.apache.mesos.chronos.schedule

import java.util.TimeZone

import org.joda.time.{DateTime, DateTimeZone, Period, Seconds}
import org.joda.time.format.ISODateTimeFormat

object WithTimeZoneConsidered extends ((ISO8601Schedule, String) => Option[ISO8601Schedule]) {
  def apply(schedule: ISO8601Schedule, timeZoneString: String): Option[ISO8601Schedule] = {
    //if the timezone is invalid, this just returns UTC/GMT,
    //so we return None if the timeZoneString != GMT but the
    //result returned by TimeZone.getTimeZone is.
    val zoneForString = TimeZone.getTimeZone(timeZoneString)
    if (zoneForString.getID == "GMT" && timeZoneString != "GMT") {
      return None
    } else {
      Some(new ISO8601Schedule(
          recurrences = schedule.recurrences,
          start = schedule.start.withZoneRetainFields(DateTimeZone.forTimeZone(zoneForString)),
          period = schedule.period
      ))
    }
  }
}

/**
 * A factory for Schedules, optionally returning a new job.
 * If the number of skips that would have occurred between the start time of the original
 * schedule and the startTime proposed is greater than the number of recurrences of the provided
 * schedule, then None will be returned, otherwise a new schedule with the recurrences adjusted for
 * the number of times the job *would* have run between the provided start date and the original start
 * date, and the start for the new schedule adjusted to the provided date.
 */
object AdjustedForStartDate extends ((ISO8601Schedule, DateTime) => Option[ISO8601Schedule]) {
  def apply(schedule: ISO8601Schedule, startTime: DateTime): Option[ISO8601Schedule] = {
      val skip = calculateSkips(startTime, schedule.start, schedule.period)
      if (schedule.recurrences == -1) {
        val newStart = schedule.start.plus(schedule.period.multipliedBy(skip))
        Some(new ISO8601Schedule(schedule.recurrences, newStart, schedule.period))
      } else if (schedule.recurrences < skip) {
        None
      } else {
        val newRecurrences = schedule.recurrences - skip
        val newStart = schedule.start.plus(schedule.period.multipliedBy(skip))
        Some(new ISO8601Schedule(newRecurrences, newStart, schedule.period))
      }
    }
  /**
   * Calculates the number of skips needed to bring the job start into the future
   */
  protected def calculateSkips(dateTime: DateTime, jobStart: DateTime, period: Period): Int = {
    // If the period is at least a month, we have to actually add the period to the date
    // until it's in the future because a month-long period might have different second
    if (period.getMonths >= 1) {
      var skips = 0
      var newDate = new DateTime(jobStart)
      while (newDate.isBefore(dateTime)) {
        newDate = newDate.plus(period)
        skips += 1
      }
      skips
    } else {
      Seconds.secondsBetween(jobStart, dateTime).getSeconds / period.toStandardSeconds.getSeconds
    }
  }

}
