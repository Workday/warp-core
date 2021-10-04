package com.workday.warp.logger

import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import com.workday.warp.config.CoreWarpProperty._
import org.slf4j.LoggerFactory
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

  // Defaulting to a log format that includes class, method, and line of message origin
  val LOG_FORMAT: String = "[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger.%method:%line - %msg%n"


  /**
   * Reads the value of wd.warp.log.level and other properties, attempt to parse as a valid logging level,
   * and set log level to that.
   */
  def setLogLevelFromWarpProperties(): Unit = {
    case class CustomLoggerLevels(id: String, level: Level)

    val consoleLevel: Level = this.parseLevel(WARP_CONSOLE_LOG_LEVEL.value, WARP_CONSOLE_LOG_LEVEL.defaultValue)

    val customLoggingLevels: List[CustomLoggerLevels] = List(
      CustomLoggerLevels("com.zaxxer.hikari", this.parseLevel(WARP_SLF4J_HIKARI_LOG_LEVEL.value, WARP_SLF4J_HIKARI_LOG_LEVEL.defaultValue)),
      CustomLoggerLevels("slick", this.parseLevel(WARP_SLF4J_SLICK_LOG_LEVEL.value, WARP_SLF4J_SLICK_LOG_LEVEL.defaultValue)),
      CustomLoggerLevels("org.flywaydb", this.parseLevel(WARP_SLF4J_FLYWAY_LOG_LEVEL.value, WARP_SLF4J_FLYWAY_LOG_LEVEL.defaultValue))
    )

    getLoggerContext.foreach { context =>
      val logEncoder: PatternLayoutEncoder = new PatternLayoutEncoder
      logEncoder.setContext(context)
      logEncoder.setPattern(LOG_FORMAT)
      logEncoder.start()

      // Configure ROOT logger
      val log: Logger = context.getLogger("ROOT")
      log.setAdditive(true)
      log.setLevel(consoleLevel)

      // Configure other loggers
      customLoggingLevels.foreach { loggingLevels =>
        context.getLogger(loggingLevels.id).setLevel(loggingLevels.level)
      }

      log.getAppender("console").asInstanceOf[ConsoleAppender[ILoggingEvent]].setEncoder(logEncoder)

      // Add all our new configured file writers
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


  /**
   * Retrieve the LoggerContext as a Try from the ILoggerFactory.
   *
   * Because we want to programmatically configure the logger, we need some methods only available via logback rather
   * than slf4j. This necessitates a cast, however if slf4j is bound at runtime to some concrete implementation other
   * than logback (e.g., slf4j-simple), this fails. Configuration will fallback to the default for that implementation.
   *
   * @return Try[LoggerContext]
   */
  private def getLoggerContext: Try[LoggerContext] = {
    Try(LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]).recoverWith { case e =>
        logger.warn("Could not cast to logback LoggerContext at runtime, logger will run with default configuration")
        Failure(e)
      }
  }


  /**
   * For log writer configuration, following pattern set out by:
   * https://akhikhl.wordpress.com/2013/07/11/programmatic-configuration-of-slf4jlogback/
   *
   * @param writerConfig - configuration properties for each new fileWriter
   */
  def addFileWriter(writerConfig: WriterConfig): Unit = {
    getLoggerContext.map { context =>
      // Create a logEncoder for the logFileAppender
      val logEncoder2: PatternLayoutEncoder = new PatternLayoutEncoder
      logEncoder2.setContext(context)
      logEncoder2.setPattern(LOG_FORMAT)
      logEncoder2.start()

      val logFileAppender: RollingFileAppender[ILoggingEvent] = new RollingFileAppender[ILoggingEvent]
      logFileAppender.setContext(context)
      logFileAppender.setName(writerConfig.fileName)
      logFileAppender.setEncoder(logEncoder2)
      logFileAppender.setAppend(true)
      logFileAppender.setFile(writerConfig.fileName)

      val logFilePolicy: TimeBasedRollingPolicy[ILoggingEvent] = new TimeBasedRollingPolicy[ILoggingEvent]
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
