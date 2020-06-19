package com.workday.warp.junit;

import com.workday.telemetron.annotation.Required;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by tomas.mccandless on 6/18/20.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(WarpTestExtension.class)
@TestTemplate
public @interface WarpTest {

    /**
     * Placeholder for the {@linkplain TestInfo#getDisplayName display name} of
     * a {@code @RepeatedTest} method: <code>{displayName}</code>
     */
    String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

    /**
     * Placeholder for the current repetition count of a {@code @RepeatedTest}
     * method: <code>{currentRepetition}</code>
     */
    String CURRENT_REPETITION_PLACEHOLDER = "{currentRepetition}";

    /**
     * Placeholder for the total number of repetitions of a {@code @RepeatedTest}
     * method: <code>{totalRepetitions}</code>
     */
    String TOTAL_REPETITIONS_PLACEHOLDER = "{totalRepetitions}";

    String OP_PLACEHOLDER = "{op}";

    /**
     * <em>Short</em> display name pattern for a repeated test: {@value}
     *
     * @see #CURRENT_REPETITION_PLACEHOLDER
     * @see #TOTAL_REPETITIONS_PLACEHOLDER
     * @see #LONG_DISPLAY_NAME
     */
    String SHORT_DISPLAY_NAME = "[" + OP_PLACEHOLDER + " " + CURRENT_REPETITION_PLACEHOLDER + " of " + TOTAL_REPETITIONS_PLACEHOLDER + "]";

    /**
     * <em>Long</em> display name pattern for a repeated test: {@value}
     *
     * @see #DISPLAY_NAME_PLACEHOLDER
     * @see #SHORT_DISPLAY_NAME
     */
    String LONG_DISPLAY_NAME = DISPLAY_NAME_PLACEHOLDER + " " + SHORT_DISPLAY_NAME;

    /**
     * The default value that will be used for {@link #invocations()} if no value is supplied.
     */
    int INVOCATIONS_DEFAULT = 1;

    /**
     * The default value that will be used for {@link #warmupInvocations()} if no value is supplied.
     */
    int WARMUP_INVOCATIONS_DEFAULT = 0;

    /**
     * The default value that will be used for {@link #threads()} if no value is supplied.
     */
    int THREADS_DEFAULT = 1;

    /**
     * The total number of invocations of the test that should be performed and measured. The total number of times
     * your test will be executed is the sum of {@link #warmupInvocations()} and {@link #invocations()}.
     * <p>
     * The default value is {@link #INVOCATIONS_DEFAULT}
     *
     * @return iterations
     */
    int invocations() default INVOCATIONS_DEFAULT;

    /**
     * The number of "warmup" runs to be invoked prior to the start of actual measurement. Warmup runs will be treated
     * differently than the runs specified in {@link #invocations()}. Warmup invocations will not have their response
     * times recorded, nor can they fail because they have not met the response time requirement specified in the
     * {@link Required} annotation.
     * <p>
     * The default value is {@link #WARMUP_INVOCATIONS_DEFAULT}
     *
     * @return warmupInvocations
     */
    int warmupInvocations() default WARMUP_INVOCATIONS_DEFAULT;

    /**
     * The number of concurrent threads that this test will be executed on.
     * <p>
     * If this value is set {@literal >} 1, all concurrent method executions will run within a single object. Test
     * developers are responsible for their own synchronization semantics within their object.
     * <p>
     * The default value is {@link #THREADS_DEFAULT}
     *
     * @return threads
     */
    int threads() default THREADS_DEFAULT;

    String name() default LONG_DISPLAY_NAME;

}
