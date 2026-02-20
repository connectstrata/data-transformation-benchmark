package com.connectstrata.evals

import scala.jdk.CollectionConverters.*

import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import zio.json.ast.Json
import zio.json.EncoderOps

object SchemaValidator {

  private val schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)

  def validate(schema: String, json: Json): Either[List[String], Unit] = {
    val jsonSchema = schemaRegistry.getSchema(schema, InputFormat.JSON)
    val errors     = jsonSchema.validate(json.toJson, InputFormat.JSON).asScala.toList
    if errors.isEmpty then Right(()) else Left(errors.map(_.getMessage))
  }
}
