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
    s"""(* $cpu, $cache, duplication factor $duplicationFactor *)
      |ListContourPlot[
      | ${mathematicaValues.map(_.mkString("{", ",", "}")).mkString("{", ",", "}")},
      |FrameLabel -> {Elements, FlatMaps},
      | PlotLegends -> BarLegend[Automatic, LegendLabel -> "% Improvement"],
      | ContourLabels -> True, PlotLabel -> "cache ${cache}, duplication factor $duplicationFactor"]
    """.stripMargin
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
      list.iterator.foreach(x => println(x.mathematica))
    }
  }

}
