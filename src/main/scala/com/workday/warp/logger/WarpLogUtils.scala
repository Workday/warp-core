package com.workday.warp.logger

import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.inject.WarpGuicer
import org.pmw.tinylog.writers.{ConsoleWriter, FileWriter}
import org.pmw.tinylog.{Configurator, Level, Logger}

import scala.util.{Failure, Success, Try}

/**
 * Provides utility methods to set log level and format at runtime based on warp properties.
 *
 * Created by tomas.mccandless on 10/19/15.
 */
object WarpLogUtils {

  // set the format to include class and method
  val LOG_FORMAT: String = "{date} {level}: {class_name}.{method}:{line} \t{message}"


  /**
   * Reads the value of wd.warp.log.level, attempt to parse as a valid logging level,
   * and set log level of our Logger to that level. If tinylog.level is set as a system property, we'll use that.
   */
  def setLogLevelFromWarpProperties(): Unit = {
    val consoleLogLevel: String = Option(System.getProperty("tinylog.level")) match {
      // try to use the system property
      case Some(level) =>
        Logger.debug(s"The value of the system property tinylog.level ($level) will be used.")
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
    val consoleLevel: Level = this.parseLevel(consoleLogLevel, WARP_CONSOLE_LOG_LEVEL.defaultValue)
    val fileLevel: Level = this.parseLevel(fileLogLevel, WARP_FILE_LOG_LEVEL.defaultValue)

    Logger.info(s"WarpLogUtils: setting log levels: console=$consoleLevel, file=$fileLevel")

    val buffer: Boolean = true
    val append: Boolean = true

    val newConfig = Configurator.currentConfig
        // no limit on stack traces
        .maxStackTraceElements(-1)
        // write log entries to console
        .writer(new ConsoleWriter, consoleLevel, this.LOG_FORMAT)
        // write log entries to a file
        .addWriter(new FileWriter(WARP_LOG_FILE.value, buffer, append), fileLevel, this.LOG_FORMAT)

    // add all our new configured writers
    val writers: Seq[WriterConfig] = WarpGuicer.baseModule.getExtraWriters
    writers foreach { writer: WriterConfig => newConfig.addWriter(writer.writer, writer.level, writer.format)}

    val slickLevel: Level = this.parseLevel(WARP_SLF4J_SLICK_LOG_LEVEL.value, WARP_SLF4J_SLICK_LOG_LEVEL.defaultValue)
    val hikariLevel: Level = this.parseLevel(WARP_SLF4J_HIKARI_LOG_LEVEL.value, WARP_SLF4J_HIKARI_LOG_LEVEL.defaultValue)
    val flywayLevel: Level = this.parseLevel(WARP_SLF4J_FLYWAY_LOG_LEVEL.value, WARP_SLF4J_FLYWAY_LOG_LEVEL.defaultValue)
    // set custom logging levels
    newConfig.writingThread(true)
      .level("slick", slickLevel)
      .level("com.zaxxer.hikari", hikariLevel)
      .level("org.flywaydb.core.internal.util.logging.slf4j", flywayLevel)
      .activate()
  }


  /**
    * Tries to parse `level` into a [[Level]]. Tries to parse `default` if `level` is not valid. If neither is valid,
    * uses [[Level.INFO]]
    *
    * @param level [[String]] to attempt to parse into a [[Level]].
    * @param default [[String]] to attempt to parse if `level` is not valid.
    * @return a [[Level]] parsed from `level` or `default`, or [[Level.INFO]] if neither is valid.
    */
  private[logger] def parseLevel(level: String, default: String): Level = {
    Try { Level valueOf level.toUpperCase } match {
      case Success(logLevel: Level) =>
        logLevel
      case Failure(exception) =>
        Logger.error(exception, s"unable to parse $level as a valid logging level, using $default")
        Try { Level valueOf default.toUpperCase } getOrElse Level.INFO
    }
  }
}
