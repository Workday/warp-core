package com.workday.warp.utils

import java.sql
import java.text.SimpleDateFormat
import java.util.Calendar

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.utils.Implicits.DecoratedDate

/**
  * Spec for [[DecoratedDate]]
  *
  * Created by vignesh.kalidas on 11/7/17.
  */
class DecoratedDateSpec extends WarpJUnitSpec {

  /**
    * Test that dates match by instantiating two epochs, with a different time component
    */
  @UnitTest
  def decoratedDateIsWithin24Hours(): Unit = {
    val dateFormatter: SimpleDateFormat = new SimpleDateFormat("MM-dd-yyyy")
    val epoch: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime)
    val epochLater: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime + 8192)

    epoch.isWithin24HoursOf(epochLater) should be (true)
  }

  /**
    * Test that dates don't match by instantiating the current date and the epoch
    */
  @UnitTest
  def decoratedDateIsNotWithin24Hours(): Unit = {
    val dateFormatter: SimpleDateFormat = new SimpleDateFormat("MM-dd-yyyy")
    val epoch: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime)
    val today: sql.Date = new sql.Date(Calendar.getInstance().getTime.getTime)

    epoch.isWithin24HoursOf(today) should be (false)
  }
}
