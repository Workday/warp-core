package com.workday.warp.common

/**
  * Trait containing some properties that are common to all experiments.
  *
  * Created by tomas.mccandless on 11/14/17.
  */
trait HasCoreWarpProperties extends WarpPropertyLike {

  /**
    * Database url used to record results. The default value opens a single h2 instance that allows multiple connections
    * and is alive until the JVM exits. If using h2, views will not be created. Currently we only support mysql and h2.
    * In the future we may extend support to postgres or other databases. A couple of other handy h2 configuration options:
    *
    * If you want a private h2 instance that is only alive for a single connection (useful for parallelizing tests):
    * jdbc:h2:mem;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1
    *
    * If you want an h2 instance that writes to disk and stays open after the JVM exits (useful for inspecting results if
    * you don't have/want mysql):
    * jdbc:h2:./build/WARP-Results;DATABASE_TO_UPPER=false;AUTO_SERVER=TRUE
    *
    * Some more documentation on h2:
    * http://www.h2database.com/html/cheatSheet.html
    * http://www.h2database.com/html/features.html#connection_modes
    *
    * Required: Yes
    * Default Value: jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
    */
  val WARP_DATABASE_URL: PropertyEntry = PropertyEntry(
    "wd.warp.jdbc.url", isRequired = true, "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;MODE=MySQL"
  )

  /**
    * Database user for recording results. Defaults to the default h2 user.
    *
    * Required: Yes
    * Default Value: sa
    */
  val WARP_DATABASE_USER: PropertyEntry = PropertyEntry("wd.warp.jdbc.user", isRequired = true, "sa")

  /**
    * Database password for recording results. Defaults to the default h2 password.
    *
    * Required: Yes
    * Default Value: ""
    */
  val WARP_DATABASE_PASSWORD: PropertyEntry = PropertyEntry("wd.warp.jdbc.password", isRequired = true, "")

  /**
    * Database driver for recording results. Defaults to h2 driver.
    * The only other supported driver is mysql (com.mysql.jdbc.Driver).
    *
    * Required: Yes
    * Default Value: org.h2.Driver
    */
  val WARP_DATABASE_DRIVER: PropertyEntry = PropertyEntry("wd.warp.jdbc.driver", isRequired = true, "org.h2.Driver")

  /**
    * Whether or not we should apply flyway schema migrations.
    *
    * This should be disabled in the warp pipelines, where we want to manually run the schema migration jobs.
    * This should probably be enabled for local development, solo machines and aws-based skylab installs.
    *
    * Required: Yes
    * Default Value: true
    */
  val WARP_MIGRATE_SCHEMA: PropertyEntry = PropertyEntry("wd.warp.migrate.schema", isRequired = true, "true")


  /**
    * Controls log level for warp console logging.
    *
    * Required: No
    * Default Value: info
    */
  val WARP_CONSOLE_LOG_LEVEL: PropertyEntry = PropertyEntry("wd.warp.log.level", isRequired = false, "info")

  /**
    * Controls log level for warp file logging.
    *
    * Required: No
    * Default Value: trace
    */
  val WARP_FILE_LOG_LEVEL: PropertyEntry = PropertyEntry("wd.warp.file.log.level", isRequired = false, "trace")

  /**
    * File where warp logs will be written.
    *
    * Required: No
    * Default Value: build/warp.log
    */
  val WARP_LOG_FILE: PropertyEntry = PropertyEntry("wd.warp.log.file", isRequired = false, "build/warp.log")

  // tracks build number of the stack, defaults to the beginning of unix time
  val SILVER_BUILD_NUMBER: PropertyEntry = PropertyEntry("SILVER_BUILD_NUMBER", isRequired = false, "1970.1.1")

  /**
    * URL for accessing influxdb.
    *
    * Required: No
    * Default Value: http://localhost:8086
    */
  val WARP_INFLUXDB_URL: PropertyEntry = PropertyEntry("wd.warp.influxdb.url", isRequired = false, "http://localhost:8086")

