package com.workday.warp.adapters.gatling.traits

/**
  * Created by ruiqi.wang 
  */
trait HasBasePackageName {
  protected val packageName: String = this.getClass.getPackage.getName
}
