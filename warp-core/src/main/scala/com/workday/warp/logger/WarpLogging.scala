package com.workday.warp.logger

import com.typesafe.scalalogging.LazyLogging
import com.workday.warp.config.WarpPropertyManager

trait WarpLogging extends LazyLogging {
  // Initializes properties for use in logger configuration
  WarpPropertyManager.version
}