package org.apache.mesos.chronos.core

import scala.concurrent.duration._
import scala.concurrent._
import org.specs2.mutable.SpecificationWithJUnit

import akka.actor.ActorSystem
import akka.actor.Scheduler
import org.apache.mesos.chronos.ExitDisabledTest
import org.joda.time.{DateTime, DateTimeZone, Period}
import org.joda.time.format.{ISODateTimeFormat, ISOPeriodFormat}

class RichRuntimeSpec extends SpecificationWithJUnit with ExitDisabledTest {

  "RichRuntime.asyncExit" should {
    "call exit" in {
      import ExecutionContext.Implicits.global
      implicit val system = ActorSystem()
      implicit val scheduler = system.scheduler
      RichRuntime().asyncExit(123)
      Await.result(exitCalled(123), Duration.Inf) must_== true
    }
  }

}