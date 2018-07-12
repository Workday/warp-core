---
title: "Persistence"
date: 2018-04-03T10:58:04-07:00
draft: true
weight: 65
---

WARP uses [slick](https://github.com/slick/slick) for our persistence library, and [flyway](https://github.com/flyway/flyway)
for managing database migrations. 

Slick can have a steep learning curve for new developers; we recommend reading [Essential Slick](http://books.underscore.io/essential-slick/essential-slick-3.html) as a primer
for those looking to get more acquainted with the library.

Our persistence module is designed to be extensible for custom needs. Internally, we augment the default schema
with some additional proprietary measurement columns. See the advanced schema page [here]({{< ref "extension_advanced.md" >}}) for a more detailed description
of our persistence architecture and how you can extend our schema to fit your needs.

## Basic Concepts

The primary tables we define are `Build`, `TestDefinition` and `TestExecution`.
`Build` allows users to track information about the version of the service they are testing. `TestDefinition` represents
the logical definition of a test that may potentially be executed many times. Typically, we use the fully qualified
method signature of the test as its identity key. `TestExecution` represents a single execution of a test, and has pointers
to the corresponding `TestDefinition` and `Build` the test was executed under.


## Slick

We make heavy use of the slick code generator for generating boilerplate implicits and typeclasses.

For example, the following generated case class is used to represent a single test execution:

{{< highlight scala "linenos=" >}}
case class TestExecutionRow(override val idTestExecution: Int,
                            override val idTestDefinition: Int, 
                            override val idBuild: Int, 
                            override val passed: Boolean, 
                            override val responseTime: Double, 
                            override val responseTimeRequirement: Double, 
                            override val startTime: java.sql.Timestamp, 
                            override val endTime: java.sql.Timestamp)
{{< /highlight >}}

## Queries

The queries we use are defined in `CoreQueries`. To obtain access to the configured database, simply mix in the trait `CorePersistenceAware` 
to obtain an instance of `CorePersistenceUtils`

`CorePersistenceUtils` contains utility methods for interacting with the database.

For example, the `createTestExecution` method is used to write a record of a single test execution:

{{< highlight scala "linenos=" >}}
/**
  * Creates, inserts, and returns a [[TestExecutionRowLike]]
  *
  * @param testId id of the measured test (usually fully qualified junit method).
  * @param timeStarted time the measured test was started.
  * @param responseTime observed duration of the measured test (seconds).
  * @param maxResponseTime maximum allowable response time set on the measured test (seconds).
  * @return a [[TestExecutionRowLike]] with the given parameters.
  */
override def createTestExecution(testId: String,
					             timeStarted: Date,
					             responseTime: Double,
					    		 maxResponseTime: Double): TestExecutionRowLike
{{< /highlight >}}

Similarly, the `recordMeasurement` method is used to persist a measurement obtained for a given test execution.

{{< highlight scala "linenos=" >}}
/**
  * Persists generic measurements in the Measurement table. Looks up the MeasurementName corresponding to
  * `name`, creates a new Measurement with the appropriate fields set.
  *
  * @param idTestExecution id for the [[TestExecutionRow]] associated with this measurement.
  * @param name name to use for this measurement.
  * @param result result of this measurement.
  */
override def recordMeasurement(idTestExecution: Int, name: String, result: Double): Unit = {
{{< /highlight >}}

In general, if you need to interact with the WARP database, mix `CorePersistenceAware` into your class and
use the provided instance of `CorePersistenceUtils`.
