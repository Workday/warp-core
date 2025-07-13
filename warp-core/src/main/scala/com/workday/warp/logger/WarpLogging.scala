package com.workday.warp.logger

import com.workday.warp.config.WarpPropertyManager

trait WarpLogging extends LoggerInit {

  // Initializes properties for use in logger configuration
  WarpPropertyManager.version
}

class WarpLoggingWrapper extends WarpLogging