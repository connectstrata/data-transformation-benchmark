package com.connectstrata.evals

import zio.json.ast.Json
import zio.Task
import zio.ZIO

object FailureExplainer {

  private def createPrompt(
      testCase: TestCase,
      originalPrompt: String,
      transformation: String,
      output: Option[Json],
      errors: List[String],
  ): String = {
    val outputStr = output.map(_.toString).getOrElse("No output produced")
    s"""You are analyzing a failed JSON transformation test. Explain why it failed in 2-3 concise sentences.

IMPORTANT: Return only plain text. Do not use markdown headers, bullet points, or code blocks. Inline formatting like backticks and quotes is fine.

Test Case: ${testCase.label}
Target Schema: ${testCase.target.evalSchema}

Original Prompt Given to Model:
$originalPrompt

Generated Transformation:
$transformation

Transformation Output:
$outputStr

Errors:
${errors.mkString("\n")}

Provide a brief plain text explanation of what went wrong and why the generated transformation didn't meet the requirements."""
  }

  def explain(
      llm: Llm,
      testCase: TestCase,
      originalPrompt: String,
      transformation: String,
      output: Option[Json],
      errors: List[String],
  ): Task[String] = {
    val prompt = createPrompt(testCase, originalPrompt, transformation, output, errors)
    ZIO.attempt(llm.generate(LlmModel.Opus46, prompt))
  }
}
