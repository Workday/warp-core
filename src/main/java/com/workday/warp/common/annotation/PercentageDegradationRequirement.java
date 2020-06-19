package com.workday.warp.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.workday.warp.common.CoreWarpProperty.WARP_PERCENTAGE_DEGRADATION_THRESHOLD;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to specify a percentage threshold for response times. (must be within this percentage of 30 day
 * historical average).
 *
 * Created by tomas.mccandless on 5/13/16.
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface PercentageDegradationRequirement {

    double DEFAULT_PERCENTAGE = Double.parseDouble(WARP_PERCENTAGE_DEGRADATION_THRESHOLD().value());

    /**
     * If a measured response time is greater than this percentage above the 30-day historical average, we will fail the
     * test.
     *
     * @return percentage threshold set for response times.
     */
    double percentage();
}
