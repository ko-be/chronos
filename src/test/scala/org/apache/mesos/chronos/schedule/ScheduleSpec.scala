package org.apache.mesos.chronos.schedule

import org.specs2.mutable.SpecificationWithJUnit

import org.joda.time.{DateTime, DateTimeZone, Period, Minutes}
import org.apache.mesos.chronos.schedule.Nextable.NextableCron
import org.apache.mesos.chronos.schedule.Nextable.NextableISO8601

class ScheduleSpec extends SpecificationWithJUnit{
  "NextableISO8601" should {
    "Reduce recurrences with each iteration" in {
      val current = ISO8601Parser("R2/2012-01-02T00:00:01.000Z/P1M").get
      val next = NextableISO8601.next(current)
      next must beSome
      next.get.recurrences must_== 1
    }

    "Return None when recurrences is 0" in {
      val current = ISO8601Parser("R0/2012-01-02T00:00:01.000Z/P1M").get
      val next = NextableISO8601.next(current)
      next must beNone
    }

  }
  "NextableCron" should {
    "Provide the next job using the current job's start time as the base" in {
      val current = CronParser("*/5 * * * *").get
      val inFiveMinutes = NextableCron.next(current)
      inFiveMinutes must beSome
      Minutes.minutesBetween(current.start.toDateTime, inFiveMinutes.get.start.toDateTime)  must_== Minutes.minutes(5)

      val inTenMinutes = NextableCron.next(inFiveMinutes.get)
      Minutes.minutesBetween(current.start.toDateTime, inTenMinutes.get.start.toDateTime) must_== Minutes.minutes(10)
    }

    "Keep the timezone consistent" in {
      val current = CronParser("0 18 * * *", "America/Los_Angeles").get
      val nextRun = NextableCron.next(current)
      nextRun must beSome
      nextRun.get.start.getHourOfDay must_== current.start.getHourOfDay
      nextRun.get.start.getZone.getID must_== "America/Los_Angeles"
    }
  }
}