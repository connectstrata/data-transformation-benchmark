package com.connectstrata.evals

enum Target(val label: String, val schema: String, val evalSchema: String) {

  case Test1
      extends Target(
        "Iterable API Event",
        Target.loadTargetSchema("test-1.json"),
        Target.loadTargetSchema("test-1-verifier.json"),
      )

  case Test2
      extends Target(
        "Braze API User",
        Target.loadTargetSchema("test-2.json"),
        Target.loadTargetSchema("test-2-verifier.json"),
      )

  case Test3
      extends Target(
        "Constant Contact Contact",
        Target.loadTargetSchema("test-3.json"),
        Target.loadTargetSchema("test-3-verifier.json"),
      )

  case Test4
      extends Target(
        "Mailchimp API List Member",
        Target.loadTargetSchema("test-4.json"),
        Target.loadTargetSchema("test-4-verifier.json"),
      )

  case Test5
      extends Target(
        "Braze API User",
        Target.loadTargetSchema("test-5.json"),
        Target.loadTargetSchema("test-5-verifier.json"),
      )

  case Test6
      extends Target(
        "Iterable API Event",
        Target.loadTargetSchema("test-6.json"),
        Target.loadTargetSchema("test-6-verifier.json"),
      )

  case Test7
      extends Target(
        "Constant Contact Contact",
        Target.loadTargetSchema("test-7.json"),
        Target.loadTargetSchema("test-7-verifier.json"),
      )

  case Test8
      extends Target(
        "Iterable API Event",
        Target.loadTargetSchema("test-8.json"),
        Target.loadTargetSchema("test-8-verifier.json"),
      )

  case Test9
      extends Target(
        "Klaviyo API Profile",
        Target.loadTargetSchema("test-9.json"),
        Target.loadTargetSchema("test-9-verifier.json"),
      )

  case Test10
      extends Target(
        "Iterable API User",
        Target.loadTargetSchema("test-10.json"),
        Target.loadTargetSchema("test-10-verifier.json"),
      )

  case Test11
      extends Target(
        "Mailchimp API List Member",
        Target.loadTargetSchema("test-11.json"),
        Target.loadTargetSchema("test-11-verifier.json"),
      )

  case Test12
      extends Target(
        "Klaviyo API Event",
        Target.loadTargetSchema("test-12.json"),
        Target.loadTargetSchema("test-12-verifier.json"),
      )

  case Test13
      extends Target(
        "Iterable API User",
        Target.loadTargetSchema("test-13.json"),
        Target.loadTargetSchema("test-13-verifier.json"),
      )

  case Test14
      extends Target(
        "Braze API User",
        Target.loadTargetSchema("test-14.json"),
        Target.loadTargetSchema("test-14-verifier.json"),
      )

  case Test15
      extends Target(
        "Klaviyo API Event",
        Target.loadTargetSchema("test-15.json"),
        Target.loadTargetSchema("test-15-verifier.json"),
      )

  case Test16
      extends Target(
        "Braze API User",
        Target.loadTargetSchema("test-16.json"),
        Target.loadTargetSchema("test-16-verifier.json"),
      )

  case Test17
      extends Target(
        "Constant Contact Contact",
        Target.loadTargetSchema("test-17.json"),
        Target.loadTargetSchema("test-17-verifier.json"),
      )

  case Test18
      extends Target(
        "Constant Contact Contact",
        Target.loadTargetSchema("test-18.json"),
        Target.loadTargetSchema("test-18-verifier.json"),
      )

  case Test19
      extends Target(
        "Mailchimp API List Member",
        Target.loadTargetSchema("test-19.json"),
        Target.loadTargetSchema("test-19-verifier.json"),
      )

  case Test20
      extends Target(
        "Iterable API User",
        Target.loadTargetSchema("test-20.json"),
        Target.loadTargetSchema("test-20-verifier.json"),
      )
}

object Target {

  private def loadTargetSchema(filename: String): String =
    scala.io.Source.fromResource(s"target-schemas/$filename").mkString
}
