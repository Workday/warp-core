code in this sbt project is for the purpose of generating slick Tables.scala from an existing database.

we have a custom code generator in the codegen directory that omits generating code for views. it only generates code
for tables. if you want to change any parameters like jdbc url, generated package, check out the Config object in this
directory.

build.sbt defines 2 subprojects:
- codegen, which compiles our custom code generator
- root, which has a dependency on the compiled artifacts from the codegen project. this project also defines a codegen
task that can be invoked to generate code.

also included is a generate.sh script that invokes sbt to generate the slick Tables.scala code, then copies that code
into the persistence-model module.

to generate code for new tables or updated columns:
- modify WARP.mwb in persistence module using mysql workbench
- export the updated model schema to a sql script (create-mysql.sql)
- source that script in mysql after dropping the warp schema
- run `./generate.sh`, or `sbt codegen` and then copy the generated files by hand into persistence-model

make sure to commit `WARP.mwb` and `create-mysql.sql` in addition to the newly generated `Tables.scala`.
