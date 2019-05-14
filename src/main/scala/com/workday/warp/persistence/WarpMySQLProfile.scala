package com.workday.warp.persistence

import java.sql.Timestamp

import slick.jdbc.MySQLProfile

import scala.language.{higherKinds, implicitConversions}

/**
  * Created by ruiqi.wang 
  */
trait WarpMySQLProfile extends MySQLProfile {

  trait WarpSlickAPI extends API {


    implicit class RegexLikeOperators(stringToMatch: Rep[String]) {
      def regexLike(pattern: Rep[String]): Rep[Boolean] = {
        val expression = SimpleExpression.binary[String, String, Boolean] { (stringToMatch, pattern, queryBuilder) =>
          queryBuilder.expr(stringToMatch)
          queryBuilder.sqlBuilder += " ~* "
          queryBuilder.expr(pattern)
        }
        expression.apply(stringToMatch, pattern)
      }
    }

    implicit class TimeStampComparisons(timestamp: Rep[Timestamp]) {
      def minusTime(interval: Rep[String]): Rep[Boolean] = {
        val expression = SimpleExpression.binary[Timestamp, String, Boolean] { (timestamp, interval, queryBuilder) =>
          queryBuilder.expr(timestamp)
          queryBuilder.sqlBuilder += " > Now() - Interval "
          queryBuilder.expr(interval)
        }
        expression.apply(timestamp, interval)
      }
    }



  }

  override val api: WarpSlickAPI = new WarpSlickAPI {}

}


object WarpMySQLProfile extends WarpMySQLProfile


