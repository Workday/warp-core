package com.workday.warp.logger

import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import com.workday.warp.config.CoreWarpProperty._
import org.slf4j.{ILoggerFactory, LoggerFactory}
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.{RollingFileAppender, TimeBasedRollingPolicy}
import com.workday.warp.inject.WarpGuicer

import scala.util.{Failure, Try}

/**
 * Provides utility methods to set log level and format at runtime based on warp properties.
 *
 * Created by tomas.mccandless on 10/19/15.
 */
object WarpLogUtils extends WarpLogging {

  // set the format to include class and method
  val LOG_FORMAT: String = "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger.%method:%line - %msg%n"

  /**
   * TODO
   *
   * Reads the value of wd.warp.log.level, attempt to parse as a valid logging level,
   * and set log level of our Logger to that level. If tinylog.level is set as a system property, we'll use that.
   * Following pattern set out by https://akhikhl.wordpress.com/2013/07/11/programmatic-configuration-of-slf4jlogback/
   */
  def setLogLevelFromWarpProperties(): Unit = {
    val consoleLevel: Level = this.parseLevel(WARP_CONSOLE_LOG_LEVEL.value, WARP_CONSOLE_LOG_LEVEL.defaultValue)
    val hikariLevel: Level = this.parseLevel(WARP_SLF4J_HIKARI_LOG_LEVEL.value, WARP_SLF4J_HIKARI_LOG_LEVEL.defaultValue)
    val slickLevel: Level = this.parseLevel(WARP_SLF4J_SLICK_LOG_LEVEL.value, WARP_SLF4J_SLICK_LOG_LEVEL.defaultValue)
    val flywayLevel: Level = this.parseLevel(WARP_SLF4J_FLYWAY_LOG_LEVEL.value, WARP_SLF4J_FLYWAY_LOG_LEVEL.defaultValue)

    getLoggerContext.map { context =>
      val logEncoder: PatternLayoutEncoder = new PatternLayoutEncoder
      logEncoder.setContext(context)
      logEncoder.setPattern(LOG_FORMAT)
      logEncoder.start()

      // Configure ROOT logger
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

      // Add all our new configured writers
      val writers: Seq[WriterConfig] = WarpGuicer.baseModule.getExtraWriters
      writers foreach addFileWriter
    }
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

  private def getLoggerContext: Try[LoggerContext] = {
    Try(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]).recoverWith { case e =>
        logger.error("Could not cast to LoggerContext at runtime, logger may not be configured", e)
        Failure(e)
      }
  }

  def addFileWriter(writerConfig: WriterConfig): Unit = {
    getLoggerContext.map { context =>
      // Create a logEncoder for the logFileAppender
      val logEncoder2 = new PatternLayoutEncoder
      logEncoder2.setContext(context)
      logEncoder2.setPattern(LOG_FORMAT)
      logEncoder2.start()

      val logFileAppender = new RollingFileAppender[ILoggingEvent]
      logFileAppender.setContext(context)
      logFileAppender.setName(writerConfig.fileName)
      logFileAppender.setEncoder(logEncoder2)
      logFileAppender.setAppend(true)
      logFileAppender.setFile(writerConfig.fileName)

      val logFilePolicy = new TimeBasedRollingPolicy[ILoggingEvent]
      logFilePolicy.setContext(context)
      logFilePolicy.setParent(logFileAppender)
      logFilePolicy.setFileNamePattern(s"${writerConfig.fileName}-%d{yyyy-MM-dd_HH}.log")
      logFilePolicy.setMaxHistory(7)
      logFilePolicy.start()

      logFileAppender.setRollingPolicy(logFilePolicy)
      logFileAppender.start()

      val log: Logger = context.getLogger(writerConfig.packageName)
      log.addAppender(logFileAppender)
      log.setLevel(writerConfig.level)
    }

  }
}
