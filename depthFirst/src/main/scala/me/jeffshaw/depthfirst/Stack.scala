package me.jeffshaw.depthfirst

private class Stack[Out] private (
  /*
  A benchmark between List and Vector showed that List is faster.
   */
  private var stack: DfList[Elem]
) {
  def isFinished: Boolean = stack.isEmpty

  def step(): Iterator[Out] = {
    if (stack.isEmpty) {
      Iterator.empty
    } else {
      val head = stack.head
      val values = head.values

      if (values.hasNext) {
        val value = values.next()
        val ops = head.ops

        head match {
          case filter: Elem.Filter =>
            val f = filter.f
            if (f(value)) {
              if (ops.isEmpty) {
                Iterator(value.asInstanceOf[Out])
              } else {
                val nextF = ops.head
                val remainingFs = ops.tail
                stack = DfCons(nextF.toElem(remainingFs, Iterator(value)), stack)
                Iterator.empty
              }
            } else {
              Iterator.empty
            }

          case flatMap: Elem.FlatMap =>
            val f = flatMap.f
            val fResults = f(value).toIterator

            if (ops.isEmpty) {
              fResults.asInstanceOf[Iterator[Out]]
            } else {
              val nextF = ops.head
              val remainingFs = ops.tail
              stack = DfCons(nextF.toElem(remainingFs, fResults), stack)
              Iterator.empty
            }

          case map: Elem.Map =>
            val f = map.f
            val result = f(value)

            if (ops.isEmpty) {
              Iterator(result.asInstanceOf[Out])
            } else {
              val nextF = ops.head
              val remainingFs = ops.tail
              stack = DfCons(nextF.toElem(remainingFs, Iterator(result)), stack)
              Iterator.empty
            }
        }

      } else {
        stack = stack.tail
        Iterator.empty
      }
    }
  }

  def iterator: Iterator[Out] = {
    for {
      outer <- Iterator.continually(step()).takeWhile(_ => !isFinished)
      result <- outer
    } yield result
  }
}

private object Stack {
  def apply[Out](elem: Elem): Stack[Out] = {
    new Stack(DfList(elem))
  }
}
