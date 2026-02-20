package com.connectstrata.evals

/**
 * Test case difficulty. Hardly scientific.
 */
enum Difficulty {
  case Low, Medium, High
}

/**
 * The score for a particular test. For now it's a simple pass / fail. If the test fails a list
 * of reasons are provided.
 */
enum Score {
  case Fail(errors: List[String])
  case Pass
}

/**
 * The resources for a single test case and a difficulty classification
 */
final case class TestCase(
    sources: List[Source],
    target: Target,
    difficulty: Difficulty,
    instructions: String,
) {

  /** Human-readable label derived from sources and target, used in logs and prompts. */
  val label: String = s"${sources.map(_.label).mkString(" + ")} -> ${target.label}"
}
