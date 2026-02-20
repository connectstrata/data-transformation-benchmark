# Data Transformation Benchmark

This repo contains an LLM eval suite that measures how well LLMs write code to transform data between common SaaS webhook payload and API response objects. For a full explanation see the associated blog post: [Data Transformation Benchmark](https://blog.connectstrata.com/p/data-transformation-benchmark)

## Run

```bash
sbt "runMain com.connectstrata.evals.Main"
```

To run a specific test, pass its 1-based index as an argument:

```bash
sbt "runMain com.connectstrata.evals.Main 3"
```

Or run `Main.scala` directly from IntelliJ.

## Environment Variables

| Provider | Variable | Description |
|----------|----------|-------------|
| OpenAI | `OPENAI_API_KEY` | OpenAI API key |
| Bedrock | `AWS_PROFILE` | AWS profile with Bedrock access |

## Bedrock Setup

Login via SSO: `aws sso login --profile <profile-name>`

## Output

Results written to `eval-results.csv` in the project root.
