package org.apache.mesos.chronos.schedule

import scala.util.{Try, Success, Failure}
import org.joda.time.{DateTime, DateTimeZone, Period}
import java.util.logging.Logger
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.DateTime
import java.util.TimeZone
import java.time.ZonedDateTime
import com.cronutils.parser.{CronParser => CronUtilsParser}
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.CronType
import com.cronutils.model.time.ExecutionTime

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

object CronParser extends Parser {
  private val log = Logger.getLogger(getClass.getName)
  def apply(input: String, timeZoneStr: String = ""): Option[CronSchedule] = {
    val unixCronParser =  new CronUtilsParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))
    val cron = Try(unixCronParser.parse(input))
    val cronExpression = Try(unixCronParser.parse(input)).map {
      parsed => {
        ExecutionTime.forCron(parsed)
      }
    }

    val dateForNextRun = cronExpression.map {
      executionTime => executionTime.nextExecution(ZonedDateTime.now)
    }.map {
      //there is no timezone to take into account here; we just want the next run for the
      //cron job.
      zdt => new DateTime(zdt.toInstant().toEpochMilli(), DateTimeZone.forID("UTC"))
    }
    dateForNextRun match {
      case Success(dateForNextRun) => Some(new CronSchedule(dateForNextRun, cron.get, cronExpression.get))
      case Failure(e) => {
        None
      }
    }
  }
}

object ParserForSchedule {
  val log = Logger.getLogger(getClass.getName)
  def apply(input: String): Option[Parser] = {
    if(ISO8601Parser.iso8601ExpressionRegex.pattern.matcher(input).matches()) {
      log.info("ISO801 Parser!")
      Some(ISO8601Parser)
    } else {
      log.info("CRON Parser!")
      Some(CronParser)
    }
  }
}