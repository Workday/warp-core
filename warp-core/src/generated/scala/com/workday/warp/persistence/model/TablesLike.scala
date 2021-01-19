package com.workday.warp.persistence.model
// !!! AUTO-GENERATED Slick data model, do not modify.

import slick.lifted.Rep
import annotation.implicitNotFound

trait TablesLike {
  val CORE_TABLES: Map[String, String] = Map(("BuildRow","BuildRowWrapper(idBuild,major,minor,patch,firstTested,lastTested)"),("MeasurementRow","MeasurementRowWrapper(idTestExecution,idMeasurementName,result)"),("MeasurementNameRow","MeasurementNameRowWrapper(idMeasurementName,name)"),("TagNameRow","TagNameRowWrapper(idTagName,name,nameType,isUserGenerated)"),("TestDefinitionRow","TestDefinitionRowWrapper(idTestDefinition,methodSignature,active,productName,subProductName,className,methodName,documentation)"),("TestDefinitionMetaTagRow","TestDefinitionMetaTagRowWrapper(idTestDefinitionTag,idTagName,value)"),("TestDefinitionTagRow","TestDefinitionTagRowWrapper(idTestDefinitionTag,idTestDefinition,idTagName,value)"),("TestExecutionRow","TestExecutionRowWrapper(idTestExecution,idTestDefinition,idBuild,passed,responseTime,responseTimeRequirement,startTime,endTime)"),("TestExecutionMetaTagRow","TestExecutionMetaTagRowWrapper(idTestExecutionTag,idTagName,value)"),("TestExecutionTagRow","TestExecutionTagRowWrapper(idTestExecutionTag,idTestExecution,idTagName,value)"))
  /** Supertrait for entity classes storing rows of table BuildLike
   *  
   *  idBuild: Database column idBuild SqlType(INT), AutoInc, PrimaryKey
   *  major: Database column major SqlType(INT)
   *  minor: Database column minor SqlType(INT)
   *  patch: Database column patch SqlType(INT)
   *  firstTested: Database column firstTested SqlType(DATETIME)
   *  lastTested: Database column lastTested SqlType(DATETIME) */
  trait BuildRowLike {
    val idBuild: Int
    val major: Int
    val minor: Int
    val patch: Int
    val firstTested: java.sql.Timestamp
    val lastTested: java.sql.Timestamp
  }
  /** Type Class for BuildRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class BuildRowLikeType[${T}]. You might pass an (implicit ev: BuildRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait BuildRowLikeType[T] {
    def idBuild(row: T): Int
    def major(row: T): Int
    def minor(row: T): Int
    def patch(row: T): Int
    def firstTested(row: T): java.sql.Timestamp
    def lastTested(row: T): java.sql.Timestamp
  }
  /** Supertrait for Table descriptions of table BuildLike */
  trait BuildLike {
    val idBuild: Rep[Int]
    val major: Rep[Int]
    val minor: Rep[Int]
    val patch: Rep[Int]
    val firstTested: Rep[java.sql.Timestamp]
    val lastTested: Rep[java.sql.Timestamp]
  }

  /** Supertrait for entity classes storing rows of table MeasurementLike
   *  
   *  idTestExecution: Database column idTestExecution SqlType(INT)
   *  idMeasurementName: Database column idMeasurementName SqlType(INT)
   *  result: Database column result SqlType(DOUBLE) */
  trait MeasurementRowLike {
    val idTestExecution: Int
    val idMeasurementName: Int
    val result: Double
  }
  /** Type Class for MeasurementRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class MeasurementRowLikeType[${T}]. You might pass an (implicit ev: MeasurementRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait MeasurementRowLikeType[T] {
    def idTestExecution(row: T): Int
    def idMeasurementName(row: T): Int
    def result(row: T): Double
  }
  /** Supertrait for Table descriptions of table MeasurementLike */
  trait MeasurementLike {
    val idTestExecution: Rep[Int]
    val idMeasurementName: Rep[Int]
    val result: Rep[Double]
  }

