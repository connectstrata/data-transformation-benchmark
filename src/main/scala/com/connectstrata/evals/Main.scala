package com.connectstrata.evals

import com.connectstrata.evals.js.Graal
import zio.json.ast.Json
import zio.json.EncoderOps
import zio.Chunk
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

object Main extends ZIOAppDefault {

  /** Recursively removes fields with null values from JSON objects. */
  private def deepDropNulls(json: Json): Json = json match {
    case Json.Obj(fields)   =>
      Json.Obj(fields.collect {
        case (key, value) if value != Json.Null => key -> deepDropNulls(value)
      })
    case Json.Arr(elements) =>
      Json.Arr(elements.map(deepDropNulls))
    case other              => other
  }

  /**
   * LLMs commonly return the value wrapped in code fences. We try to prevent it with the prompt
   * but this will strip them if the prompt instructions were not followed
   */
  private def stripCodeFences(s: String): String = {
    val trimmed = s.trim
    if trimmed.startsWith("```") then {
      val withoutStart = trimmed.dropWhile(_ != '\n').drop(1)
      if withoutStart.endsWith("```") then withoutStart.dropRight(3).trim
      else withoutStart.trim
    } else trimmed
  }

  /**
   * Merges multiple source schemas into a single JSON object schema with integer keys.
   * Each source schema becomes a property under its zero-based index.
   */
  private def mergeSourceSchemas(sourceSchemas: List[String]): String = {
    val properties = sourceSchemas.zipWithIndex
      .map {
        case (schema, index) =>
          s""""$index": $schema"""
      }
      .mkString(",\n    ")

    s"""{
       |  "type": "object",
       |  "properties": {
       |    $properties
       |  }
       |}""".stripMargin
  }

  /**
   * Merges multiple source examples into a single JSON object with zero-based integer string keys.
   */
  private def mergeSourceExamples(examples: List[Json]): Json.Obj = {
    val fields = examples.zipWithIndex.map {
      case (example, index) =>
        index.toString -> example
    }
    Json.Obj(fields*)
  }

  private def score(targetSchema: String, output: Json): Score =
    SchemaValidator.validate(targetSchema, output) match {
      case Left(errors) => Score.Fail(errors)
      case Right(_)     => Score.Pass
    }

  private def evaluateJs(
      jsCode: String,
      input: Json.Obj,
      targetSchema: String,
  ): ZIO[Any, String, (Score, Json)] =
    Graal
      .executeJs(jsCode, "transform", Seq(input))
      .mapError(e => s"JS execution failed: ${e.message}")
      .map { output =>
        val cleaned = deepDropNulls(output)
        (score(targetSchema, cleaned), cleaned)
      }

  private def getTestCases(args: Chunk[String]): ZIO[Any, IllegalArgumentException, List[TestCase]] =
    args.headOption match {
      case None         => ZIO.succeed(TestCases.all)
      case Some(numStr) =>
        ZIO
          .fromOption(numStr.toIntOption.flatMap(n => TestCases.all.lift(n - 1)).map(List(_)))
          .orElseFail(
            new IllegalArgumentException(
              s"Invalid test case number: $numStr. Valid: 1-${TestCases.all.size}",
            ),
          )
    }

  private def writeResults(results: List[TestResult]): Unit = {
    CsvWriter.writeResults(results, "eval-results.csv")
    CsvWriter.writeFailureResults(results, "eval-results-failures.csv")
    CsvWriter.writeResultsSummary(results, "eval-results-summary.csv")
    CsvWriter.writeResultsByModel(results, "eval-results-by-model.csv")
  }

  private def runEval(
      testCase: TestCase,
      model: LlmModel,
      runNumber: Int,
      explainFailures: Boolean = false,
  ): ZIO[Llm, Nothing, TestResult] = {

    (for {
      mergedInput        = mergeSourceExamples(testCase.sources.map(_.example))
      mergedSourceSchema = mergeSourceSchemas(testCase.sources.map(_.schema))
      prompt             = Prompts.createJsPrompt(mergedSourceSchema, testCase.target.schema, testCase.instructions)
      llm                <- ZIO.service[Llm]
      response           <- ZIO
                              .attempt(llm.generate(model, prompt))
                              .logError
                              .mapError(e => s"LLM call failed: ${e.getMessage}")
      cleanedResponse     = stripCodeFences(response)
      (score, outputOpt) <- evaluateJs(cleanedResponse, mergedInput, testCase.target.evalSchema)
                               .map { case (s, o) => (s, Some(o)) }
                               .catchAll(error => ZIO.succeed((Score.Fail(List(error)), None)))
      explanationOpt     <- score match {
                              case Score.Fail(errors) =>
                                ZIO
                                  .when(explainFailures)(
                                    FailureExplainer
                                      .explain(
                                        llm,
                                        testCase,
                                        prompt,
                                        cleanedResponse,
                                        outputOpt,
                                        errors,
                                      ),
                                  )
                                  .logError("Unable to generate failure explanation")
                                  .catchAll(_ => ZIO.succeed(None))
                              case Score.Pass         => ZIO.succeed(None)
                            }
    } yield TestResult(
      testCase,
      model,
      cleanedResponse,
      outputOpt,
      score,
      runNumber,
      explanationOpt,
    )).catchAllCause { cause =>
      val message = s"Test ${testCase.label} [${model.id}] run $runNumber failed"
      ZIO.logCause(message, cause) *>
        ZIO.succeed(
          TestResult(testCase, model, "", None, Score.Fail(List(message)), runNumber),
        )
    }
  }

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
    val explainFailures = false
    val numberOfRuns = 3
    val models       = List(
      LlmModel.Qwen3CoderNext,
      LlmModel.Glm47,
      LlmModel.Glm47Flash,
      LlmModel.Haiku45,
      LlmModel.Sonnet45,
      LlmModel.Opus46,
      LlmModel.Gpt5,
      LlmModel.Gpt52,
      LlmModel.Gpt5Mini,
    )

    for {
      args        <- getArgs
      testCases   <- getTestCases(args)
      testCaseZIOs = for {
                       runNumber <- 1 to numberOfRuns
                       testCase  <- testCases
                       model     <- models
                     } yield runEval(testCase, model, runNumber, explainFailures)
      results     <- ZIO
                       .collectAllPar(testCaseZIOs)
                       .provide(
                         LlmLive.layer,
                       )
    } yield writeResults(results.toList)
  }
}
