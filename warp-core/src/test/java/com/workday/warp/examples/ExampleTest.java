package com.workday.warp.examples;

import com.workday.warp.TestId;
import com.workday.warp.junit.Measure;
import com.workday.warp.junit.WarpInfo;
import com.workday.warp.junit.WarpTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.pmw.tinylog.Logger;

/**
 * Some examples of Java usage.
 *
 * Created by tomas.mccandless on 10/23/20.
 */
public class ExampleTest {

    /** A plain vanilla junit test with no extensions. */
    @Test
    public void vanilla() {
        Logger.trace("only plain junit infra");
    }


    @Test
    public void testId(final TestInfo info) {
        final String id = TestId.fromTestInfo(info).id();
        Assertions.assertTrue("com.workday.warp.examples.ExampleTest.testId".equals(id));
    }


    @Test
    @Measure
    public void measuredOnly() {
        Logger.trace("we are being measured but not repeated");
    }


    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    public void measured() {
        Logger.trace("we are being measured");
        Assertions.assertEquals(2, 1 + 1);
    }


    /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
    @WarpTest
    public void measuredWithInfo(final WarpInfo info) {
        Assertions.assertTrue(info.testId().equals("com.workday.warp.examples.ExampleTest.measuredWithInfo"));
    }
}
