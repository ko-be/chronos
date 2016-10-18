package org.apache.mesos.chronos.utils

import org.specs2.mock._
import org.specs2.mutable._
import org.mockito.Mockito._
import org.apache.mesos.chronos.scheduler.jobs.ScheduleBasedJob
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

class JobDeserializerSpec  extends SpecificationWithJUnit with Mockito {

  "deals with a null scheduleTimeZone field" in {
    val jsonParser = mock[JsonParser]
    val ctxt = mock[DeserializationContext]
    val mockCodec = mock[ObjectCodec]

    val mapper = new ObjectMapper
    val node = mapper.readValue("""{"name": "foo", "command": "bar", "epsilon": "PT20S", "runAsUser": "root", "scheduleTimeZone": null, "schedule": "R1/2016-10-18T15:39:11.352Z/PT24H"}""", classOf[JsonNode])

    when(mockCodec.readTree(any)).thenReturn(node)
    when(jsonParser.getCodec).thenReturn(mockCodec)

    val deserializer = new JobDeserializer
    val job = deserializer.deserialize(jsonParser, ctxt).asInstanceOf[ScheduleBasedJob]
    job.scheduleTimeZone must_== "UTC"
  }
}