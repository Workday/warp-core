package com.workday.telemetron.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * You may specify a requirement that your test must meet with this annotation.
 *
 * @author michael.ottati
 * @since 0.1
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Required {

    /**
     * The maximum response time threshold for running the test. If this time value is exceeded, the test will
     * fail.
     * <p>
     * The default value is 0.0
     *
     * @return maximum response time allowed for this test
     */
    public double maxResponseTime() default 0.0;


    /**
     * The TimeUnit under which to interpret {@link maxResponseTime}.
     * <p>
     * The default value is {@code TimeUnit.SECONDS}
     *
     * @return TimeUnit used to interpret {@link maxResponseTime}
     */
    public TimeUnit timeUnit() default TimeUnit.SECONDS;
}
