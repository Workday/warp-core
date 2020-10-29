package com.workday.warp.junit;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Meta-annotation used to make testId and other metadata available to tests.
 *
 * This annotation is useful for tests that want to control their own measurement without using any beforeEach or afterEach
 * hooks.
 *
 * Created by tomas.mccandless on 10/23/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(WarpInfoExtension.class)
@TestTemplate
public @interface WarpInfoProvided {
}
