package com.workday.warp.common.heaphistogram

import java.io.InputStream
import java.lang.management.ManagementFactory

import com.sun.tools.attach.VirtualMachine
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.persistence.influxdb.InfluxDBClient
import sun.tools.attach.HotSpotVirtualMachine

import scala.util.matching.Regex

/**
  * Trait that can attach to the running java process and collects the HeapHistogram
  *
  * Created by vignesh.kalidas on 4/5/18.
  */
trait HistogramIoLike extends InfluxDBClient {
  /**
    * Collects the heap histogram and parses it into a List of HeapHistogramEntry
    *
    * @return Collection of [[HeapHistogramEntry]]
    */
  def getHeapHistogram: Seq[HeapHistogramEntry] = {
    val inputStream: InputStream = heapHisto()
    val heapHistogramString: String = scala.io.Source.fromInputStream(inputStream).mkString
    val heapHistogramEntries: Array[HeapHistogramEntry] = parseHeapHistogram(heapHistogramString)

    heapHistogramEntries.toList
  }

  /**
    * Parses a string represenation of the heap to construct an array of HeapHistogramEntry
    *
    * @param heapHistogram a string representation of the heap state.
    * @return an Array of [[HeapHistogramEntry]]
    */
  private def parseHeapHistogram(heapHistogram: String): Array[HeapHistogramEntry] = {
    val lines: Array[String] = heapHistogram.split("\n")

    // scalastyle:off two.spaces
    /*
     * The following is a line from the heap histogram:
     * num     #instances         #bytes  class name
     *----------------------------------------------
     *   1:          6367        9207408  [B
     *   2:         24949        3361640  [C
     *   3:          6520         709112  java.lang.Class
     *   4:          6937         610456  java.lang.reflect.Method
     */
    // scalastyle:on two.spaces
    val pattern: Regex = "\\s*\\d+:\\s+(\\d+)\\s+(\\d+)\\s+(.*)".r

    for {
      line <- lines
      matches <- pattern.findAllIn(line).matchData
    } yield new HeapHistogramEntry(matches.group(3), matches.group(1).toLong, matches.group(2).toLong)
  }

  /**
    * Collects and persists heap histograms.
    *
    * @param testId the name of the WARP test
    */
  protected def collectAndStoreHistogram(testId: String): Unit = {
    val histo: HeapHistogramFilter = HeapHistogramFilter(this.getHeapHistogram)
    val dbName: String = WARP_INFLUXDB_HEAPHISTO_DB.value
    val persistSeriesName: String = WARP_INFLUXDB_HEAPHISTO_SERIES.value
    val plotSeriesName: String = WARP_INFLUXDB_HEAPHISTO_PLOT_SERIES.value

    this.persistHeapHistogram(histo.persistedHistogram, dbName, persistSeriesName, testId)
    this.persistHeapHistogram(histo.plottedHistogram, dbName, plotSeriesName, testId)
  }

  /**
    * Calls the heapHisto method on the HotSpotVirtualMachine with a default parameter based on the WARP property
    *
    * @param live "-all" or "-live" but can be customized by changing the WARP property or passing in another value
    * @return InputStream of the heap histogram
    */
  protected def heapHisto(live: String = if (WARP_HEAPHISTO_INVOKE_GC.value.toBoolean) "-live" else "-all"): InputStream = {
    HistogramIoLike.vm.heapHisto(live)
  }
}

/**
  * Companion object for the HistogramIoLike trait. Upon initialization, this grabs the PID of the java process and
  * attaches to it. Detachment happens at shutdown via the added hook.
  *
  * Note: In order to run this, you need tools.jar in the classpath; the simplest way to accomplish this is to run under
  * the jdk instead of jre
  */
object HistogramIoLike {
  val pid: String = ManagementFactory.getRuntimeMXBean.getName.split("@").head
  val vm: HotSpotVirtualMachine = VirtualMachine.attach(pid).asInstanceOf[HotSpotVirtualMachine]

  // The Thread constructor requires a Runnable
  Runtime.getRuntime.addShutdownHook(new Thread() { override def run(): Unit = vm.detach() })
}
