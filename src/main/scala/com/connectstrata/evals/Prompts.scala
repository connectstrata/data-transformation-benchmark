package com.connectstrata.evals

object Prompts {

  // Guidelines that are applied to prompts for all languages. Don't include language-specific details here
  private val commonGuidelines: String =
    """  - **CRITICAL — Handle const constraints first**: Before mapping any field, scan the entire target schema for `const` constraints. For every field with a `const` value, you MUST output that exact literal value — do NOT map it from a source field, even if a similarly-named source field exists. The source field may contain a different value than the const requires. For example, if the target schema has `"status": {"type": "string", "const": "active"}`, you must output the literal string `"active"` regardless of what any source `status` field contains.
      |
      |  - **Determine the requirements**: Carefully analyze the target schema, including any `const`, `pattern`, `contains`, and `required` constraints, and generate a precise list of requirements. Fields with `pattern` constraints must match the regex pattern.
      |
      |  - **Analyze the schemas carefully**: Identify all fields in the source and target schemas, including their data types, nesting levels, and any constraints.
      |
      |  - **Identify correspondences**: Determine which source fields map to which target fields. Look for:
      |    - Direct field name matches
      |    - Semantic similarities (e.g., "firstName" to "first_name")
      |    - Fields that require transformation or combination
      |    - Fields that need default values or constants
      |
      |  - **Handle data type conversions**: Before including any field in the output, verify its value matches the exact type specified in the target schema. If a source field type differs from the required target field type, you MUST include an explicit type conversion. For example, if a source field is an integer like `42` but the target schema requires a string, convert it to `"42"`. Conversely, if a source field is a string like `"123"` but the target schema requires a number, convert it to `123`.
      |
      |  - **Address structural differences**: Handle cases where:
      |    - Source fields are nested differently than target fields
      |    - Multiple source fields combine into one target field
      |    - One source field splits into multiple target fields
      |    - Arrays or collections need to be mapped
      |
      |  - **Handle missing mappings**: If a target field has no corresponding source field, omit it unless the target schema requires including it with a null value.
      |
      |  - **Preserve required fields**: Ensure all required fields in the target schema are populated.
      |
      |  - **Final check — verify every output field's type**: Before returning, walk through each field in your output and compare its type against the target schema's `"type"` field. If the target says `"type": "string"` and you're returning a number, you must convert it to a string. If the target says `"type": "integer"` and you have a string, convert it to an integer. A type mismatch will cause validation to fail even if the value is otherwise correct.""".stripMargin

  private val jsGuidelines: String =
    """  - **Reference source fields using bracket notation**: Since multiple sources are provided as an object with integer string keys, reference source fields using `input["0"].fieldName`, `input["1"].fieldName`, etc.
      |
      |  - **Handle falsy values carefully**: When providing fallback values, use the nullish coalescing operator (`??`) instead of `||`. The `||` operator treats `false`, `0`, and `""` as falsy, which can cause data loss. For example, `value ?? null` preserves `false`, while `value || null` converts it to `null`.
      |
      |  - **Convert types explicitly with String() and Number()**: This is the single most common source of errors. When the target schema specifies `"type": "string"` but the source field is a number, you MUST wrap the value with `String(value)`. When the target specifies `"type": "integer"` or `"type": "number"` but the source is a string, use `Number(value)` or `parseInt(value, 10)`. JavaScript will not auto-convert these types and the output will fail schema validation.
      |
      |  - **Use UTC date methods**: Always use `getUTCDate()`, `getUTCMonth()`, `getUTCFullYear()`, `getUTCHours()`, etc. instead of `getDate()`, `getMonth()`, `getFullYear()`, `getHours()`. JavaScript's `new Date()` parses date-only strings (e.g. "2024-03-22") as UTC, but the non-UTC getter methods return values in the local timezone, which causes off-by-one errors for dates and times near midnight UTC.
      |
      |  - **Convert Unix timestamps with arithmetic, not truncation**: To convert Unix milliseconds to Unix seconds, divide by 1000: `Math.floor(value / 1000)`. To convert Unix seconds to Unix milliseconds, multiply by 1000: `value * 1000`. Do NOT use `Math.trunc(value)` or `Number(value)` — these do not change the unit, they only coerce the type.""".stripMargin

  def createJsPrompt(
      mergedSourceSchema: String,
      targetSchema: String,
      instructions: String,
  ): String = {
    s"""
       |## Overview
       |
       |  You are an expert in JSON data transformations. Your task is to help the user transform one or more source JSON values into a new target JSON value. You will be given a JSON schema specification for all source and target values. You will write a JavaScript function to perform the transformation. The output of the generated function must adhere to the target schema.
       |
       |## Guidelines
       |
       |  Follow these guidelines:
       |
       |  $commonGuidelines
       |
       |  $jsGuidelines
       |
       |## JavaScript function requirements
       |
       |  Write a JavaScript function with the following constraints:
       |
       |  - The function MUST be named `transform`
       |  - It accepts a single parameter `input` (a JSON object)
       |  - It returns a plain JSON-serializable object conforming to the target schema
       |  - Use only standard JavaScript built-ins (no external libraries)
       |  - No async/await or Promises
       |  - No console.log or side effects
       |  - Respond with ONLY the function definition, no additional text
       |
       |  Example:
       |  ```javascript
       |  function transform(input) {
       |    return {
       |      targetField: input["0"].sourceField
       |    };
       |  }
       |  ```
       |
       |## Source schemas
       |
       |  The input to the function is a JSON object where each key is a zero-based index corresponding to a source. The schema is:
       |
       |  $mergedSourceSchema
       |
       |## Target schema
       |
       |  The output from the function must adhere to the following schema:
       |
       |  $targetSchema
       |
       |## Instructions
       |
       |  $instructions
       |""".stripMargin
  }
}
