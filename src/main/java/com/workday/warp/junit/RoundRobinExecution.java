package com.workday.warp.junit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify that junit tests should be invoked in round robin order
 * some number of times. For example, if a junit test class has test methods A, B, C and is annotated
 * with this annotation using 2 invocations, the test execution order will be A B C A B C.
 * The value provided by this annotation is read by our custom junit runner, OrderedJUnit4ClassRunner
 * while determining the order of test methods to execute.
 *
 * Created by tomas.mccandless on 8/1/15.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface RoundRobinExecution {

    int DEFAULT_INVOCATIONS = 1;

    /** number of invocations to use */
    int invocations() default DEFAULT_INVOCATIONS;
}