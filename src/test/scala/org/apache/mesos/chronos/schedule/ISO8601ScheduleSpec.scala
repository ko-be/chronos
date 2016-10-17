package org.apache.mesos.chronos.schedule
import org.specs2.mutable.SpecificationWithJUnit
import org.joda.time._
class ISO8601ScheduleSpec extends SpecificationWithJUnit {
   "ISO8601Schedule should" in {
       "Adjust a schedule for a given timezone" in {
         val schedule = ISO8601Parser("R/2012-01-02T00:00:01.000Z/P1M").get
         val adjusted = WithTimeZoneConsidered(schedule, "America/Los_Angeles").get
         adjusted.start.getZone must_== DateTimeZone.forID("America/Los_Angeles")
       }
       "Ignores Bonus timezones" in {
         val schedule = ISO8601Parser("R/2012-01-02T00:00:01.000Z/P1M").get
         val adjusted = WithTimeZoneConsidered(schedule, "BOGUS")
         adjusted must beNone
       }

       "Can skip forward an ISO8601Schedule job with a monthly period" in {
        val schedule = ISO8601Parser("R/2012-01-02T00:00:01.000Z/P1M").get
        val now = new DateTime("2012-02-01T00:00:01.000Z")
        val adjusted = AdjustedForStartDate(schedule, now).get

        // Ensure that this job runs on the first of next month
        adjusted.start.isAfter(now) must beTrue
        adjusted.start.dayOfMonth().get must_== 2
       }

       "Can skip forward a job" in {
          val schedule = ISO8601Parser("R/2012-01-01T00:00:01.000Z/PT60S").get
          val now = new DateTime("2012-01-02T00:00:01.000Z")
          val adjusted = AdjustedForStartDate(schedule, now).get
          // Ensure that this job runs today
          adjusted.start.toLocalDate must_== now.toLocalDate
        }
       "Returns None if there are no repetitions left" in {
          val schedule = ISO8601Parser("R2/2012-01-01T00:00:01.000Z/PT60S").get
          //3 minutes in the future
          val now = new DateTime("2012-01-02T00:00:04.000Z")
          val adjusted = AdjustedForStartDate(schedule, now)
          // Ensure that this job runs today
          adjusted must beNone
        }

   }
}