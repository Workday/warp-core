package com.workday.warp.utils

import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipException

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.apache.commons.io.IOUtils

/**
  * Created by sahil.shah on 5/9/16.
  */
class FileUtilsSpec extends WarpJUnitSpec {

  /**
   * Tests that a stream cannot be retrieved from a non zip file.
   */
  @UnitTest
  def testZipFileStreamCannotBeRetrievedFromNonZipFile(): Unit = {
    val filePath: String = getClass.getResource("/simpleTextFile.txt").getPath
    intercept[ZipException] {
      FileUtils.getStreamToLogFileInZip(filePath, "nonexistent")
    }
  }

  /**
   * Tests that a stream can be retrieved from a zip.
   */
  @UnitTest
  def testZipFileStreamRetrievedFromZipFile(): Unit = {
    val filePath: String = getClass.getResource("/simpleZip.zip").getPath

    val stream: InputStream = FileUtils.getStreamToLogFileInZip(filePath, "zipEntryFile.txt")

    IOUtils.toString(stream, Charset.defaultCharset) should include ("This is a zip entry")
  }

  /**
   * Tests retrieving for a nonexistent entry in an existing zip.
   */
  @UnitTest
  def testNonExistentZipEntryStreamCannotBeRetrievedFromZipFile(): Unit = {
    val zipEntryName: String = "DOES_NOT_EXIST"
    val filePath: String = getClass.getResource("/simpleZip.zip").getPath

    val thrown: Throwable = intercept[NullPointerException] {
      FileUtils.getStreamToLogFileInZip(filePath, "DOES_NOT_EXIST")
    }

    thrown.getMessage should be (s"Zip entry $zipEntryName not found in $filePath")
  }
}
