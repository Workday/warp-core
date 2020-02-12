# warp-core

[![Build Status](https://travis-ci.org/Workday/warp-core.svg?branch=master)](https://travis-ci.org/Workday/warp-core)
[![Coverage Status](https://coveralls.io/repos/github/Workday/warp-core/badge.svg?branch=master)](https://coveralls.io/github/Workday/warp-core?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.workday.warp/warp-core_2.11/badge.svg?subject=scala+2.11)](https://maven-badges.herokuapp.com/maven-central/com.workday.warp/warp-core_2.11)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.workday.warp/warp-core_2.12/badge.svg?subject=scala+2.12)](https://maven-badges.herokuapp.com/maven-central/com.workday.warp/warp-core_2.12)

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

## Code Coverage Requirements

We use scoverage and coveralls gradle plugins to track code coverage. We enforce that high coverage should be maintained. At time of
writing, coverage must be at least 92% for a build to pass. If you want to test coveralls out on your fork, sign in to coveralls
and get your repo token. Then you can generate the coverage reports and submit them to coveralls using
```
$ export COVERALLS_REPO_TOKEN=abcdefg
$ ./gradlew clean reportScoverage coveralls
```

## Scalafix

We use [scalafix](https://scalacenter.github.io/scalafix/) to automatically refactor code. Since we are interested in using semantic rules,
we also need to use semanticdb scala compiler plugin, which harvests and dumps semantic information about the symbols and types in our program.
Scalafix semantic rules depend on semanticdb compiler output. Scalafix should be run like this:
```
$ ./gradlew clean scalafix
```


## Publishing
We use the `maven-publish` gradle plugin.
https://docs.gradle.org/current/userguide/publishing_maven.html

Please use the included `publish.sh` for uploading artifacts. This script handles some subtle interaction between
creating repo tags and scala multiversion plugin.

Example usage:
```
./publish.sh snapshot minor local
```

Will increment minor version component and publish a snapshot (eg 2.3.0-SNAPSHOT) to local maven repo.

To publish to sonatype, the invocation would be something like:
```
./publish.sh candidate minor sonatype
```

To publish to sonatype, you'll need to configure your sonatype and signing credentials as project properties:

[create sonatype jira account](https://issues.sonatype.org/secure/Signup!default.jspa)

[create pgp keys](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
```
signing.keyId=BEEF
signing.password=abc123
signing.secretKeyRingFile=/full/path/to/secring.gpg

sonatypeUsername=jean-luc.picard
sonatypePassword=makeItSoNumberOne
```
Signing is not required for a local publish.

## Scala Multiversion
We use [gradle-scala-multiversion-plugin](https://github.com/ADTRAN/gradle-scala-multiversion-plugin)
to cross-compile the project with different scala-lang major versions and publish artifacts with scala version suffixes.
The versions are defined in gradle.properties, however you can also override from the command line:
```
$ ./gradlew -PscalaVersions=2.11.8,2.12.6 test
```
This plugin works by repeatedly invoking the gradle task graph with each different scala version specified.
Without any version specified, gradle will use the defaultScalaVersion from gradle.properties. This means local IDE builds
will use just one scala version. If you need to run with all configured scala versions, pass the project property `allScalaVersions`
```
$ ./gradlew -PallScalaVersions test
```
Since some of our dependencies are not cross-compiled, currently we only build for 2.12.


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
  - devSnapshot (includes some extra information in the version, including branch name and commit hash)
  - snapshot
  
Artifacts with type `snapshot` or `devSnapshot` are published to sonatype snapshots repo, 
while `final` and `candidate` artifacts are published to sonatype releases repo.

Please use the included `publish.sh` script for publishing, as that script handles interaction between creating repo tags
and scala multiversion. We don't want to create multiple tags during a release process and incorrectly publish some artifacts
under the wrong version.


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

