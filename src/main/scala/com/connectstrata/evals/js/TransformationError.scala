package com.connectstrata.evals.js

sealed trait TransformationError {
  def message: String
  def cause: Option[Throwable]
}
object TransformationError       {
  case class UnexpectedError(message: String, cause: Option[Throwable] = None) extends TransformationError
}
