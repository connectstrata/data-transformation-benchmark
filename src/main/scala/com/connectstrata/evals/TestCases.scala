package com.connectstrata.evals

/**
 * Test cases
 */
object TestCases {

  // ===========================================================================
  // Difficulty.Low
  //
  // Test that the model can produce an object that matches the source schema given minimal instructions
  // ===========================================================================

  // Test 1
  // Minimal Punchh to Iterable event with no explicit instructions - tests basic schema conformance
  private val test1 = TestCase(
    sources = List(Source.PunchhCouponIssuanceDiscount),
    target = Target.Test1,
    difficulty = Difficulty.Low,
    instructions = "Map this Punchh coupon issuance webhook to an Iterable custom event.",
  )

  // Test 2
  // Explicit field mapping with snake_case conversion - tests following specific field requirements
  private val test2 = TestCase(
    sources = List(Source.HubSpotContact),
    target = Target.Test2,
    difficulty = Difficulty.Low,
    instructions =
      "Create a Braze user for this HubSpot Contact. Map the HubSpot contact's name fields to Braze user attributes.",
  )

  // Test 3
  // Webhook to API request - tests mapping customer data to a contact with create_source and basic fields
  // possibly a higher difficulty due to the vast number of optional fields
  private val test3 = TestCase(
    sources = List(Source.ShopifyCustomersCreate),
    target = Target.Test3,
    difficulty = Difficulty.Low,
    instructions =
      "Convert the Shopify customer to a Constant Contact contact. Include address, phone, and notes if available.",
  )

  // Test 4
  // Minimal contact mapping from HubSpot to Mailchimp with fixed status and merge fields
  private val test4 = TestCase(
    sources = List(Source.HubSpotContact),
    target = Target.Test4,
    difficulty = Difficulty.Low,
    instructions = """Map the HubSpot contact to a Mailchimp list member. Use status "subscribed" and set merge fields FNAME and LNAME from firstname and lastname.""",
  )

  // Test 5
  // Basic field projection from Shopify customer to Braze attributes
  private val test5 = TestCase(
    sources = List(Source.ShopifyCustomersCreate),
    target = Target.Test5,
    difficulty = Difficulty.Low,
    instructions = """Create a Braze users/track payload with one attributes object. Map external_id from the top-level Shopify customer id, and map email, first_name, and last_name.""",
  )

  // Test 6
  // Simple webhook to event mapping with millisecond-to-second conversion
  private val test6 = TestCase(
    sources = List(Source.HubSpotContactCreated),
    target = Target.Test6,
    difficulty = Difficulty.Low,
    instructions = """Transform the HubSpot contact.created webhook into an Iterable event. Use eventName "hubspot_contact_created", userId from objectId, createdAt from occurredAt, and include portal_id and subscription_type in dataFields.""",
  )

  // Test 7
  // Direct contact mapping from Salesforce to Constant Contact with minimal required fields
  private val test7 = TestCase(
    sources = List(Source.SalesforceContact),
    target = Target.Test7,
    difficulty = Difficulty.Low,
    instructions = """Map Salesforce contact data into a Constant Contact contact. Include first_name, last_name, job_title, email_address.address, set email_address.permission_to_send to "implicit", and set create_source to "Contact".""",
  )

  // ===========================================================================
  // Difficulty.Medium
  //
  // Test that the model can re-format data, manipulate arrays, build custom objects
  // ===========================================================================

  // Test 8
  // Event construction with dataFields object and type conversion (Long to String for coupon_campaign_id)
  private val test8 = TestCase(
    sources = List(Source.PunchhCouponIssuanceDiscount),
    target = Target.Test8,
    difficulty = Difficulty.Medium,
    instructions = """Map this Punchh coupon issuance to an Iterable custom event named "Coupon Issuance - Discount". Use the top-level timestamp for createdAt. In dataFields, include campaign_name, coupon_campaign_id, and applicable_flat_discount.""",
  )

  // Test 9
  // Multiple inputs (2 webhooks) requiring data combination - tests merging data from different sources into a single output
  private val test9 = TestCase(
    sources = List(
      Source.ShopifyOrderFulfilled,
      Source.ShopifyCustomersCreate,
    ),
    target = Target.Test9,
    difficulty = Difficulty.Medium,
    instructions = "Combine the Shopify order and customer webhooks into a Klaviyo profile. Use customer data for identity fields and the order's shipping address for location. Track the order ID in fulfilled_orders.",
  )

