package com.workday.warp.junit;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alias for adding our measurement extension.
 *
 * Note, however that we do not control the ordering of before/after junit hooks, thus our measurements
 * may include extra overhead if invoked in this way.
 *
 * Created by tomas.mccandless on 11/2/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(MeasurementExtension.class)
public @interface Measure {

}
