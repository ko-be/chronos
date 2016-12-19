package org.apache.mesos.chronos.schedule

import java.time.ZonedDateTime
import java.util.TimeZone

import com.cronutils.model.time.ExecutionTime
import com.cronutils.model.Cron
import org.joda.time.{DateTime, DateTimeZone, Period}
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

final case class CronSchedule (val start: DateTime, val cron: Cron) extends Schedule {
  val recurrences = -1.toLong

  override def toString() = {
    cron.asString()
  }
}

trait Nextable[T] {
  def next(current: T): Option[T]
}

object Nextable {
  implicit object NextableCron extends Nextable[CronSchedule] {
    def next(current: CronSchedule): Option[CronSchedule] = {
      val executionTime = ExecutionTime.forCron(current.cron)
      val dateForNextRun = executionTime.nextExecution(current.start.toGregorianCalendar().toZonedDateTime())
      val nextRun = new DateTime(dateForNextRun.toInstant().toEpochMilli(), DateTimeZone.forTimeZone(TimeZone.getTimeZone(dateForNextRun.getZone())))
      Some(new CronSchedule(nextRun, current.cron))
    }
  }
  implicit object NextableISO8601 extends Nextable[ISO8601Schedule] {
    def next(current: ISO8601Schedule): Option[ISO8601Schedule] = {
      current.recurrences match {
        case -1 => Some(new ISO8601Schedule(current.recurrences, current.start.plus(current.period), current.period))
        case 0 => None
        case _ => Some(new ISO8601Schedule(current.recurrences - 1, current.start.plus(current.period), current.period))
      }
    }
  }
}