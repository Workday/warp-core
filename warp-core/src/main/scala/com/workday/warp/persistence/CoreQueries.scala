package com.workday.warp.persistence

import java.sql.Timestamp
import java.time.LocalDate

import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.Tables.profile.api._
import com.workday.warp.persistence.TablesLike._

import scala.concurrent.ExecutionContext.Implicits.global
import com.workday.warp.persistence.IdentifierSyntax._

/**
  * Defines functions for creating read and write [[Query]]. These queries can be composed, converted to [[DBIOAction]],
  * and executed using a [[Connection]] to produce results.
  *
  * In the case where the return value contains some "RowLike", the return type will be [[DBIO]]. This is to allow
  * overriding of return types with more specific types. [[Query]] is invariant, so it cannot be subclassed, while
  * [[DBIO]] is covariant.
  *
  * Note: These queries reference the Tables defined in com.workday.warp.persistence.model.
  *
  * Created by leslie.lam on 2/7/18.
  */
trait CoreQueries extends AbstractQueries {

  /**
    * Creates a [[DBIO]] for selecting from [[Build]].
    *
    * @param major major version of the build.
    * @param minor minor version of the build.
    * @param patch patch version of the build.
    * @return a [[DBIO]] for selecting the [[BuildRow]] with the specified parameters.
    */
  override def readBuildQuery(major: Int,
                              minor: Int,
                              patch: Int): DBIO[Option[BuildRowWrapper]] = {
    (Build filter { build: Build =>
      build.major === major && build.minor === minor && build.patch === patch
    }).result.headOption
  }


  /**
    * Creates a [[DBIO]] for selecting from [[TestDefinition]].
    *
    * @param methodSignature method signature of [[TestDefinitionRow]] (usually fully-qualified method name).
    * @return a [[DBIO]] for selecting the [[TestDefinitionRow]] with the specified method signature.
    */
  override def readTestDefinitionQuery(methodSignature: String): DBIO[Option[TestDefinitionRowWrapper]] = {
    TestDefinition.filter(_.methodSignature === methodSignature).result.headOption
  }

  /**
   * Creates a [[DBIO]] for selecting from [[TestDefinitionTag]].
   *
   * @param idTestDefinition to filter TestDefinitionTags with
   * @param testDefinitionTagName to filter TestDefinitionTags with
   * @return a [[DBIO]] for selecting the [[TestDefinitionTagRow]] that matches the provided id and name
   */
  def readTestDefinitionTagQuery(idTestDefinition: Int, testDefinitionTagName: String): DBIO[Option[TestDefinitionTagRowWrapper]] = {
    val query = for {
      tagName <- TagName if tagName.name === testDefinitionTagName
      testDefinitionTag <- TestDefinitionTag if testDefinitionTag.idTestDefinition === idTestDefinition &&
                                                testDefinitionTag.idTagName === tagName.idTagName
    } yield testDefinitionTag

    query.result.map(_.headOption)
  }

  def readTestDefinitionMetaTagQuery(
                                      idTestDefinition: Int, testDefinitionMetaTagName: String
                                    ): DBIO[Option[TestDefinitionMetaTagRowWrapper]] = {
    val query = for {
      tagName <- TagName if tagName.name === testDefinitionMetaTagName
      testDefinitionMetaTag <- TestDefinitionMetaTag if testDefinitionMetaTag.idTestDefinitionTag === idTestDefinition &&
                                                        testDefinitionMetaTag.idTagName === tagName.idTagName
    } yield testDefinitionMetaTag

    query.result.map(_.headOption)
  }


  /**
    * Creates a [[Query]] for selecting the method signature of `testExecution`.
    *
    * @param testExecution [[TestExecutionRow]] to read the method signature of.
    * @return a [[Query]] for selecting the method signature of `testExecution`
    */
  override def readTestExecutionSignatureQuery[T: TestExecutionRowLikeType](testExecution: T): DBIO[Option[String]] = {
    TestDefinition.filter(_.idTestDefinition === testExecution.idTestDefinition).map(_.methodSignature).result.headOption
  }


