Depth First flatMap
===================

Motivation
==========
While thinking about data locality, performance, and flatMap, I realized that for most data structures, flatMap behaves exactly the way we wouldn't want it to.

Consider the order of operations for an eager data structure when performing `s.flatMap(sElem => f0(sElem).flatMap(f0sElem => f1(f0sElem).flatMap(...)...)`. First, `f0` is applied to each element of `s`. Second, `f1` is applied to each element of `s.flatMap(f0)`. If `s` or `s.flatMap(f0)` are too large to fit into cache, then by the time `f1` will be run, the values it will act on have been evicted and will have to be loaded from RAM.

Ideally, we'd want to perform `f0`, `f1`, etc. in a depth first search. This way, any uses of a given element of the collection will be active while the element is already in cache. Of course this isn't a guarantee, but it's more likely.

Implementation
==============

I created a run-time for Scala's `for` syntax operations that performs them in a depth-first manner. It uses two stacks. The first is the list of operations to be performed, and the second is an iterator over the values that the operations will apply to. The operations on the stack are exposed as an iterator. When a value is requested, stack operations are performed until a value without no further operations is found.

Clever readers will realize that this is similar to the way Stream works. If you have a Stream that is the result of various operations, and then ask for an element, the data structure will perform the minimum work to get it. In fact [a simple experiment](examples/src/main/scala/me/jeffshaw/depthfirst/examples/StreamComparison.scala) comparing the two shows similar behavior. I haven't thought deeply about why, but the depth first flatMap is faster for large data sets.

I have not analyzed the memory use of DepthFirst.

CPU Benchmarks
==========

I have run benchmarks using various sizes of `Vector[Int]`s, various numbers of `flatMap`s of `x => Vector(x)`, and on a few different CPUs. Generally speaking, if you are using collections on the order of 100,000, or have 4 or more flatMaps, maps, or filters, you'll gain 10% by using `DepthFirst`. The benefit increases as the collection size increases, the number of operations on the collection increases, or the amount of cache your CPU has increases.

Following are graphs of the % improvement you can expect from overhead coming from your collection operations. I apologize for the inconsistent colors. I'll try to improve them in the future.

[DepthFirstBenchmarks.scala](benchmarks/src/main/scala/me/jeffshaw/depthfirst/benchmarks/DepthFirstBenchmarks.scala)

## vs Vector

6 MB cache, Intel i7 - 4980 HQ, dedicated MacBook

![image](https://www.jeffshaw.me/depthfirst/M0/6.png)

8 MB cache, dedicated AMD Ryzen, Windows

![image](https://www.jeffshaw.me/depthfirst/M0/8.png)

30 MB cache, Intel E5-2650L, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/30.png)

40 MB cache, Intel E5-2697A, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/40.png)

## vs Stream

6 MB cache, Intel i7 - 4980 HQ, dedicated MacBook

![image](https://www.jeffshaw.me/depthfirst/M0/6stream.png)

8 MB cache, dedicated AMD Ryzen, Windows

![image](https://www.jeffshaw.me/depthfirst/M0/8stream.png)

30 MB cache, Intel E5-2650L, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/30stream.png)

40 MB cache, Intel E5-2697A, VM from Digital Ocean

![image](https://www.jeffshaw.me/depthfirst/M0/40stream.png)
