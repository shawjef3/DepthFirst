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

  val mathematica: String = {
    val values = mathematicaValues.map(_.mkString("{", ",", "}")).mkString("{", ",", "}")
    val cacheMb = cache / 1024 / 1024
    val plotLabel = "\"" + s"cache ${cacheMb}MB, duplication factor $duplicationFactor" + "\""
    s"""<| "values" ->  $values, "plotLabel" -> $plotLabel |>"""
  }
}

object ContourListPlot {

  val list = Select[ContourListPlot](
    """select cpu, cache, duplication_factor AS duplicationFactor, array_agg(ROW("values", iterations, improvement))
      |from compare('DepthFirstBenchmarks.classicVector', 'DepthFirstBenchmarks.stackDepthFirstVector')
      |group by cpu, cache, duplication_factor
      |order by cache, duplication_factor
      |""".stripMargin
  )

  def main(args: Array[String]): Unit = {
    val h = new HikariConfig()
    h.setJdbcUrl("jdbc:postgresql://psql.jeffshaw.me/data_local_flatmap?user=postgres&password=ofPMstLSqmsGr4r5")
    val pool = Pool(h)
    pool.withConnection {implicit connection =>
      val dataSets = list.vector().map(_.mathematica).mkString("{", ",", "}")
      println(
        s"""dataSets := $dataSets
           |
           |ListContourPlot[
           | #values,
           |FrameLabel -> {Elements, FlatMaps},
           | PlotLegends -> BarLegend[Automatic, LegendLabel -> "% Improvement"],
           | ContourLabels -> True, PlotLabel -> #plotLabel]& /@ dataSets
           |""".stripMargin
      )
    }
  }

}
