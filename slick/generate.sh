#!/bin/bash

# generate new slick code, then copy it into our persistence module
sbt clean codegen
mkdir -pv ../src/generated/scala/com/workday/warp/persistence/model
cp target/src_managed/slick/com/workday/warp/persistence/model/Tables.scala ../src/generated/scala/com/workday/warp/persistence/model/Tables.scala
cp target/src_managed/slick/com/workday/warp/persistence/model/TablesLike.scala ../src/generated/scala/com/workday/warp/persistence/model/TablesLike.scala
