package com.workday.warp.persistence.mysql

import java.sql

import slick.jdbc.MySQLProfile.api._
import slick.lifted.{Rep, SimpleExpression}

/**
  * Created by ruiqi.wang
  * Extended slick dsl support for MySQL
  */
trait HasWarpSlickDsl {

  /**
    * dsl class for string related operations.
    *
    * @param str String to perform MySQL operations on
    */
  implicit class StringExtensions(str: Rep[String]) {

    /**
      * Correlates to MySQL's `REGEXP` operator
      *
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

    /**
      * Correlates to "QUOTE()"
      *
      * @return properly SQL formatted escape string
      */
    def quote(): Rep[String] = {
      val expression = SimpleExpression.unary[String, String] { (str, queryBuilder) =>
        queryBuilder.sqlBuilder += " QUOTE ("
        queryBuilder.expr(str)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(str)
    }

    /**
      * Translates to YEAR(string) function
      *
      * @return just the year in Int
      */
    def year(): Rep[Int] = {
      val expression = SimpleExpression.unary[String, Int] { (str, queryBuilder) =>
        queryBuilder.sqlBuilder += " YEAR ("
        queryBuilder.expr(str)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(str)
    }

    /**
      * Translates to DATE(string) function
      *
      * @return just the date in string
      */
    def date(): Rep[sql.Date] = {
      val expression = SimpleExpression.unary[String, sql.Date] { (str, queryBuilder) =>
        queryBuilder.sqlBuilder += " DATE ("
        queryBuilder.expr(str)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(str)
    }
  }

  /**
    * dsl class for DateTime related operations
    *
    * @param date sql DateTime to perform operations on
    */
  implicit class DateExtensions(date: Rep[sql.Date]) {

    /**
      * UNIX_TIMESTAMP operation - seconds elapsed since 1970-01-01 00:00:00 UTC
      * Takes in a parameter of date: equivalent to UNIX_TIMESTAMP(date)
      *
      * @return seconds in int
      */
    def unixTimestamp(): Rep[Long] = {
      val expression = SimpleExpression.unary[sql.Date, Long] { (dateNode, queryBuilder) =>
        queryBuilder.sqlBuilder += " UNIX_TIMESTAMP ("
        queryBuilder.expr(dateNode)
        queryBuilder.sqlBuilder += ")"
      }

      expression.apply(date)
    }
  }

  /**
    * dsl class for Timestamp related operations
    *
    * @param timestamp sql Timestamp to perform operations on
    */
  implicit class TimeStampExtensions(timestamp: Rep[sql.Timestamp]) {

    /**
      * Roughly translates to `[Timestamp] > Now() - Interval` MySQL syntax.
      *
      * @param interval interval in string format
      * @return whether or not given timestamp is between now and `interval` ago.
      */
    def isWithinPast(interval: String): Rep[Boolean] = {
      val expression = SimpleExpression.unary[sql.Timestamp, Boolean] { (timestamp, queryBuilder) =>
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += " > Now() - Interval "
        queryBuilder.sqlBuilder += interval
      }
      expression.apply(timestamp)
    }

    /**
      * Translates to DATE() function
      *
      * @return just date in "yyyy-MM-dd"
      */
    def date(): Rep[sql.Date] = {
      val expression = SimpleExpression.unary[sql.Timestamp, sql.Date] { (timestamp, queryBuilder) =>
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
      * TODO: DATE and DATETIME parameters
      *
      * @return seconds in int
      */
    def unixTimestamp(): Rep[Long] = {
      val expression = SimpleExpression.unary[sql.Timestamp, Long] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " UNIX_TIMESTAMP ("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }

    /**
      * Translates to YEAR(timestamp) function
      *
      * @return just the year in Int
      */
    def year(): Rep[Int] = {
      val expression = SimpleExpression.unary[sql.Timestamp, Int] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " YEAR ("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }

    /**
      * Translates to SUBDATE(timestamp, interval) function
      *
      * @return just the date as string
      */
    def subdate(interval: String): Rep[sql.Timestamp] = {
      val expression = SimpleExpression.unary[sql.Timestamp, sql.Timestamp] { (timestamp, queryBuilder) =>
        queryBuilder.sqlBuilder += " subdate("
        queryBuilder.expr(timestamp)
        queryBuilder.sqlBuilder += ", INTERVAL "
        queryBuilder.sqlBuilder += interval
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(timestamp)
    }
  }


  object TimeStampExtensions {

    /**
      * equivalent to NOW()
      *
      * @return string of date and time
      */
    def now(): Rep[String] = {
      SimpleExpression.nullary[String] { queryBuilder =>
        queryBuilder.sqlBuilder += " NOW() "
      }
    }

    /**
      * UNIX_TIMESTAMP operation - seconds elapsed since 1970-01-01 00:00:00 UTC
      *
      * @return seconds in int
      */
    def unixTimestamp(): Rep[Long] = {
      SimpleExpression.nullary[Long] { queryBuilder =>
        queryBuilder.sqlBuilder += " UNIX_TIMESTAMP ()"
      }
    }

    /**
      * SUBDATE operation: date - interval
      * Equivalent to SUBDATE(date, INTERVAL expr unit)
      *
      * @return string date
      */
    def subdate(date: String, interval: String): Rep[sql.Timestamp] = {
      val expression = SimpleExpression.unary[String, sql.Timestamp] { (date, queryBuilder) =>
        queryBuilder.sqlBuilder += " subdate("
        queryBuilder.expr(date)
        queryBuilder.sqlBuilder += ", INTERVAL "
        queryBuilder.sqlBuilder += interval
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(date)
    }

    /**
      * SUBDATE operation: date - days
      * Equivalent to SUBDATE(date, days)
      *
      * @return string date
      */
    def subdate(date: String, amount: Int): Rep[sql.Timestamp] = {
      val expression = SimpleExpression.binary[String, Int, sql.Timestamp] { (date, amount, queryBuilder) =>
        queryBuilder.sqlBuilder += " subdate("
        queryBuilder.expr(date)
        queryBuilder.sqlBuilder += " , "
        queryBuilder.expr(amount)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(date, amount)
    }
  }

  /**
    * dsl class for math related operation
    */
  object NumberExtension {

    /**
      * translates to Round(number, decimals)
      *
      * @return number
      */
    def round(number: Rep[Double], decimal: Int): Rep[Double] = {
      val expression = SimpleExpression.binary[Double, Int, Double] { (number, decimal, queryBuilder) =>
        queryBuilder.sqlBuilder += "ROUND ("
        queryBuilder.expr(number)
        queryBuilder.sqlBuilder += ","
        queryBuilder.expr(decimal)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(number, decimal)
    }

    /**
      * translates to Round(number)
      *
      * @return number
      */
    def round(number: Rep[Double]): Rep[Int] = {
      val expression = SimpleExpression.unary[Double, Int] { (number, queryBuilder) =>
        queryBuilder.sqlBuilder += "ROUND ("
        queryBuilder.expr(number)
        queryBuilder.sqlBuilder += ")"
      }
      expression.apply(number)
    }
  }
}


