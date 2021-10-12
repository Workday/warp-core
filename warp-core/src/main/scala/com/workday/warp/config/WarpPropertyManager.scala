package com.workday.warp.config

import java.io.File
import java.util.Properties
import com.workday.warp.inject.WarpGuicer
import com.workday.warp.logger.WarpLogUtils
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
  * Manages runtime-determination of warp configuration properties.
  *
  * Builds an immutable map containing the values of each value enumerated in [[CoreWarpProperty]]. The value of a given
  * enumerated [[PropertyEntry]] can come from several different sources, for example JVM system properties, the warp configuration
  * file (warp.properties), or the default set in the definition of that enumeration.
  *
  * Created by tomas.mccandless on 10/21/15.
  * Based on a class created by michael.ottati on 3/29/13
  */
object WarpPropertyManager {

  /**
    * Properties containing this prefix will be passed through as system properties with this
    * prefix stripped off. This is the same prefix and behavior that Gradle uses and users should
    * be somewhat familiar with this technique.
    */
  private val SYSTEM_PROPERTY_PREFIX: String = "systemProp."
  private val WARP_PROPERTIES: String = "warp.properties"
  private val WARP_CONFIG_DIRECTORY_PROPERTY: String = "wd.warp.config.directory"

  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  // immutable copy of jvm system properties
  val systemProps: Map[String, String] = System.getProperties.asScala.toMap
  // determine the directory to search for warp configuration files
  val configDirectory: String = computeConfigDirectory
  // compute the path to the warp configuration file we should load
  val propertyFile: String = computePropertyFile

  // load the configuration file
  // note that if the file does not exist, we log a warning and continue execution with an empty property set.
  // (all default property values will be used)
  // if there is an unrecoverable exception from configuration library, we'll throw that exception
  val configuration: PropertiesConfiguration = Try(new Configurations().properties(propertyFile)).recoverWith {
    case _: ConfigurationException if !new File(propertyFile).exists() =>
      logger.warn(s"$propertyFile does not exist!" +
        "\n    Be aware that if the property values you require have not been passed in as Java System properties" +
        "\n    this program is very likely to fail when an unset required property is accessed.\n")
      // fall back on empty config
      Success(new PropertiesConfiguration)
    case exception: Exception =>
      logger.error(s"Error loading WARP Configuration file: $propertyFile \n\n", exception)
      Failure(exception)
  }.get

  // use di to see which property set we are working with
  val propertyEntries: Seq[PropertyEntry] = WarpGuicer.getProperty.values

  // create an immutable map of all warp properties. implementation of property value lookup should consult this map.
  val overlayedProperties: Map[String, String] = overlayProperties
  // set jvm system properties based on configuration file properties beginning with "systemProp."
  propagateSystemProperties()
  // log the configuration values to be used
  logPropertyValues()
  // configure our logger
  WarpLogUtils.setLogLevelFromWarpProperties()


  /**
   * @param entry a PropertyEntry to look up the value of
   * @return the value of PropertyEntry
   */
  @throws[WarpConfigurationException]("when a property is required and does not have a value")
  def valueOf(entry: PropertyEntry): String = {
    this.valueOf(entry.propertyName, entry.isRequired)
  }



  /**
   * Looks up the value of propertyName according to several potential sources for that value.
   *
   * @param propertyName name of the property to look up, eg `wd.warp.foo`
   * @param required whether the value is required. Will throw an exception is the property is required but does not
   *                 have a provided value
   * @return value of propertyName
   */
  @throws[WarpConfigurationException]("when a property is required and does not have a value")
  def valueOf(propertyName: String, required: Boolean): String = {
    val maybeValue: Option[String] = this.overlayedProperties.get(propertyName) match {
      case Some(propertyValue: String) => Option(propertyValue)
      case None if required => throw new WarpConfigurationException(s"$propertyName is required but is null")
      case _ => None
    }

    maybeValue.orNull
  }



  /**
   * Logs the jvm system properties set for each configuration parameter with a name beginning with "systemProp.".
   *
   * Also logs the configuration values for each warp property in the WarpProperty enum.
   *
   * Only called for side effects.
   */
  def logPropertyValues(): Unit = {
    val banner: StringBuilder = new StringBuilder
    val usedConfigFile: String = if (new File(this.propertyFile).exists()) this.propertyFile else "none"

    // add WARP Framework Version and Configuration File in startup banner
    banner ++= s"\nWARP Framework Version = '$version', Configuration File = '$usedConfigFile'"

    // retrieve all properties with SystemProp. prefix from configuration file
    val systemProperties: Iterator[String] = this.configuration.getKeys.asScala.filter(_.startsWith(this.SYSTEM_PROPERTY_PREFIX))

    // if SystemProp.* values exist add System Properties header to banner
    if (systemProperties.nonEmpty) {
      banner ++= "\nSystem Properties added from warp properties file:"
    }

    // loop over all systemProp.* values, log their configured values
    systemProperties foreach { propertyName =>
      // drop the systemProp. prefix to obtain something that should be set in jvm system properties
      val sysPropKey: String = propertyName drop this.SYSTEM_PROPERTY_PREFIX.length
      // don't print password literals
      val redactedValue: String = redact(sysPropKey, System.getProperty(sysPropKey))
      banner ++= s"\n    $propertyName=$redactedValue"
    }

    banner ++= "\nWarp Configuration Properties:"

    this.propertyEntries.toList.sortBy(_.propertyName).map { entry: PropertyEntry =>
      val name: String = entry.propertyName
      // value is not required to be present just for this lookup
      val propertyValue: String = entry.normalize(this.valueOf(name, required = false))
      // don't print password literals
      val redactedValue: String = redact(name, propertyValue)
      banner ++= s"\n    $name=$redactedValue"
    }

    logger.info(banner.toString)
  }