  /**
    * Username to use for accessing influxdb. Ignored unless authentication is explicitly enabled in influxdb
    *
    * Required: No
    * Default Value: root
    */
  val WARP_INFLUXDB_USER: PropertyEntry = PropertyEntry("wd.warp.influxdb.user", isRequired = false, "root")

  /**
    * Password to use when accessing influxdb. Ignored unless authentication is explicitly enabled in influxdb
    *
    * Required: No
    * Default Value: root
    */
  val WARP_INFLUXDB_PASSWORD: PropertyEntry = PropertyEntry("wd.warp.influxdb.password", isRequired = false, "root")


  /**
    * Name of the default retention policy used when accessing influxdb. The default defue here, "autogen" is compatible
    * with influxdb 1.+. Newer versions renamed the default retention policy from "default" to "autogen".
    *
    * Required: No
    * Default Value: autogen
    */
  val WARP_INFLUXDB_RETENTION_POLICY: PropertyEntry = PropertyEntry("wd.warp.influxdb.retention.policy", isRequired = false, "autogen")


  /**
    * Name of the influxdb database to store heap histograms in. Will be created if it does not exist. Defaulting
    * to skylab as that is also the default datasource currently configured in our grafana install.
    *
    * Required: No
    * Default Value: skylab
    */
  val WARP_INFLUXDB_HEAPHISTO_DB: PropertyEntry = PropertyEntry("wd.warp.influxdb.heaphisto.db", isRequired = false, "skylab")


  /**
    * Name of the influxdb series to store heap histograms in. An influxdb series is roughly analagous to a mysql
    * table. We store a larger subset of the entire class histogram here, and a smaller subset (which is intended
    * to be plotted) in WARP_INFLUXDB_HEAPHISTO_PLOT_SERIES
    *
    * Required: No
    * Default Value: heapHistograms
    */
  val WARP_INFLUXDB_HEAPHISTO_SERIES: PropertyEntry = PropertyEntry(
    "wd.warp.influxdb.heaphisto.series", isRequired = false, "heapHistograms"
  )


  /**
    * Name of the influxdb series to store top heap histogram entries in. This series is used to store a smaller
    * subset of the overall class histogram. This smaller subset is what the plots in grafana should be based on.
    */
  val WARP_INFLUXDB_HEAPHISTO_PLOT_SERIES: PropertyEntry = PropertyEntry(
    "wd.warp.influxdb.heaphisto.plot.series", isRequired = false, "topHeapHistograms"
  )


  /**
    * Whether heap histogram collection is enabled (bracketing the test run). Disabled by default. Clobbered by the
    * value of WARP_CONTINUOUS_HEAPHISTO_ENABLED. (if both properties evaluate to true, only the continuous
    * collector will be enabled).
    *
    * Required: No
    * Default Value: false
    */
  val WARP_HEAPHISTO_ENABLED: PropertyEntry = PropertyEntry("wd.warp.heaphisto.enabled", isRequired = false, "false")


  /**
    * Whether continuous heap histogram collection is enabled. Disabled by default. Clobbers the value of
    * WARP_HEAPHISTO_ENABLED. (if both properties evaluate to true, only the continuous collector will be enabled
    * since heap histogram collection is expensive)
    *
    * Required: No
    * Default Value: false
    */
  val WARP_CONTINUOUS_HEAPHISTO_ENABLED: PropertyEntry = PropertyEntry(
    "wd.warp.continuous.heaphisto.enabled", isRequired = false, "false"
  )


  /**
    * When processing heap histograms, persist entries for all generated classes, even if they were not among
    * the top resource consumers.
    *
    * Required: No
    * Default Value: true
    */
  val WARP_HEAPHISTO_INCLUDE_GENERATED: PropertyEntry = PropertyEntry(
    "wd.warp.heaphisto.persist.generated", isRequired = false, "true"
  )