  /**
    * Creates a [[DBIO]] for reading all the [[TestExecutionRow]] with the given method signature.
    *
    * @param methodSignature signature to look up.
    * @return a [[DBIO]] for reading [[TestExecutionRow]] with the given signature.
    */
  override def readTestExecutionQuery(methodSignature: String): DBIO[Seq[TestExecutionRowWrapper]] = {
    val query: Query[TestExecution, TestExecutionRow, Seq] = for {
      testDefinition <- TestDefinition if testDefinition.methodSignature === methodSignature
      testcase <- TestExecution if testcase.idTestDefinition === testDefinition.idTestDefinition
    } yield testcase

    query.result
  }

  /**
   * Creates a [[DBIO]] for selecting from [[TestExecutionTag]].
   *
   * @param idTestExecution to filter TestExecutionTags with
   * @param testExecutionTagName to filter TestExecutionTags with
   * @return a [[DBIO]] for selecting the [[TestExecutionTagRow]] that matches the provided id and name
   */
  def readTestExecutionTagQuery(idTestExecution: Int, testExecutionTagName: String): DBIO[Option[TestExecutionTagRowWrapper]] = {
    val query = for {
      tagName <- TagName if tagName.name === testExecutionTagName
      testExecutionTag <- TestExecutionTag if testExecutionTag.idTestExecution === idTestExecution &&
                                              testExecutionTag.idTagName === tagName.idTagName
    } yield testExecutionTag

    query.result.map(_.headOption)
  }


  /** @return [[DBIO]] for reading the number of rows in [[TestExecution]]. */
  override def numTestExecutionsQuery: DBIO[Int] = TestExecution.length.result


  /**
    * Creates a [[DBIO]] for reading the [[MeasurementNameRow]] with the given name.
    *
    * @param name name to look up in [[MeasurementName]] table.
    * @return a [[DBIO]] for reading the [[MeasurementNameRow]] with the given name.
    */
  override def readMeasurementNameQuery(name: String): DBIO[Option[MeasurementNameRowWrapper]] = {
    (MeasurementName filter { _.name === name }).result.headOption
  }


  /**
    * Creates a [[DBIO]] for reading the [[TagNameRow]] with the given name.
    *
    * @param name name to look up in [[TagName]] table.
    * @return a [[DBIO]] for reading the [[TagNameRow]] with the given name.
    */
  override def readTagNameQuery(name: String): DBIO[Option[TagNameRowWrapper]] = {
    (TagName filter { _.name === name }).result.headOption
  }


  /**
    * Creates a generic [[DBIO]] of test executions; can then be used to filter and map for specific fields.
    *
    * @param identifier [[CoreIdentifier]] containing the methodSignature to query TestExecutions
    * @return a [[DBIO]] for reading test executions.
    */
  override def testExecutionsQuery[I: IdentifierType](identifier: I): DBIO[Seq[TestExecutionRowWrapper]] = {
    val query: Query[TestExecution, TestExecutionRow, Seq] = for {
      testDefinition <- TestDefinition if testDefinition.methodSignature === identifier.methodSignature
      testExecution <- TestExecution if testExecution.idTestDefinition === testDefinition.idTestDefinition
    } yield testExecution

    query.result
  }


  /**
    * Creates a [[DBIO]] for reading historical response times.
    *
    * @param identifier a [[CoreIdentifier]] containing the methodSignature to query TestExecutions
    * @return a [[DBIO]] for reading historical response times.
    */
  override def responseTimesQuery[I: IdentifierType](identifier: I): DBIO[Seq[Double]] = {
    for {
      maybeRow <- this.testExecutionsQuery(identifier)
      result <- DBIO.successful(maybeRow.map(_.responseTime))
    } yield result
  }


  /**
    * Creates a [[DBIO]] for reading historical response times. The response time for the [[TestExecutionRow]] with `excludeIdTestExecution`
    * will be excluded from the results.
    *
    * @param identifier a [[CoreIdentifier]] containing the methodSignature to query TestExecutions
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @return a [[DBIO]] for reading historical response times.
    */
  override def responseTimesQuery[I: IdentifierType](identifier: I, excludeIdTestExecution: Int): DBIO[Seq[Double]] = {
    for {
      maybeRow <- this.testExecutionsQuery(identifier)
      result <- DBIO.successful(maybeRow.collect { case testExecution if testExecution.idTestExecution != excludeIdTestExecution =>
        testExecution.responseTime
      })
    } yield result
  }


