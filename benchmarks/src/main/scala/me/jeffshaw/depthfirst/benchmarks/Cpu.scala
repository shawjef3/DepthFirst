package me.jeffshaw.depthfirst.benchmarks

import scala.sys.process._

/**
  * Get the cpu id for the current system for the cpus table.
  */
object Cpu {
  private def windowsGetCpu(): String = {
    "wmic cpu get Name".lineStream(2).trim()
  }

  private def macGetCpu(): String = {
    Process(Seq("sysctl", "-n", "machdep.cpu.brand_string")).lineStream.head.trim()
  }

  private def linuxGetCpu(): String = {
    (Process(Seq("cat", "/proc/cpuinfo")) #| Process(Seq("grep", "model name"))).lineStream.head.split(':')(1).trim
  }

  val name: String =
    System.getProperty("os.name") match {
      case "Windows 10" =>
        windowsGetCpu()
      case "Mac OS X" =>
        macGetCpu()
      case "Linux" =>
        linuxGetCpu()
    }

  val cpuIds =
    Map(
      "AMD Ryzen 7 1700 Eight-Core Processor" -> 1,
      "Intel(R) Xeon(R) CPU E5-2697A v4 @ 2.60GHz" -> 2,
      "Intel(R) Core(TM) i7-4980HQ CPU @ 2.80GHz" -> 3,
      "Intel(R) Xeon(R) CPU E5-2650L v3 @ 1.80GHz" -> 4,
      "Intel(R) Xeon(R) CPU E5-2650 v4 @ 2.20GHz" -> 5
    )

  val id = cpuIds(name)
}
