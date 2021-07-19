package com.workday.warp.logger

import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import com.workday.warp.config.CoreWarpProperty._
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender

/**
 * Provides utility methods to set log level and format at runtime based on warp properties.
 *
 * Created by tomas.mccandless on 10/19/15.
 */
object WarpLogUtils extends WarpLogging {

  // set the format to include class and method
  val LOG_FORMAT: String = "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger.%method:%line - %msg%n"

  /**
   * Reads the value of wd.warp.log.level, attempt to parse as a valid logging level,
   * and set log level of our Logger to that level. If tinylog.level is set as a system property, we'll use that.
   */
  def setLogLevelFromWarpProperties(): Unit = {
    val consoleLevel: Level = this.parseLevel(WARP_CONSOLE_LOG_LEVEL.value, WARP_CONSOLE_LOG_LEVEL.defaultValue)
    val hikariLevel: Level = this.parseLevel(WARP_SLF4J_HIKARI_LOG_LEVEL.value, WARP_SLF4J_HIKARI_LOG_LEVEL.defaultValue)
    val slickLevel: Level = this.parseLevel(WARP_SLF4J_SLICK_LOG_LEVEL.value, WARP_SLF4J_SLICK_LOG_LEVEL.defaultValue)
    val flywayLevel: Level = this.parseLevel(WARP_SLF4J_FLYWAY_LOG_LEVEL.value, WARP_SLF4J_FLYWAY_LOG_LEVEL.defaultValue)

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    // via https://akhikhl.wordpress.com/2013/07/11/programmatic-configuration-of-slf4jlogback/
    val logEncoder: PatternLayoutEncoder = new PatternLayoutEncoder
    logEncoder.setContext(context)
    logEncoder.setPattern(LOG_FORMAT)
    logEncoder.start()

    val log: Logger = context.getLogger("ROOT")
    log.setAdditive(true)
    log.setLevel(consoleLevel)

    val hikariLog: Logger = context.getLogger("com.zaxxer.hikari")
    hikariLog.setLevel(hikariLevel)

    val slickLog: Logger = context.getLogger("slick")
    slickLog.setLevel(slickLevel)

    val flywayLog: Logger = context.getLogger("org.flywaydb")
    flywayLog.setLevel(flywayLevel)

    val warpLog: Logger = context.getLogger("com.workday.warp")
    warpLog.setLevel(consoleLevel)

    log.getAppender("console").asInstanceOf[ConsoleAppender[ILoggingEvent]].setEncoder(logEncoder)
  }


  /**
    * Tries to parse `level` into a [[Level]]. Tries to parse `default` if `level` is not valid. If neither is valid,
    * uses [[Level.DEBUG]]
    *
    * @param level [[String]] to attempt to parse into a [[Level]].
    * @param default [[Option[String]]] to attempt to parse if `level` is not valid. Default value of None.
    * @return a [[Level]] parsed from `level` or `default`, or [[Level.DEBUG]] if neither is valid.
    */
  private[logger] def parseLevel(level: String, default: Option[String] = None): Level = {
    // ch.qos.logback.classic.Level.valueOf defaults to Level.DEBUG
    Level.toLevel(level, Level valueOf default.getOrElse(""))
  }
}