  /**
    * Creates a [[DBIO]] for reading historical response times. The response time for the [[TestExecutionRow]] with `excludeIdTestExecution`
    * and a startTime timestamp before 'startDateCutoff' will be excluded from the results.
    *
    * @param identifier [[CoreIdentifier]] containing the methodSignature of the [[TestExecutionLike]]
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @param startDateLowerBound only include cases that start after/on this lower bound date.
    * @return a [[DBIO]] for reading historical response times.
    */
  override def responseTimesQuery[I: IdentifierType](identifier: I,
                                  excludeIdTestExecution: Int,
                                  startDateLowerBound: LocalDate): DBIO[Seq[Double]] = {
    val timestampLowerBound: Timestamp = Timestamp.valueOf(startDateLowerBound.atStartOfDay)
    for {
      maybeRow <- this.testExecutionsQuery(identifier)
      result <- DBIO.successful(maybeRow.filter(testExecution => testExecution.idTestExecution != excludeIdTestExecution &&
                                                           (testExecution.startTime.after(timestampLowerBound) ||
                                                            testExecution.startTime.equals(timestampLowerBound)))
                                        .map(_.responseTime))
    } yield result
  }


  /**
    * Creates a [[DBIO]] for reading historical Measurement rows.
    *
    * @param identifier a [[CoreIdentifier]] containing the idTestDefinition of the [[TestExecutionLike]]
    * @param excludeIdTestExecution idTestExecution to exclude from results.
    * @return a [[DBIO]] for reading historical measurement rows.
    */
  override def measurementQuery[I: IdentifierType](identifier: I, excludeIdTestExecution: Int):
      DBIO[Seq[(MeasurementRowWrapper, Double)]] = {
    val query: Query[(Measurement, Rep[Double]), (MeasurementRow, Double), Seq] = for {
      testDefinition <- TestDefinition if testDefinition.idTestDefinition === identifier.idTestDefinition
      testExecution <- TestExecution.sortBy(_.startTime.desc) if testExecution.idTestDefinition === testDefinition.idTestDefinition
      measurement <- Measurement if measurement.idTestExecution === testExecution.idTestExecution
      measurementDescription <- MeasurementName if
        measurementDescription.name =!= "java.lang:type=Memory:HeapMemoryUsage (used, delta)" &&
        measurementDescription.idMeasurementName === measurement.idMeasurementName
    } yield (measurement, testExecution.responseTime)

    query.result
  }


  /**
    * Creates a [[DBIO]] for reading the tag with id `idTagName` set on the [[TestExecutionRow]] with id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRow]] to look up tags for.
    * @param idTagName id of the [[TagNameRow]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  override def testExecutionTagsQuery(idTestExecution: Int, idTagName: Int): DBIO[Option[(String, String)]] = {
    (for {
      name <- TagName if name.idTagName === idTagName
      tag <- TestExecutionTag if tag.idTestExecution === idTestExecution && tag.idTagName === idTagName
    } yield (name.name, tag.value)).result.headOption
  }


  /**
    * Creates a [[DBIO]] for reading the entire row of a tag with id `idTagName` set on the [[TestExecutionRow]] with
    * id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRow]] to look up tags for.
    * @param idTagName id of the [[TagNameRow]] to look up.
    * @return a [[DBIO]] for looking up the whole row for a specified tag.
    */
  override def testExecutionTagsRowQuery(idTestExecution: Int, idTagName: Int) : DBIO[Option[TestExecutionTagRowWrapper]]= {
    TestExecutionTag.filter(row => row.idTestExecution === idTestExecution && row.idTagName === idTagName).result.headOption
  }

