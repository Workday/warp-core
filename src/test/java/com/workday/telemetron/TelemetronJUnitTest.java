package com.workday.telemetron;

import com.workday.telemetron.annotation.Required;
import com.workday.telemetron.spec.TelemetronJUnitSpec;
import com.workday.warp.common.category.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.Duration;

/**
 * JUnit Test demonstrating how to extend TelemetronJUnitSpec from Java
 *
 * Created by leslie.lam on 12/20/17.
 */
public class TelemetronJUnitTest extends TelemetronJUnitSpec {

    /**
     * To disable response time requirements, explicitly call the super constructor
     * and set shouldVerifyResponseTime to false.
     */
    public TelemetronJUnitTest() {
        super(false);
    }

    /**
     * Checks that an exception is not thrown when we disable response time requirement checking.
     */
    @Test
    @Category(UnitTest.class)
    @Required(maxResponseTime = 1)
    public void exceeds() {
        this.telemetron().setResponseTime(Duration.ofSeconds(10));
    }
}
