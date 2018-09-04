# warp-core

<img src="https://img.shields.io/travis/Workday/warp-core/master.svg?sanitize=true">
<img src="https://img.shields.io/coveralls/github/Workday/warp-core/master.svg?sanitize=true">

WARP (Workday Automated Regression Platform) is a flexible, lightweight, (mostly) functional Scala framework for collecting performance metrics and conducting sound experimental benchmarking.

WARP features a domain specific language for describing experimental designs (conducting repeated trials for different experimental groups). Our library allows users to wrap existing tests with layers of measurement collectors that write performance metrics to a relational database. We also allow users to create arbiters to express custom failure criteria and performance requirements. One arbiter included out of the box is the RobustPcaArbiter, which uses machine learning to detect when a test is deviating from expected behavior and signal an alert.

We believe engineers should reason more scientifically about code performance, and we are excited to provide a platform that allows for easily describing an experiment, collecting benchmark results, and conducting statistical analyses to verify hypotheses about expected performance.

More detailed documentation can be found [here.](https://workday.github.io/warp-core)

## Getting Started
You can start up the required services (note that the included docker-compose file is not intended to be used in production) to run tests with
```
$ docker-compose up -d
```
And test via
```
$ ./gradlew clean test
```
or to just run unit tests, run the `unitTest` task.

All port values and service version numbers are in `.env`.


## Publishing
We use the `maven-publish` gradle plugin.
https://docs.gradle.org/current/userguide/publishing_maven.html

Artifacts can be published to sonatype using `./gradlew publish`.
You'll need to configure your sonatype and signing credentials as project properties:
```
signing.keyId=BEEF
signing.password=abc123
signing.secretKeyRingFile=/full/path/to/secring.gpg

sonatypeUsername=jean-luc.picard
sonatypePassword=makeItSoNumberOne
```
Artifacts can be published to local maven repo using `./gradlew publishToMavenLocal`. Signing is not required for local publish.

## Scala Multiversion
We use [gradle-scala-multiversion-plugin](https://github.com/ADTRAN/gradle-scala-multiversion-plugin)
to cross-compile the project with different scala-lang major versions and publish artifacts with scala version suffixes.
The versions are defined in gradle.properties, however you can also override from the command line:
```
$ ./gradlew -PscalaVersions=2.11.8,2.12.6 test
```

## Versioning
We use the `nebula.release` plugin to determine versions.
https://github.com/nebula-plugins/nebula-release-plugin

Versions exist as git tags in the canonical fork. If you are publishing from your fork, you may need to periodically
sync the tags:
```
git fetch upstream --tags
git push origin --tags
```

There are 4 types of releases we support:
  - final
  - candidate (rc)
  - devSnapshot includes some extra information in the version, including branch name and commit hash.
  - snapshot
  
Artifacts with type `snapshot` or `devSnapshot` are published to workday-unit repo, 
while `final` and `candidate` artifacts are published to workday-release.

By default, the minor version number will be incremented based on the most recent tagged release. If you instead need to
increment the major or patch version, use the property `release.scope`:
```
./gradlew <snapshot|devSnapshot|candidate|final> -Prelease.scope=patch
```

The version can also be overridden using the property `release.version` (please refrain from using this if possible):
```
./gradlew -Prelease.version=1.2.3 final
```


## Dependencies

To enforce repeatable builds while allowing developers to use the flexibility of dynamic dependency ranges, we use the
dependency-lock plugin.

We create a new lock file as follows:
```
./gradlew generateLock saveLock test commitLock
```

Note that at publishing time, the build is configured to resolve dependencies for the pom from `dependencies_2.11.lock`
or `dependencies_2.12.lock`, depending on the scala version being used.
If you have updated `versionInfo.gradle`, you probably need to recreate the dependency lock file as well.

Please see https://github.com/nebula-plugins/gradle-dependency-lock-plugin/wiki/Usage for more detailed information.

Please avoid using global locks. We have noticed behavior where this can override the version of scala-library used by the
zinc compiler.


## Common Errors

```
~/code/warp-core(master*) » gradle clean unitTest --refresh-dependencies
* What went wrong:
A problem occurred evaluating root project 'warp-core'.
> com.google.common.util.concurrent.ExecutionError: java.lang.NoClassDefFoundError: org/gradle/api/artifacts/VersionConstraint

```

You probably need a newer version of gradle. The above error has been observed with gradle 4.2.1, however version 4.4 works.
The root cause appears to be a runtime incompatibility with one of the plugins.

```
$ docker-compose up -d
...
ERROR: for mysql  Cannot start service mysql: driver failed programming external connectivity on endpoint warpcore_mysql_1 (31cb1d98ed696bba0c9ef8cdee368186c52c15c2d0c0f69f1ffae0e7db406a3d): Error starting userland proxy: Bind for 0.0.0.0:3306 failed: port is already allocated
ERROR: Encountered errors while bringing up the project.
```

Change the MySQL port (or that of the offending service) in `.env`

## Authors

Note that this code has been ported from an internal repository and the public commit history does not accurately reflect authorship.
See `CONTRIBUTORS.md` for a full list of everyone who has contributed to this project.