  /**
    * Creates a [[Query]] for reading the tags set on the [[TestExecutionRow]] with id `idTestExecution`.
    *
    * @param idTestExecution id of the [[TestExecutionRow]] to look up tags for.
    * @return
    */
  override def testExecutionTagsQuery(idTestExecution: Int): Query[(Rep[String], Rep[String]), (String, String), Seq] = {
    for {
      tag <- TestExecutionTag if tag.idTestExecution === idTestExecution
      name <- TagName if tag.idTagName === name.idTagName
    } yield (name.name, tag.value)
  }


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestDefinitionRow]] with id `idTestDefinition`.
    *
    * @param idTestDefinition id of the [[TestDefinitionRow]] to look up tags for.
    * @param idTagName id of the [[TagNameRow]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  override def testDefinitionTagsQuery(idTestDefinition: Int, idTagName: Int):
      Query[(Rep[String], Rep[String]), (String, String), Seq] = {
    for {
      name <- TagName if name.idTagName === idTagName
      tag <- TestDefinitionTag if tag.idTestDefinition === idTestDefinition && tag.idTagName === idTagName
    } yield (name.name, tag.value)
  }


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestDefinitionTagRow]] with id `TestDefinitionTag`.
    *
    * @param idTestDefinitionTag id of the [[TestDefinitionTagRow]] to look up metatags for.
    * @param idTagName id of the [[TagNameRow]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  override def testDefinitionMetaTagQuery(idTestDefinitionTag: Int, idTagName: Int):
      Query[(Rep[String], Rep[String]), (String, String), Seq] = {
    for {
      name <- TagName if name.idTagName === idTagName
      tag <- Tables.TestDefinitionMetaTag if tag.idTestDefinitionTag === idTestDefinitionTag && tag.idTagName === idTagName
    } yield (name.name, tag.value)
  }


  /**
    * Creates a [[Query]] for reading the tag with id `idTagName` set on the [[TestExecutionTagRow]] with id `TestExecutionTag`.
    *
    * @param idTestExecutionTag id of the [[TestExecutionTagRow]] to look up metatags for.
    * @param idTagName id of the [[TagNameRow]] to look up.
    * @return a [[Query]] for looking up a specified tag.
    */
  override def testExecutionMetaTagQuery(idTestExecutionTag: Int, idTagName: Int):
      Query[(Rep[String], Rep[String]), (String, String), Seq] = {
    for {
      name <- TagName if name.idTagName === idTagName
      tag <- Tables.TestExecutionMetaTag if tag.idTestExecutionTag === idTestExecutionTag && tag.idTagName === idTagName
    } yield (name.name, tag.value)
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[Build]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[Build]].
    */
  override def writeBuildQuery[T: BuildRowLikeType](row: T): DBIO[BuildRowWrapper] = {
    Build returning Build.map(_.idBuild) into ((row, id) => row.copy(idBuild = id)) += row
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestDefinition]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestDefinition]].
    */
  override def writeTestDefinitionQuery[T: TestDefinitionRowLikeType](row: T): DBIO[TestDefinitionRowWrapper] = {
    TestDefinition returning TestDefinition.map(_.idTestDefinition) into ((row, id) => row.copy(idTestDefinition = id)) += row
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[MeasurementName]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[MeasurementName]].
    */
  override def writeMeasurementNameQuery[T: MeasurementNameRowLikeType](row: T):
      DBIO[MeasurementNameRowWrapper] = {
    MeasurementName returning MeasurementName.map(_.idMeasurementName) into ((row, id) =>
      row.copy(idMeasurementName = id)) += row
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[MeasurementName]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[MeasurementName]].
    */
  override def writeTagNameQuery[T: TagNameRowLikeType](row: T): DBIO[TagNameRowWrapper] = {
    TagName returning TagName.map(_.idTagName) into ((row, id) => row.copy(idTagName = id)) += row
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestExecution]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestExecution]].
    */
  override def writeTestExecutionQuery[T: TestExecutionRowLikeType](row: T): DBIO[TestExecutionRowWrapper] = {
    TestExecution returning TestExecution.map(_.idTestExecution) into ((row, id) => row.copy(idTestExecution = id)) += row
  }


  /**
   * Creates a [[DBIO]] for inserting or updating `row` into [[TestExecutionTag]] and returning an [[Option]] with
   * the result
   *
   * @param row to be inserted
   * @return a [[DBIO]] (not yet executed) for inserting or updating `row` into [[TestExecutionTag]]
   */
  override def insertOrUpdateTestExecutionTagQuery[T: TestExecutionTagRowLikeType](row: T): DBIO[Option[TestExecutionTagRowWrapper]] = {
    TestExecutionTag returning TestExecutionTag.map(_.idTestExecutionTag) into (
      (row, id) => row.copy(idTestExecutionTag = id)
    ) insertOrUpdate(row)
  }

  /**
   * Creates a [[DBIO]] for inserting or updating `row` into [[TestExecutionMetaTag]] and returning an [[Int]] with the
   * rows affected
   *
   * @param row to be inserted
   * @return a [[DBIO]] (not yet executed) for inserting or updating `row` into [[TestExecutionMetaTag]]
   */
  override def insertOrUpdateTestExecutionMetaTagQuery[T: TestExecutionMetaTagRowLikeType](row: T): DBIO[Int] = {
    TestExecutionMetaTag.insertOrUpdate(row)
  }


  /**
   * Creates a [[DBIO]] for inserting or updating `row` into [[TestDefinitionTag]] and returning an [[Option]] with
   * the result
   *
   * @param row to be inserted
   * @return a [[DBIO]] (not yet executed) for inserting or updating `row` into [[TestDefinitionTag]]
   */
  override def insertOrUpdateTestDefinitionTagQuery[T: TestDefinitionTagRowLikeType](row: T): DBIO[Option[TestDefinitionTagRowWrapper]] = {
    TestDefinitionTag returning TestDefinitionTag.map(_.idTestDefinitionTag) into (
      (row, id) => row.copy(idTestDefinitionTag = id)
    ) insertOrUpdate(row)
  }


  /**
   * Creates a [[DBIO]] for inserting or updating `row` into [[TestDefinitionMetaTag]] and returning an [[Int]] with the
   * rows affected
   *
   * @param row to be inserted
   * @return a [[DBIO]] (not yet executed) for inserting or updating `row` into [[TestDefinitionMetaTag]]
   */
  override def insertOrUpdateTestDefinitionMetaTagQuery[T: TestDefinitionMetaTagRowLikeType](row: T): DBIO[Int] = {
    TestDefinitionMetaTag.insertOrUpdate(row)
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestExecutionTag]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestExecutionTag]].
    */
  override def writeTestExecutionTagQuery[T: TestExecutionTagRowLikeType](row: T): DBIO[TestExecutionTagRowWrapper] = {
    TestExecutionTag returning TestExecutionTag.map(_.idTestExecutionTag) into ((row, id) => row.copy(idTestExecutionTag = id)) += row
  }


  /**
    * Creates a [[DBIO]] for inserting `row` into [[TestDefinitionTag]] and returning it with updated auto-increment id.
    *
    * @param row row to be inserted.
    * @return a [[DBIO]] (not yet executed) for inserting `row` into [[TestDefinitionTag]].
    */
  override def writeTestDefinitionTagQuery[T: TestDefinitionTagRowLikeType](row: T): DBIO[TestDefinitionTagRowWrapper] = {
    TestDefinitionTag returning TestDefinitionTag.map(_.idTestDefinitionTag) into ((row, id) => row.copy(idTestDefinitionTag = id)) += row
  }


  /**
    * Creates a [[DBIO]] for updating the threshold of `testExecution`.
    *
    * @param testExecution [[TestExecutionRow]] to update.
    * @param newThreshold new threshold to set on `testExecution`.
    * @return a [[DBIO]] (not yet executed) for updating the threshold of `testExecution`.
    */
  override def updateTestExecutionThreshold[T: TestExecutionRowLikeType](testExecution: T, newThreshold: Double): DBIO[Int] = {
    TestExecution filter { _.idTestExecution === testExecution.idTestExecution } map { _.responseTimeRequirement } update newThreshold
  }
}
