package com.connectstrata.evals

import java.io.FileWriter
import java.io.PrintWriter

/**
 * Simple logger that writes debug output to eval-result.log
 */
class DebugLogger(filename: String = "eval-result.log") {
  private val writer = new PrintWriter(new FileWriter(filename, false))

  def log(message: String): Unit = {
    writer.println(message)
    writer.flush()
  }

  def close(): Unit = writer.close()
}
