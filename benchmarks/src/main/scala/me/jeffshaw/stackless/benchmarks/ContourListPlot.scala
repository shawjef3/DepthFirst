package me.jeffshaw.stackless.benchmarks

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

  def mathematica(comparison: ContourListPlot.Comparison): String = {
    val values = mathematicaValues.map(_.mkString("{", ",", "}")).mkString("{", ",", "}")
    val cacheMb = cache / 1024 / 1024
    val plotLabel = "\"" + s"${comparison.base} vs ${comparison.comparison},\\ncache ${cacheMb}MB, duplication factor $duplicationFactor" + "\""
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

  case class Comparison(
    base: String,
    comparison: String
  )

  object Comparison {
    def valueOf(csv: String): Comparison = {
      val Array(base, comparison) = csv.split(',')
      Comparison(base, comparison)
    }
  }

  val list =
    Select[ContourListPlot](
      s"""select cpu, cache, duplication_factor AS duplicationFactor, array_agg(ROW("values", iterations, improvement))
         |from compare(@base, @comparison)
         |group by cpu, cache, duplication_factor
         |order by cache, duplication_factor
         |""".stripMargin
    )

  def listContourPlot(comparison: Comparison, plotData: Vector[ContourListPlot]): String = {
    val dataSets = plotData.map(_.mathematica(comparison)).mkString("{", ",", "}")
    s"""(* ${comparison.base} vs ${comparison.comparison}*)
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
    val comparisons = args.tail.map(Comparison.valueOf)
    val pool = Pool(h)
    pool.withConnection {implicit connection =>
      for {
        comparison <- comparisons
      } {
        val dataSets = list.onProduct(comparison).vector()
        println(listContourPlot( comparison, dataSets))
      }
    }
  }

}
