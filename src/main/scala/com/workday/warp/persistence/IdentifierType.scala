package com.workday.warp.persistence

import scala.annotation.implicitNotFound

/**
  * A type class containing values used to identify a [[com.workday.warp.persistence.TablesLike.TestExecutionLike]].
  */
@implicitNotFound("Could not find an implicit value for evidence of type class IdentifierType[${T}]." +
  " You might pass an (implicit ev: IdentifierType[${T}]) parameter to your method or import ${T}Type._")
trait IdentifierType[T] {
  def methodSignature(identifier: T): String
  def idTestDefinition(identifier: T): Int
}

object IdentifierType {
  def apply[T: IdentifierType]: IdentifierType[T] = implicitly[IdentifierType[T]]
}

object IdentifierSyntax {
  implicit class IdentifierOps[I: IdentifierType](identifier: I) {
    def methodSignature: String = IdentifierType[I].methodSignature(identifier)
    def idTestDefinition: Int = IdentifierType[I].idTestDefinition(identifier)
  }
}
