package com.connectstrata.evals

import java.time.Duration

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.bedrock.BedrockChatModel
import dev.langchain4j.model.bedrock.BedrockChatRequestParameters
import dev.langchain4j.model.chat.request.json.JsonRawSchema
import dev.langchain4j.model.chat.request.json.JsonSchema
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.chat.request.ResponseFormatType
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import zio.ZLayer

/**
 * Supported model providers
 */
enum LlmProvider {
  case OpenAI, Anthropic, Bedrock
}

/**
 * Predefined LLM model entries that pair a provider with a model ID.
 */
enum LlmModel(val provider: LlmProvider, val id: String) {
  case Haiku45        extends LlmModel(LlmProvider.Bedrock, "us.anthropic.claude-haiku-4-5-20251001-v1:0")
  case Sonnet45       extends LlmModel(LlmProvider.Bedrock, "us.anthropic.claude-sonnet-4-5-20250929-v1:0")
  case Opus46         extends LlmModel(LlmProvider.Bedrock, "us.anthropic.claude-opus-4-6-v1")
  case KimiK25        extends LlmModel(LlmProvider.Bedrock, "moonshotai.kimi-k2.5")
  case KimiK2Thinking extends LlmModel(LlmProvider.Bedrock, "moonshot.kimi-k2-thinking")
  case Glm47          extends LlmModel(LlmProvider.Bedrock, "zai.glm-4.7")
  case Glm47Flash     extends LlmModel(LlmProvider.Bedrock, "zai.glm-4.7-flash")
  case Qwen3CoderNext extends LlmModel(LlmProvider.Bedrock, "qwen.qwen3-coder-next")
  case Gpt52          extends LlmModel(LlmProvider.OpenAI, "gpt-5.2-2025-12-11")
  case Gpt5           extends LlmModel(LlmProvider.OpenAI, "gpt-5-2025-08-07")
  case Gpt5Mini       extends LlmModel(LlmProvider.OpenAI, "gpt-5-mini-2025-08-07")
}

/**
 * Abstraction over LLM providers for generating text completions.
 */
trait Llm {

  /**
   * Generate a text completion from the given model.
   *
   * @param outputSchema Optional JSON schema string to constrain the model's output. This is passed
   *                     as an optional parameter because the support for structured output varies.
   */
  def generate(
      model: LlmModel,
      message: String,
      outputSchema: Option[String] = None,
  ): String
}

object LlmLive extends Llm {
  private def getChatModel(model: LlmModel): ChatModel = model.provider match {
    case LlmProvider.Anthropic =>
      AnthropicChatModel
        .builder()
        .apiKey(sys.env("ANTHROPIC_API_KEY"))
        .modelName(model.id)
        .maxTokens(4096)
        .beta("structured-outputs-2025-11-13")
        .build()
    case LlmProvider.Bedrock   =>
      val profile       = sys.env.getOrElse("AWS_PROFILE", "default")
      val bedrockClient = BedrockRuntimeClient
        .builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(ProfileCredentialsProvider.create(profile))
        .build()
      BedrockChatModel
        .builder()
        .client(bedrockClient)
        .modelId(model.id)
        .timeout(Duration.ofHours(1L))
        .defaultRequestParameters(
          BedrockChatRequestParameters.builder().maxOutputTokens(4096).build(),
        )
        .build()
    case LlmProvider.OpenAI    =>
      OpenAiChatModel
        .builder()
        .apiKey(sys.env("OPENAI_API_KEY"))
        .modelName(model.id)
        .timeout(Duration.ofHours(1L))
        .build()
  }

  def generate(
      model: LlmModel,
      message: String,
      outputSchema: Option[String] = None,
  ): String = {
    val chatModel = getChatModel(model)

    (outputSchema, model.provider) match {
      case (None, _)                                                     => chatModel.chat(message)
      case (Some(schemaStr), LlmProvider.OpenAI | LlmProvider.Anthropic) =>
        val schema         = JsonSchema
          .builder()
          .name("Spec")
          .rootElement(JsonRawSchema.from(schemaStr))
          .build()
        val responseFormat = ResponseFormat
          .builder()
          .`type`(ResponseFormatType.JSON)
          .jsonSchema(schema)
          .build()

        val request = ChatRequest
          .builder()
          .messages(UserMessage.from(message))
          .responseFormat(responseFormat)
          .build()

        chatModel.chat(request).aiMessage().text()
      case (Some(_), LlmProvider.Bedrock | LlmProvider.Anthropic)        =>
        // Anthropic doesn't support recursive JSON schemas in structured output definitions.
        // Furthermore, langchain4j's Bedkrock integration doesn't support structured outputs at all
        // https://github.com/langchain4j/langchain4j/issues/4543
        chatModel.chat(message)
    }
  }

  val layer = ZLayer.succeed(LlmLive)
}

object LlmMock extends Llm {
  def generate(
      model: LlmModel,
      message: String,
      outputSchema: Option[String] = None,
  ): String = "{\"sources\": [{ \"String\": { \"value\": \"foo\" } }], \"transformations\": []}"

  val layer = ZLayer.succeed(LlmMock)
}
