package com.workday.warp.common.utils

import com.workday.warp.common.utils.Implicits.DecoratedArray
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.category.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Spec for [[DecoratedArray]]
  *
  * Created by tomas.mccandless on 12/14/15.
  */
class DecoratedArraySpec extends WarpJUnitSpec {

  /** Checks normal usage of mapWithImage. Provided anonymous function must have Tuple return type. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def mapWithImage(): Unit = {
    val m: Map[String, Int] = Array("a", "ab", "abc") mapWithImage { s: String => s -> s.length }
    m shouldBe Map("a" -> 1, "ab" -> 2, "abc" -> 3)
  }


  /** Checks that we can't compile when keyValuePair() does not have the correct return type. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def compileMapWithImage(): Unit = {
    "val m: Map[Int, Int] = Array(2, 3, 5, 7) mapWithImage { n: Int => n -> n }" should compile
    "val m: Map[Int, Int] = Array(2, 3, 5, 7) mapWithImage { n: Int => n }" shouldNot compile
  }


  /** Checks normal usage of mapWithImages. Provided anonymous functions must have the correct return types. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def mapWithImages(): Unit = {
    val m: Map[String, Int] = Array("a", "ab", "abc").mapWithImages { s: String => s + "b" } { s: String => s.length }
    m shouldBe Map("ab" -> 1, "abb" -> 2, "abcb" -> 3)
  }


  /** Checks that we can't compile when key() or value() does not have the correct return type. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def compileMapWithImages(): Unit = {
    "val m: Map[Int, Int] = Array(2, 3, 5, 7).mapWithImages { n: Int => n } { n: Int => n }" should compile
    // only key() function is provided
    "val m: Map[Int, Int] = Array(2, 3, 5, 7).mapWithImages { n: Int => n }" shouldNot compile
    // key() return type is String
    "val m: Map[Int, Int] = Array(2, 3, 5, 7).mapWithImages { n: Int => n.toString } { n: Int => n }" shouldNot compile
    // value() return type is String
    "val m: Map[Int, Int] = Array(2, 3, 5, 7).mapWithImages { n: Int => n } { n: Int => n.toString }" shouldNot compile
  }
}
