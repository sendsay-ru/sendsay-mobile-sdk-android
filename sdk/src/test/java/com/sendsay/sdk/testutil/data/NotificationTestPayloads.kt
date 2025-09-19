package com.sendsay.sdk.testutil.data

internal object NotificationTestPayloads {
    /* ktlint-disable max-line-length */
    val BASIC_NOTIFICATION = hashMapOf(
            "action" to "app",
            "title" to "push title",
            "message" to "push notification message"
    )
    val DEEPLINK_NOTIFICATION = hashMapOf(
            "action" to "deeplink",
            "url" to "app://test",
            "title" to "push title",
            "message" to "push notification message"
    )
    val BROWSER_NOTIFICATION = hashMapOf(
            "action" to "browser",
            "url" to "http://google.com",
            "title" to "push title",
            "message" to "push notification message"
    )

    val ACTIONS_NOTIFICATION = hashMapOf(
            "action" to "app",
            "actions" to "[{\"action\":\"app\",\"title\":\"Action 1 title\"},{\"action\":\"deeplink\",\"title\":\"Action 2 title\",\"url\":\"app:\\/\\/deeplink\"},{\"action\":\"browser\",\"title\":\"Action 3 title\",\"url\":\"http:\\/\\/google.com\"}]",
            "title" to "push title",
            "message" to "push notification message"
    )

    val PRODUCTION_NOTIFICATION = hashMapOf(
            "notification_id" to "1",
            "action" to "app",
            "actions" to """[
            {"action":"app","title":"Action 1 title"},
            {"action":"deeplink","title":"Action 2 title","url":"http:\/\/deeplink?search=something"},
            {"action":"browser","title":"Action 3 title","url":"http:\/\/google.com?search=something"}
        ]""",
            "url_params" to """{"utm_campaign":"Testing mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay"}""",
            "title" to "Notification title",
            "attributes" to """{
            "campaign_name":"Wassil's push",
            "event_type":"campaign",
            "action_id":2,
            "action_type":"mobile notification",
            "campaign_policy":"",
            "subject":"Notification title",
            "action_name":"Unnamed mobile push",
            "recipient":"eMxrdLuMalE:APA91bFgzKPVtem5aA0ZL0PFm_FgksAtVCOhzIQywX7DZQx2dKiVUepgl_Yw2aIrGZ7gpblCHltL6PWfXLoRw_5aZvV9swkPtUNwYjMNoF2f7igXgNe5Ovgyi8q5fmoX9QVHtyt8C-0Z",
            "language":"",
            "campaign_id":"5db9ab54b073dfb424ccfa6f",
            "platform":"android",
            "sent_timestamp":1614585422.20,
            "type":"push"
        }""",
            "message" to "Notification text"
    )

    val SILENT_NOTIFICATION = hashMapOf(
            "title" to "Silent push",
            "action" to "app",
            "silent" to "true",
            "attributes" to """{ "silent_test": "value" }"""
    )

    val PRODUCTION_NOTIFICATION_WITHOUT_SENT_TIME_AND_TYPE = hashMapOf(
            "notification_id" to "1",
            "action" to "app",
            "actions" to """[
              {"action":"app","title":"Action 1 title"},
              {"action":"deeplink","title":"Action 2 title","url":"http:\/\/deeplink?search=something"},
              {"action":"browser","title":"Action 3 title","url":"http:\/\/google.com?search=something"}
          ]""",
            "url_params" to """{"utm_campaign":"Testing mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay"}""",
            "title" to "Notification title",
            "attributes" to """{
              "campaign_name":"Wassil's push",
              "event_type":"campaign",
              "action_id":2,
              "action_type":"mobile notification",
              "campaign_policy":"",
              "subject":"Notification title",
              "action_name":"Unnamed mobile push",
              "recipient":"eMxrdLuMalE:APA91bFgzKPVtem5aA0ZL0PFm_FgksAtVCOhzIQywX7DZQx2dKiVUepgl_Yw2aIrGZ7gpblCHltL6PWfXLoRw_5aZvV9swkPtUNwYjMNoF2f7igXgNe5Ovgyi8q5fmoX9QVHtyt8C-0Z",
              "language":"",
              "campaign_id":"5db9ab54b073dfb424ccfa6f",
              "platform":"android"
          }""",
            "message" to "Notification text"
    )