  // Test 10
  // Multiple inputs with distractors (5 inputs, only 1 relevant), field combination (full_name), array construction
  // (favorite_restaurants), ISO 8601 to Unix timestamp conversion, and phone number formatting
  private val test10 = TestCase(
    sources = List(
      Source.HubSpotContact,
      Source.HubSpotContactCreated,
      Source.SalesforceAccount,
      Source.SalesforceContact,
      Source.PunchhCouponIssuanceDiscount,
    ),
    target = Target.Test10,
    difficulty = Difficulty.Medium,
    instructions = """Build an Iterable user from the HubSpot contact. Concatenate first and last name into a field named full_name. Map the contact phoneNumber to the iterable profile field phoneNumber and make sure it is prepended with "+1" if it isn't already. Build a favorite_restaurants field from the custom restaurant properties. Convert the HubSpot webhook's occurredAt from milliseconds to Unix seconds for profile_updated_at.""",
  )

  // Test 11
  // Simple field mappings with a hardcoded value and Mailchimp's merge_fields convention - straightforward transformation
  private val test11 = TestCase(
    sources = List(Source.SalesforceContact),
    target = Target.Test11,
    difficulty = Difficulty.Medium,
    instructions = """Map the Salesforce contact to a Mailchimp list member with status "subscribed". Use FNAME and LNAME merge fields.""",
  )

  // Test 12
  // Nested event payload generation from Shopify order with line-item array shaping and numeric coercion
  private val test12 = TestCase(
    sources = List(
      Source.ShopifyOrderFulfilled,
      Source.PunchhCouponIssuanceDiscount,
    ),
    target = Target.Test12,
    difficulty = Difficulty.Medium,
    instructions = """Create a Klaviyo event from the Shopify order. Set metric name to "order_fulfilled", time from processed_at, profile email from the order email, and in properties include order_id, currency, total_price as a number, and line_items as an array of objects with item_name, item_price as a number, and quantity.""",
  )

  // Test 13
  // Two-source HubSpot merge with distractor source and profile enrichment
  private val test13 = TestCase(
    sources = List(
      Source.HubSpotContact,
      Source.HubSpotContactCreated,
      Source.SalesforceAccount,
    ),
    target = Target.Test13,
    difficulty = Difficulty.Medium,
    instructions = """Build an Iterable user from the HubSpot sources. Map email, build full_name from firstname + lastname, map phoneNumber with a +1 prefix when missing, map favorite_restaurants from favoriteRestaurant1 and favoriteRestaurant2, map profile_updated_at from occurredAt, and set source_system to "hubspot".""",
  )

  // Test 14
  // Salesforce opportunity expansion into Braze events plus aggregate attributes
  private val test14 = TestCase(
    sources = List(
      Source.SalesforceContact,
      Source.SalesforceOpportunities,
      Source.ShopifyCustomersCreate,
    ),
    target = Target.Test14,
    difficulty = Difficulty.Medium,
    instructions = """Using Salesforce contact and opportunities, build a Braze payload with events and attributes arrays. Create one event per opportunity with name "opportunity_snapshot", external_id from Contact Id, time from opportunity CloseDate, and properties containing opportunity_name, stage, and probability. Add one attributes object with external_id, email, top_probability (max Probability), and open_pipeline_count (number of opportunities).""",
  )

  // ===========================================================================
  // Difficulty.High
  // ===========================================================================

  // Test 15
  // Complex JSON:API nested structure, Unix timestamp -> ISO 8601 conversion, array field filtering,
  // and a distractor source that must be ignored
  private val test15 = TestCase(
    sources = List(
      Source.PunchhCouponIssuanceDiscount,
      Source.HubSpotContact,
    ),
    target = Target.Test15,
    difficulty = Difficulty.High,
    instructions = """Transform the Punchh coupon webhook into a Klaviyo event. The metric name is "coupon_issued". Build the profile from payload.user data. Include coupon details and redeemed menu items (item_name and item_amount only) in event properties.""",
  )

  // Test 16
  // Dual-target output (events + attributes arrays), deep nested flattening with key prefixing,
  // ISO 8601 passthrough, boolean aggregation into nested subscriptions object,
  // and a distractor source that must be ignored
  private val test16 = TestCase(
    sources = List(
      Source.PunchhCouponIssuanceDiscount,
      Source.HubSpotContactCreated,
    ),
    target = Target.Test16,
    difficulty = Difficulty.High,
    instructions = """Transform the Punchh webhook into a Braze request with both events and attributes arrays. Use payload.user.external_source_id as external_id in both. Name the event "coupon_issued" and use payload.expiry_at for the event time. Map loyalty data and subscription booleans to attributes.""",
  )

