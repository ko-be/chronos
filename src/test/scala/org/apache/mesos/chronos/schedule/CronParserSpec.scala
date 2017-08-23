package org.apache.mesos.chronos.schedule

import java.time.{ZonedDateTime, ZoneId}

import org.apache.mesos.chronos.AlternativeTimezoneTest
import org.joda.time.{DateTime, DateTimeZone, Period}
import org.specs2.mutable.SpecificationWithJUnit

class CronParserSpec extends SpecificationWithJUnit {
 "Cron Parser should" in {
   "Parse Unix cron specifications to a next run schedule" in {
     val result = CronParser("01 * * * *")
     result must beSome
   }
   "Create an accurate start date for the schedule it produces" in {
     //this is racy - this *could* fail if you the time ticks
     //from 23:59:59 to 00:00:00 in between the parsing and
     //the running of the test.
     val result = CronParser("0 0 * * *")
     result.get.start must_== DateTime.now().plusDays( 1 ).withTimeAtStartOfDay();
   }
   "Return None when you provide a bad cron schedule" in {
     val result = CronParser("HI")
     result must beNone
   }
   "Creates a start time object with the correct time zone" in {
     val result = CronParser("0 18 * * *", "America/Los_Angeles")
     result.get.start.hourOfDay().get must_== 18
     result.get.start.getZone.getID must_== "America/Los_Angeles"
   }
 }
}

class CronParserAltTZSpec extends SpecificationWithJUnit with AlternativeTimezoneTest {
  "In US/Pacific Cron Parser should" in {
   "Return a schedule with the correct TZ" in {
     val result = CronParser("0 18 * * *")
     result.get.start.hourOfDay().get must_== 18
     result.get.start.getZone.getID == "UTC"
   }
  }
}