---
title: "Dependency Injection"
date: 2018-04-03T10:57:50-07:00
draft: true
weight: 70
---

WARP uses [Guice](https://github.com/google/guice) for some dependency bindings.

In particular, we bind a concrete implementation of `AbstractMeasurementCollectionController`,
an implementation of `WarpPropertyLike` (which enumerates all the available runtime configuration parameters),
and provide an extension hook for users to bind additional [tinylog](https://github.com/pmwmedia/tinylog) Writers.

The binding module we provide out of the box in `DefaultWarpModule` should be fine for most use cases.
However, advanced users can implement their own bindings in a new module and set the system property `wd.warp.inject.module`
with the fully qualified value of their module class.

Internally, we use an augmented `MeasurementCollectionController` and an extended version of `CoreWarpPropery`.
