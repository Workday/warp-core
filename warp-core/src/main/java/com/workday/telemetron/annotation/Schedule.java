package com.workday.telemetron.annotation;

import com.workday.telemetron.math.NullDistribution;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * The <code>Schedule</code> annotation is used to configure measurement framework with information
 * about how the test should be invoked.
 * <p>
 * A simple test looks like this:
 * <pre>
 * public class Example {
 *    <b>&#064;Test</b>
 *    <b>&#064;Schedule(invocations = 3)</b>
 *    public void method() {
 *       Thread.sleep(100);
 *    }
 * }
 * </pre>
 * <p>
 * The <code>Schedule</code> annotation supports several optional parameters.
 * The <code>invocations</code> parameter indicates how many times the test method should be
 * invoked.
 * <p>
 * The <code>warmupInvocations</code> parameter indicates how many times the test should be
 * invoked before it's requirements are verified @see Requirements
 * <p>
 * The <code>threads</code> parameter indicates how many individual threads will be used to invoke the
 * test. This is often used in conjunction with <code>invocations</code> and <code>warmupInvocations</code>
 * <pre>
 *    <b>&#064;Test</b>
 *    <b>&#064;Schedule(invocations = 30,threads = 10)</b>
 *    public void multithreaded() {
 *       new ArrayList&lt;Object&gt;();
 *    }
 * </pre>
 * <b>THREAD SAFETY WARNING:</b> The <code>Scheduler</code> does not create multiple instances of the object
 * being tested. If your test case references instance variables, these references should be synchronized to
 * ensure thread safety. Consider the following test case:
 * <p>
 * <pre>
 * public class IncorrectMultiThreadedTest {
 *
 *     private String threadName;
 *     <b>&#064;Test</b>
 *     <b>&#064;Schedule(invocations = 30,threads = 10)</b>
 *     public void conditionalNap() throws InterruptedException {
 *         String threadName = Thread.currentThread().getName();
 *         System.out.println("Thread = " + threadName + " " + this);
 *         this.threadName = threadName;
 *         if (threadName.endsWith("-0")) {
 *             Thread.sleep(2);
 *         } else {
 *             Thread.sleep(1);
 *         }
 *         assertEquals(threadName, this.threadName);
 *     }
 * }
 *
 * This test case will fail. The reason it will fail is because there will only be a single copy of the
 * <code>IncorrectMultiThreadedTest</code> object. Access to the threadName field will be shared by all
 * of the executing threads.
 * </pre>
 *
 * @author michael.ottati
 * @since 0.1
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Schedule {

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

    /**
     * The statistical distribution that governs the expected elapsed time between successive test invocations.
     * @return
     */
    Distribution distribution() default @Distribution(clazz = NullDistribution.class, parameters = {});
}
