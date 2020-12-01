package com.workday.warp

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

/**
  * Implicits for constructing [[TestId]].
  *
  * You may prefer using the corresponding explicit methods on [[TestId]] companion object.
  *
  * This provides some flexibility in terms of multiple entrypoints into our framework,
  * and avoids the boilerplate of multiple method overloadings.
  *
  * Java users can statically import and explicitly call these methods, or use methods such as [[TestId.fromTestInfo()]].
  *
  * Created by tomas.mccandless on 6/18/20.
  */
object TestIdImplicits {


  /**
    * Constructs a [[TestId]] from a [[TestInfo]].
    *
    * @param info a [[TestInfo]], usually obtained from a default [[org.junit.jupiter.api.extension.ParameterResolver]]
    *             as part of a running test.
    * @return a [[TestId]] used to identify tests.
    */
  implicit def testInfo2TestId(info: TestInfo): TestId = TestId.fromTestInfo(info)


  /**
    * Constructs a [[TestId]] from an [[ExtensionContext]].
    *
    * @param context an [[ExtensionContext]], usually obtained as part of [[org.junit.jupiter.api.BeforeEach]] or other hook.
    * @return a [[TestId]] used to identify tests.
    */
  implicit def extensionContext2TestId(context: ExtensionContext): TestId = TestId.fromExtensionContext(context)


  /**
    * Constructs a [[TestId]] from a fully qualified method signature.
    *
    * Included mainly for java interop and backwards compatibility, however,
    * note that there may be overloaded ambiguous methods. Prefer using `testInfoIsTestId` over this.
    *
    * @param signature
    * @return
    */
  implicit def string2TestId(signature: String): TestId = TestId.fromString(signature)
}
