package com.workday.warp.logger

import com.workday.warp.config.WarpPropertyManager
import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait that initializes the logger configuration for Warp.
 * It sets up the logger context and initializes properties for use in logger configuration.
 *
 * This trait is intended to be mixed into classes that require logging capabilities.
 */
trait WarpLogging {

  LoggerInit.init()

  // Initializes properties for use in logger configuration
  WarpPropertyManager.version

  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}

class WarpLoggingWrapper extends WarpLogging