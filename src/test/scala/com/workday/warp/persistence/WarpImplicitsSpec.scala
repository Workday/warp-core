package com.workday.warp.persistence

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category
import Tables.profile.api._
import Tables._
import com.workday.warp.persistence.mysql.WarpSlickImplicits

/**
  * Created by ruiqi.wang 
  */
class WarpImplicitsSpec extends WarpJUnitSpec with CorePersistenceAware with WarpSlickImplicits {

  @Test
  @Category(Array(classOf[UnitTest]))
  def itWorks(): Unit = {
    iThink()
  }

  def iThink(): Unit = {
    val query = TestExecution.groupBy(_.passed)

    val action = query.map { case (id, metrics) => (
      id,
      metrics.map(_.responseTime).std
      )
    }

    val row: Seq[(Boolean, Option[Double])] = this.persistenceUtils.runWithRetries(action.result, 1)
  }

}
