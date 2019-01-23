# Depth First flatMap

by Jeff Shaw

# Motivation

While thinking about data locality, performance, and flatMap, I created an implementation of flatMap that is independent of the collection type, and faster than the usual method for large numbers of flatMaps. Use it when you have a large number of chained flatMaps.

# Implementation

I created a run-time for Scala's `for` syntax operations that performs them in a depth-first manner. It uses a stack of iterators. A stack element is an operation to perform, along with the values for the operation. When a value is requested, operations are performed until a value with no further operations is found.

This is similar to the way Stream works. If you have a Stream that is the result of various operations, and then ask for an element, the data structure will perform the minimum work to get it.

I have not analyzed the memory use of DepthFirst.

# Use

## Dependency

The current draft is on Sonatype's Maven repository for Scala 2.10, 2.11 and 2.12.

`"me.jeffshaw.depthfirst" %% "depthfirst" % "0.1"`

## Example

`DepthFirst` is a data structure that you build up using the familiar `for` syntax. `filter` and `flatMap` do not execute your functions, but instead build an additional data structure, which is a `TraversableOnce`. You can force execution using `map`, `foreach`, `toIterator`, or any methods that use those methods, such as `==`.

The following will print the results immediately.

```scala
import me.jeffshaw.depthfirst.DepthFirst

val vv = Vector(1,2,3)

for {
  v0 <- DepthFirst(vv)
  v1 <- Vector(v0)
  v2 <- Vector(v1)
} println(v2)
```

The following will create the resulting collection before printing the results.

```scala
import me.jeffshaw.depthfirst.DepthFirst

val vv = Vector(1,2,3)

val vv_ = {
  for {
    v0 <- DepthFirst(vv)
    v1 <- Vector(v0)
    v2 <- Vector(v1)
  } yield v2
}.toVector

println(vv_)
```

## DepthFirstList

DepthFirstList demonstrates a data structure that is inherently depth first. You can use DepthFirstList as you would List, although there are odd performance characteristics. For instance, if you append to the end of a DepthFirstList, it forces execution.

# CPU Benchmarks

I have run benchmarks using various sizes of `Vector[Int]`s, various numbers of `flatMap`s of `x => Vector(x)`, and on a few different CPUs. Generally speaking, if you are using collections on the order of ______, or have _ or more flatMaps, you'll gain 10% by using `DepthFirst`. The benefit increases as the collection size increases, the number of flatMaps increases, or the amount of cache your CPU has increases.

Following are graphs of the % improvement you can expect from overhead coming from your collection operations. I apologize for the inconsistent colors. I'll try to improve them in the future.

[DepthFirstBenchmarks.scala](benchmarks/src/main/scala/me/jeffshaw/depthfirst/benchmarks/DepthFirstBenchmarks.scala)

If the images aren't loading, try the [pdf](https://drive.google.com/file/d/1BF1Ps3XMxM_eMu9IesyH_q38TumHn-PN/view?usp=sharing).

## vs Vector

DepthFirst performs better when using Vectors if your Vectors have more than about 1000 elements.

6 MB cache, Intel i7 - 4980 HQ, dedicated MacBook

![image](https://www.jeffshaw.me/depthfirst/M0/6.png)

8 MB cache, AMD Ryzen 1700, dedicated Windows

![image](https://www.jeffshaw.me/depthfirst/M0/8.png)

30 MB cache, Intel E5-2650L, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/30.png)

40 MB cache, Intel E5-2697A, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/40.png)

## vs Stream

Stream performs much better than using Vector directly, and so we have to be using collections on the order of 10^6 to see a benefit from DepthFirst.

6 MB cache, Intel i7 - 4980 HQ, dedicated MacBook

![image](https://www.jeffshaw.me/depthfirst/M0/6stream.png)

8 MB cache, AMD Ryzen 1700, dedicated Windows

![image](https://www.jeffshaw.me/depthfirst/M0/8stream.png)

30 MB cache, Intel E5-2650L, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/30stream.png)

40 MB cache, Intel E5-2697A, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/40stream.png)

## Best Case

The ideal use of DepthFirst is on a CPU with a large cache and large collections. The number of operations doesn't much matter. A CPU with 40 MB of cache and a collection with 3 million elements can see a 69% improvement in the overhead used by a single flatMap. Additional flatMaps yield an increase in the improvement.

![image](https://www.jeffshaw.me/depthfirst/M0/40big.png)

## Data

See [benchmark_data.csv](benchmarks/benchmark_data.csv).

## License

The contents of this repository are usable under the terms of [GPL 3](https://www.gnu.org/licenses/gpl-3.0.en.html).
