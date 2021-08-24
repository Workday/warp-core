package com.workday.warp.logger

import com.workday.warp.config.WarpPropertyManager
import org.slf4j.{Logger, LoggerFactory}

trait WarpLogging {
  // Initializes properties for use in logger configuration
  WarpPropertyManager.version

  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}

class WarpLoggingWrapper extends WarpLogging