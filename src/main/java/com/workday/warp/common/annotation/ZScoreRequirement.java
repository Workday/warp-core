package com.workday.warp.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.workday.warp.common.CoreWarpProperty.WARP_ZSCORE_PERCENTILE_THRESHOLD;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to specify a percentile (z-score) threshold for response times.
 *
 * Be aware that this requirement strategy won't give good results if your test measurements are very stable with low variance.
 *
 * Created by tomas.mccandless on 1/25/16.
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface ZScoreRequirement {

    double DEFAULT_PERCENTILE = Double.parseDouble(WARP_ZSCORE_PERCENTILE_THRESHOLD().value());

    /**
     * If a measured response time is greater than this percentile, assuming a gaussian distribution, we will fail the
     * test.
     *
     * @return percentile threshold set for response times.
     */
    double percentile();
}
