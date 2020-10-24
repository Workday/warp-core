package com.workday.warp.examples;

import com.workday.warp.junit.WarpInfo;
import com.workday.warp.junit.WarpInfoProvided;
import com.workday.warp.junit.WarpTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.pmw.tinylog.Logger;

/**
 * Created by tomas.mccandless on 10/23/20.
 */
public class ExampleTest {

    /** A plain vanilla junit test with no extensions. */
    @Test
    public void vanilla() {
        Logger.trace("only plain junit infra");
    }


    /** An example of using WarpInfoExtension only for the purpose of getting access to a testId. */
    @WarpInfoProvided
    public void run(final WarpInfo info) {
        Logger.trace("we are only being executed");
        Assert.assertTrue(info.testId().equals("com.workday.warp.examples.ExampleTest.run"));
    }


    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    public void measured() {
        Logger.trace("we are being measured");
    }


    /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
    @WarpTest()
    public void measuredWithInfo(final WarpInfo info) {
        Assert.assertTrue(info.testId().equals("com.workday.warp.examples.ExampleTest.measuredWithInfo"));
    }
}
