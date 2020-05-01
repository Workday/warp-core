package com.workday.warp.persistence.mysql

import scala.language.{higherKinds, implicitConversions}
import slick.ast.BaseTypedType
import slick.lifted.{Query, Rep}

/**
  * Created by ruiqi.wang
  */
trait WarpSlickImplicits {

  implicit def customSingleColumnQueryExtensionMethods[B1: BaseTypedType, C[_]](q: Query[Rep[B1], _, C]):
  WarpSlickSingleColumnExtensions[B1, B1, C] = new WarpSlickSingleColumnExtensions[B1, B1, C](q)

  implicit def customSingleOptionColumnQueryExtensionMethods[B1: BaseTypedType, C[_]](q: Query[Rep[Option[B1]], _, C]):
  WarpSlickSingleColumnExtensions[B1, Option[B1], C] = new WarpSlickSingleColumnExtensions[B1, Option[B1], C](q)

}
