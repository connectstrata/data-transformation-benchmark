package com.connectstrata.evals.js

import scala.jdk.CollectionConverters.*

import org.graalvm.polyglot.proxy.ProxyArray
import org.graalvm.polyglot.proxy.ProxyObject
import org.graalvm.polyglot.Value
import zio.json.ast.Json
import zio.Chunk
import zio.IO
import zio.ZIO

object GraalJsonConverters {
  def jsonToPrimitive(json: Json): Object = json match {
    case Json.Null        => null
    case Json.Num(value)  => value.doubleValue(): java.lang.Double
    case Json.Bool(value) => value: java.lang.Boolean
    case Json.Str(value)  => value: java.lang.String
    case Json.Arr(values) => ProxyArray.fromList(values.map(jsonToPrimitive).asJava)
    case Json.Obj(values) =>
      val map = new java.util.HashMap[String, Object]()
      values.foreach((name, value) => map.put(name, jsonToPrimitive(value)))
      ProxyObject.fromMap(map)
  }

  def valueToJson(value: Value): IO[TransformationError, Json] = {
    if value.isNull then ZIO.succeed(Json.Null)
    else if value.isBoolean then ZIO.succeed(Json.Bool(value.asBoolean()))
    else if value.isNumber then {
      if value.fitsInBigInteger() then ZIO.succeed(Json.Num(value.asBigInteger()))
      else if value.fitsInDouble() then ZIO.succeed(Json.Num(value.asDouble()))
      else ZIO.fail(TransformationError.UnexpectedError(s"Cannot convert Value to Number: ${value.toString}"))
    } else if value.isString then ZIO.succeed(Json.Str(value.asString()))
    else if value.hasArrayElements then {
      val size = value.getArraySize
      // note - generally don't use foreachPar with GraalJS
      ZIO.foreach(Chunk.fromIterable(0L until size))(idx => valueToJson(value.getArrayElement(idx))).map(Json.Arr(_))
    } else if value.hasMembers then {
      val keys = value.getMemberKeys.asScala.toSeq
      ZIO
        .foreach(keys) { key =>
          valueToJson(value.getMember(key)).map(json => key -> json)
        }
        .map(Json.Obj(_*))
    } else {
      ZIO.fail(TransformationError.UnexpectedError(s"Cannot convert Value to Json: $value"))
    }
  }
}
