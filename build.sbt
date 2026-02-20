ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.1"

lazy val root = (project in file("."))
  .settings(
    name := "data-transformation-benchmark",
    idePackagePrefix := Some("com.connectstrata"),
    libraryDependencies ++= Seq(
      "dev.zio"                  %% "zio"                    % "2.1.24",
      "dev.zio"                  %% "zio-json"               % "0.7.45",
      "dev.zio"                  %% "zio-test"               % "2.1.24" % Test,
      "dev.zio"                  %% "zio-test-sbt"           % "2.1.24" % Test,
      "org.graalvm.polyglot"      % "polyglot"               % "23.1.10",
      "org.graalvm.polyglot"      % "js-community"           % "23.1.10",
      "dev.langchain4j"           % "langchain4j-open-ai"    % "1.10.0",
      "dev.langchain4j"           % "langchain4j-anthropic"  % "1.10.0",
      "dev.langchain4j"           % "langchain4j-bedrock"    % "1.10.0",
      "software.amazon.awssdk"    % "sso"                    % "2.33.5"  % Runtime,
      "software.amazon.awssdk"    % "ssooidc"                % "2.33.5"  % Runtime,
      "com.networknt"             % "json-schema-validator"   % "3.0.0",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    fork := true,
  )
