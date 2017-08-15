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

  val collections = Set("Stream", "List", "Vector")

  val scalaComparisons =
    for {
      collection <- collections
      comparison <- Set("DepthFirstBenchmarks.streamDepthFirst", "DepthFirstBenchmarks.stackDepthFirst")
    } yield ("classic" + collection, comparison + collection)

  val javaStreamComparisons =
    for {
      collection <- collections
    } yield ("DepthFirstBenchmarks.classic" + collection, "javaStream")

  def list(base: String, comparison: String): Select[ContourListPlot] =
    Select[ContourListPlot](
      s"""select cpu, cache, duplication_factor AS duplicationFactor, array_agg(ROW("values", iterations, improvement))
         |from compare('$base', '$comparison')
         |group by cpu, cache, duplication_factor
         |order by cache, duplication_factor
         |""".stripMargin
    )

  def listContourPlot(base: String, comparison: String, plotData: Vector[ContourListPlot]): String = {
    val dataSets = plotData.map(_.mathematica(base, comparison)).mkString("{", ",", "}")
    s"""(* $base vs $comparison*)
       |
       |dataSets := $dataSets
       |
       |ListContourPlot[#values, FrameLabel -> {Elements, FlatMaps},
       |   ColorFunction -> ColorData[{"TemperatureMap", {-100, 100}}],
       |   ColorFunctionScaling -> False,
       |   PlotLegends ->
       |    BarLegend[Automatic, LegendLabel -> "% Improvement"],
       |   ContourLabels -> True, PlotLabel -> #plotLabel,
       |   ImageSize -> Medium] & /@ dataSets
       |""".stripMargin
  }

  def main(args: Array[String]): Unit = {
    val h = new HikariConfig()
    h.setJdbcUrl(args(0))
    val pool = Pool(h)
    pool.withConnection {implicit connection =>
      for {
        (base, comparison) <- Vector(("CanBuildFromBenchmarks.classicList", "CanBuildFromBenchmarks.dfList"))
      } {
        val dataSets = list(base, comparison).vector()
        println(listContourPlot(base, comparison, dataSets))
      }
    }
  }

}
