package org.apache.mesos.chronos.schedule

import org.joda.time.{DateTime, DateTimeZone, Period}
import java.util.logging.Logger
import org.joda.time.format.ISOPeriodFormat
import java.util.TimeZone

object ISO8601Parser {
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
      val start: Option[DateTime] = startStr match { 
        case "" => Some(DateTime.now(DateTimeZone.UTC)) 
        case _ => WithTimeZoneConsidered(DateTime.parse(startStr), timeZoneString)
      }
      val period: Period = ISOPeriodFormat.standard.parsePeriod(periodStr)
      start.map(dateTime => new ISO8601Schedule(recurrences, dateTime, period))      
  }
}