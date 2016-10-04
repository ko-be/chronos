package org.apache.mesos.chronos.schedule

import org.joda.time.DateTime

case class CronSchedule (val start: DateTime) extends Schedule {
  val recurrences = -1.toLong
}
