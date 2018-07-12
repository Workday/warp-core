package com.workday.telemetron.junit

import com.workday.telemetron.annotation.{AfterOnce, BeforeOnce, Schedule}
import org.junit.{Rule, Test}
import org.scalatest.Matchers
import org.scalatest.junit.JUnitSuite

/**
  * Created by vignesh.kalidas on 2/8/17.
  */
class SurroundOnceSpec extends JUnitSuite with Matchers {
  private[this] val someTelemetronRule: TelemetronRule = TelemetronRule()

  @Rule
  def name: TelemetronRule = this.someTelemetronRule

  /**
    * Test whether the invocations counter has been decremented by the BeforeOnce method
    */
  @Test
  def shouldBeOne(): Unit = SurroundOnceSpec.invocations should be (1)

  /**
    * Test whether the invocations counter has been decremented by the BeforeOnce method,
    * and also incremented by the AfterOnce method.
    */
  @Test
  def shouldStillBeOne(): Unit = SurroundOnceSpec.invocations should be (1)

  /**
    * Test whether the invocations counter has been decremented by the BeforeOnce method and incremented by the
    * AfterOnce method, with multiple threads and invocations
    */
  @Test
  @Schedule(threads = 4, invocations = 128)
  def shouldStillBeOneMultithreaded(): Unit = SurroundOnceSpec.invocations should be (1)

  /**
    * Test whether a method with both annotations behaves as expected (will continually be incremented before and after
    * each test run, so its only requirement is to be greater than zero
    */
  @Test
  def bothAnnotationsShouldWork(): Unit = SurroundOnceSpec.shouldBeNonZero should be > 0
}

/**
  * Companion object for the test class
  */
object SurroundOnceSpec {
  /**
    * Run this method once after each test method
    */
  @AfterOnce
  def incrementInvocations(): Unit = {
    SurroundOnceSpec.invocations += 1
  }

  /**
    * Run this method once before each test method
    */
  @BeforeOnce
  def decrementInvocations(): Unit = {
    SurroundOnceSpec.invocations -= 1
  }

  /**
    * Run this method once before and after each test method
    */
  @BeforeOnce
  @AfterOnce
  def incrementShouldBeOne(): Unit = {
    SurroundOnceSpec.shouldBeNonZero += 1
  }

  var shouldBeNonZero: Int = 0
  var invocations: Int = 2
}

