package com.connectstrata.evals

import zio.json.ast.Json
import zio.json.DecoderOps

// When constructing enum cases, you can throw exceptions (e.g. Option.get) when loading schemas/examples
// since these are part of the test harness. We want to fail fast if there are test harness issues.
enum Source(val label: String, val schema: String, val example: Json) {

  case PunchhCouponIssuanceDiscount
      extends Source(
        "Punchh Coupon Issuance Webhook",
        Source.loadSchema("source-schemas/punchh-coupon-issuance-discount.json"),
        Source.loadExample("source-examples/punchh-coupon-issuance-discount.json"),
      )

  case HubSpotContact
      extends Source(
        "HubSpot API Contact",
        Source.loadSchema("source-schemas/hubspot-contact.json"),
        Source.loadExample("source-examples/hubspot-contact.json"),
      )

  case ShopifyCustomersCreate
      extends Source(
        "Shopify Customer Created Webhook",
        Source.loadSchema("source-schemas/shopify-customers-create.json"),
        Source.loadExample("source-examples/shopify-customers-create-webhook.json"),
      )

  case ShopifyOrderFulfilled
      extends Source(
        "Shopify Order Fulfilled Webhook",
        Source.loadSchema("source-schemas/shopify-order-fulfilled.json"),
        Source.loadExample("source-examples/shopify-order-fulfilled-webhook.json"),
      )

  case HubSpotContactCreated
      extends Source(
        "HubSpot Contact Created Webhook",
        Source.loadSchema("source-schemas/hubspot-contact-created.json"),
        Source.loadExample("source-examples/hubspot-contact-created-webhook.json"),
      )

  case PipedriveDeals
      extends Source(
        "Pipedrive Deals",
        Source.loadSchema("source-schemas/pipedrive-deals.json"),
        Source.loadExample("source-examples/pipedrive-deals.json"),
      )

  case PipedriveDealProducts
      extends Source(
        "Pipedrive Deal Products",
        Source.loadSchema("source-schemas/pipedrive-deal-products.json"),
        Source.loadExample("source-examples/pipedrive-deal-products.json"),
      )

  case PipedriveDealDiscounts
      extends Source(
        "Pipedrive Deal Discounts",
        Source.loadSchema("source-schemas/pipedrive-deal-discounts.json"),
        Source.loadExample("source-examples/pipedrive-deal-discounts.json"),
      )

  case SalesforceAccount
      extends Source(
        "Salesforce API Account",
        Source.loadSchema("source-schemas/salesforce-account.json"),
        Source.loadExample("source-examples/salesforce-account.json"),
      )

  case SalesforceContact
      extends Source(
        "Salesforce API Contact",
        Source.loadSchema("source-schemas/salesforce-contact.json"),
        Source.loadExample("source-examples/salesforce-contact.json"),
      )

  case SalesforceOpportunities
      extends Source(
        "Salesforce API Opportunities",
        s"""{"type":"array","items":${Source.loadSchema("source-schemas/salesforce-opportunity.json")}}""",
        Source.loadExample("source-examples/salesforce-opportunities.json"),
      )
}

object Source {
  private def loadSchema(path: String): String =
    scala.io.Source.fromResource(path).mkString

  private def loadExample(path: String): Json =
    scala.io.Source.fromResource(path).mkString.fromJson[Json].toOption.get
}