  /**
    * When processing heap histograms, we consult this property. We sort the heap histogram entries by both
    * instances and byte usage, and union the top n classes from both sorted lists to obtain the final list of
    * histogram entries that will be persisted in influxdb. A similar property below controls how many entries from
    * each sublist will be considered to be persisted in a separate series that will be plotted.
    *
    * Required: No
    * Default Value: 500
    */
  val WARP_HEAPHISTO_PROCESSING_LIMIT: PropertyEntry = PropertyEntry(
    "wd.warp.heaphisto.processing.limit", isRequired = false, "500"
  )


  /**
    * When processing heap histograms, we consult this property. We sort the heap histogram entries by both
    * instances and byte usage, and union the top n classes from both sorted lists to obtain the final list of
    * histogram entries that will be put into a separate series to be plotted.
    *
    * Required: No
    * Default Value: 30
    */
  val WARP_HEAPHISTO_GRAFANA_LIMIT: PropertyEntry = PropertyEntry(
    "wd.warp.heaphisto.grafana.limit", isRequired = false, "30"
  )


  /**
    * Whether we should invoke a full GC prior to sampling the heap.
    * In other words, this essentially controls the difference between "jmap -histo:live" and "jmap -histo:all".
    *
    * Required: No
    * Default Value: false
    */
  val WARP_HEAPHISTO_INVOKE_GC: PropertyEntry = PropertyEntry(
    "wd.warp.heaphisto.invoke.gc", isRequired = false, "false"
  )


  /**
    * Controls the threadpool size for parallel measurement collection
    *
    * Required: No
    * Default Value: 8
    */
  val WARP_NUM_COLLECTOR_THREADS: PropertyEntry = PropertyEntry("wd.warp.num.collector.threads", isRequired = false, "8")

  /**
    * Controls the default measurement interval (ms) for continuous measurement collectors.
    *
    * Required: No
    * Default Value: 60000 (once per minute)
    */
  val WARP_CONTINUOUS_MEASUREMENT_INTERVAL: PropertyEntry = PropertyEntry(
    "wd.warp.continuous.measurement.interval.ms", isRequired = false, "60000"
  )

  /**
    * Controls whether rpca anomaly detection will take place.
    *
    * Required: No
    * Default Value: false
    */
  val WARP_ANOMALY_RPCA_ENABLED: PropertyEntry = PropertyEntry("wd.warp.anomaly.rpca.arbiter.enabled", isRequired = false, "false")

  /**
    * Penalty for the L (low-rank) component of measurement in anomaly detection.
    * See Zhou's 2010 paper on Stable Principal Component Pursuit.
    * See {@link com.workday.warp.math.RPCA}
    *
    * Required: No
    * Default Value: 1.0
    */
  val WARP_ANOMALY_RPCA_L_PENALTY: PropertyEntry = PropertyEntry("wd.warp.anomaly.rpca.l.penalty", isRequired = false, "1.0")

  /**
    * Numerator for the S (sparse) component penalty of measurement in anomaly detection. This value will be divided by
    * sqrt(n), where n is the length of the series being analyzed, to determine the final S penalty used during analysis.
    *
    * See Zhou's 2010 paper on Stable Principal Component Pursuit.
    * See {@link com.workday.warp.math.RPCA}
    *
    * Required: No
    * Default Value: 1.4
    */
  val WARP_ANOMALY_RPCA_S_PENALTY_NUMERATOR: PropertyEntry = PropertyEntry(
    "wd.warp.anomaly.rpca.s.penalty.numerator", isRequired = false, "1.4"
  )

  /**
    * Threshold for the S component of measurement. If the S component of a standardized measurement is greater than
    * this value, treat that measurement as an anomaly.
    *
    * Required: No
    * Default Value: 0.0
    */
  val WARP_ANOMALY_RPCA_S_THRESHOLD: PropertyEntry = PropertyEntry("wd.warp.anomaly.rpca.s.threshold", isRequired = false, "0.0")

