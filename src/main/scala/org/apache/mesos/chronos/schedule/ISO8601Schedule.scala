package org.apache.mesos.chronos.schedule

import java.util.TimeZone

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Period
import org.joda.time.format.ISODateTimeFormat

class ISO8601Schedule(val recurrences: Long, val start: DateTime, val period: Period) extends Schedule with Equals {

  def canEqual(other: Any) = {
    other.isInstanceOf[org.apache.mesos.chronos.schedule.ISO8601Schedule]
  }

  override def equals(other: Any) = {
    other match {
      case that: org.apache.mesos.chronos.schedule.ISO8601Schedule => that.canEqual(ISO8601Schedule.this) && recurrences == that.recurrences && start == that.start && period == that.period
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime * (prime + recurrences.hashCode) + start.hashCode) + period.hashCode
  }
  
  override def toString() = {
    "R%s/%s/%s".format(if(recurrences > 0) recurrences.toString else "", ISODateTimeFormat.dateTime.print(start), period.toString())
  }
}

object WithTimeZoneConsidered extends ((DateTime, String) => Option[DateTime]) {
  def apply(startTime: DateTime, timeZoneString: String): Option[DateTime] = {
    //if the timezone is invalid, this just returns UTC/GMT,
    //so we return None if the timeZoneString != GMT but the 
    //result returned by TimeZone.getTimeZone is.
    val zoneForString = TimeZone.getTimeZone(timeZoneString)
    if (zoneForString.getID == "GMT" && timeZoneString != "GMT") {
      return None
    } else {
      return Some(startTime.withZoneRetainFields(DateTimeZone.forTimeZone(zoneForString)))
    }
  }
}
    
    