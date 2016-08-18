package org.apache.mesos.chronos.scheduler.api

import mesosphere.mesos.util.FrameworkIdUtil
import org.apache.curator.framework.recipes.leader.LeaderLatch
import org.apache.mesos.Protos.FrameworkID
import org.apache.mesos.chronos.ChronosTestHelper
import org.apache.mesos.chronos.scheduler.config.SchedulerConfiguration
import org.mockito.Mockito.when
import org.specs2.mock._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class InfoResourceSpec extends SpecificationWithJUnit with Mockito {
  "InfoResource" should {
    "Return the expected keys in the JSON" in {
        val fixture = new Fixture
        val resource = fixture.infoResource()
        val response = resource.info()
        val content = response.getEntity()
        response.getStatus should be equalTo(200)
     }
   }

   class Fixture {
        val frameworkId = FrameworkID.newBuilder
            .setValue("some_id")
            .build()
        val schedulerConfiguration = ChronosTestHelper.makeConfig()
        val frameworkIdUtil = mock[FrameworkIdUtil]
        implicit val timeout =  mesosphere.util.BackToTheFuture.Implicits.defaultTimeout
        implicit val context = scala.concurrent.ExecutionContext.Implicits.global
        when(frameworkIdUtil.fetch(context, timeout)).thenReturn(Some(frameworkId))
        val leaderLatch = mock[LeaderLatch]
        def infoResource() = new InfoResource(schedulerConfiguration, frameworkIdUtil, leaderLatch)
    }
}
