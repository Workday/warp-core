package com.workday.warp.persistence

/**
  * A case class containing values used to identify a [[Tables.TestExecution]].
  */
case class CoreIdentifier(methodSignature: String = "", idTestDefinition: Int = 0)

object CoreIdentifierType {
  def apply[T: IdentifierType]: IdentifierType[T] = implicitly[IdentifierType[T]]

  implicit object CoreIdentifierIsIdentifierType extends IdentifierType[CoreIdentifier] {
    def methodSignature(identifier: CoreIdentifier): String = identifier.methodSignature
    def idTestDefinition(identifier: CoreIdentifier): Int = identifier.idTestDefinition
  }
}
