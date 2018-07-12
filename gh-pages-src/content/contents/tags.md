---
title: "Tags"
date: 2018-04-12T16:55:06-07:00
draft: true
weight: 55
---

Tags are simply key-value pairs that are attached to entities in the database.

We support adding tags to `TestDefinition`, `TestExecution`, and other tags themselves in the form of metatags.

Internally, we use tags to differentiate multiple dataset series, annotate tests under triage with their ticket numbers, and record additional metadata about the environment in which a test was executed.

Tags can be configured at the start of test using the DSL. 
