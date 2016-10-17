package org.apache.mesos.chronos.scheduler.jobs

import org.joda.time._
import org.apache.mesos.chronos.schedule.ISO8601Parser
import org.apache.mesos.chronos.schedule.ISO8601Schedule
import org.specs2.mutable._

class ScheduleStreamSpec extends SpecificationWithJUnit {

  "ScheduleStream" should {
    "return the schedule it was constructed with at its head" in {
      val schedule = ISO8601Parser("R3/2012-01-01T00:00:00.000Z/P1D").get
      val stream = new ScheduleStream(schedule, null, "")
      stream.head must_==(schedule, null, "")
    }
    
    "return a new schedule with the recurrences and start time of the schedule modified appropriately at its tail" in {
      val schedule = ISO8601Parser("R3/2012-01-01T00:00:00.000Z/P1D").get
      val stream = new ScheduleStream(schedule, "", "")
      stream.tail.get.head must_== (new ISO8601Schedule(2, schedule.start.plus(schedule.period), schedule.period), "", "")
    }
    
    "return an empty when the recurrences for a job at its head is 0" in {
      val schedule = ISO8601Parser("R0/2012-01-01T00:00:00.000Z/P1D").get
      val stream = new ScheduleStream(schedule, "myjob", "GMT")
      stream.tail must beNone
    }
    
  }

}