  /** Supertrait for entity classes storing rows of table MeasurementNameLike
   *  
   *  idMeasurementName: Database column idMeasurementName SqlType(INT), AutoInc, PrimaryKey
   *  name: Database column name SqlType(VARCHAR), Length(255,true) */
  trait MeasurementNameRowLike {
    val idMeasurementName: Int
    val name: String
  }
  /** Type Class for MeasurementNameRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class MeasurementNameRowLikeType[${T}]. You might pass an (implicit ev: MeasurementNameRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait MeasurementNameRowLikeType[T] {
    def idMeasurementName(row: T): Int
    def name(row: T): String
  }
  /** Supertrait for Table descriptions of table MeasurementNameLike */
  trait MeasurementNameLike {
    val idMeasurementName: Rep[Int]
    val name: Rep[String]
  }

  /** Supertrait for entity classes storing rows of table TagNameLike
   *  
   *  idTagName: Database column idTagName SqlType(INT), AutoInc, PrimaryKey
   *  name: Database column name SqlType(VARCHAR), Length(255,true)
   *  nameType: Database column nameType SqlType(VARCHAR), Length(255,true), Default(plain_txt)
   *  isUserGenerated: Database column isUserGenerated SqlType(BIT), Default(true) */
  trait TagNameRowLike {
    val idTagName: Int
    val name: String
    val nameType: String
    val isUserGenerated: Boolean
  }
  /** Type Class for TagNameRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TagNameRowLikeType[${T}]. You might pass an (implicit ev: TagNameRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TagNameRowLikeType[T] {
    def idTagName(row: T): Int
    def name(row: T): String
    def nameType(row: T): String
    def isUserGenerated(row: T): Boolean
  }
  /** Supertrait for Table descriptions of table TagNameLike */
  trait TagNameLike {
    val idTagName: Rep[Int]
    val name: Rep[String]
    val nameType: Rep[String]
    val isUserGenerated: Rep[Boolean]
  }

  /** Supertrait for entity classes storing rows of table TestDefinitionLike
   *  
   *  idTestDefinition: Database column idTestDefinition SqlType(INT), AutoInc, PrimaryKey
   *  methodSignature: Database column methodSignature SqlType(VARCHAR), Length(255,true)
   *  active: Database column active SqlType(BIT)
   *  productName: Database column productName SqlType(VARCHAR), Length(255,true)
   *  subProductName: Database column subProductName SqlType(VARCHAR), Length(255,true)
   *  className: Database column className SqlType(VARCHAR), Length(255,true)
   *  methodName: Database column methodName SqlType(VARCHAR), Length(255,true)
   *  documentation: Database column documentation SqlType(TEXT), Default(None) */
  trait TestDefinitionRowLike {
    val idTestDefinition: Int
    val methodSignature: String
    val active: Boolean
    val productName: String
    val subProductName: String
    val className: String
    val methodName: String
    val documentation: Option[String]
  }
  /** Type Class for TestDefinitionRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestDefinitionRowLikeType[${T}]. You might pass an (implicit ev: TestDefinitionRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestDefinitionRowLikeType[T] {
    def idTestDefinition(row: T): Int
    def methodSignature(row: T): String
    def active(row: T): Boolean
    def productName(row: T): String
    def subProductName(row: T): String
    def className(row: T): String
    def methodName(row: T): String
    def documentation(row: T): Option[String]
  }
  /** Supertrait for Table descriptions of table TestDefinitionLike */
  trait TestDefinitionLike {
    val idTestDefinition: Rep[Int]
    val methodSignature: Rep[String]
    val active: Rep[Boolean]
    val productName: Rep[String]
    val subProductName: Rep[String]
    val className: Rep[String]
    val methodName: Rep[String]
    val documentation: Rep[Option[String]]
  }

