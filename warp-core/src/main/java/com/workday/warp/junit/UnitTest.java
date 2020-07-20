package com.workday.warp.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


/**
 * Dropin replacement meta-annotation for @Test to be used for Tagging unit tests.
 *
 * Can be used from gradle:
 *
 * <code>
 * task unitTest(type: Test) {
 *     useJUnitPlatform {
 *         includeTags 'unitTest'
 *         excludeTags 'integTest', 'ci'
 *     }
 * }
 * </code>
 *
 * Created by tomas.mccandless on 6/7/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("unitTest")
@Test
public @interface UnitTest {
}
