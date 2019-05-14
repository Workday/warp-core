package com.workday.warp.persistence.mysql

import com.workday.warp.persistence.mysql.SlickExtensionMethods._
import slick.ast.Library.SqlAggregateFunction
import slick.ast.TypedType
import slick.lifted.FunctionSymbolExtensionMethods._
import slick.lifted.{Query, Rep}

import scala.language.{higherKinds, implicitConversions}
/**
  * Created by ruiqi.wang 
  */
class SlickExtensionMethods[B1, P1, C[_]](val q: Query[Rep[P1], _, C]) extends AnyVal {
  type OptionTM = TypedType[Option[B1]]

  /** Aggregation function for standard deviation */
  def std(implicit tm: OptionTM): Rep[Option[B1]] = Std.column[Option[B1]](q.toNode)

}

object SlickExtensionMethods {


  val Std: SqlAggregateFunction = new SqlAggregateFunction("STD")

}
