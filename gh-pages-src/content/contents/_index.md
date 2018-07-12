+++
title = "Contents"
alwaysopen = "true"
+++


To help engineers reason about performance in a more scientific way, We’re proud to introduce WARP (Workday Automated Regression Platform): a flexible, lightweight, (mostly) functional Scala framework for collecting performance metrics and conducting sound experimental benchmarking.

WARP features a domain specific language for describing experimental designs (conducting repeated trials for different experimental groups). 
Our library allows users to wrap existing tests with layers of measurement collectors that write performance metrics to a relational
database. We also allow users to create arbiters to express custom failure criteria and performance requirements. One arbiter included
out of the box is the `RobustPcaArbiter`, which uses machine learning to detect when a test is deviating from expected
behavior and signal an alert.

We believe engineers should reason more scientifically about code performance, and we are excited to provide a platform that allows
for easily describing an experiment, collecting benchmark results, and conducting statistical analyses to verify hypotheses
about expected performance.
