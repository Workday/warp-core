package com.workday.warp.logger

import ch.qos.logback.classic.LoggerContext
import com.workday.warp.config.CoreWarpProperty._
import org.slf4j.event.Level
import org.pmw.tinylog.writers.{ConsoleWriter, FileWriter}
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import scala.util.{Failure, Success, Try}

/**
 * Provides utility methods to set log level and format at runtime based on warp properties.
 *
 * Created by tomas.mccandless on 10/19/15.
 */
object WarpLogUtils extends WarpLogging {

  // set the format to include class and method
  val LOG_FORMAT: String = "{date} {level}: {class_name}.{method}:{line} \t{message}"


  // TODO: consider another logging property/name, but keep this one around (give new one precedence)
  /**
   * Reads the value of wd.warp.log.level, attempt to parse as a valid logging level,
   * and set log level of our Logger to that level. If tinylog.level is set as a system property, we'll use that.
   */
  def setLogLevelFromWarpProperties(): Unit = {
    val consoleLogLevel: String = Option(System.getProperty("tinylog.level")) match {
      // try to use the system property
      case Some(level) =>
        logger.debug(s"The value of the system property tinylog.level ($level) will be used.")
        level
      // otherwise fall back on the warp property
      case None =>
        WARP_CONSOLE_LOG_LEVEL.value
    }

    val fileLogLevel: String = WARP_FILE_LOG_LEVEL.value

    this.configureLogger(consoleLogLevel, fileLogLevel)
  }


  /**
    * Sets logging level of the Logger.
    *
    * Logs a warning message if `consoleLogLevel` or `fileLogLevel` cannot be parsed as a valid logging level.
    * Configures two writers, a [[ConsoleWriter]], and a [[FileWriter]]. The log level for the console writer is
    * [[WARP_CONSOLE_LOG_LEVEL]], while the log level for the file writer is [[WARP_FILE_LOG_LEVEL]]. The log file location
    * is [[WARP_LOG_FILE]].
    *
    * @param consoleLogLevel the level at which to set logging for console output.
    * @param fileLogLevel the level at which to set logging for file output.
    */
  private[this] def configureLogger(consoleLogLevel: String, fileLogLevel: String): Unit = {
    // default to INFO for console log, TRACE for file log
//    val consoleLevel: Level = this.parseLevel(consoleLogLevel, WARP_CONSOLE_LOG_LEVEL.defaultValue)
//    val fileLevel: Level = this.parseLevel(fileLogLevel, WARP_FILE_LOG_LEVEL.defaultValue)

    val context: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    val logEncoder: PatternLayoutEncoder = new PatternLayoutEncoder
    logEncoder.setContext(context)
    logEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36}:%line - %msg%n")
    logEncoder.start()

    val logConsoleAppender: ConsoleAppender[_] = new ConsoleAppender[_]
    logConsoleAppender.setContext(context)
    logConsoleAppender.setName("console")
    logConsoleAppender.setEncoder(logEncoder)
    logConsoleAppender.start()


  }


  /**
    * Tries to parse `level` into a [[Level]]. Tries to parse `default` if `level` is not valid. If neither is valid,
    * uses [[Level.INFO]]
    *
    * @param level [[String]] to attempt to parse into a [[Level]].
    * @param default [[Option[String]]] to attempt to parse if `level` is not valid.
    * @return a [[Level]] parsed from `level` or `default`, or [[Level.INFO]] if neither is valid.
    */
  private[logger] def parseLevel(level: String, default: Option[String]): Level = {
    Try { Level valueOf level.toUpperCase } match {
      case Success(logLevel: Level) =>
        logLevel
      case Failure(exception) =>
        logger.error(s"unable to parse $level as a valid logging level, using $default", exception)

        val maybeLevel: Option[Level] = for {
          d <- default
          l <- Try(Level.valueOf(d)).toOption
        } yield l

        maybeLevel getOrElse Level.INFO
    }
  }
}
