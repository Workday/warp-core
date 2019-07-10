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
      *
      * @param interval interval in string format
      * @return whether or not given timestamp is between now and `interval` ago.
      */
    def isWithinPast(interval: String): Rep[Boolean] = {
      val expression = SimpleExpression.unary[Timestamp, Boolean] { (timestamp, queryBuilder) =>
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += " > Now() - Interval "
        queryBuilder.sqlBuilder += interval
      }
      expression.apply(timestamp)
    }

    /**
      *  Translates to YEAR() function
      *  @return just the year in Int
      */
    def year(): Rep[Int] = {
      val expression = SimpleExpression.unary[Timestamp, Int] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " YEAR ("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }

    /**
      * Translates to DATE() function
      * @return just date in "yyyy-MM-dd"
      */
    def date(): Rep[String] = {
      val expression = SimpleExpression.unary[Timestamp, String] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " DATE ("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }

    /**
      * UNIX_TIMESTAMP operation - seconds elapsed since 1970-01-01 00:00:00 UTC
      * Takes in a parameter of TimeStamp: equivalent to UNIX_TIMESTAMP(date)
      *
      * TODO: default empty parameter to calculate seconds based on current time, as well
      * as DATE and DATETIME parameters
      *
      * @return seconds in int
      */
    def unixTimestamp(): Rep[Int] = {
      val expression = SimpleExpression.unary[Timestamp, Int] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " UNIX_TIMESTAMP ("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }

    /**
      * SUBDATE operation: date - interval
      * Equivalent to SUBDATE(date, INTERVAL expr unit)
      *
      * TODO: SUBDATE(expr, days)
      *
      * @return string date
      */
    def subdate(date: String, interval: String): Rep[String] = {
      val expression = SimpleExpression.unary[String, String] { (date, queryBuilder) =>
        queryBuilder.sqlBuilder += " subdate("
        queryBuilder.expr(date)
        queryBuilder.sqlBuilder += ", INTERVAL "
        queryBuilder.sqlBuilder += interval
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(date)
    }
  }

  object TimeStampExtensions {

    /**
      * equivalent to NOW()
      * @return string of date and time
      */
    def now(): Rep[String] = {
      SimpleExpression.nullary[String] { (queryBuilder) =>
        queryBuilder.sqlBuilder += " NOW() "
      }
    }
  }
}
