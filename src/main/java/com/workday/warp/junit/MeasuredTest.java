package com.workday.warp.junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for measurement extension.
 *
 * TODO check extension order
 *
 * Created by tomas.mccandless on 6/18/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Test
@ExtendWith(MeasurementExtension.class)
public @interface MeasuredTest {
}
