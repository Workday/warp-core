package com.workday.warp.logger

import ch.qos.logback.classic.{Level, LoggerContext}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Try}

/**
 * Preconfiguration of the logger context to set log levels and prevent spurious log entries.
 * This trait is intended to quiesce debug entries from transitive dependencies that occur in the early stages of initialization
 * before we have had a chance to configure the logger properly.
 */
trait LoggerInit {

  // early configuration of this log level to
  // Prevent spurious log entries from commons-beanutils library, which is a transitive dependency.
  getLoggerContext.foreach(_.getLogger("org.apache.commons.beanutils.converters").setLevel(Level.INFO))

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
