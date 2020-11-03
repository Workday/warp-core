package com.workday.telemetron.annotation;

import com.workday.telemetron.math.NullDistribution;
import com.workday.telemetron.math.DistributionLike;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to specify a statistical distribution that governs the expected delay between consecutive test invocations.
 *
 * Can be used for creating load tests.
 *
 * @author tomas.mccandless
 *
 * @deprecated use junit5
 */
@Retention(RUNTIME)
@Target(METHOD)
@Deprecated
public @interface Distribution {

    /**
     * A Distribution specifying the expected duration between test invocations. Defaults to a null distribution that
     * simply returns 0.
     *
     * @return Distribution
     */
    Class<? extends DistributionLike> clazz() default NullDistribution.class;

    /**
     * Parameters that should be used to create an instance of {@link #clazz()}.
     *
     * @return parameters
     */
    double[] parameters() default {};
}
