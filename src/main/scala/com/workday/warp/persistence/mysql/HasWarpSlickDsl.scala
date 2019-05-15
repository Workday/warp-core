package com.workday.warp.persistence.mysql

import java.sql.Timestamp
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{Rep, SimpleExpression}

/**
  * Created by ruiqi.wang
  * Extended slick dsl support for MySQL
  */
trait HasWarpSlickDsl {

  /**
    * dsl class for string related operations.
    * @param str String to perform MySQL operations on
    */
  implicit class StringExtensions(str: Rep[String]) {

    /**
      * Correlates to MySQL's `REGEXP` operator
      * @param pattern regex pattern
      * @return whether or not the string matched
      */
    def regexMatch(pattern: Rep[String]): Rep[Boolean] = {
      val expression = SimpleExpression.binary[String, String, Boolean] { (str, pattern, queryBuilder) =>
        queryBuilder.expr(str)
        queryBuilder.sqlBuilder += " REGEXP "
        queryBuilder.expr(pattern)
      }
      expression.apply(str, pattern)
    }

  }

  /**
    * dsl class for Timestamp related operations
    * @param timestamp sql Timestamp to perform operations on
    */
  implicit class TimeStampExtensions(timestamp: Rep[Timestamp]) {

    /**
      * Roughly translates to `[Timestamp] > Now() - Interval` MySQL syntax.
      * @param interval interval in string format
      * @return whether or not given timestamp is between now and `interval` ago.
      */
    def betweenInterval(interval: String): Rep[Boolean] = {
      val expression = SimpleExpression.unary[Timestamp, Boolean] { (timestamp, queryBuilder) =>
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += " > Now() - Interval "
        queryBuilder.sqlBuilder += interval
      }
      expression.apply(timestamp)
    }

  }

}
