---
title: "Runtime Property Configuration"
date: 2018-04-03T10:57:41-07:00
draft: true
weight: 60
---

WARP has many runtime configuration options. For example, one can set the log level, or override default JDBC persistence
properties.

Configuration properties can be set (in order of decreasing precedence) using environment variables, JVM system properties, or by creating a configuration file.

For example, the default database we write to is an in-memory H2 database, however the following minimal `warp.properties`
file can be used as a starting point to write results to MySQL:

```
wd.warp.jdbc.url=jdbc:mysql://localhost:3307/warp?createDatabaseIfNotExist=true&zeroDateTimeBehavior=convertToNull
wd.warp.jdbc.password=1234
wd.warp.jdbc.user=root
wd.warp.jdbc.driver=com.mysql.jdbc.Driver

wd.warp.log.level=info
```

In general, the environment variable form of a warp property is capitalized, with underscores instead of periods. For example, to 
set the log level property, one could pass a JVM system property `-Dwd.warp.log.level=debug`, or set an environment variable
`WD_WARP_LOG_LEVEL=debug`.

By default, WARP will search for a `warp.properties` file in the current working directory and in `~/.warp/`, however the
expected location of the properties file can be overridden using the JVM system property `wd.warp.config.directory`.

There are many tunable knobs; for more detail, see `WarpPropertyManager`, `CoreWarpProperty`, and `PropertyEntry`


