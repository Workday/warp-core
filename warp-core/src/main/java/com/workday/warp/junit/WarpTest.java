package com.workday.warp.junit;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Meta-annotation used to mark a test for measurement.
 *
 * Note that we apply TestTemplate annotation here, which is required for our parameter resolvers to be correctly detected.
 *
 * Created by tomas.mccandless on 6/18/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(WarpTestExtension.class)
@TestTemplate
public @interface WarpTest {

    /**
     * Number of "warmup" runs to be invoked prior to the start of actual measurement.
     *
     * Warmup runs will be treated differently than runs specified in {@link #trials()}. Warmup invocations will not
     * be measured in any way, nor will they fail due to unsatisfied time requirement.
     *
     * @return number of unmeasured warmup invocations.
     */
    int warmups() default 0;


    /**
     * The total number of invocations of the test that should be performed and measured.
     *
     * The total number of times your test will be executed is the sum of {@link #warmups()} and {@link #trials()}.
     * <p>
     *
     * @return number of measured trial invocations.
     */
    int trials() default 1;
}
