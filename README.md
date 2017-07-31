# Depth First flatMap

# Motivation

While thinking about data locality, performance, and flatMap, I realized that for most data structures, flatMap behaves exactly the way we wouldn't want it to.

Consider the order of operations for an eager data structure when performing the following.

```scala
val vv = Vector(...)
for {
  v0 <- vv
  v1 <- f0(v0)
  v2 <- f1(v1)
} yield v2
```

First, `f0` is applied to each element of `vv`. Second, `f1` is applied to each element of `vv.flatMap(f0)`. If `vv` or `vv.flatMap(f0)` are too large to fit into cache, then by the time `f1` runs, some of the values it will act on have been evicted and will have to be loaded from RAM.

Ideally, we'd want to perform `f0` and `f1` in a depth first manner. This way, any uses of a value are more likely to be in cache, since the value was more recently used or created. Of course this isn't a guarantee.

# Implementation

I created a run-time for Scala's `for` syntax operations that performs them in a depth-first manner. It uses two stacks. The first is the list of operations to be performed, and the second is an iterator over the values that the operations will apply to. The operations on the stack are exposed as an iterator. When a value is requested, stack operations are performed until a value without no further operations is found.

Clever readers will realize that this is similar to the way Stream works. If you have a Stream that is the result of various operations, and then ask for an element, the data structure will perform the minimum work to get it. In fact [a simple experiment](examples/src/main/scala/me/jeffshaw/depthfirst/examples/StreamComparison.scala) comparing the two shows similar behavior. I haven't thought deeply about why, but the depth first flatMap is faster for large data sets.

I have not analyzed the memory use of DepthFirst.

# Use

## Dependency

The current draft is on Sonatype's Maven repository for Scala 2.10, 2.11 and 2.12.

`"me.jeffshaw.depthfirst" %% "depthfirst" % "0.0-M0"`

## Example

`DataLocal` is a data structure that you build up using the familiar `for` syntax. The result is `TraversableOnce`, with the underlying implementation being an `Iterator`.

The following will print the results immediately.

```scala
import me.jeffshaw.depthfirst.DepthFirst

val vv = Vector(...)

for {
  v0 <- DepthFirst(vv)
  v1 <- f0(v0)
  v2 <- f1(v1)
} println(v2)
```

The following will create the resulting collection before printing the results.

```scala
import me.jeffshaw.depthfirst.DepthFirst

val vv = Vector(...)

val vv_ = {
  for {
    v0 <- DepthFirst(vv)
    v1 <- f0(v0)
    v2 <- f1(v1)
  } yield v2
}.toVector

println(vv_)
```

# CPU Benchmarks

I have run benchmarks using various sizes of `Vector[Int]`s, various numbers of `flatMap`s of `x => Vector(x)`, and on a few different CPUs. Generally speaking, if you are using collections on the order of 100,000, or have 4 or more flatMaps, maps, or filters, you'll gain 10% by using `DepthFirst`. The benefit increases as the collection size increases, the number of operations on the collection increases, or the amount of cache your CPU has increases.

Following are graphs of the % improvement you can expect from overhead coming from your collection operations. I apologize for the inconsistent colors. I'll try to improve them in the future.

[DepthFirstBenchmarks.scala](benchmarks/src/main/scala/me/jeffshaw/depthfirst/benchmarks/DepthFirstBenchmarks.scala)

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

## License

This contents of this repository are usable under the terms of [GPL 3](https://www.gnu.org/licenses/gpl-3.0.en.html).