    val NOTIFICATION_WITH_NESTED_ATTRIBUTES = hashMapOf(
            "notification_id" to "1",
            "action" to "app",
            "url_params" to """{"utm_campaign":"Testing mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay"}""",
            "title" to "Notification title",
            "attributes" to """{
                  "campaign_name":"Wassil's push",
                  "event_type":"campaign",
                  "action_id":2,
                  "action_type":"mobile notification",
                  "campaign_policy":"",
                  "subject":"Notification title",
                  "action_name":"Unnamed mobile push",
                  "recipient":"eMxrdLuMalE:APA91bFgzKPVtem5aA0ZL0PFm_FgksAtVCOhzIQywX7DZQx2dKiVUepgl_Yw2aIrGZ7gpblCHltL6PWfXLoRw_5aZvV9swkPtUNwYjMNoF2f7igXgNe5Ovgyi8q5fmoX9QVHtyt8C-0Z",
                  "language":"",
                  "campaign_id":"5db9ab54b073dfb424ccfa6f",
                  "platform":"android",
                  "first_level_attribute": {
                        "second_level__nested_attribute": { 
                                "third_level_attribute":"third_level_value"
                        },
                        "second_level_attribute":"second_level_value" },
                  "product_list": [{"item_id": "1234","item_quantity": 3}, {"item_id": "2345","item_quantity": 2}, { "item_id": "6789","item_quantity": 1}],
	              "product_ids": ["1234", "2345", "6789"],
                  "push_content": {"title": "Hey!",
                        "actions": [{
                            "title": "Action 1 title",
                            "action": "app"
                        }],
                        "message": "We have a great deal for you today, don't miss it!"
                }
            }""",
            "message" to "Notification text"
    )
    val PRODUCTION_NOTIFICATION_2 = hashMapOf(
            "notification_id" to "1545339447",
            "source" to "xnpe_platform",
            "silent" to "false",
            "action" to "app",
            "url_params" to """{"utm_campaign":"Unnamed mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay","utm_content":"hu"}""",
            "title" to "Test sending",
            "attributes" to """{
            "campaign_name":"Use Case 001: alfa",
            "event_type":"campaign",
            "action_id":84,
            "action_type":"mobile notification",
            "campaign_policy":"",
            "subject":"Test sending",
            "action_name":"Unnamed mobile push",
            "recipient":"dMALLSnQbHQ:APA91bEnnmqvcgy-89VPpVoik-Pt96jpg1HNLnVjSDfQvdQPiCYAUxH0xba6dTlDB0IGt1EcqcW8XgHMoywrmOUoLBZP_oL-mpJFvbQDgKsBPGdEPJHxIJ0HKXrbFkL-1GnjiqY6sA6q",
            "language":"",
            "campaign_id":"5fc5439d3680dcf8ecf1fae1",
            "platform":"android",
            "sent_timestamp":1658403764.924152
        }""",
            "message" to "PROD issue test"
    )

    val NOTIFICATION_WITH_DENIED_CONSENT = hashMapOf(
        "notification_id" to "1",
        "action" to "app",
        "actions" to """[
            {"action":"app","title":"Action 1 title"},
            {"action":"deeplink","title":"Action 2 title","url":"http:\/\/deeplink?search=something"},
            {"action":"browser","title":"Action 3 title","url":"http:\/\/google.com?search=something"}
        ]""",
        "url_params" to """{"utm_campaign":"Testing mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay"}""",
        "title" to "Notification title",
        "attributes" to """{
            "campaign_name":"Wassil's push",
            "event_type":"campaign",
            "action_id":2,
            "action_type":"mobile notification",
            "campaign_policy":"",
            "subject":"Notification title",
            "action_name":"Unnamed mobile push",
            "recipient":"eMxrdLuMalE:APA91bFgzKPVtem5aA0ZL0PFm_FgksAtVCOhzIQywX7DZQx2dKiVUepgl_Yw2aIrGZ7gpblCHltL6PWfXLoRw_5aZvV9swkPtUNwYjMNoF2f7igXgNe5Ovgyi8q5fmoX9QVHtyt8C-0Z",
            "language":"",
            "campaign_id":"5db9ab54b073dfb424ccfa6f",
            "platform":"android",
            "sent_timestamp":1614585422.20,
            "type":"push"
        }""",
        "message" to "Notification text",
        "consent_category_tracking" to "",
        "has_tracking_consent" to "false"
    )

    val NOTIFICATION_WITH_DENIED_CONSENT_BUT_ACTION_FORCED = hashMapOf(
        "notification_id" to "1",
        "action" to "browser",
        "url" to "http://google.com?search=something&xnpe_force_track=true",
        "actions" to """[
            {"action":"app","title":"Action 1 title"},
            {"action":"deeplink","title":"Action 2 title","url":"http:\/\/deeplink?search=something&xnpe_force_track=true"},
            {"action":"browser","title":"Action 3 title","url":"http:\/\/google.com?search=something&xnpe_force_track=true"}
        ]""",
        "url_params" to """{"utm_campaign":"Testing mobile push","utm_medium":"mobile_push_notification","utm_source":"sendsay"}""",
        "title" to "Notification title",
        "attributes" to """{
            "campaign_name":"Wassil's push",
            "event_type":"campaign",
            "action_id":2,
            "action_type":"mobile notification",
            "campaign_policy":"",
            "subject":"Notification title",
            "action_name":"Unnamed mobile push",
            "recipient":"eMxrdLuMalE:APA91bFgzKPVtem5aA0ZL0PFm_FgksAtVCOhzIQywX7DZQx2dKiVUepgl_Yw2aIrGZ7gpblCHltL6PWfXLoRw_5aZvV9swkPtUNwYjMNoF2f7igXgNe5Ovgyi8q5fmoX9QVHtyt8C-0Z",
            "language":"",
            "campaign_id":"5db9ab54b073dfb424ccfa6f",
            "platform":"android",
            "sent_timestamp":1614585422.20,
            "type":"push"
        }""",
        "message" to "Notification text",
        "consent_category_tracking" to "",
        "has_tracking_consent" to "false"
    )
    /* ktlint-enable== max-line-length */
}