  /**
    * Minimum number of historical measurements required for rpca anomaly detection.
    *
    * Required: No
    * Default Value: 30
    */
  val WARP_ANOMALY_RPCA_MINIMUM_N: PropertyEntry = PropertyEntry("wd.warp.anomaly.rpca.minimum.n", isRequired = false, "30")

  /**
    * When this is set to true, we'll run rpca on all historical data, then filter out anomalies and rerun the algorithm
    * on only normal historical measurements. Comparing today's measurement to only normal historical measurements
    * will help the system have a longer memory. In other words, it won't be as quick to start believing that anomalous
    * measurements are actually normal when there is a string of consecutive anomalous measurements.
    *
    * Required: No
    * Default Value: false
    */
  val WARP_ANOMALY_DOUBLE_RPCA: PropertyEntry = PropertyEntry("wd.warp.anomaly.double.rpca", isRequired = false, "false")

  /**
    * Number of most recent measurements we'll consider when wd.warp.anomaly.double.rpca is true. Running double rpca
    * on thousands of historical measurements can be expensive; processing 4000 measurements takes around 40 seconds,
    * while processing 1000 measurements takes 2 seconds, and 2000 measurements takes around 7 seconds. Considering a
    * subset of the full data is a time optimization since we don't want too much arbiter overhead.
    *
    * Required: No
    * Default Value: 2000
    */
  val WARP_ANOMALY_DOUBLE_RPCA_TRUNCATION: PropertyEntry = PropertyEntry(
    "wd.warp.anomaly.double.rpca.truncation", isRequired = false, "2000"
  )

  /**
    * Whether SMART number arbiter is enabled. We'll use bisection method to find the minimum response time that would
    * be flagged as an anomaly given the historical response times, then use that as a maximum response time threshold.
    *
    * Required: No
    * Default Value: false
    */
  val WARP_ANOMALY_SMART_ENABLED: PropertyEntry = PropertyEntry("wd.warp.anomaly.smart.enabled", isRequired = false, "false")

  /**
    * Maximum number of iterations to use during bisection method for finding SMART number.
    *
    * Required: No
    * Default Value: 20
    */
  val WARP_ANOMALY_SMART_MAX_ITERATIONS: PropertyEntry = PropertyEntry("wd.warp.anomaly.smart.max.iterations", isRequired = false, "20")

  /**
    * Scalar for SMART number before it is treated as a threshold. Can be used to give tests extra breathing room,
    * reducing false positives when response times are known to be volatile.
    *
    * Required: No
    * Default Value: 1.0
    */
  val WARP_ANOMALY_SMART_SCALAR: PropertyEntry = PropertyEntry("wd.warp.anomaly.smart.scalar", isRequired = false, "1.0")

  /**
    * Number of retries that will be attempted for find or create operations.
    *
    * Required: No
    * Default Value: 4
    */
  val WARP_PERSISTENCE_RETRIES: PropertyEntry = PropertyEntry("wd.warp.persistence.retries", isRequired = false, "4")

  /**
    * Minimum number of recorded measurements necessary for historical arbiters. Applies to all arbiters that extend
    * the abstract class {@link com.workday.warp.arbiters.HistoricalArbiter}.
    *
    * Required: No
    * Default Value: 30
    */
  val WARP_ARBITER_MINIMUM_N: PropertyEntry = PropertyEntry("wd.warp.arbiter.minimum.n", isRequired = false, "30")

  /**
    * Whether historical arbiters should consider the entire measurement history, or just a sliding window of the most
    * recent measurements. The size of the sliding window is controlled by wd.warp.arbiter.sliding.window.size.
    * Applies to all arbiters that extend the abstract class {@link com.workday.warp.arbiters.HistoricalArbiter}.
    *
    * Required: No
    * Default Value: false
    */
  val WARP_ARBITER_SLIDING_WINDOW: PropertyEntry = PropertyEntry("wd.warp.arbiter.sliding.window", isRequired = false, "false")

