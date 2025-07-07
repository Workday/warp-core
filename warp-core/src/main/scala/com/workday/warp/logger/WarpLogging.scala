package com.workday.warp.logger

import ch.qos.logback.classic.{Level, LoggerContext}
import com.workday.warp.config.WarpPropertyManager
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

trait WarpLogging {

  getLoggerContext match {
    case Success(context) =>
      context.getLogger("org.apache.commons.beanutils.converters").setLevel(Level.INFO)
    case Failure(_) =>
      logger.warn("No LoggerContext found, logging configuration will not be applied.")
  }

  // Initializes properties for use in logger configuration
  WarpPropertyManager.version

  /**
    * Retrieve the LoggerContext as a Try from the ILoggerFactory.
    *
    * Because we want to programmatically configure the logger, we need some methods only available via logback rather
    * than slf4j. This necessitates a cast, however if slf4j is bound at runtime to some concrete implementation other
    * than logback (e.g., slf4j-simple), this fails. Configuration will fallback to the default for that implementation.
    *
    * @return Try[LoggerContext]
    */
  def getLoggerContext: Try[LoggerContext] = {
    Try(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]).recoverWith { case e =>
      logger.warn("Could not cast to logback LoggerContext at runtime, logger will run with default configuration")
      Failure(e)
    }
  }

  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}

class WarpLoggingWrapper extends WarpLogging