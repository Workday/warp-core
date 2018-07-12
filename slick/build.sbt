val slickVersion = "3.2.0"
val mysqlConnectorVersion = "5.1.27"
val slf4jVersion = "1.7.10"

/** main project containing main source code depending on slick and codegen project */
lazy val root = (project in file("."))
  .settings(sharedSettings)
  .settings(slick := slickCodeGenTask.value) // register manual sbt command)
  .settings(sourceGenerators in Compile += slickCodeGenTask.taskValue) // register automatic code generation on every compile, remove for only manual use)
  .dependsOn(codegen)


/** codegen project containing the customized code generator */
lazy val codegen = project
  .settings(sharedSettings)
  .settings(libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % slickVersion)


lazy val sharedSettings = Seq(
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-feature", "-unchecked", "-deprecation"),
  libraryDependencies ++= List(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "mysql" % "mysql-connector-java" % mysqlConnectorVersion,
    "org.slf4j" % "slf4j-nop" % slf4jVersion
  )
)

// code generation task, just run `sbt codegen`
lazy val slick = TaskKey[Seq[File]]("codegen")
lazy val slickCodeGenTask = Def.task {

  val dir = target.value
  val cp = (dependencyClasspath in Compile).value
  val r = (runner in Compile).value
  val s = streams.value
  val outputDir = (dir / "src_managed" / "slick").getPath // place generated files in sbt's managed sources folder

  /*
   * Changelog from sbt 1.0.1: Drops toError(opt: Option[String]): Unit (equivalent to opt foreach sys.error); if used to wrap
   * ScalaRun#run then the replacement is scalaRun.run(...).failed foreach (sys error _.getMessage)
   */
  r.run("com.workday.warp.TableCodeGenerator", cp.files, Array(outputDir), s.log).failed foreach (sys error _.getMessage)

  val fname = outputDir + "/demo/Tables.scala"
  Seq(file(fname))
}

