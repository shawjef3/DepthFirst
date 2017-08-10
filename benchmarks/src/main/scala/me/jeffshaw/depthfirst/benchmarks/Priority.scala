package me.jeffshaw.depthfirst.benchmarks

import scala.sys.process._

object Priority {

  private val logger =
    ProcessLogger(println(_))

  def pid(): String =
    java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@')(0)

  private def setWindowsPriority(): Unit =
    s"wmic process where ProcessId=${pid()} CALL setpriority 128".run(logger)

  private def setLinuxPriority(): Unit =
    s"renice -n -10 -p ${pid()}".run(logger)

  def set(): Unit = {
    System.getProperty("os.name") match {
      case "Windows 10" =>
        setWindowsPriority()
      case "Linux" | "Mac OS X" =>
        setLinuxPriority()
    }

  }

}
