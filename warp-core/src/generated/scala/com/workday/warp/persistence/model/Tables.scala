package com.workday.warp.persistence.model
// !!! AUTO-GENERATED Slick data model, do not modify.
import com.workday.warp.persistence.TablesLike._

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(Build.schema, Measurement.schema, MeasurementName.schema, TagName.schema, TestDefinition.schema, TestDefinitionMetaTag.schema, TestDefinitionTag.schema, TestExecution.schema, TestExecutionMetaTag.schema, TestExecutionTag.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  object RowTypeClasses {
    implicit object BuildRowTypeClassObject extends BuildRowLikeType[BuildRow] {
      def idBuild(row: BuildRow): Int = row.idBuild
      def major(row: BuildRow): Int = row.major
      def minor(row: BuildRow): Int = row.minor
      def patch(row: BuildRow): Int = row.patch
      def firstTested(row: BuildRow): java.sql.Timestamp = row.firstTested
      def lastTested(row: BuildRow): java.sql.Timestamp = row.lastTested
    }

    implicit object BuildRowWrapperTypeClassObject extends BuildRowLikeType[BuildRowWrapper] {
      def idBuild(row: BuildRowWrapper): Int = row.idBuild
      def major(row: BuildRowWrapper): Int = row.major
      def minor(row: BuildRowWrapper): Int = row.minor
      def patch(row: BuildRowWrapper): Int = row.patch
      def firstTested(row: BuildRowWrapper): java.sql.Timestamp = row.firstTested
      def lastTested(row: BuildRowWrapper): java.sql.Timestamp = row.lastTested
    }
    implicit object MeasurementRowTypeClassObject extends MeasurementRowLikeType[MeasurementRow] {
      def idTestExecution(row: MeasurementRow): Int = row.idTestExecution
      def idMeasurementName(row: MeasurementRow): Int = row.idMeasurementName
      def result(row: MeasurementRow): Double = row.result
    }

    implicit object MeasurementRowWrapperTypeClassObject extends MeasurementRowLikeType[MeasurementRowWrapper] {
      def idTestExecution(row: MeasurementRowWrapper): Int = row.idTestExecution
      def idMeasurementName(row: MeasurementRowWrapper): Int = row.idMeasurementName
      def result(row: MeasurementRowWrapper): Double = row.result
    }
    implicit object MeasurementNameRowTypeClassObject extends MeasurementNameRowLikeType[MeasurementNameRow] {
      def idMeasurementName(row: MeasurementNameRow): Int = row.idMeasurementName
      def name(row: MeasurementNameRow): String = row.name
    }

    implicit object MeasurementNameRowWrapperTypeClassObject extends MeasurementNameRowLikeType[MeasurementNameRowWrapper] {
      def idMeasurementName(row: MeasurementNameRowWrapper): Int = row.idMeasurementName
      def name(row: MeasurementNameRowWrapper): String = row.name
    }
    implicit object TagNameRowTypeClassObject extends TagNameRowLikeType[TagNameRow] {
      def idTagName(row: TagNameRow): Int = row.idTagName
      def name(row: TagNameRow): String = row.name
      def nameType(row: TagNameRow): String = row.nameType
      def isUserGenerated(row: TagNameRow): Boolean = row.isUserGenerated
    }

    implicit object TagNameRowWrapperTypeClassObject extends TagNameRowLikeType[TagNameRowWrapper] {
      def idTagName(row: TagNameRowWrapper): Int = row.idTagName
      def name(row: TagNameRowWrapper): String = row.name
      def nameType(row: TagNameRowWrapper): String = row.nameType
      def isUserGenerated(row: TagNameRowWrapper): Boolean = row.isUserGenerated
    }
    implicit object TestDefinitionRowTypeClassObject extends TestDefinitionRowLikeType[TestDefinitionRow] {
      def idTestDefinition(row: TestDefinitionRow): Int = row.idTestDefinition
      def methodSignature(row: TestDefinitionRow): String = row.methodSignature
      def active(row: TestDefinitionRow): Boolean = row.active
      def productName(row: TestDefinitionRow): String = row.productName
      def subProductName(row: TestDefinitionRow): String = row.subProductName
      def className(row: TestDefinitionRow): String = row.className
      def methodName(row: TestDefinitionRow): String = row.methodName
      def documentation(row: TestDefinitionRow): Option[String] = row.documentation
    }

    implicit object TestDefinitionRowWrapperTypeClassObject extends TestDefinitionRowLikeType[TestDefinitionRowWrapper] {
      def idTestDefinition(row: TestDefinitionRowWrapper): Int = row.idTestDefinition
      def methodSignature(row: TestDefinitionRowWrapper): String = row.methodSignature
      def active(row: TestDefinitionRowWrapper): Boolean = row.active
      def productName(row: TestDefinitionRowWrapper): String = row.productName
      def subProductName(row: TestDefinitionRowWrapper): String = row.subProductName
      def className(row: TestDefinitionRowWrapper): String = row.className
      def methodName(row: TestDefinitionRowWrapper): String = row.methodName
      def documentation(row: TestDefinitionRowWrapper): Option[String] = row.documentation
    }
    implicit object TestDefinitionMetaTagRowTypeClassObject extends TestDefinitionMetaTagRowLikeType[TestDefinitionMetaTagRow] {
      def idTestDefinitionTag(row: TestDefinitionMetaTagRow): Int = row.idTestDefinitionTag
      def idTagName(row: TestDefinitionMetaTagRow): Int = row.idTagName
      def value(row: TestDefinitionMetaTagRow): String = row.value
    }

    implicit object TestDefinitionMetaTagRowWrapperTypeClassObject extends TestDefinitionMetaTagRowLikeType[TestDefinitionMetaTagRowWrapper] {
      def idTestDefinitionTag(row: TestDefinitionMetaTagRowWrapper): Int = row.idTestDefinitionTag
      def idTagName(row: TestDefinitionMetaTagRowWrapper): Int = row.idTagName
      def value(row: TestDefinitionMetaTagRowWrapper): String = row.value
    }
    implicit object TestDefinitionTagRowTypeClassObject extends TestDefinitionTagRowLikeType[TestDefinitionTagRow] {
      def idTestDefinitionTag(row: TestDefinitionTagRow): Int = row.idTestDefinitionTag
      def idTestDefinition(row: TestDefinitionTagRow): Int = row.idTestDefinition
      def idTagName(row: TestDefinitionTagRow): Int = row.idTagName
      def value(row: TestDefinitionTagRow): String = row.value
    }

    implicit object TestDefinitionTagRowWrapperTypeClassObject extends TestDefinitionTagRowLikeType[TestDefinitionTagRowWrapper] {
      def idTestDefinitionTag(row: TestDefinitionTagRowWrapper): Int = row.idTestDefinitionTag
      def idTestDefinition(row: TestDefinitionTagRowWrapper): Int = row.idTestDefinition
      def idTagName(row: TestDefinitionTagRowWrapper): Int = row.idTagName
      def value(row: TestDefinitionTagRowWrapper): String = row.value
    }
    implicit object TestExecutionRowTypeClassObject extends TestExecutionRowLikeType[TestExecutionRow] {
      def idTestExecution(row: TestExecutionRow): Int = row.idTestExecution
      def idTestDefinition(row: TestExecutionRow): Int = row.idTestDefinition
      def idBuild(row: TestExecutionRow): Int = row.idBuild
      def passed(row: TestExecutionRow): Boolean = row.passed
      def responseTime(row: TestExecutionRow): Double = row.responseTime
      def responseTimeRequirement(row: TestExecutionRow): Double = row.responseTimeRequirement
      def startTime(row: TestExecutionRow): java.sql.Timestamp = row.startTime
      def endTime(row: TestExecutionRow): java.sql.Timestamp = row.endTime
    }

    implicit object TestExecutionRowWrapperTypeClassObject extends TestExecutionRowLikeType[TestExecutionRowWrapper] {
      def idTestExecution(row: TestExecutionRowWrapper): Int = row.idTestExecution
      def idTestDefinition(row: TestExecutionRowWrapper): Int = row.idTestDefinition
      def idBuild(row: TestExecutionRowWrapper): Int = row.idBuild
      def passed(row: TestExecutionRowWrapper): Boolean = row.passed
      def responseTime(row: TestExecutionRowWrapper): Double = row.responseTime
      def responseTimeRequirement(row: TestExecutionRowWrapper): Double = row.responseTimeRequirement
      def startTime(row: TestExecutionRowWrapper): java.sql.Timestamp = row.startTime
      def endTime(row: TestExecutionRowWrapper): java.sql.Timestamp = row.endTime
    }
    implicit object TestExecutionMetaTagRowTypeClassObject extends TestExecutionMetaTagRowLikeType[TestExecutionMetaTagRow] {
      def idTestExecutionTag(row: TestExecutionMetaTagRow): Int = row.idTestExecutionTag
      def idTagName(row: TestExecutionMetaTagRow): Int = row.idTagName
      def value(row: TestExecutionMetaTagRow): String = row.value
    }

    implicit object TestExecutionMetaTagRowWrapperTypeClassObject extends TestExecutionMetaTagRowLikeType[TestExecutionMetaTagRowWrapper] {
      def idTestExecutionTag(row: TestExecutionMetaTagRowWrapper): Int = row.idTestExecutionTag
      def idTagName(row: TestExecutionMetaTagRowWrapper): Int = row.idTagName
      def value(row: TestExecutionMetaTagRowWrapper): String = row.value
    }
    implicit object TestExecutionTagRowTypeClassObject extends TestExecutionTagRowLikeType[TestExecutionTagRow] {
      def idTestExecutionTag(row: TestExecutionTagRow): Int = row.idTestExecutionTag
      def idTestExecution(row: TestExecutionTagRow): Int = row.idTestExecution
      def idTagName(row: TestExecutionTagRow): Int = row.idTagName
      def value(row: TestExecutionTagRow): String = row.value
    }

    implicit object TestExecutionTagRowWrapperTypeClassObject extends TestExecutionTagRowLikeType[TestExecutionTagRowWrapper] {
      def idTestExecutionTag(row: TestExecutionTagRowWrapper): Int = row.idTestExecutionTag
      def idTestExecution(row: TestExecutionTagRowWrapper): Int = row.idTestExecution
      def idTagName(row: TestExecutionTagRowWrapper): Int = row.idTagName
      def value(row: TestExecutionTagRowWrapper): String = row.value
    }
  }

  /** Entity class storing rows of table Build
   *  @param idBuild Database column idBuild SqlType(INT), AutoInc, PrimaryKey
   *  @param major Database column major SqlType(INT)
   *  @param minor Database column minor SqlType(INT)
   *  @param patch Database column patch SqlType(INT)
   *  @param firstTested Database column firstTested SqlType(DATETIME)
   *  @param lastTested Database column lastTested SqlType(DATETIME) */
  class BuildRowWrapper(val idBuild: Int, val major: Int, val minor: Int, val patch: Int, val firstTested: java.sql.Timestamp, val lastTested: java.sql.Timestamp) extends BuildRowLike
  case class BuildRow(override val idBuild: Int, override val major: Int, override val minor: Int, override val patch: Int, override val firstTested: java.sql.Timestamp, override val lastTested: java.sql.Timestamp) extends BuildRowWrapper(idBuild, major, minor, patch, firstTested, lastTested)
  implicit def BuildRowWrapper2BuildRow(x: BuildRowWrapper): BuildRow = BuildRow(x.idBuild, x.major, x.minor, x.patch, x.firstTested, x.lastTested)
  implicit def BuildRow2BuildRowWrapper(x: BuildRow): BuildRowWrapper = new BuildRowWrapper(x.idBuild, x.major, x.minor, x.patch, x.firstTested, x.lastTested)
  implicit def BuildRowFromTypeClass[T: BuildRowLikeType](x: T): BuildRow = BuildRow(implicitly[BuildRowLikeType[T]].idBuild(x), implicitly[BuildRowLikeType[T]].major(x), implicitly[BuildRowLikeType[T]].minor(x), implicitly[BuildRowLikeType[T]].patch(x), implicitly[BuildRowLikeType[T]].firstTested(x), implicitly[BuildRowLikeType[T]].lastTested(x))
  /** GetResult implicit for fetching BuildRow objects using plain SQL queries */
  implicit def GetResultBuildRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[BuildRow] = GR{
    prs => import prs._
    BuildRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table Build. Objects of this class serve as prototypes for rows in queries. */
  class Build(_tableTag: Tag) extends profile.api.Table[BuildRow](_tableTag, None, "Build") with BuildLike {
    def * = (idBuild, major, minor, patch, firstTested, lastTested) <> (BuildRow.tupled, BuildRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idBuild), Rep.Some(major), Rep.Some(minor), Rep.Some(patch), Rep.Some(firstTested), Rep.Some(lastTested)).shaped.<>({r=>import r._; _1.map(_=> BuildRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idBuild SqlType(INT), AutoInc, PrimaryKey */
    val idBuild: Rep[Int] = column[Int]("idBuild", O.AutoInc, O.PrimaryKey)
    /** Database column major SqlType(INT) */
    val major: Rep[Int] = column[Int]("major")
    /** Database column minor SqlType(INT) */
    val minor: Rep[Int] = column[Int]("minor")
    /** Database column patch SqlType(INT) */
    val patch: Rep[Int] = column[Int]("patch")
    /** Database column firstTested SqlType(DATETIME) */
    val firstTested: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("firstTested")
    /** Database column lastTested SqlType(DATETIME) */
    val lastTested: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("lastTested")

    /** Uniqueness Index over (major,minor,patch) (database name buildNumber) */
    val index1 = index("buildNumber", (major, minor, patch), unique=true)
  }
  /** Collection-like TableQuery object for table Build */
  lazy val Build = new TableQuery(tag => new Build(tag))

  /** Entity class storing rows of table Measurement
   *  @param idTestExecution Database column idTestExecution SqlType(INT)
   *  @param idMeasurementName Database column idMeasurementName SqlType(INT)
   *  @param result Database column result SqlType(DOUBLE) */
  class MeasurementRowWrapper(val idTestExecution: Int, val idMeasurementName: Int, val result: Double) extends MeasurementRowLike
  case class MeasurementRow(override val idTestExecution: Int, override val idMeasurementName: Int, override val result: Double) extends MeasurementRowWrapper(idTestExecution, idMeasurementName, result)
  implicit def MeasurementRowWrapper2MeasurementRow(x: MeasurementRowWrapper): MeasurementRow = MeasurementRow(x.idTestExecution, x.idMeasurementName, x.result)
  implicit def MeasurementRow2MeasurementRowWrapper(x: MeasurementRow): MeasurementRowWrapper = new MeasurementRowWrapper(x.idTestExecution, x.idMeasurementName, x.result)
  implicit def MeasurementRowFromTypeClass[T: MeasurementRowLikeType](x: T): MeasurementRow = MeasurementRow(implicitly[MeasurementRowLikeType[T]].idTestExecution(x), implicitly[MeasurementRowLikeType[T]].idMeasurementName(x), implicitly[MeasurementRowLikeType[T]].result(x))
  /** GetResult implicit for fetching MeasurementRow objects using plain SQL queries */
  implicit def GetResultMeasurementRow(implicit e0: GR[Int], e1: GR[Double]): GR[MeasurementRow] = GR{
    prs => import prs._
    MeasurementRow.tupled((<<[Int], <<[Int], <<[Double]))
  }
  /** Table description of table Measurement. Objects of this class serve as prototypes for rows in queries. */
  class Measurement(_tableTag: Tag) extends profile.api.Table[MeasurementRow](_tableTag, None, "Measurement") with MeasurementLike {
    def * = (idTestExecution, idMeasurementName, result) <> (MeasurementRow.tupled, MeasurementRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestExecution), Rep.Some(idMeasurementName), Rep.Some(result)).shaped.<>({r=>import r._; _1.map(_=> MeasurementRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestExecution SqlType(INT) */
    val idTestExecution: Rep[Int] = column[Int]("idTestExecution")
    /** Database column idMeasurementName SqlType(INT) */
    val idMeasurementName: Rep[Int] = column[Int]("idMeasurementName")
    /** Database column result SqlType(DOUBLE) */
    val result: Rep[Double] = column[Double]("result")

    /** Primary key of Measurement (database name Measurement_PK) */
    val pk = primaryKey("Measurement_PK", (idTestExecution, idMeasurementName))

    /** Foreign key referencing MeasurementName (database name idMeasurementDescription) */
    lazy val measurementNameFk = foreignKey("idMeasurementDescription", idMeasurementName, MeasurementName)(r => r.idMeasurementName, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestExecution (database name idTestExecution_Measurement) */
    lazy val testExecutionFk = foreignKey("idTestExecution_Measurement", idTestExecution, TestExecution)(r => r.idTestExecution, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Measurement */
  lazy val Measurement = new TableQuery(tag => new Measurement(tag))

  /** Entity class storing rows of table MeasurementName
   *  @param idMeasurementName Database column idMeasurementName SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(255,true) */
  class MeasurementNameRowWrapper(val idMeasurementName: Int, val name: String) extends MeasurementNameRowLike
  case class MeasurementNameRow(override val idMeasurementName: Int, override val name: String) extends MeasurementNameRowWrapper(idMeasurementName, name)
  implicit def MeasurementNameRowWrapper2MeasurementNameRow(x: MeasurementNameRowWrapper): MeasurementNameRow = MeasurementNameRow(x.idMeasurementName, x.name)
  implicit def MeasurementNameRow2MeasurementNameRowWrapper(x: MeasurementNameRow): MeasurementNameRowWrapper = new MeasurementNameRowWrapper(x.idMeasurementName, x.name)
  implicit def MeasurementNameRowFromTypeClass[T: MeasurementNameRowLikeType](x: T): MeasurementNameRow = MeasurementNameRow(implicitly[MeasurementNameRowLikeType[T]].idMeasurementName(x), implicitly[MeasurementNameRowLikeType[T]].name(x))
  /** GetResult implicit for fetching MeasurementNameRow objects using plain SQL queries */
  implicit def GetResultMeasurementNameRow(implicit e0: GR[Int], e1: GR[String]): GR[MeasurementNameRow] = GR{
    prs => import prs._
    MeasurementNameRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table MeasurementName. Objects of this class serve as prototypes for rows in queries. */
  class MeasurementName(_tableTag: Tag) extends profile.api.Table[MeasurementNameRow](_tableTag, None, "MeasurementName") with MeasurementNameLike {
    def * = (idMeasurementName, name) <> (MeasurementNameRow.tupled, MeasurementNameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idMeasurementName), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> MeasurementNameRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idMeasurementName SqlType(INT), AutoInc, PrimaryKey */
    val idMeasurementName: Rep[Int] = column[Int]("idMeasurementName", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))

    /** Uniqueness Index over (name) (database name measurementName_UNIQUE) */
    val index1 = index("measurementName_UNIQUE", name, unique=true)
  }
  /** Collection-like TableQuery object for table MeasurementName */
  lazy val MeasurementName = new TableQuery(tag => new MeasurementName(tag))

  /** Entity class storing rows of table TagName
   *  @param idTagName Database column idTagName SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(255,true)
   *  @param nameType Database column nameType SqlType(VARCHAR), Length(255,true), Default(plain_txt)
   *  @param isUserGenerated Database column isUserGenerated SqlType(BIT), Default(true) */
  class TagNameRowWrapper(val idTagName: Int, val name: String, val nameType: String = "plain_txt", val isUserGenerated: Boolean = true) extends TagNameRowLike
  case class TagNameRow(override val idTagName: Int, override val name: String, override val nameType: String = "plain_txt", override val isUserGenerated: Boolean = true) extends TagNameRowWrapper(idTagName, name, nameType, isUserGenerated)
  implicit def TagNameRowWrapper2TagNameRow(x: TagNameRowWrapper): TagNameRow = TagNameRow(x.idTagName, x.name, x.nameType, x.isUserGenerated)
  implicit def TagNameRow2TagNameRowWrapper(x: TagNameRow): TagNameRowWrapper = new TagNameRowWrapper(x.idTagName, x.name, x.nameType, x.isUserGenerated)
  implicit def TagNameRowFromTypeClass[T: TagNameRowLikeType](x: T): TagNameRow = TagNameRow(implicitly[TagNameRowLikeType[T]].idTagName(x), implicitly[TagNameRowLikeType[T]].name(x), implicitly[TagNameRowLikeType[T]].nameType(x), implicitly[TagNameRowLikeType[T]].isUserGenerated(x))
  /** GetResult implicit for fetching TagNameRow objects using plain SQL queries */
  implicit def GetResultTagNameRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean]): GR[TagNameRow] = GR{
    prs => import prs._
    TagNameRow.tupled((<<[Int], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table TagName. Objects of this class serve as prototypes for rows in queries. */
  class TagName(_tableTag: Tag) extends profile.api.Table[TagNameRow](_tableTag, None, "TagName") with TagNameLike {
    def * = (idTagName, name, nameType, isUserGenerated) <> (TagNameRow.tupled, TagNameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTagName), Rep.Some(name), Rep.Some(nameType), Rep.Some(isUserGenerated)).shaped.<>({r=>import r._; _1.map(_=> TagNameRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTagName SqlType(INT), AutoInc, PrimaryKey */
    val idTagName: Rep[Int] = column[Int]("idTagName", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
    /** Database column nameType SqlType(VARCHAR), Length(255,true), Default(plain_txt) */
    val nameType: Rep[String] = column[String]("nameType", O.Length(255,varying=true), O.Default("plain_txt"))
    /** Database column isUserGenerated SqlType(BIT), Default(true) */
    val isUserGenerated: Rep[Boolean] = column[Boolean]("isUserGenerated", O.Default(true))

    /** Uniqueness Index over (name) (database name tagName_UNIQUE) */
    val index1 = index("tagName_UNIQUE", name, unique=true)
  }
  /** Collection-like TableQuery object for table TagName */
  lazy val TagName = new TableQuery(tag => new TagName(tag))

  /** Entity class storing rows of table TestDefinition
   *  @param idTestDefinition Database column idTestDefinition SqlType(INT), AutoInc, PrimaryKey
   *  @param methodSignature Database column methodSignature SqlType(VARCHAR), Length(255,true)
   *  @param active Database column active SqlType(BIT)
   *  @param productName Database column productName SqlType(VARCHAR), Length(255,true)
   *  @param subProductName Database column subProductName SqlType(VARCHAR), Length(255,true)
   *  @param className Database column className SqlType(VARCHAR), Length(255,true)
   *  @param methodName Database column methodName SqlType(VARCHAR), Length(255,true)
   *  @param documentation Database column documentation SqlType(TEXT), Default(None) */
  class TestDefinitionRowWrapper(val idTestDefinition: Int, val methodSignature: String, val active: Boolean, val productName: String, val subProductName: String, val className: String, val methodName: String, val documentation: Option[String] = None) extends TestDefinitionRowLike
  case class TestDefinitionRow(override val idTestDefinition: Int, override val methodSignature: String, override val active: Boolean, override val productName: String, override val subProductName: String, override val className: String, override val methodName: String, override val documentation: Option[String] = None) extends TestDefinitionRowWrapper(idTestDefinition, methodSignature, active, productName, subProductName, className, methodName, documentation)
  implicit def TestDefinitionRowWrapper2TestDefinitionRow(x: TestDefinitionRowWrapper): TestDefinitionRow = TestDefinitionRow(x.idTestDefinition, x.methodSignature, x.active, x.productName, x.subProductName, x.className, x.methodName, x.documentation)
  implicit def TestDefinitionRow2TestDefinitionRowWrapper(x: TestDefinitionRow): TestDefinitionRowWrapper = new TestDefinitionRowWrapper(x.idTestDefinition, x.methodSignature, x.active, x.productName, x.subProductName, x.className, x.methodName, x.documentation)
  implicit def TestDefinitionRowFromTypeClass[T: TestDefinitionRowLikeType](x: T): TestDefinitionRow = TestDefinitionRow(implicitly[TestDefinitionRowLikeType[T]].idTestDefinition(x), implicitly[TestDefinitionRowLikeType[T]].methodSignature(x), implicitly[TestDefinitionRowLikeType[T]].active(x), implicitly[TestDefinitionRowLikeType[T]].productName(x), implicitly[TestDefinitionRowLikeType[T]].subProductName(x), implicitly[TestDefinitionRowLikeType[T]].className(x), implicitly[TestDefinitionRowLikeType[T]].methodName(x), implicitly[TestDefinitionRowLikeType[T]].documentation(x))
  /** GetResult implicit for fetching TestDefinitionRow objects using plain SQL queries */
  implicit def GetResultTestDefinitionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean], e3: GR[Option[String]]): GR[TestDefinitionRow] = GR{
    prs => import prs._
    TestDefinitionRow.tupled((<<[Int], <<[String], <<[Boolean], <<[String], <<[String], <<[String], <<[String], <<?[String]))
  }
  /** Table description of table TestDefinition. Objects of this class serve as prototypes for rows in queries. */
  class TestDefinition(_tableTag: Tag) extends profile.api.Table[TestDefinitionRow](_tableTag, None, "TestDefinition") with TestDefinitionLike {
    def * = (idTestDefinition, methodSignature, active, productName, subProductName, className, methodName, documentation) <> (TestDefinitionRow.tupled, TestDefinitionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestDefinition), Rep.Some(methodSignature), Rep.Some(active), Rep.Some(productName), Rep.Some(subProductName), Rep.Some(className), Rep.Some(methodName), documentation).shaped.<>({r=>import r._; _1.map(_=> TestDefinitionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestDefinition SqlType(INT), AutoInc, PrimaryKey */
    val idTestDefinition: Rep[Int] = column[Int]("idTestDefinition", O.AutoInc, O.PrimaryKey)
    /** Database column methodSignature SqlType(VARCHAR), Length(255,true) */
    val methodSignature: Rep[String] = column[String]("methodSignature", O.Length(255,varying=true))
    /** Database column active SqlType(BIT) */
    val active: Rep[Boolean] = column[Boolean]("active")
    /** Database column productName SqlType(VARCHAR), Length(255,true) */
    val productName: Rep[String] = column[String]("productName", O.Length(255,varying=true))
    /** Database column subProductName SqlType(VARCHAR), Length(255,true) */
    val subProductName: Rep[String] = column[String]("subProductName", O.Length(255,varying=true))
    /** Database column className SqlType(VARCHAR), Length(255,true) */
    val className: Rep[String] = column[String]("className", O.Length(255,varying=true))
    /** Database column methodName SqlType(VARCHAR), Length(255,true) */
    val methodName: Rep[String] = column[String]("methodName", O.Length(255,varying=true))
    /** Database column documentation SqlType(TEXT), Default(None) */
    val documentation: Rep[Option[String]] = column[Option[String]]("documentation", O.Default(None))

    /** Uniqueness Index over (methodSignature) (database name methodSignature) */
    val index1 = index("methodSignature", methodSignature, unique=true)
    /** Index over (productName,subProductName,className,methodName) (database name reportingDescription) */
    val index2 = index("reportingDescription", (productName, subProductName, className, methodName))
  }
  /** Collection-like TableQuery object for table TestDefinition */
  lazy val TestDefinition = new TableQuery(tag => new TestDefinition(tag))

  /** Entity class storing rows of table TestDefinitionMetaTag
   *  @param idTestDefinitionTag Database column idTestDefinitionTag SqlType(INT)
   *  @param idTagName Database column idTagName SqlType(INT)
   *  @param value Database column value SqlType(VARCHAR), Length(255,true) */
  class TestDefinitionMetaTagRowWrapper(val idTestDefinitionTag: Int, val idTagName: Int, val value: String) extends TestDefinitionMetaTagRowLike
  case class TestDefinitionMetaTagRow(override val idTestDefinitionTag: Int, override val idTagName: Int, override val value: String) extends TestDefinitionMetaTagRowWrapper(idTestDefinitionTag, idTagName, value)
  implicit def TestDefinitionMetaTagRowWrapper2TestDefinitionMetaTagRow(x: TestDefinitionMetaTagRowWrapper): TestDefinitionMetaTagRow = TestDefinitionMetaTagRow(x.idTestDefinitionTag, x.idTagName, x.value)
  implicit def TestDefinitionMetaTagRow2TestDefinitionMetaTagRowWrapper(x: TestDefinitionMetaTagRow): TestDefinitionMetaTagRowWrapper = new TestDefinitionMetaTagRowWrapper(x.idTestDefinitionTag, x.idTagName, x.value)
  implicit def TestDefinitionMetaTagRowFromTypeClass[T: TestDefinitionMetaTagRowLikeType](x: T): TestDefinitionMetaTagRow = TestDefinitionMetaTagRow(implicitly[TestDefinitionMetaTagRowLikeType[T]].idTestDefinitionTag(x), implicitly[TestDefinitionMetaTagRowLikeType[T]].idTagName(x), implicitly[TestDefinitionMetaTagRowLikeType[T]].value(x))
  /** GetResult implicit for fetching TestDefinitionMetaTagRow objects using plain SQL queries */
  implicit def GetResultTestDefinitionMetaTagRow(implicit e0: GR[Int], e1: GR[String]): GR[TestDefinitionMetaTagRow] = GR{
    prs => import prs._
    TestDefinitionMetaTagRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table TestDefinitionMetaTag. Objects of this class serve as prototypes for rows in queries. */
  class TestDefinitionMetaTag(_tableTag: Tag) extends profile.api.Table[TestDefinitionMetaTagRow](_tableTag, None, "TestDefinitionMetaTag") with TestDefinitionMetaTagLike {
    def * = (idTestDefinitionTag, idTagName, value) <> (TestDefinitionMetaTagRow.tupled, TestDefinitionMetaTagRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestDefinitionTag), Rep.Some(idTagName), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> TestDefinitionMetaTagRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestDefinitionTag SqlType(INT) */
    val idTestDefinitionTag: Rep[Int] = column[Int]("idTestDefinitionTag")
    /** Database column idTagName SqlType(INT) */
    val idTagName: Rep[Int] = column[Int]("idTagName")
    /** Database column value SqlType(VARCHAR), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))

    /** Primary key of TestDefinitionMetaTag (database name TestDefinitionMetaTag_PK) */
    val pk = primaryKey("TestDefinitionMetaTag_PK", (idTestDefinitionTag, idTagName))

    /** Foreign key referencing TagName (database name idTagName_DefinitionMetaTag) */
    lazy val tagNameFk = foreignKey("idTagName_DefinitionMetaTag", idTagName, TagName)(r => r.idTagName, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestDefinitionTag (database name idTestDefinitionTag_DefinitionMetaTag) */
    lazy val testDefinitionTagFk = foreignKey("idTestDefinitionTag_DefinitionMetaTag", idTestDefinitionTag, TestDefinitionTag)(r => r.idTestDefinitionTag, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table TestDefinitionMetaTag */
  lazy val TestDefinitionMetaTag = new TableQuery(tag => new TestDefinitionMetaTag(tag))

  /** Entity class storing rows of table TestDefinitionTag
   *  @param idTestDefinitionTag Database column idTestDefinitionTag SqlType(INT), AutoInc, PrimaryKey
   *  @param idTestDefinition Database column idTestDefinition SqlType(INT)
   *  @param idTagName Database column idTagName SqlType(INT)
   *  @param value Database column value SqlType(VARCHAR), Length(255,true) */
  class TestDefinitionTagRowWrapper(val idTestDefinitionTag: Int, val idTestDefinition: Int, val idTagName: Int, val value: String) extends TestDefinitionTagRowLike
  case class TestDefinitionTagRow(override val idTestDefinitionTag: Int, override val idTestDefinition: Int, override val idTagName: Int, override val value: String) extends TestDefinitionTagRowWrapper(idTestDefinitionTag, idTestDefinition, idTagName, value)
  implicit def TestDefinitionTagRowWrapper2TestDefinitionTagRow(x: TestDefinitionTagRowWrapper): TestDefinitionTagRow = TestDefinitionTagRow(x.idTestDefinitionTag, x.idTestDefinition, x.idTagName, x.value)
  implicit def TestDefinitionTagRow2TestDefinitionTagRowWrapper(x: TestDefinitionTagRow): TestDefinitionTagRowWrapper = new TestDefinitionTagRowWrapper(x.idTestDefinitionTag, x.idTestDefinition, x.idTagName, x.value)
  implicit def TestDefinitionTagRowFromTypeClass[T: TestDefinitionTagRowLikeType](x: T): TestDefinitionTagRow = TestDefinitionTagRow(implicitly[TestDefinitionTagRowLikeType[T]].idTestDefinitionTag(x), implicitly[TestDefinitionTagRowLikeType[T]].idTestDefinition(x), implicitly[TestDefinitionTagRowLikeType[T]].idTagName(x), implicitly[TestDefinitionTagRowLikeType[T]].value(x))
  /** GetResult implicit for fetching TestDefinitionTagRow objects using plain SQL queries */
  implicit def GetResultTestDefinitionTagRow(implicit e0: GR[Int], e1: GR[String]): GR[TestDefinitionTagRow] = GR{
    prs => import prs._
    TestDefinitionTagRow.tupled((<<[Int], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table TestDefinitionTag. Objects of this class serve as prototypes for rows in queries. */
  class TestDefinitionTag(_tableTag: Tag) extends profile.api.Table[TestDefinitionTagRow](_tableTag, None, "TestDefinitionTag") with TestDefinitionTagLike {
    def * = (idTestDefinitionTag, idTestDefinition, idTagName, value) <> (TestDefinitionTagRow.tupled, TestDefinitionTagRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestDefinitionTag), Rep.Some(idTestDefinition), Rep.Some(idTagName), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> TestDefinitionTagRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestDefinitionTag SqlType(INT), AutoInc, PrimaryKey */
    val idTestDefinitionTag: Rep[Int] = column[Int]("idTestDefinitionTag", O.AutoInc, O.PrimaryKey)
    /** Database column idTestDefinition SqlType(INT) */
    val idTestDefinition: Rep[Int] = column[Int]("idTestDefinition")
    /** Database column idTagName SqlType(INT) */
    val idTagName: Rep[Int] = column[Int]("idTagName")
    /** Database column value SqlType(VARCHAR), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))

    /** Foreign key referencing TagName (database name idTagDescription_TestDefinitionTag) */
    lazy val tagNameFk = foreignKey("idTagDescription_TestDefinitionTag", idTagName, TagName)(r => r.idTagName, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestDefinition (database name idTestDefinition_Tag) */
    lazy val testDefinitionFk = foreignKey("idTestDefinition_Tag", idTestDefinition, TestDefinition)(r => r.idTestDefinition, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (value,idTagName,idTestDefinition) (database name idTestDefinition_value_TagName_unique) */
    val index1 = index("idTestDefinition_value_TagName_unique", (value, idTagName, idTestDefinition), unique=true)
  }
  /** Collection-like TableQuery object for table TestDefinitionTag */
  lazy val TestDefinitionTag = new TableQuery(tag => new TestDefinitionTag(tag))

  /** Entity class storing rows of table TestExecution
   *  @param idTestExecution Database column idTestExecution SqlType(INT), AutoInc, PrimaryKey
   *  @param idTestDefinition Database column idTestDefinition SqlType(INT)
   *  @param idBuild Database column idBuild SqlType(INT)
   *  @param passed Database column passed SqlType(BIT)
   *  @param responseTime Database column responseTime SqlType(DOUBLE)
   *  @param responseTimeRequirement Database column responseTimeRequirement SqlType(DOUBLE)
   *  @param startTime Database column startTime SqlType(DATETIME)
   *  @param endTime Database column endTime SqlType(DATETIME) */
  class TestExecutionRowWrapper(val idTestExecution: Int, val idTestDefinition: Int, val idBuild: Int, val passed: Boolean, val responseTime: Double, val responseTimeRequirement: Double, val startTime: java.sql.Timestamp, val endTime: java.sql.Timestamp) extends TestExecutionRowLike
  case class TestExecutionRow(override val idTestExecution: Int, override val idTestDefinition: Int, override val idBuild: Int, override val passed: Boolean, override val responseTime: Double, override val responseTimeRequirement: Double, override val startTime: java.sql.Timestamp, override val endTime: java.sql.Timestamp) extends TestExecutionRowWrapper(idTestExecution, idTestDefinition, idBuild, passed, responseTime, responseTimeRequirement, startTime, endTime)
  implicit def TestExecutionRowWrapper2TestExecutionRow(x: TestExecutionRowWrapper): TestExecutionRow = TestExecutionRow(x.idTestExecution, x.idTestDefinition, x.idBuild, x.passed, x.responseTime, x.responseTimeRequirement, x.startTime, x.endTime)
  implicit def TestExecutionRow2TestExecutionRowWrapper(x: TestExecutionRow): TestExecutionRowWrapper = new TestExecutionRowWrapper(x.idTestExecution, x.idTestDefinition, x.idBuild, x.passed, x.responseTime, x.responseTimeRequirement, x.startTime, x.endTime)
  implicit def TestExecutionRowFromTypeClass[T: TestExecutionRowLikeType](x: T): TestExecutionRow = TestExecutionRow(implicitly[TestExecutionRowLikeType[T]].idTestExecution(x), implicitly[TestExecutionRowLikeType[T]].idTestDefinition(x), implicitly[TestExecutionRowLikeType[T]].idBuild(x), implicitly[TestExecutionRowLikeType[T]].passed(x), implicitly[TestExecutionRowLikeType[T]].responseTime(x), implicitly[TestExecutionRowLikeType[T]].responseTimeRequirement(x), implicitly[TestExecutionRowLikeType[T]].startTime(x), implicitly[TestExecutionRowLikeType[T]].endTime(x))
  /** GetResult implicit for fetching TestExecutionRow objects using plain SQL queries */
  implicit def GetResultTestExecutionRow(implicit e0: GR[Int], e1: GR[Boolean], e2: GR[Double], e3: GR[java.sql.Timestamp]): GR[TestExecutionRow] = GR{
    prs => import prs._
    TestExecutionRow.tupled((<<[Int], <<[Int], <<[Int], <<[Boolean], <<[Double], <<[Double], <<[java.sql.Timestamp], <<[java.sql.Timestamp]))
  }
  /** Table description of table TestExecution. Objects of this class serve as prototypes for rows in queries. */
  class TestExecution(_tableTag: Tag) extends profile.api.Table[TestExecutionRow](_tableTag, None, "TestExecution") with TestExecutionLike {
    def * = (idTestExecution, idTestDefinition, idBuild, passed, responseTime, responseTimeRequirement, startTime, endTime) <> (TestExecutionRow.tupled, TestExecutionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestExecution), Rep.Some(idTestDefinition), Rep.Some(idBuild), Rep.Some(passed), Rep.Some(responseTime), Rep.Some(responseTimeRequirement), Rep.Some(startTime), Rep.Some(endTime)).shaped.<>({r=>import r._; _1.map(_=> TestExecutionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestExecution SqlType(INT), AutoInc, PrimaryKey */
    val idTestExecution: Rep[Int] = column[Int]("idTestExecution", O.AutoInc, O.PrimaryKey)
    /** Database column idTestDefinition SqlType(INT) */
    val idTestDefinition: Rep[Int] = column[Int]("idTestDefinition")
    /** Database column idBuild SqlType(INT) */
    val idBuild: Rep[Int] = column[Int]("idBuild")
    /** Database column passed SqlType(BIT) */
    val passed: Rep[Boolean] = column[Boolean]("passed")
    /** Database column responseTime SqlType(DOUBLE) */
    val responseTime: Rep[Double] = column[Double]("responseTime")
    /** Database column responseTimeRequirement SqlType(DOUBLE) */
    val responseTimeRequirement: Rep[Double] = column[Double]("responseTimeRequirement")
    /** Database column startTime SqlType(DATETIME) */
    val startTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("startTime")
    /** Database column endTime SqlType(DATETIME) */
    val endTime: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("endTime")

    /** Foreign key referencing Build (database name idBuild) */
    lazy val buildFk = foreignKey("idBuild", idBuild, Build)(r => r.idBuild, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestDefinition (database name description) */
    lazy val testDefinitionFk = foreignKey("description", idTestDefinition, TestDefinition)(r => r.idTestDefinition, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table TestExecution */
  lazy val TestExecution = new TableQuery(tag => new TestExecution(tag))

  /** Entity class storing rows of table TestExecutionMetaTag
   *  @param idTestExecutionTag Database column idTestExecutionTag SqlType(INT)
   *  @param idTagName Database column idTagName SqlType(INT)
   *  @param value Database column value SqlType(VARCHAR), Length(255,true) */
  class TestExecutionMetaTagRowWrapper(val idTestExecutionTag: Int, val idTagName: Int, val value: String) extends TestExecutionMetaTagRowLike
  case class TestExecutionMetaTagRow(override val idTestExecutionTag: Int, override val idTagName: Int, override val value: String) extends TestExecutionMetaTagRowWrapper(idTestExecutionTag, idTagName, value)
  implicit def TestExecutionMetaTagRowWrapper2TestExecutionMetaTagRow(x: TestExecutionMetaTagRowWrapper): TestExecutionMetaTagRow = TestExecutionMetaTagRow(x.idTestExecutionTag, x.idTagName, x.value)
  implicit def TestExecutionMetaTagRow2TestExecutionMetaTagRowWrapper(x: TestExecutionMetaTagRow): TestExecutionMetaTagRowWrapper = new TestExecutionMetaTagRowWrapper(x.idTestExecutionTag, x.idTagName, x.value)
  implicit def TestExecutionMetaTagRowFromTypeClass[T: TestExecutionMetaTagRowLikeType](x: T): TestExecutionMetaTagRow = TestExecutionMetaTagRow(implicitly[TestExecutionMetaTagRowLikeType[T]].idTestExecutionTag(x), implicitly[TestExecutionMetaTagRowLikeType[T]].idTagName(x), implicitly[TestExecutionMetaTagRowLikeType[T]].value(x))
  /** GetResult implicit for fetching TestExecutionMetaTagRow objects using plain SQL queries */
  implicit def GetResultTestExecutionMetaTagRow(implicit e0: GR[Int], e1: GR[String]): GR[TestExecutionMetaTagRow] = GR{
    prs => import prs._
    TestExecutionMetaTagRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table TestExecutionMetaTag. Objects of this class serve as prototypes for rows in queries. */
  class TestExecutionMetaTag(_tableTag: Tag) extends profile.api.Table[TestExecutionMetaTagRow](_tableTag, None, "TestExecutionMetaTag") with TestExecutionMetaTagLike {
    def * = (idTestExecutionTag, idTagName, value) <> (TestExecutionMetaTagRow.tupled, TestExecutionMetaTagRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestExecutionTag), Rep.Some(idTagName), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> TestExecutionMetaTagRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestExecutionTag SqlType(INT) */
    val idTestExecutionTag: Rep[Int] = column[Int]("idTestExecutionTag")
    /** Database column idTagName SqlType(INT) */
    val idTagName: Rep[Int] = column[Int]("idTagName")
    /** Database column value SqlType(VARCHAR), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))

    /** Primary key of TestExecutionMetaTag (database name TestExecutionMetaTag_PK) */
    val pk = primaryKey("TestExecutionMetaTag_PK", (idTestExecutionTag, idTagName))

    /** Foreign key referencing TagName (database name idTagName_ExecutionMetaTag) */
    lazy val tagNameFk = foreignKey("idTagName_ExecutionMetaTag", idTagName, TagName)(r => r.idTagName, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestExecutionTag (database name idTestExecutionTag_ExecutionMetaTag) */
    lazy val testExecutionTagFk = foreignKey("idTestExecutionTag_ExecutionMetaTag", idTestExecutionTag, TestExecutionTag)(r => r.idTestExecutionTag, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table TestExecutionMetaTag */
  lazy val TestExecutionMetaTag = new TableQuery(tag => new TestExecutionMetaTag(tag))

  /** Entity class storing rows of table TestExecutionTag
   *  @param idTestExecutionTag Database column idTestExecutionTag SqlType(INT), AutoInc, PrimaryKey
   *  @param idTestExecution Database column idTestExecution SqlType(INT)
   *  @param idTagName Database column idTagName SqlType(INT)
   *  @param value Database column value SqlType(VARCHAR), Length(255,true) */
  class TestExecutionTagRowWrapper(val idTestExecutionTag: Int, val idTestExecution: Int, val idTagName: Int, val value: String) extends TestExecutionTagRowLike
  case class TestExecutionTagRow(override val idTestExecutionTag: Int, override val idTestExecution: Int, override val idTagName: Int, override val value: String) extends TestExecutionTagRowWrapper(idTestExecutionTag, idTestExecution, idTagName, value)
  implicit def TestExecutionTagRowWrapper2TestExecutionTagRow(x: TestExecutionTagRowWrapper): TestExecutionTagRow = TestExecutionTagRow(x.idTestExecutionTag, x.idTestExecution, x.idTagName, x.value)
  implicit def TestExecutionTagRow2TestExecutionTagRowWrapper(x: TestExecutionTagRow): TestExecutionTagRowWrapper = new TestExecutionTagRowWrapper(x.idTestExecutionTag, x.idTestExecution, x.idTagName, x.value)
  implicit def TestExecutionTagRowFromTypeClass[T: TestExecutionTagRowLikeType](x: T): TestExecutionTagRow = TestExecutionTagRow(implicitly[TestExecutionTagRowLikeType[T]].idTestExecutionTag(x), implicitly[TestExecutionTagRowLikeType[T]].idTestExecution(x), implicitly[TestExecutionTagRowLikeType[T]].idTagName(x), implicitly[TestExecutionTagRowLikeType[T]].value(x))
  /** GetResult implicit for fetching TestExecutionTagRow objects using plain SQL queries */
  implicit def GetResultTestExecutionTagRow(implicit e0: GR[Int], e1: GR[String]): GR[TestExecutionTagRow] = GR{
    prs => import prs._
    TestExecutionTagRow.tupled((<<[Int], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table TestExecutionTag. Objects of this class serve as prototypes for rows in queries. */
  class TestExecutionTag(_tableTag: Tag) extends profile.api.Table[TestExecutionTagRow](_tableTag, None, "TestExecutionTag") with TestExecutionTagLike {
    def * = (idTestExecutionTag, idTestExecution, idTagName, value) <> (TestExecutionTagRow.tupled, TestExecutionTagRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(idTestExecutionTag), Rep.Some(idTestExecution), Rep.Some(idTagName), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> TestExecutionTagRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column idTestExecutionTag SqlType(INT), AutoInc, PrimaryKey */
    val idTestExecutionTag: Rep[Int] = column[Int]("idTestExecutionTag", O.AutoInc, O.PrimaryKey)
    /** Database column idTestExecution SqlType(INT) */
    val idTestExecution: Rep[Int] = column[Int]("idTestExecution")
    /** Database column idTagName SqlType(INT) */
    val idTagName: Rep[Int] = column[Int]("idTagName")
    /** Database column value SqlType(VARCHAR), Length(255,true) */
    val value: Rep[String] = column[String]("value", O.Length(255,varying=true))

    /** Foreign key referencing TagName (database name idTagDescription_TestCaseTag) */
    lazy val tagNameFk = foreignKey("idTagDescription_TestCaseTag", idTagName, TagName)(r => r.idTagName, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing TestExecution (database name idTestExecution_Tag) */
    lazy val testExecutionFk = foreignKey("idTestExecution_Tag", idTestExecution, TestExecution)(r => r.idTestExecution, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (value,idTagName,idTestExecution) (database name idTestExecution_value_TagName_unique) */
    val index1 = index("idTestExecution_value_TagName_unique", (value, idTagName, idTestExecution), unique=true)
  }
  /** Collection-like TableQuery object for table TestExecutionTag */
  lazy val TestExecutionTag = new TableQuery(tag => new TestExecutionTag(tag))
}
