package org.apache.mesos.chronos.scheduler.api

import org.apache.mesos.chronos.scheduler.jobs.JobScheduler
import org.mockito.Mockito.when
import org.specs2.mock._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class LeaderResourceSpec extends SpecificationWithJUnit with Mockito {
  "LeaderResource" should {
    "Return a 200" in {
        val fixture = new Fixture
        val resource = fixture.leaderResource()
        val response = resource.getLeader()
        response.getStatus should be equalTo(200)
     }
   }

   class Fixture {
        val mockScheduler = mock[JobScheduler]
        when(mockScheduler.getLeader).thenReturn("hostname")
        def leaderResource() = new LeaderResource(mockScheduler)
    }
}
