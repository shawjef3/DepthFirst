package me.jeffshaw.stackless.benchmarks

import java.nio.file.{Files, Paths}
import com.rocketfuel.sdbc.PostgreSql._
import com.zaxxer.hikari.HikariConfig

/**
  * Send the results of DepthFirstBenchmarks to PostgreSql.
  */
object ResultsToSql {
  val resultsPath = Paths.get("results.log")

  val insert =
    Insert(
      """INSERT INTO benchmarks (name, duplication_factor, iterations, "values", microseconds, microseconds_error, cpu_id) VALUES (
        |  @name, @duplicationFactor, @iterations, @values, @microseconds, @microsecondsError, @cpuId
        |)""".stripMargin
    )

  case class Result(
    name: String,
    duplicationFactor: Int,
    iterations: Int,
    values: Int,
    microseconds: Double,
    microsecondsError: Double,
    cpuId: Int = Cpu.id
  )

  object Result {
    val Name = "name"
    val DuplicationFactor = "duplicationFactor"
    val Iterations = "iterations"
    val Values = "values"
    val Microseconds = "microseconds"
    val MicrosecondsError = "microsecondsError"

    val valuesMatcher = "\\[info\\] ([^ ]+) +(\\d+) +(\\d+) +(\\d+) +avgt +\\d+ +([0-9.]+) . +([0-9.]+)  us/op".r(
      Name, DuplicationFactor, Iterations, Values, Microseconds, MicrosecondsError
    )

    def valueOf(line: String): Option[Result] = {
      for (m <- valuesMatcher.findFirstMatchIn(line)) yield {
        Result(
          name = m.group(Name),
          duplicationFactor = m.group(DuplicationFactor).toInt,
          iterations = m.group(Iterations).toInt,
          values = m.group(Values).toInt,
          microseconds = m.group(Microseconds).toDouble,
          microsecondsError = m.group(MicrosecondsError).toDouble
        )
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val jdbcUrl = args(0)
    val poolConfig = new HikariConfig()
    poolConfig.setJdbcUrl(jdbcUrl)
    val pool = Pool(poolConfig)

    val batchInsert =
      io.Source.fromInputStream(Files.newInputStream(resultsPath)).getLines().foldLeft(Batch()) {
        case (accum, line) =>
          //regex from https://stackoverflow.com/questions/25189651/how-to-remove-ansi-control-chars-vt100-from-a-java-string
          //to remove terminal control characters
          Result.valueOf(line.replaceAll("\\e\\[[\\d;]*[^\\d;]","")) match {
            case None =>
              accum
            case Some(result) =>
              accum :+ insert.onProduct(result)
          }
      }

    pool.withConnection { implicit connection =>
      batchInsert.batch()
    }

  }

}
