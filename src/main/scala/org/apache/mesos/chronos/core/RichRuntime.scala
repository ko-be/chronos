package org.apache.mesos.chronos.core

import java.util.logging.Logger
import java.util.{ Timer, TimerTask }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, _ }

import akka.Done

class RichRuntime {

  private[this] val logger = Logger.getLogger(getClass.getName)

  def asyncExit(
    exitCode: Int = RichRuntime.FatalErrorSignal,
    waitForExit: FiniteDuration = 10.seconds)(implicit ec: ExecutionContext): Future[Done] = {
    val timer = new Timer()
    val promise = Promise[Done]()
    timer.schedule(new TimerTask {
      override def run(): Unit = {
        logger.info("Halting JVM")
        promise.success(Done)
        Runtime.getRuntime.halt(exitCode)
      }
    }, waitForExit.toMillis)
    Future(sys.exit(exitCode))
    promise.future
  }
}

object RichRuntime {
  val FatalErrorSignal = 137
  val DefaultExitDelay = 10.seconds

  def apply(): RichRuntime = {
    new RichRuntime
  }
}