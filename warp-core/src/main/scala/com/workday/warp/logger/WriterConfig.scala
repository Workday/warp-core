package com.workday.warp.logger

import ch.qos.logback.classic.Level


/**
  * Represents a [[Writer]] along with the level and format that should be used.
  *
  * Created by tomas.mccandless on 12/7/17.
  */
case class WriterConfig(fileName: String, packageName: String, level: Level = Level.INFO, format: String = WarpLogUtils.LOG_FORMAT)
