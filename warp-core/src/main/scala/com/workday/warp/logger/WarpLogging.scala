package com.workday.warp.logger

import com.workday.warp.config.WarpPropertyManager

/**
 * Trait that initializes the logger configuration for Warp.
 * It sets up the logger context and initializes properties for use in logger configuration.
 *
 * This trait is intended to be mixed into classes that require logging capabilities.
 */
trait WarpLogging extends LoggerInit {

  // Initializes properties for use in logger configuration
  WarpPropertyManager.version
}

class WarpLoggingWrapper extends WarpLogging