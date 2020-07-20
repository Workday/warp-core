package com.workday.telemetron.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used in conjunction with <code>Schedule</code> to specify a one-time teardown, regardless
 * of the number of invocations configured.
 *
 * Methods annotated with <code>AfterClass</code> JUnit annotation are invoked once per class
 * instantiation. The telemetron <code>Schedule</code> annotation causes multiple instances of
 * the test class to be created, however some use cases can still utilize a single setup/teardown
 * cycle for the entire configured test schedule.
 *
 * Created by vignesh.kalidas on 2/2/17.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AfterOnce {
}
