package com.workday.warp.dsl;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to specify that an annotated method or class is part of the dsl contract.
 *
 * Care should be taken when modifying such entities.
 *
 * Created by tomas.mccandless on 4/22/16.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface DslApi { }
