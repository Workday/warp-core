package com.workday.warp.junit;

import org.junit.jupiter.api.RepeatedTest;

/**
 * Created by tomas.mccandless on 6/18/20.
 */
public interface WarpInfo {

    /**
     * Get the current repetition of the corresponding
     * {@link RepeatedTest @RepeatedTest} method.
     */
    int currentRepetition();

    /**
     * Get the total number of repetitions of the corresponding
     * {@link RepeatedTest @RepeatedTest} method.
     *
     * @see RepeatedTest#value
     */
    int totalRepetitions();
}
