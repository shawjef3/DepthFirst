package me.jeffshaw.depthfirst.benchmarks

import com.rocketfuel.sdbc.PostgreSql._
import com.zaxxer.hikari.HikariConfig

case class ContourListPlot(
  cpu: String,
  cache: Int,
  duplicationFactor: Int,
  values: String
) {
  val mathematicaValues = {
    values.drop(2).dropRight(2).split("\",\"").map(_.drop(1).dropRight(1).split(',')).collect {
      case record if record.forall(_.nonEmpty) && record.length == 3 =>
        record.map(_.toInt)
    }
  }

  def mathematica(base: String, comparison: String): String = {
    val values = mathematicaValues.map(_.mkString("{", ",", "}")).mkString("{", ",", "}")
    val cacheMb = cache / 1024 / 1024
    val plotLabel = "\"" + s"$base vs $comparison,\\ncache ${cacheMb}MB, duplication factor $duplicationFactor" + "\""
    s"""<| "values" ->  $values, "plotLabel" -> $plotLabel |>"""
  }
}

object ContourListPlot {

  def list(base: String, comparison: String): Select[ContourListPlot] =
    Select[ContourListPlot](
      s"""select cpu, cache, duplication_factor AS duplicationFactor, array_agg(ROW("values", iterations, improvement))
         |from compare('DepthFirstBenchmarks.$base', 'DepthFirstBenchmarks.$comparison')
         |group by cpu, cache, duplication_factor
         |order by cache, duplication_factor
         |""".stripMargin
    )

  def main(args: Array[String]): Unit = {
    val h = new HikariConfig()
    h.setJdbcUrl(args(0))
    val pool = Pool(h)
    pool.withConnection {implicit connection =>
      val base = args(1)
      val comparison = args(2)
      val dataSets = list(base, comparison).vector().map(_.mathematica(base, comparison)).mkString("{", ",", "}")
      println(
        s"""(* $base vs $comparison *)
           |
           |dataSets := $dataSets
           |
           |ListContourPlot[
           | #values,
           |FrameLabel -> {Elements, FlatMaps},
           | PlotLegends -> BarLegend[Automatic, LegendLabel -> "% Improvement"],
           | ContourLabels -> True, PlotLabel -> #plotLabel, ImageSize -> Medium]& /@ dataSets
           |""".stripMargin
      )
    }
  }

}
