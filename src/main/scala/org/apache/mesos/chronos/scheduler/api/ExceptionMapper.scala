package org.apache.mesos.chronos.scheduler.api

import java.util.logging.{Level, Logger}
import org.apache.mesos.chronos.utils.RequiredFieldMissingException

import javax.ws.rs.ext.ExceptionMapper;
import com.fasterxml.jackson.databind.JsonMappingException
import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.ext.Provider


@Provider
class RequiredFieldMissingExceptionMapper extends ExceptionMapper[RequiredFieldMissingException] {
  private val log = Logger.getLogger(getClass.getName)

  def toResponse(exception: RequiredFieldMissingException): Response = {
      log.info(exception.getMessage)
      return Response.status(422).entity(Map("errors" -> List(s"${exception.getMessage}"))).build
  }
}

@Provider
class JsonMappingExceptionMapper extends ExceptionMapper[JsonMappingException] {
  private val log = Logger.getLogger(getClass.getName)
  
  def toResponse(exception: JsonMappingException): Response = {
    log.info(exception.getMessage)
    return Response.status(422).entity(Map("errors" -> List(s"${exception.getMessage}"))).build
  }
}

@Provider
class GenericExceptionMapper extends ExceptionMapper[Exception] {
  private val log = Logger.getLogger(getClass.getName)
  
  def toResponse(exception: Exception): Response = {
    log.info(exception.getMessage)
    return Response.status(422).entity(exception.getMessage).build
  }
}