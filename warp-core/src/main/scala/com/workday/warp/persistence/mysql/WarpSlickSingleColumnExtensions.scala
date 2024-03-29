package com.workday.warp.persistence.mysql

import com.workday.warp.persistence.mysql.WarpSlickSingleColumnExtensions._
import slick.ast.Library.SqlAggregateFunction
import slick.ast.TypedType
import slick.lifted.FunctionSymbolExtensionMethods._
import slick.lifted.{Query, Rep}


/**
  * Created by ruiqi.wang
  *
  */
protected class WarpSlickSingleColumnExtensions[B1, P1, C[_]](val q: Query[Rep[P1], _, C]) {
  type OptionTM = TypedType[Option[B1]]

  /** Aggregation function for standard deviation */
  def std(implicit tm: OptionTM): Rep[Option[B1]] = Std.column[Option[B1]](q.toNode)

}

object WarpSlickSingleColumnExtensions {

  val Std: SqlAggregateFunction = new SqlAggregateFunction("STD")

}
