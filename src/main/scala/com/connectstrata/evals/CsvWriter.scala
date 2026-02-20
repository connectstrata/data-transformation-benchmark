package com.connectstrata.evals

import java.io.PrintWriter

object CsvWriter {

  def writeResultsByModel(results: List[TestResult], outputPath: String): Unit = {
    val rows = results
      .groupBy(_.model)
      .map {
        case (model, groupResults) =>
          val score = groupResults.count(_.score == Score.Pass).toDouble / groupResults.size.toDouble
          (model, score)
      }
      .toList
      .sortBy(-_._2)
      .zipWithIndex
      .map {
        case ((model, score), index) =>
          s"${index + 1},${model.id},$score"
      }

    val header = "rank,model,score"
    writeCsv(header, rows, outputPath)
  }

  private def writeCsv(header: String, rows: List[String], outputPath: String): Unit = {
    val csv    = (header +: rows).mkString("\n")
    val writer = new PrintWriter(outputPath)
    try writer.write(csv)
    finally writer.close()
  }

  def writeResults(results: List[TestResult], outputPath: String): Unit = {
    val rows = results
      .sortBy(r =>
        (
          r.model.id,
          r.testCase.difficulty.ordinal,
          r.testCase.target.label,
        ),
      )
      .map { r =>
        val result         = r.score match {
          case Score.Pass    => "Pass"
          case Score.Fail(_) => "Fail"
        }
        val error          = r.score match {
          case Score.Pass         => ""
          case Score.Fail(errors) => errors.mkString("; ")
        }
        val transformation = r.transformation
        val testNumber     = TestCases.all.indexOf(r.testCase) + 1
        val sources        = r.testCase.sources.map(_.label).distinct.mkString("; ")
        val target         = r.testCase.target.label
        val difficulty     = r.testCase.difficulty

        s"$result,$testNumber,${r.model.id},${escapeCsv(sources)},${escapeCsv(target)},$difficulty,${escapeCsv(error)},${escapeCsv(transformation)}"
      }

    val header = "result,test,model,sources,target,difficulty,error,transformation"
    val csv    = (header +: rows).mkString("\n")

    val writer = new PrintWriter(outputPath)
    try writer.write(csv)
    finally writer.close()
  }

  def writeFailureResults(results: List[TestResult], outputPath: String): Unit = {
    val failures = results.filter(_.score match {
      case Score.Fail(_) => true
      case Score.Pass    => false
    })

    writeResults(failures, outputPath)
  }

  def writeResultsSummary(results: List[TestResult], outputPath: String): Unit = {
    val rows = results
      .sortBy(r =>
        (
          r.model.id,
          r.testCase.difficulty.ordinal,
          r.testCase.target.label,
        ),
      )
      .map { r =>
        val result     = r.score match {
          case Score.Pass    => "Pass"
          case Score.Fail(_) => "Fail"
        }
        val error      = r.score match {
          case Score.Pass         => ""
          case Score.Fail(errors) => errors.mkString("; ")
        }
        val testNumber = TestCases.all.indexOf(r.testCase) + 1
        val sources    = r.testCase.sources.map(_.label).distinct.mkString("; ")
        val target     = r.testCase.target.label
        val difficulty = r.testCase.difficulty

        s"$result,$testNumber,${r.model.id},${escapeCsv(sources)},${escapeCsv(target)},$difficulty,${escapeCsv(error)}"
      }

    val header = "result,test,model,sources,target,difficulty,error"
    writeCsv(header, rows, outputPath)
  }

  private def escapeCsv(value: String): String = {
    val escaped = value.replace("\"", "\"\"")
    s""""$escaped""""
  }
}