  // Test 17
  // 3-input cross-source merge (Deals + Deal Products + Deal Discounts) with a distractor source,
  // custom field object parsing with hashed keys, filtering of deal/product records, and multiple derived fields
  private val test17 = TestCase(
    sources = List(
      Source.PipedriveDeals,
      Source.PipedriveDealProducts,
      Source.PipedriveDealDiscounts,
      Source.PunchhCouponIssuanceDiscount,
    ),
    target = Target.Test17,
    difficulty = Difficulty.High,
    instructions = """Transform Pipedrive deals, deal-products, and deal-discounts into a Constant Contact contact. Treat a deal as eligible only when status is "open", is_archived is false, person is non-null, and organization is non-null. Use the first eligible deal's person.name for first_name/last_name and organization.name for company_name. Use "Account" as create_source. Build one note per eligible deal with content including deal title, stage, and status. For each note, derive probability_bucket from result_score (hot >= 0.95, warm >= 0.85, else cold), product_total as the sum of enabled product sums for that deal, discount_total as the sum of amount discounts plus percentage discounts based on deal.value, and net_total = product_total - discount_total.""",
  )

  // Test 18
  // 3-input cross-source merge (Account + Contact + Opportunities) with an unrelated distractor source,
  // multiline address parsing, date reformatting (ISO -> MM/DD/YYYY), and array iteration for notes
  private val test18 = TestCase(
    sources = List(
      Source.SalesforceAccount,
      Source.SalesforceContact,
      Source.SalesforceOpportunities,
      Source.PunchhCouponIssuanceDiscount,
      Source.ShopifyCustomersCreate,
      Source.ShopifyOrderFulfilled,
    ),
    target = Target.Test18,
    difficulty = Difficulty.High,
    instructions = """Merge Salesforce Account, Contact, and Opportunities into a Constant Contact contact. Use "Account" as create_source. Map the Contact's mailing address as kind "home". Include work and mobile phone numbers. Build a note from each Opportunity in the format: "Name | StageName | Probability | CloseDate". Set permission_to_send to "implicit" when the contact's email has not bounced.""",
  )

  // Test 19
  // High-complexity rollup to Mailchimp: cross-source filtering, joins, aggregate arithmetic, and derived tags
  private val test19 = TestCase(
    sources = List(
      Source.PipedriveDeals,
      Source.PipedriveDealProducts,
      Source.PipedriveDealDiscounts,
      Source.PunchhCouponIssuanceDiscount,
    ),
    target = Target.Test19,
    difficulty = Difficulty.High,
    instructions = """Transform Pipedrive deals/products/discounts into a Mailchimp list member. Eligible deals are status=open, is_archived=false, and both person and organization present. Use first eligible deal person.name for merge_fields FNAME/LNAME. Extract email from the custom_fields object using the source schema field descriptions to identify which hash key corresponds to the email field. Set status to "subscribed". In merge_fields include: TOTAL_OPEN_DEALS, TOTAL_PRODUCT_SUM (sum enabled product sums across eligible deals), TOTAL_DISCOUNT_SUM (amount discounts plus percentage discounts based on deal value), NET_PIPELINE (TOTAL_PRODUCT_SUM minus TOTAL_DISCOUNT_SUM), and BEST_BUCKET (hot if max result_score >= 0.95, warm if >= 0.85, else cold). Output all rollup merge_field values as strings. Add tags open_deals_<count>, pipeline_<bucket>, and net_<net_pipeline_integer>.""",
  )

  // Test 20
  // High-complexity Salesforce merge to Iterable user with multiple derived metrics and distractor filtering
  private val test20 = TestCase(
    sources = List(
      Source.SalesforceAccount,
      Source.SalesforceContact,
      Source.SalesforceOpportunities,
      Source.PunchhCouponIssuanceDiscount,
      Source.ShopifyOrderFulfilled,
    ),
    target = Target.Test20,
    difficulty = Difficulty.High,
    instructions = """Build an Iterable user from Salesforce account/contact/opportunities. Map email from Contact Email and userId from Contact Id. In dataFields include full_name, account_name, title, work_phone, mobile_phone, opportunity_count, max_probability, avg_probability, next_close_date, forecast_categories, and opportunity_summaries. forecast_categories should include each opportunity ForecastCategoryName. opportunity_summaries should have one string per opportunity in the format "Name | StageName | Probability | CloseDate". avg_probability is the arithmetic mean of opportunity Probability values.""",
  )

  val all: List[TestCase] =
    List(
      test1,
      test2,
      test3,
      test4,
      test5,
      test6,
      test7,
      test8,
      test9,
      test10,
      test11,
      test12,
      test13,
      test14,
      test15,
      test16,
      test17,
      test18,
      test19,
      test20,
    )
}
