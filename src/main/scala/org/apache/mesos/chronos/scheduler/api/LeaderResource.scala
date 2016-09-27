package org.apache.mesos.chronos.scheduler.api

import com.google.inject.Inject
import javax.ws.rs.{GET, Path, Produces}
import javax.ws.rs.core.{Response, MediaType}
import org.apache.mesos.chronos.scheduler.jobs.JobScheduler


@Path(PathConstants.leaderPath)
class LeaderResource @Inject()(
  val jobScheduler: JobScheduler
) {

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def getLeader(): Response = {
    Response.ok(Map("leader" -> jobScheduler.getLeader)).build

  }
}
