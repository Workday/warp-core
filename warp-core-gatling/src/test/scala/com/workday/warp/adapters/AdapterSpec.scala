package com.workday.warp.adapters

import java.time.Duration

import com.workday.warp.TestIdImplicits.string2TestId
import com.workday.warp.TrialResult
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.{ExecutionTag, Tag}

import scala.util.Try

object AdapterSpecConstants {
  val duration: Duration = Duration.ofNanos(1000000)
}

class SuccessfulInvokationAdapter(override val tags: List[Tag] = Nil) extends Adapter[String]("com.wday.warp.adap.spec", tags) {
  /** @return a [[scala.util.Success]] containing a [[TrialResult]], or a wrapped exception. */
  override def invoke(): Try[TrialResult[String]] = {
    Try(TrialResult(maybeResponseTime = Some(AdapterSpecConstants.duration), maybeResult = None))
  }
}

class FailureInvokationAdapter(override val tags: List[Tag] = Nil) extends Adapter[String]("com.wday.warp.adap.spec", tags) {
  /** @return a [[scala.util.Success]] containing a [[TrialResult]], or a wrapped exception. */
  override def invoke(): Try[TrialResult[String]] = {
    Try(throw new RuntimeException("Failed!"))
  }
}

class AdapterSpec extends WarpJUnitSpec {

  @UnitTest
  def measureSuccessSpec(): Unit = {
    val specAdapter: SuccessfulInvokationAdapter = new SuccessfulInvokationAdapter(List(ExecutionTag("key", "value")))
    specAdapter.measure().maybeResponseTime should be (Some(AdapterSpecConstants.duration))
  }

  @UnitTest
  def measureFailureSpec(): Unit = {
    val specAdapter: FailureInvokationAdapter = new FailureInvokationAdapter(List(ExecutionTag("key", "value")))
    Try(specAdapter.measure()).isFailure should be (true)
  }
}
