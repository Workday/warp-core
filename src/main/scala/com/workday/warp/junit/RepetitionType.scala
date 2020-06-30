package com.workday.warp.junit

/**
  * Created by tomas.mccandless on 6/30/20.
  */
sealed trait RepetitionType {
  def name: String
}

case object Warmup extends RepetitionType {
  override val name: String = "warmup"
}

case object Trial extends RepetitionType {
  override val name: String = "trial"
}
