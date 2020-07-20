package com.workday.warp.persistence

import java.time.LocalDate

import slick.dbio.DBIO
import slick.lifted.{Query, Rep}
import TablesLike._

/**
  * Defines function definitions for creating read and write [[Query]]. These queries can be composed, converted to [[DBIO]],
  * and executed using a [[Connection]] to produce results.
  *
  * In the case where the return value contains some "RowLike", the return type will be [[DBIO]]. This is to allow
  * overriding of return types with more specific types. [[Query]] is invariant, so it cannot be subclassed, while
  * [[DBIO]] is covariant.
  *
  * Created by leslie.lam on 2/7/18.
  *
  * TODO can we make implicits with this? or modify the generator to have them? very difficult to read.
  * move all these to the generator? decorate each class with these query methods?
  *
  * autogenerate an id method on each of the appropriate tables?
  *
  * autogenerate the connection as well?
  *
  */
trait AbstractQueries {

  /**
    * Creates a [[DBIO]] for selecting from [[BuildLike]].
    *
    * @param major version of the build.
    * @param minor version of the build.
    * @param patch version of the build.
    * @return a [[DBIO]] for selecting the [[BuildRowLike]] with the specified parameters.
    */
  def readBuildQuery(major: Int,
                     minor: Int,
                     patch: Int): DBIO[Seq[BuildRowLike]]


  /**
    * Creates a [[DBIO]] for selecting from [[TestDefinitionLike]].
    *
    * @param methodSignature method signature of [[TestDefinitionRowLike]] (usually fully-qualified method name).
    * @return a [[DBIO]] for selecting the [[TestDefinitionRowLike]] with the specified method signature.
    */
  def readTestDefinitionQuery(methodSignature: String): DBIO[Seq[TestDefinitionRowLike]]


  /**
    * Creates a [[Query]] for selecting the method signature of `testExecution`.
    *
    * @param testExecution [[TestExecutionRowLike]] to read the method signature of.
    * @return a [[Query]] for selecting the method signature of `testExecution`
    */
  def readTestExecutionSignatureQuery[T: TestExecutionRowLikeType](testExecution: T): Query[Rep[String], String, Seq]


  /**
    * Creates a [[DBIO]] for reading all the [[TestExecutionRowLike]] with the given method signature.
    *
    * @param methodSignature signature to look up.
    * @return a [[DBIO]] for reading [[TestExecutionRowLike]] with the given signature.
    */
  def readTestExecutionQuery(methodSignature: String): DBIO[Seq[TestExecutionRowLike]]


  /** @return [[DBIO]] for reading the number of rows in [[TestExecutionLike]]. */
  def numTestExecutionsQuery: DBIO[Int]


  /**
    * Creates a [[DBIO]] for reading the [[MeasurementNameRowLike]] with the given name.
    *
    * @param name name to look up in [[MeasurementNameLike]] table.
    * @return a [[DBIO]] for reading the [[MeasurementNameRowLike]] with the given name.
    */
  def readMeasurementNameQuery(name: String): DBIO[Seq[MeasurementNameRowLike]]


  /**
    * Creates a [[DBIO]] for reading the [[TagNameRowLike]] with the given name.
    *
    * @param name name to look up in [[TagNameLike]] table.
    * @return a [[DBIO]] for reading the [[TagNameRowLike]] with the given name.
    */
  def readTagNameQuery(name: String): DBIO[Seq[TagNameRowLike]]


  /**
    * Creates a generic [[DBIO]] of test executions; can then be used to filter and map for specific fields.
    *
    * @param identifier [[IdentifierType]] containing the identifying parameters of the [[TestExecutionLike]]
    * @return a [[DBIO]] for reading test executions.
    */
  def testExecutionsQuery[I: IdentifierType](identifier: I): DBIO[Seq[TestExecutionRowLike]]

  /**
    * Creates a [[DBIO]] for reading historical response times.
    *
    * @param identifier [[IdentifierType]] containing the identifying parameters of the [[TestExecutionLike]]
    * @return a [[DBIO]] for reading historical response times.
    */
  def responseTimesQuery[I: IdentifierType](identifier: I): DBIO[Seq[Double]]


  /**
    * Creates a [[DBIO]] for reading historical response times. The response time for the [[TestExecutionRowLike]]
    * with `excludeIdTestExecution` will be excluded from the results.
    *
    * @param identifier [[IdentifierType]] containing the identifying parameters of the [[TestExecutionLike]]
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @return a [[DBIO]] for reading historical response times.
    */
  def responseTimesQuery[I: IdentifierType](identifier: I, excludeIdTestExecution: Int): DBIO[Seq[Double]]


