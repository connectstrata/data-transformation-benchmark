package com.connectstrata.evals

import zio.json.ast.Json

/**
 * The result of a test case evaluation
 */
final case class TestResult(
    testCase: TestCase,
    model: LlmModel,
    transformation: String,
    output: Option[Json],
    score: Score,
    runNumber: Int,
    failureExplanation: Option[String] = None,
    promptSuggestion: Option[String] = None,
)
