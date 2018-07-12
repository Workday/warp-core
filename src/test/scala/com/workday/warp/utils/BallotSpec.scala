package com.workday.warp.utils

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 1/27/16.
  */
class BallotSpec extends WarpJUnitSpec {

  /** Checks that passing votes do not fail the test */
  @Test
  @Category(Array(classOf[UnitTest]))
  def passed(): Unit = {
    val ballot: Ballot = new Ballot

    // simulate recording some votes
    ballot.registerVote(None)
    ballot.registerVote(None)
    ballot.registerVote(None)
    ballot.checkAndThrow()
  }


  /** Checks that the exception thrown when there are votes for failure contains messages from each distinct vote */
  @Test
  @Category(Array(classOf[UnitTest]))
  def failed(): Unit = {
    val failure1: String = "failed heap delta requirement"
    val failure2: String = "failed cumulative probability threshold requirement"
    val ballot: Ballot = new Ballot

    // simulate recording some votes
    ballot.registerVote(None)
    ballot.registerVote(Option(new RequirementViolationException(failure1)))
    ballot.registerVote(None)
    ballot.registerVote(Option(new RequirementViolationException(failure2)))

    val exception: RequirementViolationException = intercept[RequirementViolationException] {
      ballot.checkAndThrow()
    }

    val message: String = exception.getMessage
    message should startWith (Ballot.ERROR_MESSAGE_PREFIX)
    message should include (failure1)
    message should include (failure2)
  }
}