  /**
    * Number of historical measurements that should be taken into consideration by historical arbiters when sliding
    * windows are enabled. Applies to all arbiters that extend the abstract class
    * {@link com.workday.warp.arbiters.HistoricalArbiter}.
    *
    * Required: No
    * Default Value: 30
    */
  val WARP_ARBITER_SLIDING_WINDOW_SIZE: PropertyEntry = PropertyEntry("wd.warp.arbiter.sliding.window.size", isRequired = false, "30")

  /**
    * Whether z-score arbitration is enabled.
    *
    * Required: No
    * Default Value: false
    */
  val WARP_ZSCORE_PERCENTILE_ARBITER_ENABLED: PropertyEntry = PropertyEntry(
    "wd.warp.zscore.percentile.arbiter.enabled", isRequired = false, "false"
  )

  /**
    * Whether anomaly detection provided by lambda function is enabled.
    *
    * Required: Yes
    * Default Value: false
    */
  val WARP_LAMBDA_ARBITER_ENABLED: PropertyEntry = PropertyEntry("wd.warp.lambda.arbiter.enabled", isRequired = true, "false")

  /**
    * Fully qualified URL for anomaly detection lambda function.
    *
    * Required: Yes
    * Default Value: https://nsd0jwpj65.execute-api.us-west-1.amazonaws.com/prod/anomaly_detection
    */
  val WARP_LAMBDA_ARBITER_URL: PropertyEntry = PropertyEntry(
    "wd.warp.lambda.arbiter.url", isRequired = true, "https://nsd0jwpj65.execute-api.us-west-1.amazonaws.com/prod/anomaly_detection"
  )

  /**
    * API key for the API that invokes the anomaly detection lambda function
    *
    * Required: Yes
    * Default Value: None
    */
  val WARP_LAMBDA_ARBITER_API_KEY: PropertyEntry = PropertyEntry("wd.warp.lambda.arbiter.api.key", isRequired = true)

  /**
    * Threshold used by the percentile (gaussian distribution z-score) arbiter.
    *
    * Required: No
    * Default Value: 98
    */
  val WARP_ZSCORE_PERCENTILE_THRESHOLD: PropertyEntry = PropertyEntry("wd.warp.zscore.percentile.threshold", isRequired = false, "98")

  /**
    * Whether percentage arbitration is enabled. Measured response times must not be greater than x% above the historical
    * mean of sliding window size.
    *
    * Required: No
    * Default Value: False
    */
  val WARP_PERCENTAGE_DEGRADATION_ARBITER_ENABLED: PropertyEntry = PropertyEntry(
    "wd.warp.percentage.degradation.arbiter.enabled", isRequired = false, "false"
  )

  /**
    * Threshold used by the percentage arbiter. (Ensures response time is not greater than this percentage more than
    * the 30-day historical average)
    *
    * Required: No
    * Default Value: 15
    */
  val WARP_PERCENTAGE_DEGRADATION_THRESHOLD: PropertyEntry = PropertyEntry(
    "wd.warp.percentage.degradation.threshold", isRequired = false, "15"
  )

  /**
    * Log level for slick specific logging.
    *
    * Required: No
    * Default Value: INFO
    */
  val WARP_SLF4J_SLICK_LOG_LEVEL: PropertyEntry = PropertyEntry(
    "wd.warp.slf4j.slick.log.level", isRequired = false, "INFO"
  )

  /**
    * Log level for hikari-cp specific logging.
    *
    * Required: No
    * Default Value: INFO
    */
  val WARP_SLF4J_HIKARI_LOG_LEVEL: PropertyEntry = PropertyEntry(
    "wd.warp.slf4j.hikari.log.level", isRequired = false, "INFO"
  )

  /**
    * Log level for flyway specific logging
    *
    * Required: No
    * Default Value: INFO
    */
  val WARP_SLF4J_FLYWAY_LOG_LEVEL: PropertyEntry = PropertyEntry(
    "wd.warp.slf4j.flyway.log.level", isRequired = false, "INFO"
  )


}
