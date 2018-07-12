package com.workday.warp.logger

import org.pmw.tinylog.Level
import org.pmw.tinylog.writers.Writer

/**
  * Represents a [[Writer]] along with the level and format that should be used.
  *
  * Created by tomas.mccandless on 12/7/17.
  */
case class WriterConfig(writer: Writer, level: Level = Level.INFO, format: String = WarpLogUtils.LOG_FORMAT)
