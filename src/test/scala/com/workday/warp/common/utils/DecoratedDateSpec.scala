package com.workday.warp.common.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.sql

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.utils.Implicits.DecoratedDate
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.Test

/**
  * Spec for [[DecoratedDate]]
  *
  * Created by vignesh.kalidas on 11/7/17.
  */
class DecoratedDateSpec extends WarpJUnitSpec {

  /**
    * Test that dates match by instantiating two epochs, with a different time component
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def decoratedDateIsWithin24Hours(): Unit = {
    val dateFormatter: SimpleDateFormat = new SimpleDateFormat("MM-dd-yyyy")
    val epoch: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime)
    val epochLater: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime + 8192)

    epoch.isWithin24HoursOf(epochLater) should be (true)
  }

  /**
    * Test that dates don't match by instantiating the current date and the epoch
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def decoratedDateIsNotWithin24Hours(): Unit = {
    val dateFormatter: SimpleDateFormat = new SimpleDateFormat("MM-dd-yyyy")
    val epoch: sql.Date = new sql.Date(dateFormatter.parse("01-01-1970").getTime)
    val today: sql.Date = new sql.Date(Calendar.getInstance().getTime.getTime)

    epoch.isWithin24HoursOf(today) should be (false)
  }
}