  /**
   * Propagates the values of warp properties whose names begin with "systemProp" to jvm system properties.
   *
   * For example, if {@code systemProp.wd.warp.foo=bar} is set in the warp properties file, {@code wd.warp.foo=bar} will
   * be added as a jvm system property, but only if there is not already a jvm system property set for {@code wd.warp.foo}.
   *
   * Also logs (at INFO level) all the properties that are being passed in.
   *
   * Called during initialization just for side effects.
   */
  def propagateSystemProperties(): Unit = {
    this.configuration.getKeys.asScala.filter(_.startsWith(this.SYSTEM_PROPERTY_PREFIX)) foreach { propertyName =>
      val sysPropKey: String = propertyName drop this.SYSTEM_PROPERTY_PREFIX.length
      val propertyValue: String = this.configuration.getString(propertyName)

      // add the jvm system property only if it isn't already set
      if (!this.systemProps.contains(sysPropKey)) {
        System.setProperty(sysPropKey, propertyValue)
      }
    }
  }



  /**
    * Computes a map containing the assigned values of each WarpProperty according to the following sources in order of
    * decreasing precedence:
    *
    *   1: environment variables
    *   2: jvm system properties
    *   3: properties from the warp configuration file (warp.properties)
    *   4: default property values provided in the enum
    *
    * @return an immutable [[Map]] containing the values for each WarpProperty
    */
  def overlayProperties: Map[String, String] = {
    val properties: mutable.Map[String, String] = mutable.Map[String, String]()

    // add non-null default values
    this.propertyEntries foreach { entry : PropertyEntry =>
      entry.defaultValue foreach { default: String =>
        properties += (entry.propertyName -> default)
      }
    }

    // add properties from the property file
    this.configuration.getKeys.asScala foreach { key =>
      properties += (key -> this.configuration.getString(key))
    }

    // add overrides
    this.propertyEntries foreach { entry =>
      val key: String = entry.propertyName

      // if we find a system property override, add it to our map
      this.systemProps.get(key) foreach { value =>
        properties += (key -> value)
      }

      // if we find an environment variable override, add it to our map
      sys.env.get(entry.envVarName) foreach { value =>
        properties += (key -> value)
      }
    }

    properties.toMap
  }



  /**
   * @return the version of warp currently being executed.
   */
  def version: String = {
    val warpProperties: Properties = new Properties
    warpProperties.load(WarpPropertyManager.getClass.getResourceAsStream("version.properties"))
    warpProperties.getProperty("warp-version")
  }



  /**
   * @return the name of the warp properties file.
   */
  def computePropertyFile: String = {
    val default: String = this.computeConfigDirectory + File.separator + this.WARP_PROPERTIES
    val version: String = this.version
    val versionedPropertyFile: File = new File(s"$default-$version")

    // check for a properties file declared with an extension that matches the current version
    if (versionedPropertyFile.exists) versionedPropertyFile.getAbsolutePath
    else default
  }



  /**
   * Determines the file system path of the warp configuration directory as follows:
   *
   * 1. If wd.warp.config.directory Java system property is supplied in the TeamCity build configuration, look for
   * warp.properties file in that location.
   * 2. If wd.warp.config.directory is not defined, check if ./config/warp.properties exists
   * (i.e. /data/teamcity/buildAgent1140/work/{unique job identifier}/{module}/config/warp.properties).
   * 3. Finally, look for a warp.properties file in the users home .warp directory (i.e. /home/teamcity/.warp)
   */
  def computeConfigDirectory: String = {
    val propertyFile: File = new File(s"config/${this.WARP_PROPERTIES}")

    // check the jvm system property
    if (this.systemProps.contains(this.WARP_CONFIG_DIRECTORY_PROPERTY)) {
      this.systemProps(this.WARP_CONFIG_DIRECTORY_PROPERTY)
    }
    // check the default file name in the current working directory
    else if (propertyFile.exists) {
      propertyFile.getAbsoluteFile.getParent
    }
    // user's home directory
    else {
      this.systemProps("user.home") + File.separator + ".warp"
    }
  }



  /**
   * Redacts propertyValue. If propertyName does not appear to be sensitive, returns propertyValue unchanged.
   *
   * @param propertyName name of the property to be redacted.
   * @param propertyValue value to be redacted.
   * @return redacted version of propertyValue.
   */
  private def redact(propertyName: String, propertyValue: String): String = {
    if (propertyName.toLowerCase.contains("password")) "XXXXXXXX"
    else propertyValue
  }
}
