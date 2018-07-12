package com.workday.warp.common.utils

import java.io.InputStream
import java.util.zip.ZipException

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.apache.commons.io.IOUtils
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by sahil.shah on 5/9/16.
  */
class FileUtilsSpec extends WarpJUnitSpec {

  /**
   * Tests that a stream cannot be retrieved from a non zip file.
   */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testZipFileStreamCannotBeRetrievedFromNonZipFile(): Unit = {
    val filePath: String = getClass.getResource("/simpleTextFile.txt").getPath
    intercept[ZipException] {
      FileUtils.getStreamToLogFileInZip(filePath, "nonexistent")
    }
  }

  /**
   * Tests that a stream can be retrieved from a zip.
   */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testZipFileStreamRetrievedFromZipFile(): Unit = {
    val filePath: String = getClass.getResource("/simpleZip.zip").getPath

    val stream: InputStream = FileUtils.getStreamToLogFileInZip(filePath, "zipEntryFile.txt")

    IOUtils.toString(stream) should include ("This is a zip entry")
  }

  /**
   * Tests retrieving for a nonexistent entry in an existing zip.
   */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testNonExistentZipEntryStreamCannotBeRetrievedFromZipFile(): Unit = {
    val zipEntryName: String = "DOES_NOT_EXIST"
    val filePath: String = getClass.getResource("/simpleZip.zip").getPath

    val thrown: Throwable = intercept[NullPointerException] {
      FileUtils.getStreamToLogFileInZip(filePath, "DOES_NOT_EXIST")
    }

    thrown.getMessage should be (s"Zip entry $zipEntryName not found in $filePath")
  }
}
