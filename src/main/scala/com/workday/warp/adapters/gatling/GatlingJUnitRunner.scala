// MIT License
//
// Copyright (c) 2017 Pravo.ru
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.workday.warp.adapters.gatling

import io.gatling.app.Gatling
import io.gatling.core.Predef.Simulation
import io.gatling.core.config.GatlingPropertiesBuilder
import org.junit.runner.{Description, Runner}
import org.junit.runner.notification.{RunNotifier, Failure => JUnitFailure}
import org.pmw.tinylog.Logger

import scala.util.{Failure, Success, Try}

/**
  * JUnitRunner extension to run Gatling tests.
  *
  * Copied from https://github.com/Pravoru/gatling-junitrunner. We couldn't use his artifact directly since it is not cross-compiled.
  * @param simulationClass
  */
final class GatlingJUnitRunner(simulationClass: Class[_ <: Simulation]) extends Runner {

  /**
    * JUnit Description to Identify Tests
    */
  override val getDescription: Description = Description.createSuiteDescription(s"${simulationClass.getCanonicalName}(Gatling)")

  /**
    * Runner method to run gatling test.
    *
    * Uses JUnit notifier to notify results of the test.
    * @param notifier
    */
  override def run(notifier: RunNotifier): Unit = {
    Try {
      notifier.fireTestStarted(getDescription)
      val properties: GatlingPropertiesBuilder = new GatlingPropertiesBuilder
      properties.simulationClass(simulationClass.getCanonicalName)
      properties.noReports()
      properties.resultsDirectory("build/")
      Gatling.fromMap(properties.build)
    } match {
      case Success(0) =>
        Logger.debug("Test succeeded!")
      case Success(nonZero) =>
        notifier.fireTestFailure(new JUnitFailure(getDescription, new RuntimeException(s"Simulation ended with result code $nonZero")))
      case Failure(e) =>
        notifier.fireTestFailure(new JUnitFailure(getDescription, e))
        Logger.error(e)
    }
    notifier.fireTestFinished(getDescription)
  }
}
