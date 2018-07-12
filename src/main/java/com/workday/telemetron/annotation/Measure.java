package com.workday.telemetron.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>Measure</code> annotation is used to indicate whether or not response time
 * measurements should be persisted.
 *
 * Created by leslie.lam 1/18/2018.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Measure {
}
