package org.apache.mesos.chronos

import java.util.TimeZone
import org.specs2.specification.BeforeAfterEach

trait AlternativeTimezoneTest extends BeforeAfterEach {
   val defaultTimeZone = TimeZone.getDefault()
   def before {
     TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
   }

   def after {
     TimeZone.setDefault(defaultTimeZone)
   }
}