---
title: "Case Study: Hypothesis Testing"
date: 2018-04-06T10:47:35-07:00
draft: true
weight: 79
---

Two of the most challenging aspects of experimental design are the creation of a strong testable hypothesis
and devising an experiment to test it. This article will describe features in WARP designed to 
assist users in evaluating their experimental hypotheses.

Thanks to Michael Ottati for writing the original form of this document.

## The Hypothesis
A hypothesis is a theory or speculation typically originating from a question:  

> _If X changes, what happens to Y?_  
 
A question can be converted into a hypothesis by reformulating it as a statement.  

>  _A change in in X causes a change in Y._  

A [directional hypothesis](http://methods.sagepub.com/reference/encyc-of-research-design/n114.xml) is stronger form of hypothetical
that predicts the relationship of the change in the variables, e.g. when X increases Y
increases along with it, a direct relationship. An inverse relationship, the prediction
that an increase in X causes a corresponding decrease in Y is another form of directional 
hypothesis. 

The remainder of this article will illustrate how an engineer might use WARP to validate the 
following (obvious) directional hypothesis:

> _If we replace binary search on a sufficiently large `Vector[Int]` with linear search, performance will degrade._  

## The Null Hypothesis

A [null hypothesis](https://en.wikipedia.org/wiki/Null_hypothesis) is a hypothesis that states 
there is no statistical significance between the two variables of the hypothesis. Hypothesis 
testing begins with disproving (falsifying) the null hypothesis. Once the null hypothesis has 
been disproven, the experimenter has grounds for concluding that a relationship exists between 
the variables. In our case, the null hypothesis states that there is no significant difference
between linear search and binary search. Of course, we can never _accept_ the null hypothesis, only fail to reject it.

## The Test Environment
In order to validate the assertion that: _A change in X causes a change in Y_ one must first validate
that: _Constant X yields constant Y_. Stated differently, the basis for any performance test
must be a provably repeatable test.

In cases where constant X does not yield constant Y, it may be due to noise in your test environment. There 
are many causes of noise in computer systems: unaccounted for background processing, periodic task execution, and 
many others. 

Performance tests should be based upon augmentations of demonstrably repeatable tests, and run in noise
free environments. We will construct our experiment by constructing it on top of one such test. 

A repeatable test is a mandatory precondition upon which to
test, and ideally validate, our hypothesis. No valid conclusion can be drawn from a test that is itself not repeatable.

Idempotency is an important concept in computer science, much literature has been written on this topic. It is often 
difficult to design idempotent tests for systems that mutate state. It is sometimes impossible to 
design an idempotent test when the thing being tested mutates (or destroys) the underlying 
state necessary to rerun it. "Fire all Employees" is a hypothetical example of 
such a test, it can only be meaningfully run once. This [What is idempotency?](http://www.restapitutorial.com/lessons/idempotency.html)
article provides a short mildly humorous tutorial on the topic.

### Exogenous factors affecting repeatability
#### Warmups
When running performance tests, it is almost always the case that the first invocation of a test behaves
differently than all subsequent runs of the same test. This is a well known issue that has several common and well understood causes. It is nearly always the case that the first execution through any code path involves considerably 
more processing than subsequent invocations. The list below enumerates some common reasons for these differences,
it is by no means exhaustive. 

* Operating System Cache 
* JVM Class Loading 
* JIT Compilation
* Execution Code Path Differences the first time through. (lazy loading, application cache, ...)

The performance effects of first time execution are well known, well understood, and ubiquitous. Nearly every performance
tool of any sophistication includes a "warm up" capability for this reason. WARP includes a simple mechanism 
within its test execution launcher to specify warmups and invocations. By manipulating these two properties you
may specify how many unrecorded warmups will be run, as well as how many invocations should be recorded
into your final results.

#### Operating System Drift
Performance tests are exquisitely sensitive to the environments they run in. A performance test run on your 
laptop will not yield the same performance on a standalone host in a data center. It is important to remain 
mindful of this when running tests on different platforms. The fact that the same test run on different hardware performs
differently is intuitively obvious to most people. 

What is less obvious, is the fact that the same test run on the same machine may also perform differently over time. Internally,
we saw an example of this recently after  [Meltdown and Spectre](https://meltdownattack.com/) patches were applied to several systems in our lab. 
These patches caused an across the board performance degradation for all tests running in our labs. 

Machines can also degrade over time in unexpected ways. It is therefore important to periodically calibrate
machines to ensure that their performance is comparable to other hosts that are thought to be identical. After
the Meltdown patches were applied, some of the machines in our lab drifted apart from each other. They were brought back into 
sync with each other, only by re-imaging all of them. 

## Change one parameter, measure, repeat.
If more than a single test parameter is changed between two test runs, no valid conclusion can be made about the result. 
In order to fully disambiguate the causality of a change, the experimenter 
must vary only a single parameter at a time. When more than one parameter is varied between two test 
runs, it is impossible to attribute back to either individual paramater what portion of the resulting change each individual parameter 
was responsible for.  

Imagine the experiment of switching gas brands to determine if the new brand provides better gas mileage. Your test
design might be to drive along previously traveled route where you have recorded consistent gas mileage in the past. Prior to 
starting your test, your neighbor asks you to tow his boat to your destination. You agree since you are traveling there anyway. 

This experiment run without the boat may or may not have yielded better gas mileage. The second variable of extra weight
so dominated the results however that it led to the (possibly) incorrect conclusion that gas mileage got worse. When multi
variable changes are made it is impossible to amortize the effects of the individual changes or to even know if one
of the changes leads to a worse result than would have been the case if it had not been changed at all. 

Changing a single thing at a time is the golden rule of hypothesis testing, it is also the rule most frequently 
violated, often leading to fallacious results. 

## Test Design
In order to test our hypothesis, we must vary a single parameter: using binary search vs linear search.
We also know that it is good practice to run a series of invocations in each configuration to verify that results 
are repeatable in both configurations. Finally, it is a good practice to warm up the system by running a few test runs
prior to recording the data we are going to analyze.

In addition to running our test multiple times in each configuration, we need some way to record how the
test was run so we can later understand our result data. WARP has several features that will assist
us in this task. The most important of feature is tagging. Tags are attributes that can be configured, and recorded 
with each test run. 

Tags are simply key-value pairs that are recorded in the results database, and joined with the test execution. The easiest way 
to think about tags is that they are an extensible attribute set that allow the recording of variable state at the time 
your test is run. For this experiment, we can name our tag _search-type_ and we can record two values for it
to discriminate our series: _binary_|_linear_

## Code Setup

Suppose we have the two following search implementations:

* Binary Search

{{< highlight scala "linenos=" >}}
def binarySearch(numbers: Vector[Int], target: Int): Boolean = {

  @tailrec def helper(left: Int, right: Int): Boolean = {
    val mid: Int = left + ((right - left) / 2)
    val midElement: Int = numbers(mid)

    if (right - left < 1) false
    else if (midElement < target) helper(left, mid - 1)
    else if (midElement == target) true
    else helper(mid + 1, right)
  }

  helper(0, numbers.length - 1)
}
{{< /highlight >}}

* Linear Search

{{< highlight scala "linenos=" >}}
@tailrec private def linearSearch(numbers: Vector[Int], target: Int, i: Int = 0): Boolean = {
  if (i == numbers.length) false
  else if (numbers(i) == target) true
  else linearSearch(numbers, target, i + 1)
}
{{< /highlight >}}

As good computer scientists, we know that binary search will significantly outperform the linear variant on sufficiently
large datasets. Here is how we could elucidate that fact using WARP to capture test statistics:

{{< highlight scala "linenos=" >}}
@Test
def hypothesisExample(): Unit = {
  // randomly generate a dataset, and a collection of numbers to search for.
  val dataset: Vector[Int] = Vector.fill(10000000)(Random.nextInt(100)).sorted
  val targets: Seq[Int] = List.fill(10)(Random.nextInt(100))

  // run both search algorithms with the DSL.
  val linearResults: Seq[TrialResult[_]] = using only defaults warmups 5 invocations 30 tags {
    List(ExecutionTag("search-type", "linear"))
  } measure {
    targets.foreach(i => linearSearch(dataset, i))
  }
  val binaryResults: Seq[TrialResult[_]] = using only defaults warmups 5 invocations 30 tags {
    List(ExecutionTag("search-type", "binary"))
  } measure {
    targets.foreach(i => binarySearch(dataset, i))
  }

  // extract wall-clock times for each trial
  val linearTimes: Array[Double] = linearResults.map(_.maybeTestExecution.get.responseTime * 1000).toArray
  val binaryTimes: Array[Double] = binaryResults.map(_.maybeTestExecution.get.responseTime * 1000).toArray

  // conduct stats test and obtain p-value
  val statTestResult: Option[AllRegressionStatTestResults] = TwoSampleRegressionTest.testDifferenceOfMeans(
    binaryTimes, "binary search", linearTimes, "linear search"
  )
  val pValue: Double = statTestResult.get.regressionTest.pValue
}
{{< /highlight >}}

Our experiment creates a randomly generated collection of 10 million integers, each in the range (0, 99). We evaluate each search
algorithm by repeatedly searching for elements in another randomly generated sequence. We tag invocations
in each series with a value for `search-type`, given by the name of the algorithm. Tags are stored
in our database as additional metadata for easier discrimination of series.

On average, it takes 444ms to perform 10 searches using the linear algorithm. By contrast, 
the binary search algorithm takes only 1ms to perform the same task!

We can confirm this difference by inspecting our p-value. A p-value gives the probability 
that we could observe the same data result _assuming_ the truth of the null hypothesis.

In our case, we obtain a very small p-value: `1.51E-37`. The decision to reject or fail to reject the null hypothesis
hinges on our desired confidence value. At the 99% confidence level, since our p-value < 0.01, we can successfully reject the
null hypothesis and conclude that there is a significant difference between binary search and linear search!


## Summary

This article has demonstrated the use of existing features in WARP to quickly validate or repudiate an 
experimental hypothesis. The techniques described in this article can be can be adopted to test any well-formed hypothesis. 

Researchers wishing to conduct hypothesis testing are strongly advised to base their experimental setup on idempotent tests 
whenever possible. 

We have shown how to use WARP to collect measurements for experimental trials and perform some
rudimentary statistical analysis to either reject or fail to reject our null hypothesis.

The WARP DSL makes it simple to design and execute experiments, and includes features to assist with testing for statistical significance.
