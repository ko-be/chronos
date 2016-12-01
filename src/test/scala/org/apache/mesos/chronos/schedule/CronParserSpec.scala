package org.apache.mesos.chronos.schedule

import org.specs2.mutable.SpecificationWithJUnit

import org.joda.time.{DateTime, DateTimeZone, Period}

class CronParserSpec extends SpecificationWithJUnit{
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
 }
}