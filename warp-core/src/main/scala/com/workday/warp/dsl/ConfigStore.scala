package com.workday.warp.dsl

import scala.collection.concurrent.TrieMap

/**
  * Makes instances of [[ExecutionConfig]] available to measured tests.
  *
  * Some tests need to know how many times they are being iterated in order to properly construct log directories.
  *
  * Created by tomas.mccandless on 7/8/16.
  */
object ConfigStore {

  private[this] val configs: TrieMap[String, ExecutionConfig] = TrieMap()


  /**
    * Gets the [[ExecutionConfig]] being used for `testId`.
    *
    * @param testId fully qualified method signature of the measured test.
    * @return the [[ExecutionConfig]] that is configured for `testId`.
    */
  def get(testId: String): Option[ExecutionConfig] = this.configs.get(testId)


  /**
    * Puts `config` associated with `testId`.
    *
    * @param testId fully qualified method signature of the measured test.
    * @param config [[ExecutionConfig]] that is configured for `testId`.
    */
  def put(testId: String, config: ExecutionConfig): Unit = this.configs.put(testId, config)
}
