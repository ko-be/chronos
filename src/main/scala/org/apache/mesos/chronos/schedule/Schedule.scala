package org.apache.mesos.chronos.schedule

import org.joda.time.{DateTime, Period}
import org.joda.time.format.ISODateTimeFormat

sealed trait Schedule {
  val recurrences: Long
  val start: DateTime
}

final case class ISO8601Schedule(val recurrences: Long, val start: DateTime, val period: Period) extends Schedule {
  override def toString() = {
    "R%s/%s/%s".format(if(recurrences > 0) recurrences.toString else "", ISODateTimeFormat.dateTime.print(start), period.toString())
  }
}

final case class CronSchedule (val start: DateTime) extends Schedule {
  val recurrences = -1.toLong
}