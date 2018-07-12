package com.workday.warp

import slick.codegen.SourceCodeGenerator
import slick.jdbc.MySQLProfile
import slick.jdbc.meta.MTable
import slick.model.Model

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Only generates code for tables, not views.
  *
  * See https://github.com/slick/slick-codegen-customization-example/blob/master/codegen/CustomizedCodeGenerator.scala
  *
  * Created by tomas.mccandless on 1/17/17.
  */
object TableCodeGenerator {

  private val db: MySQLProfile.backend.DatabaseDef = MySQLProfile.api.Database.forURL(Config.url, driver = Config.jdbcDriver)

  /**
    * Generates slick code for tables, not views, and writes that generated code to the file specified in `args(0)`.
    *
    * Original capitalization from the underlying schema is retained.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val outputDir: String = args(0)
    System.out.println(s"generating slick code, output dir = $outputDir")

    // This section generates the Tables.scala code
    val codeGen: Future[SourceCodeGenerator] = db.run {
      MySQLProfile.defaultTables map {
        // we don't want to generate any code for views, or for schema_version table used by flyway
        _.filter { table: MTable => table.tableType.equalsIgnoreCase("table") && !table.name.name.equalsIgnoreCase("schema_version") }
      } flatMap { name: Seq[MTable] =>
        MySQLProfile.createModelBuilder(name, ignoreInvalidDefaults = false).buildModel
      }
    } map { model: Model =>
      new slick.codegen.SourceCodeGenerator(model) {
        // retain the original capitalization for row case classes
        override def entityName: (String) => String = tableName => tableName + "Row"

        // retain the original capitalization for table classes
        override def tableName: (String) => String = tableName => tableName

        // This section generates the classes representing the tables. (e.g. BuildInfo).
        override def Table = new Table(_) { table =>
          // retain original capitalization for columns. We uncapitalize to ensure generated fields are lower-cased
          override def Column = new Column(_) {
            override def rawName: String = this.model.name.uncapitalize
          }

          // omit the schema name (an Option[String]) from the args.
          // this makes it simpler to use the generated code with an arbitrary schema in our jdbc url
          override def TableClass = new TableClassDef {
            // Extend TablesLike
            override def parents: Seq[String] = Seq(name + "Like")

            override def code: String = {
              val supertypes: String = parents.map(" with " + _).mkString("")
              val args: Iterable[String] = Seq("None", "\""+model.name.table+"\"")
              s"""
class $name(_tableTag: Tag) extends profile.api.Table[$elementType](_tableTag, ${args.mkString(", ")})$supertypes {
  ${indent(body.map(_.mkString("\n")).mkString("\n\n"))}
}
        """.trim()
            }
          }

          // This section generates the wrapper classes and the case classes. (e.g. BuildInfoRowWrapper and BuildInfoRow)
          override def EntityType = new EntityTypeDef {
            // The case classes will extend the Wrapper class
            override def parents: Seq[String] = Seq(name + "Wrapper")

            // we don't want generated case classes to be final. not a big deal, but this avoids compiler warnings like:
            //  "The outer reference in this type test cannot be checked at run time"
            override def caseClassFinal: Boolean = false

            override def code = {
              val wrapperName = name + "Wrapper"
              val typeClassName = name + "LikeType"

              // classArgs = arguments for the wrapper class (needs val)
              // caseClassArgs = arguments for the case class (needs override val)
              // parentClassArgs = arguments for case class extends clause (simply list of arguments)
              val (classArgs, caseClassArgs, parentClassArgs) = columns.map(c =>
                c.default.map( v =>
                  (s"val ${c.name}: ${c.exposedType} = $v",
                    s"override val ${c.name}: ${c.exposedType} = $v",
                    s"${c.name}")
                ).getOrElse(
                  (s"val ${c.name}: ${c.exposedType}",
                    s"override val ${c.name}: ${c.exposedType}",
                    s"${c.name}")
                )
              ).unzip3

              // implicitArgs = arguments for the implicit conversion functions
              // typeClassArgs = arguments for the implicit conversion from type class to case class
              val (implicitArgs, typeClassArgs) = columns.map(c =>
                (s"x.${c.name}", s"implicitly[$typeClassName[T]].${c.name}(x)")
              ).unzip

              // Create the extends clause. We use take(1) and drop(1) to avoid exceptions thrown by .head and .tail
              val prns = (parents.take(1).map(" extends "+_) ++ parents.drop(1).map(" with "+_)).mkString("")

              s"""
class ${wrapperName}(${classArgs.mkString(", ")}) extends ${name}Like
case class $name(${caseClassArgs.mkString(", ")})$prns(${parentClassArgs.mkString(", ")})
implicit def ${wrapperName}2${name}(x: $wrapperName): $name = $name(${implicitArgs.mkString(", ")})
implicit def ${name}2${wrapperName}(x: $name): $wrapperName = new $wrapperName(${implicitArgs.mkString(", ")})
implicit def ${name}FromTypeClass[T: $typeClassName](x: T): $name = $name(${typeClassArgs.mkString(", ")})
                """.trim()
            }
          }
        }

        // Function that generates the type classes definitions for each case class and wrapper class
        def generateTypeClass(table: Table): String = {
          // Name of the Row type (e.g. BuildInfoRow)
          val rowName: String = table.EntityType.name
          // Name of the Wrapper class (e.g. BuildInfoRowWrapper)
          val wrapperName: String = rowName + "Wrapper"
          // Name of the type class (e.g. BuildInfoRowLikeType)
          val parentTypeClassName: String = rowName + "LikeType"
          // Generates each of the required function definitions for the type class
          // (e.g. def idBuildInfo(row: BuildInfoRow): Int = row.idBuildInfo)
          def typeClassMembers(name: String): Seq[String] = {
            table.columns.map(c => s"def ${c.name}(row: $name): ${c.exposedType} = row.${c.name}")
          }

          s"""
implicit object ${rowName}TypeClassObject extends $parentTypeClassName[$rowName] {
  ${indent(typeClassMembers(rowName).mkString("\n"))}
}

implicit object ${wrapperName}TypeClassObject extends $parentTypeClassName[$wrapperName] {
  ${indent(typeClassMembers(wrapperName).mkString("\n"))}
}
  """.trim()
        }

        // Code inside of the Tables trait
        override def code: String = {
          "import slick.model.ForeignKeyAction\n" +
            ( if(tables.exists(_.hlistEnabled)){
              "import slick.collection.heterogeneous._\n"+
                "import slick.collection.heterogeneous.syntax._\n"
            } else ""
              ) +
            ( if(tables.exists(_.PlainSqlMapper.enabled)){
              "// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.\n"+
                "import slick.jdbc.{GetResult => GR}\n"
            } else ""
              ) +
            "\n/** DDL for all tables. Call .create to execute. */" +
            (
              if(tables.length > 5)
                "\nlazy val schema = Array(" + tables.map(_.TableValue.name + ".schema").mkString(", ") + ").reduceLeft(_ ++ _)"
              else
                "\nlazy val schema = " + tables.map(_.TableValue.name + ".schema").mkString(" ++ ")
              ) +
            "\n@deprecated(\"Use .schema instead of .ddl\", \"3.0\")"+
            "\ndef ddl = schema" +
            "\n\n" +
            indent("object RowTypeClasses {\n" + tables.map(generateTypeClass).mkString("\n")) +
            "\n}" +
            "\n\n" +
            tables.map(_.code.mkString("\n")).mkString("\n\n")
        }

        // Wraps the code with a trait "Tables" and the correct package.
        override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
          s"""
package ${pkg}
// !!! AUTO-GENERATED Slick data model, do not modify.
import com.workday.warp.persistence.TablesLike._

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  ${indent(code)}
}
""".trim()
        }
      }
    }

    // This section generates the TablesLike.scala code
    val traitsGen: Future[SourceCodeGenerator] = db.run {
      MySQLProfile.defaultTables map {
        // we don't want to generate any code for views, or for schema_version table used by flyway
        _.filter { table: MTable => table.tableType.equalsIgnoreCase("table") && !table.name.name.equalsIgnoreCase("schema_version") }
      } flatMap { name: Seq[MTable] =>
        MySQLProfile.createModelBuilder(name, ignoreInvalidDefaults = false).buildModel
      }
    } map { model: Model =>
      new slick.codegen.SourceCodeGenerator(model) {
        // retain the original capitalization for row case classes
        override def entityName: (String) => String = tableName => tableName + "RowLike"

        // retain the original capitalization for table classes
        override def tableName: (String) => String = tableName => tableName + "Like"

        // Generates the type class definitions for each "RowLike" trait
        // We don't need a type class definition for the table supertrait, because it's never passed as a type parameter
        def generateTypeClass(table: Table): String = {
          // Name of the "RowLike" trait
          val name: String = table.EntityType.name
          // Object wrapping the type class definition
          val objectName: String = name + "TypeClassObject"
          // Type Class name (e.g. BuildInfoRowLikeType[BuildInfoRowLike]
          val parentTypeClassName: String = name + "Type[" + name + "]"
          // Generate each function required for the type class
          val typeClassMembers: Seq[String] = table.columns.map(c => s"def ${c.name}(row: $name): ${c.exposedType} = row.${c.name}")

          s"""
implicit object $objectName extends $parentTypeClassName {
  ${indent(typeClassMembers.mkString("\n"))}
}
  """.trim()
        }

        override def Table = new Table(_) { table =>
          // Only generate EntityType (base trait for case classes) and TableClass (base trait for table classes)
          override def definitions: Seq[Def] = Seq(EntityType, TableClass)

          // Base Trait for case classes
          // Type Classes for Rows
          override def EntityType = new EntityTypeDef {
            override def doc = s"Supertrait for entity classes storing rows of table ${TableValue.name}\n\n" +
              columns.map(c => c.name+": "+c.doc).mkString("\n")

            override def code = {
              val members: Seq[String] = columns.map(c => s"val ${c.name}: ${c.exposedType}")
              // Use take(1) and drop(1) to avoid exceptions if parents is empty
              val supertypes: String = (parents.take(1).map("extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")

              val typeClassName: String = name + "Type"
              val typeClassMembers: Seq[String] = columns.map(c => s"def ${c.name}(row: T): ${c.exposedType}")

              s"""
trait $name $supertypes{
  ${indent(members.mkString("\n"))}
}
/** Type Class for $name **/
@implicitNotFound("Could not find an implicit value for evidence of type class $typeClassName[$${T}]. You might pass an (implicit ev: $typeClassName[$${T}]) parameter to your method or import Tables.RowTypeClasses._")
trait $typeClassName[T] {
  ${indent(typeClassMembers.mkString("\n"))}
}
              """.trim()
            }
          }

          // retain original capitalization for columns. JiraProjects table has upper-cased columns, we uncapitalize
          // to ensure generated fields are lower-cased
          override def Column = new Column(_) {
            override def rawName: String = this.model.name.uncapitalize
          }

          // Base Trait for Table classes
          override def TableClass = new TableClassDef {
            override def doc = s"Supertrait for Table descriptions of table ${TableValue.name}\n"

            override def code: String = {
              val members: Seq[String] = columns.map(c => s"val ${c.name}: Rep[${c.actualType}]")
              // Use take(1) and drop(1) to avoid exceptions if parents is empty
              val supertypes: String = (parents.take(1).map("extends " + _) ++ parents.drop(1).map(" with " + _)).mkString("")

              s"""
trait $name $supertypes{
  ${indent(members.mkString("\n"))}
}
              """.trim()
            }
          }
        }

        // Creates the tuples to create a map for core tables to their wrapper class constructors
        // This will be used in extended schemas so they can simply pull in the correct constructor without needing to
        // examine the wrapper class structure.
        val coreTablesTuples: String = s"${tables.map(t => (s""""${t.model.name.table}Row"""",
          s""""${t.model.name.table}RowWrapper(${t.columns.map(_.name).mkString(",")})"""")).mkString(",")}"

        override def code: String =
          s"""
val CORE_TABLES: Map[String, String] = Map($coreTablesTuples)
${tables.map(_.code.mkString("\n")).mkString("\n\n")}
object RowTypeClasses {
${indent(tables.map(generateTypeClass).mkString("\n"))}
}
          """.trim()

        override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
          s"""
package ${pkg}
// !!! AUTO-GENERATED Slick data model, do not modify.

import slick.lifted.Rep
import annotation.implicitNotFound

trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
  ${indent(code)}
}
          """.trim()
        }
      }
    }

    Await.result(codeGen, 20.seconds).writeToFile(Config.slickProfile, outputDir, Config.packageName)
    Await.result(traitsGen, 20.seconds).writeToFile(Config.slickProfile, outputDir, Config.packageName,
      Config.traitsContainer, Config.traitsFile)
  }
}