  /** Supertrait for entity classes storing rows of table TestDefinitionMetaTagLike
   *  
   *  idTestDefinitionTag: Database column idTestDefinitionTag SqlType(INT)
   *  idTagName: Database column idTagName SqlType(INT)
   *  value: Database column value SqlType(VARCHAR), Length(255,true) */
  trait TestDefinitionMetaTagRowLike {
    val idTestDefinitionTag: Int
    val idTagName: Int
    val value: String
  }
  /** Type Class for TestDefinitionMetaTagRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestDefinitionMetaTagRowLikeType[${T}]. You might pass an (implicit ev: TestDefinitionMetaTagRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestDefinitionMetaTagRowLikeType[T] {
    def idTestDefinitionTag(row: T): Int
    def idTagName(row: T): Int
    def value(row: T): String
  }
  /** Supertrait for Table descriptions of table TestDefinitionMetaTagLike */
  trait TestDefinitionMetaTagLike {
    val idTestDefinitionTag: Rep[Int]
    val idTagName: Rep[Int]
    val value: Rep[String]
  }

  /** Supertrait for entity classes storing rows of table TestDefinitionTagLike
   *  
   *  idTestDefinitionTag: Database column idTestDefinitionTag SqlType(INT), AutoInc, PrimaryKey
   *  idTestDefinition: Database column idTestDefinition SqlType(INT)
   *  idTagName: Database column idTagName SqlType(INT)
   *  value: Database column value SqlType(VARCHAR), Length(255,true) */
  trait TestDefinitionTagRowLike {
    val idTestDefinitionTag: Int
    val idTestDefinition: Int
    val idTagName: Int
    val value: String
  }
  /** Type Class for TestDefinitionTagRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestDefinitionTagRowLikeType[${T}]. You might pass an (implicit ev: TestDefinitionTagRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestDefinitionTagRowLikeType[T] {
    def idTestDefinitionTag(row: T): Int
    def idTestDefinition(row: T): Int
    def idTagName(row: T): Int
    def value(row: T): String
  }
  /** Supertrait for Table descriptions of table TestDefinitionTagLike */
  trait TestDefinitionTagLike {
    val idTestDefinitionTag: Rep[Int]
    val idTestDefinition: Rep[Int]
    val idTagName: Rep[Int]
    val value: Rep[String]
  }

  /** Supertrait for entity classes storing rows of table TestExecutionLike
   *  
   *  idTestExecution: Database column idTestExecution SqlType(INT), AutoInc, PrimaryKey
   *  idTestDefinition: Database column idTestDefinition SqlType(INT)
   *  idBuild: Database column idBuild SqlType(INT)
   *  passed: Database column passed SqlType(BIT)
   *  responseTime: Database column responseTime SqlType(DOUBLE)
   *  responseTimeRequirement: Database column responseTimeRequirement SqlType(DOUBLE)
   *  startTime: Database column startTime SqlType(DATETIME)
   *  endTime: Database column endTime SqlType(DATETIME) */
  trait TestExecutionRowLike {
    val idTestExecution: Int
    val idTestDefinition: Int
    val idBuild: Int
    val passed: Boolean
    val responseTime: Double
    val responseTimeRequirement: Double
    val startTime: java.sql.Timestamp
    val endTime: java.sql.Timestamp
  }
  /** Type Class for TestExecutionRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestExecutionRowLikeType[${T}]. You might pass an (implicit ev: TestExecutionRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestExecutionRowLikeType[T] {
    def idTestExecution(row: T): Int
    def idTestDefinition(row: T): Int
    def idBuild(row: T): Int
    def passed(row: T): Boolean
    def responseTime(row: T): Double
    def responseTimeRequirement(row: T): Double
    def startTime(row: T): java.sql.Timestamp
    def endTime(row: T): java.sql.Timestamp
  }
  /** Supertrait for Table descriptions of table TestExecutionLike */
  trait TestExecutionLike {
    val idTestExecution: Rep[Int]
    val idTestDefinition: Rep[Int]
    val idBuild: Rep[Int]
    val passed: Rep[Boolean]
    val responseTime: Rep[Double]
    val responseTimeRequirement: Rep[Double]
    val startTime: Rep[java.sql.Timestamp]
    val endTime: Rep[java.sql.Timestamp]
  }