  /**
    * Creates a [[DBIO]] for reading historical response times. The response time for the [[TestExecutionRowLike]]
    * with `excludeIdTestExecution` and a startTime timestamp before 'startDateCutoff' will be excluded from the results.
    *
    * @param identifier [[IdentifierType]] containing the identifying parameters of the [[TestExecutionLike]]
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @param startDateLowerBound only include cases that start after/on this lower bound date.
    * @return a [[DBIO]] for reading historical response times.
    */
  def responseTimesQuery[I: IdentifierType](identifier: I,
                                            excludeIdTestExecution: Int,
                                            startDateLowerBound: LocalDate): DBIO[Seq[Double]]


  /**
    * Creates a [[DBIO]] for reading historical Measurement rows.
    *
    * @param identifier [[IdentifierType]] containing the identifying parameters of the [[TestExecutionLike]]
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @return a [[DBIO]] for reading historical measurement rows.
    */
  def measurementQuery[I: IdentifierType](identifier: I, excludeIdTestExecution: Int): DBIO[Seq[(MeasurementRowLike, Double)]]


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestExecutionRowLike]] with id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRowLike]] to look up tags for.
    * @param idTagName id of the [[TagNameRowLike]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  def testExecutionTagsQuery(idTestExecution: Int, idTagName: Int): Query[(Rep[String], Rep[String]), (String, String), Seq]


  /**
    * Creates a [[DBIO]] for reading the entire row of a tag with id `idTagName` set on the [[TestExecutionRowLike]] with
    * id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRowLike]] to look up tags for.
    * @param idTagName id of the [[TagNameRowLike]] to look up.
    * @return a [[DBIO]] for looking up the whole row for a specified tag.
    */
  def testExecutionTagsRowQuery(idTestExecution: Int, idTagName: Int) : DBIO[Seq[TestExecutionTagRowLike]]

  /**
    * Creates a [[Query]] for reading the tags set on the [[TestExecutionRowLike]] with id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRowLike]] to look up tags for.
    * @return a [[Query]] for looking up tags on a [[TestExecutionRowLike]]
    */
  def testExecutionTagsQuery(idTestExecution: Int): Query[(Rep[String], Rep[String]), (String, String), Seq]


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestDefinitionRowLike]] with id `idTestDefinition`.
    *
    * @param idTestDefinition id of the [[TestDefinitionRowLike]] to look up tags for.
    * @param idTagName id of the [[TagNameRowLike]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  def testDefinitionTagsQuery(idTestDefinition: Int, idTagName: Int): Query[(Rep[String], Rep[String]), (String, String), Seq]


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestDefinitionTagRowLike]] with id `TestDefinitionTag`.
    *
    * @param idTestDefinitionTag id of the [[TestDefinitionTagRowLike]] to look up metatags for.
    * @param idTagName id of the [[TagNameRowLike]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  def testDefinitionMetaTagQuery(idTestDefinitionTag: Int, idTagName: Int): Query[(Rep[String], Rep[String]), (String, String), Seq]


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestExecutionTagRowLike]] with id `TestExecutionTag`.
    *
    * @param idTestExecutionTag id of the [[TestExecutionTagRowLike]] to look up metatags for.
    * @param idTagName id of the [[TagNameRowLike]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  def testExecutionMetaTagQuery(idTestExecutionTag: Int, idTagName: Int): Query[(Rep[String], Rep[String]), (String, String), Seq]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[BuildLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[BuildLike]].
    */
  def writeBuildQuery[T: BuildRowLikeType](row: T): DBIO[BuildRowLike]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestDefinitionLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestDefinitionLike]].
    */
  def writeTestDefinitionQuery[T: TestDefinitionRowLikeType](row: T): DBIO[TestDefinitionRowLike]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[MeasurementNameLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[MeasurementNameLike]].
    */
  def writeMeasurementNameQuery[T: MeasurementNameRowLikeType](row: T): DBIO[MeasurementNameRowLike]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[MeasurementNameLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[MeasurementNameLike]].
    */
  def writeTagNameQuery[T: TagNameRowLikeType](row: T): DBIO[TagNameRowLike]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestExecutionLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestExecutionLike]].
    */
  def writeTestExecutionQuery[T: TestExecutionRowLikeType](row: T): DBIO[TestExecutionRowLike]

  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestExecutionTagLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestExecutionTagLike]].
    */
  def writeTestExecutionTagQuery[T: TestExecutionTagRowLikeType](row: T): DBIO[TestExecutionTagRowLike]


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestDefinitionTagLike]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestDefinitionTagLike]].
    */
  def writeTestDefinitionTagQuery[T: TestDefinitionTagRowLikeType](row: T): DBIO[TestDefinitionTagRowLike]


  /**
    * Creates a [[DBIO]] for updating the threshold of `testExecution`.
    *
    * @param testExecution [[TestExecutionRowLike]] to update.
    * @param newThreshold new threshold to set on `testExecution`.
    * @return a [[DBIO]] (not yet executed) for updating the threshold of `testExecution`.
    */
  def updateTestExecutionThreshold[T: TestExecutionRowLikeType](testExecution: T, newThreshold: Double): DBIO[Int]
}
