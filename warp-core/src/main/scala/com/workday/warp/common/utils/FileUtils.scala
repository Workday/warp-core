package com.workday.warp.common.utils

import java.io.{File, InputStream}
import java.util.zip.{ZipEntry, ZipFile}

/**
  * Utilities for interacting with files.
  *
  * Created by sahil.shah on 5/9/16.
  */
object FileUtils {

  /**
    * Returns an InputStream to an entry in a zip file without unpacking it.
    *
    * @param zipFilePath path to the actual zip file
    * @param zipFileEntry name of the entry in the zip
    * @return InputStream to the entry in the zip
    */
  def getStreamToLogFileInZip(zipFilePath: String, zipFileEntry: String): InputStream = {
    val allLogsZip: ZipFile = new ZipFile(new File(zipFilePath))
    val entry: Option[ZipEntry] = Option(allLogsZip.getEntry(zipFileEntry))

    if (entry.isEmpty) {
      throw new NullPointerException(s"Zip entry $zipFileEntry not found in $zipFilePath")
    }

    allLogsZip.getInputStream(allLogsZip.getEntry(zipFileEntry))
  }
}
