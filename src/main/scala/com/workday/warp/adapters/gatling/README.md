# Gatling Integration

warp-core provides basic built-in gatling integrations.
 
 
 
There are two classes that can be extended from: `WarpFunSpec`, for functional testing, and `WarpSimulation`, for integration or load testing. They mirror Gatling's own classes, `GatlingHttpFunSpec`, and `Simulation`, respectively.

## Usage

Extending `WarpFunSpec` and `WarpSimulation` should be relatively straightforward. Any existing Gatling simulation or functional spec that compiles and runs can be converted to a warp-gatling test by simply changing the superclass. 

For example

```scala
import com.workday.warp.adapters.gatling._
class A extends Simulation {
  // YOUR CODE HERE
}
```
```scala
import com.workday.warp.adapters.gatling._
class B extends GatlingHttpFunSpec {
  // Your CODE HERE
}
```

can be be run with warp like so:

```scala
import com.workday.warp.adapters.gatling._

class A extends WarpSimulation {
  // YOUR CODE HERE
}
```
```scala
import com.workday.warp.adapters.gatling._
class B extends WarpFunSpec {
  // YOUR CODE HERE
}
``` 

#### Customization

##### Name

By default, the `testId` of the warp-gatling test will be set as the fully qualified path that the test is located in relative to the root project. For example, if in your directory you have a test with path `com.foo.bar.MyTest`, the testId will be persisted in the database as `com.foo.bar.MyTest`. To give your simulation/spec a name other than its simple class name, you can pass in the `testId: String` as a parameter to the auxiliary constructor.

For example, in `MyTest`, if you pass in a custom `testId`,

```scala
class MyTest extends WarpSimulation("CustomNamedTest")
```

the test id will be set and persisted as `com.foo.bar.CustomNamedTest` when run through the warp pipeline. Note that the package name remains the same for both default and custom test names.

##### Hooks

Some advanced users of Gatling find the `before` and `after` hooks useful for running custom code both prior and after a gatling simulation run. `warp` actually takes advantages of these hooks to start and stop the collectors around the lifecycle of a simulation. The warp-gatling integration provides 4 additional hooks that a user can override for additional flexibility, so any custom code that run around a gatling test can also be run in a warp-gatling test. You may also add/remove collectors/arbiters in these hooks as well.

The order of operations is as follows

1. `beforeStart(): Unit`
2. `controller.startMeasurement()`
3. `afterStart(): Unit`
4. `actual gatling test execution`
5. `beforeEnd(): Unit`
6. `controller.endMeasurement()`
7. `afterEnd(): Unit`

Steps `1-3` are enclosed in the `before` hook of a warp-gatling test, and steps `5-7` are enclosed in the `after` hook.

`beforeStart()`, `afterStart()`, `beforeEnd()`, `afterEnd()` by default do nothing, and can be overridden as needed. For example, custom code in the `@before` and `@after` hooks of a gatling test :

```scala
class Foo extends Simulation {
    before {
        Logger.info("Before starting")
    }
    // YOUR CODE HERE
```

would look like this in a `WarpSimulation`:

```scala
class Foo extends WarpSimulation {
  override def beforeStart(): Unit = {
    Logger.info("Before starting")
  }
  // YOUR CODE HERE
}
```


## Future Enhancements

Future enhancements for gatling integration with warp may/may not include:

- provide bridge dsl between existing warp dsl and gatling's own dsl 
- ability to add user tags (*et necessitate* custom controller)
- a warp-gatling common module (which will include the bridge dsl + other useful utilities)
