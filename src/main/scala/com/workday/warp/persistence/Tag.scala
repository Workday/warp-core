package com.workday.warp.persistence

import com.workday.warp.persistence.Tables.{TestDefinitionRow, TestExecutionRow}

import scala.util.Try

/**
  * Case class hierarchy encoding tags for both [[TestExecutionRow]] and [[TestDefinitionRow]].
  *
  * Created by tomas.mccandless on 12/5/16.
  */

// neat wrapper for the below classes
case class PersistTagResult(tag: Tag, tryTag: (Try[Tag], List[PersistMetaTagResult]))
case class PersistMetaTagResult(metaTag: MetaTag, tryMetaTag: Try[MetaTag])


// metatags associated with outer tags
sealed abstract class MetaTag(val key: String,
                              val value: String,
                              val isUserGenerated: Boolean,
                              val taggedFor: Class[_])

// metatag for a TestDefinition
case class DefinitionMetaTag(override val key: String,
                             override val value: String,
                             override val isUserGenerated: Boolean = true)
  extends MetaTag(key, value, isUserGenerated, classOf[TestDefinitionRow])

// metatag for a TestExecution
case class ExecutionMetaTag(override val key: String,
                            override val value: String,
                            override val isUserGenerated: Boolean = true)
  extends MetaTag(key, value, isUserGenerated, classOf[TestExecutionRow])




sealed abstract class Tag(val key: String,
                          val value: String,
                          val isUserGenerated: Boolean,
                          val metaTags: List[MetaTag],
                          val taggedFor: Class[_])

// tag for a TestDefinition. these tags will be common to all executions of a test (eg, instanceId)
case class DefinitionTag(override val key: String,
                         override val value: String,
                         override val metaTags: List[DefinitionMetaTag] = List.empty,
                         override val isUserGenerated: Boolean = true)
  extends Tag(key, value, isUserGenerated, metaTags, classOf[TestDefinitionRow])

// tag for a TestExecution. these tags will be applied to just a single execution of a test (eg, A/B testing)
case class ExecutionTag(override val key: String,
                        override val value: String,
                        override val metaTags: List[ExecutionMetaTag] = List.empty,
                        override val isUserGenerated: Boolean = true)
  extends Tag(key, value, isUserGenerated, metaTags, classOf[TestExecutionRow])
