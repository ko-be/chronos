package org.apache.mesos.chronos.schedule

import org.specs2.mutable.SpecificationWithJUnit
import org.joda.time.{DateTime, DateTimeZone, Period}
import org.joda.time.format.{ISODateTimeFormat, ISOPeriodFormat}

class ISO8601ParserSpec extends SpecificationWithJUnit {
 "ISO8601Parser" should {
   "Parse a standard ISO8601 expression" in {
     val result = ISO8601Parser("R5/2012-01-02T00:00:00.000Z/P1D").get
     val expectedPeriod = ISOPeriodFormat.standard.parsePeriod("P1D")
     val expectedStart = DateTime.parse("2012-01-02T00:00:00.000Z")

     result.recurrences must_== 5
     result.period must_== expectedPeriod
     result.start must_== expectedStart
   }

   "Consider no repeat string as unlimited (-1)" in {
     val result = ISO8601Parser("R/2012-01-02T00:00:00.000Z/P1D").get
     result.recurrences must_== -1
   }

   "Consider the provided time zone string with higher precedence than that in the datestring" in {
     val timeZoneString = "America/Los_Angeles"
     val result = ISO8601Parser("R/2012-01-02T00:00:00.000Z/P1D", timeZoneString).get
     val utcDate = DateTime.parse("2012-01-02T00:00:00.000Z")
     result.start.isAfter(utcDate) must beTrue
   }

   "Returns None for an invalid timezone string" in {
     val result = ISO8601Parser("R/2012-01-02T00:00:00.000Z/P1D", "foo")
     result must beNone
   }
 }
}