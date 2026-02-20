package com.connectstrata.evals.js

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import zio.json.ast.Json
import zio.IO
import zio.Scope
import zio.ZIO

object Graal {
  private def scopedContext: ZIO[Scope, TransformationError.UnexpectedError, Context] = {
    //    ZIO.fromTry(Using(Context.create("js")) { context =>
    //    ZIO.acquireReleaseWith(ZIO.attempt(Context.create("js")))(ctx => ZIO.attempt(ctx.close()).ignore) { ctx =>
    ZIO
      .fromAutoCloseable(
        ZIO.attempt(
          Context
            .newBuilder("js")
            // disables warning log - can also do it via `-Dpolyglot.engine.WarnInterpreterOnly=false` property
            //            .option("engine.WarnInterpreterOnly", "false")
            // disables compilation - maybe better than the above option?
            //            .option("engine.Compilation", "false")
            .build(),
        ),
      )
      .mapError(t =>
        TransformationError.UnexpectedError(
          s"Unable to create Javascript context",
          cause = Some(t),
        ),
      )
  }

  private def defineAndGetFunction(context: Context, js: String, functionName: String): Value = {
    context.eval("js", js)
    context.getBindings("js").getMember(functionName)
  }

  private def executeFunction(function: Value, params: Seq[Json]): IO[TransformationError, Json] = {
    val graalParams = params.map(GraalJsonConverters.jsonToPrimitive)
    // TODO - context.execute is not thread safe
    //  currently we're creating a new context for each execution, but I imagine that's very inefficient?
    ZIO
      .attempt(function.execute(graalParams*))
      .mapError(t => TransformationError.UnexpectedError(t.getMessage, cause = Some(t)))
      .flatMap(GraalJsonConverters.valueToJson)
  }

  def executeJs(js: String, functionName: String, params: Seq[Json]): IO[TransformationError, Json] = ZIO.scoped {
    scopedContext.flatMap { context =>
      ZIO
        .attempt(defineAndGetFunction(context, js, functionName))
        .mapError(t => TransformationError.UnexpectedError(t.getMessage, cause = Some(t)))
        .flatMap(executeFunction(_, params))
    }
  }
}