  /** Supertrait for entity classes storing rows of table TestExecutionMetaTagLike
   *  
   *  idTestExecutionTag: Database column idTestExecutionTag SqlType(INT)
   *  idTagName: Database column idTagName SqlType(INT)
   *  value: Database column value SqlType(VARCHAR), Length(255,true) */
  trait TestExecutionMetaTagRowLike {
    val idTestExecutionTag: Int
    val idTagName: Int
    val value: String
  }
  /** Type Class for TestExecutionMetaTagRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestExecutionMetaTagRowLikeType[${T}]. You might pass an (implicit ev: TestExecutionMetaTagRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestExecutionMetaTagRowLikeType[T] {
    def idTestExecutionTag(row: T): Int
    def idTagName(row: T): Int
    def value(row: T): String
  }
  /** Supertrait for Table descriptions of table TestExecutionMetaTagLike */
  trait TestExecutionMetaTagLike {
    val idTestExecutionTag: Rep[Int]
    val idTagName: Rep[Int]
    val value: Rep[String]
  }

  /** Supertrait for entity classes storing rows of table TestExecutionTagLike
   *  
   *  idTestExecutionTag: Database column idTestExecutionTag SqlType(INT), AutoInc, PrimaryKey
   *  idTestExecution: Database column idTestExecution SqlType(INT)
   *  idTagName: Database column idTagName SqlType(INT)
   *  value: Database column value SqlType(VARCHAR), Length(255,true) */
  trait TestExecutionTagRowLike {
    val idTestExecutionTag: Int
    val idTestExecution: Int
    val idTagName: Int
    val value: String
  }
  /** Type Class for TestExecutionTagRowLike **/
  @implicitNotFound("Could not find an implicit value for evidence of type class TestExecutionTagRowLikeType[${T}]. You might pass an (implicit ev: TestExecutionTagRowLikeType[${T}]) parameter to your method or import Tables.RowTypeClasses._")
  trait TestExecutionTagRowLikeType[T] {
    def idTestExecutionTag(row: T): Int
    def idTestExecution(row: T): Int
    def idTagName(row: T): Int
    def value(row: T): String
  }
  /** Supertrait for Table descriptions of table TestExecutionTagLike */
  trait TestExecutionTagLike {
    val idTestExecutionTag: Rep[Int]
    val idTestExecution: Rep[Int]
    val idTagName: Rep[Int]
    val value: Rep[String]
  }
  object RowTypeClasses {
  implicit object BuildRowLikeTypeClassObject extends BuildRowLikeType[BuildRowLike] {
      def idBuild(row: BuildRowLike): Int = row.idBuild
      def major(row: BuildRowLike): Int = row.major
      def minor(row: BuildRowLike): Int = row.minor
      def patch(row: BuildRowLike): Int = row.patch
      def firstTested(row: BuildRowLike): java.sql.Timestamp = row.firstTested
      def lastTested(row: BuildRowLike): java.sql.Timestamp = row.lastTested
    }
    implicit object MeasurementRowLikeTypeClassObject extends MeasurementRowLikeType[MeasurementRowLike] {
      def idTestExecution(row: MeasurementRowLike): Int = row.idTestExecution
      def idMeasurementName(row: MeasurementRowLike): Int = row.idMeasurementName
      def result(row: MeasurementRowLike): Double = row.result
    }
    implicit object MeasurementNameRowLikeTypeClassObject extends MeasurementNameRowLikeType[MeasurementNameRowLike] {
      def idMeasurementName(row: MeasurementNameRowLike): Int = row.idMeasurementName
      def name(row: MeasurementNameRowLike): String = row.name
    }
    implicit object TagNameRowLikeTypeClassObject extends TagNameRowLikeType[TagNameRowLike] {
      def idTagName(row: TagNameRowLike): Int = row.idTagName
      def name(row: TagNameRowLike): String = row.name
      def nameType(row: TagNameRowLike): String = row.nameType
      def isUserGenerated(row: TagNameRowLike): Boolean = row.isUserGenerated
    }
    implicit object TestDefinitionRowLikeTypeClassObject extends TestDefinitionRowLikeType[TestDefinitionRowLike] {
      def idTestDefinition(row: TestDefinitionRowLike): Int = row.idTestDefinition
      def methodSignature(row: TestDefinitionRowLike): String = row.methodSignature
      def active(row: TestDefinitionRowLike): Boolean = row.active
      def productName(row: TestDefinitionRowLike): String = row.productName
      def subProductName(row: TestDefinitionRowLike): String = row.subProductName
      def className(row: TestDefinitionRowLike): String = row.className
      def methodName(row: TestDefinitionRowLike): String = row.methodName
      def documentation(row: TestDefinitionRowLike): Option[String] = row.documentation
    }
    implicit object TestDefinitionMetaTagRowLikeTypeClassObject extends TestDefinitionMetaTagRowLikeType[TestDefinitionMetaTagRowLike] {
      def idTestDefinitionTag(row: TestDefinitionMetaTagRowLike): Int = row.idTestDefinitionTag
      def idTagName(row: TestDefinitionMetaTagRowLike): Int = row.idTagName
      def value(row: TestDefinitionMetaTagRowLike): String = row.value
    }
    implicit object TestDefinitionTagRowLikeTypeClassObject extends TestDefinitionTagRowLikeType[TestDefinitionTagRowLike] {
      def idTestDefinitionTag(row: TestDefinitionTagRowLike): Int = row.idTestDefinitionTag
      def idTestDefinition(row: TestDefinitionTagRowLike): Int = row.idTestDefinition
      def idTagName(row: TestDefinitionTagRowLike): Int = row.idTagName
      def value(row: TestDefinitionTagRowLike): String = row.value
    }
    implicit object TestExecutionRowLikeTypeClassObject extends TestExecutionRowLikeType[TestExecutionRowLike] {
      def idTestExecution(row: TestExecutionRowLike): Int = row.idTestExecution
      def idTestDefinition(row: TestExecutionRowLike): Int = row.idTestDefinition
      def idBuild(row: TestExecutionRowLike): Int = row.idBuild
      def passed(row: TestExecutionRowLike): Boolean = row.passed
      def responseTime(row: TestExecutionRowLike): Double = row.responseTime
      def responseTimeRequirement(row: TestExecutionRowLike): Double = row.responseTimeRequirement
      def startTime(row: TestExecutionRowLike): java.sql.Timestamp = row.startTime
      def endTime(row: TestExecutionRowLike): java.sql.Timestamp = row.endTime
    }
    implicit object TestExecutionMetaTagRowLikeTypeClassObject extends TestExecutionMetaTagRowLikeType[TestExecutionMetaTagRowLike] {
      def idTestExecutionTag(row: TestExecutionMetaTagRowLike): Int = row.idTestExecutionTag
      def idTagName(row: TestExecutionMetaTagRowLike): Int = row.idTagName
      def value(row: TestExecutionMetaTagRowLike): String = row.value
    }
    implicit object TestExecutionTagRowLikeTypeClassObject extends TestExecutionTagRowLikeType[TestExecutionTagRowLike] {
      def idTestExecutionTag(row: TestExecutionTagRowLike): Int = row.idTestExecutionTag
      def idTestExecution(row: TestExecutionTagRowLike): Int = row.idTestExecution
      def idTagName(row: TestExecutionTagRowLike): Int = row.idTagName
      def value(row: TestExecutionTagRowLike): String = row.value
    }
  }
}
