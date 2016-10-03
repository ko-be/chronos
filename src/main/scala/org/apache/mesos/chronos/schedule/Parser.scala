package org.apache.mesos.chronos.schedule

import org.joda.time.{DateTime, DateTimeZone, Period}
import java.util.logging.Logger
import org.joda.time.format.ISOPeriodFormat
import java.util.TimeZone

trait Parser {
  def apply(input: String, timeZoneStr: String = ""): Option[Schedule]
}

object ISO8601Parser extends Parser{
  private val log = Logger.getLogger(getClass.getName)
  val iso8601ExpressionRegex = """(R[0-9]*)/(.*)/(P.*)""".r

  def apply(input: String, timeZoneStr: String = ""): Option[ISO8601Schedule] = {
      val iso8601ExpressionRegex(repeatStr, startStr, periodStr) = input
      val timeZoneString = timeZoneStr match {
        case "" => "UTC"
        case _ => timeZoneStr
      }
      val recurrences: Long = repeatStr.length match {
        case 1 => -1L
        case _ => repeatStr.substring(1).toLong
      }
      //TODO: deal with no period provided?
      val period: Period = ISOPeriodFormat.standard.parsePeriod(periodStr)
      val start: DateTime = startStr match {
        case "" => DateTime.now(DateTimeZone.UTC)
        case _ => DateTime.parse(startStr)
      }
      WithTimeZoneConsidered(new ISO8601Schedule(recurrences, start, period), timeZoneString)
  }
}

object ParserForSchedule {
  def apply(input: String): Option[Parser] = {
    Some(ISO8601Parser)
  }
}