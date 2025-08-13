---
title: Отслеживание
excerpt: Отслеживайте клиентов и события с помощью Android SDK
slug: android-sdk-tracking
categorySlug: integrations
parentDocSlug: android-sdk
---

Вы можете отслеживать события в Engagement, чтобы узнать больше о шаблонах использования вашего приложения и сегментировать клиентов по их взаимодействиям.

По умолчанию SDK автоматически отслеживает определенные события, включая:

* Установку (после установки приложения и после вызова [anonymize](#anonymize))
* Начало и окончание пользовательской сессии

Кроме того, вы можете отслеживать любые пользовательские события, связанные с вашим бизнесом.


## События

### Отслеживание событий

Используйте метод `trackEvent()` для отслеживания любого типа пользовательских событий, связанных с вашим бизнесом.

Вы можете использовать любое имя для типа пользовательского события. Мы рекомендуем использовать описательное и удобочитаемое имя.

Обратитесь к документации [Пользовательские события](https://documentation.bloomreach.com/engagement/docs/custom-events) для обзора часто используемых пользовательских событий.

#### Аргументы

| Имя                       | Тип                                   | Описание |
| ------------------------- | -------------- | ----------- |
| properties                | PropertiesList | Словарь свойств события. |
| timestamp                 | Double         | Unix timestamp (в секундах), указывающий, когда событие было отслежено. Укажите значение `nil` для использования текущего времени. |
| eventType **(обязательно)**  | String         | Название типа события, например `screen_view`. |

#### Примеры

Представьте, что вы хотите отслеживать, какие экраны просматривает клиент. Вы можете создать пользовательское событие `screen_view` для этого.

Сначала создайте `PropertiesList` со свойствами, которые вы хотите отслеживать вместе с этим событием. В нашем примере вы хотите отслеживать имя экрана, поэтому вы включаете свойство `screen_name` вместе с любыми другими соответствующими свойствами:

```kotlin
val properties = PropertiesList(
    hashMapOf(
        Pair("screen_name", "dashboard"),
        Pair("other_property", 123.45)
    )
)
```

Передайте список свойств в `trackEvent()` вместе с `eventType` (`screen_view`) следующим образом:

```kotlin
Sendsay.trackEvent(
    properties = properties,
    timestamp = null,
    eventType =  "screen_view"
)
```

Второй пример ниже показывает, как вы можете использовать вложенную структуру для сложных свойств при необходимости:

```kotlin
val properties = PropertiesList(
    hashMapOf(
        Pair("purchase_status", "success"),
        Pair("product_list", arrayOf(
            hashMapOf(
                Pair("product_id", "abc123"),
                Pair("quantity", 2)
            ),
            hashMapOf(
                Pair("product_id", "abc456"),
                Pair("quantity", 1)
            )
        ))
    )
)
Sendsay.trackEvent(
    properties = properties,
    timestamp = null,
    eventType =  "purchase"
)
```

> 👍
>
> По желанию вы можете указать пользовательскую временную метку `timestamp`, если событие произошло в другое время. По умолчанию будет использовано текущее время.

## Клиенты

[Идентификация ваших клиентов](https://documentation.bloomreach.com/engagement/docs/customer-identification) позволяет отслеживать их на различных устройствах и платформах, улучшая качество данных о клиентах.

Без идентификации события отслеживаются для анонимного клиента, определяемого только по cookie. Когда клиент идентифицируется по hard ID, эти события будут перенесены вновь идентифицированному клиенту.

> 👍
>
> Помните, что, хотя пользователь приложения и запись клиента могут быть связаны через soft или hard ID, они являются отдельными сущностями, каждая со своим собственным жизненным циклом. Подумайте о том, как соотносятся их жизненные циклы и когда использовать [identify](#identify) и [anonymize](#anonymize).

### Идентификация

Используйте метод `identifyCustomer()` для идентификации клиента по его уникальному [hard ID](https://documentation.bloomreach.com/engagement/docs/customer-identification#hard-id).

По умолчанию hard ID - это `registered`, и его значение обычно адрес электронной почты клиента. Однако ваш проект Engagement может определять другой hard ID.

По желанию вы можете отслеживать дополнительные свойства клиента, такие как имя и фамилия, возраст и т.д.

> ❗️
>
> Хотя возможно использовать `identifyCustomer` с [soft ID](https://documentation.bloomreach.com/engagement/docs/customer-identification#section-soft-id), разработчики должны соблюдать осторожность при этом. В некоторых случаях (например, после использования `anonymize`) это может непреднамеренно связать текущего пользователя с неправильным профилем клиента.

> ❗️
>
> SDK сохраняет данные, включая hard ID клиента, в локальном кэше на устройстве. Удаление hard ID из локального кэша требует вызова [anonymize](#anonymize) в приложении.
> Если профиль клиента анонимизирован или удален в веб-приложении Bloomreach Engagement, последующая инициализация SDK в приложении может привести к повторной идентификации или созданию профиля клиента из локально кэшированных данных.

#### Аргументы

| Имя                         | Тип            | Описание |
| --------------------------- | -------------- | ----------- |
| customerIds **(обязательно)**  | CustomerIds    | Словарь уникальных идентификаторов клиента. Принимаются только идентификаторы, определенные в проекте Engagement. |
| properties                  | PropertiesList | Словарь свойств клиента. |
| timestamp                   | Double         | Unix timestamp (в секундах), указывающий, когда свойства клиента были обновлены. Укажите значение `nil` для использования текущего времени. |

#### Примеры

Сначала создайте словарь `CustomerIds`, содержащий как минимум hard ID клиента:

```kotlin
val customerIds = CustomerIds().withId("registered","jane.doe@example.com")
```

По желанию создайте словарь с дополнительными свойствами клиента:

```kotlin
val properties = PropertiesList(
    hashMapOf(
        Pair("first_name", "Jane"),
        Pair("last_name", "Doe"),
        Pair("age", 32)
    )
)
```

Передайте словари `customerIds` и `properties` в `identifyCustomer()`:

```kotlin
Sendsay.identifyCustomer(
    customerIds = customerIds,
    properties = properties
)
```

If you only want to update the customer ID without any additional properties, you can pass a `PropertiesList` initialized with an empty `HashMap` into `properties`:

```swift
Sendsay.identifyCustomer(
    customerIds = customerIds,
    properties = PropertiesList(hashMapOf())
)
```

> 👍
>
> Optionally, you can provide a custom `timestamp` if the identification happened at a different time. By default the current time will be used.

### Anonymize

Используйте метод `anonymize()` для удаления всей информации, сохраненной локально, и сброса текущего состояния SDK. Типичный случай использования - когда пользователь выходит из приложения.

Вызов этого метода приведет к тому, что SDK:

* Удалит токен push-уведомлений для текущего клиента из локального хранилища устройства и профиля клиента в Engagement.
* Очистит локальные репозитории и кэши, исключая отслеженные события.
* Отследит новое начало сессии, если включен `automaticSessionTracking`.
* Создаст новую запись клиента в Engagement (генерируется новый soft ID `cookie`).
* Назначит предыдущий токен push-уведомлений новой записи клиента.
* Предварительно загрузит внутриприложенные сообщения, блоки контента и входящие сообщения приложения для нового клиента.
* Отследит новое событие `installation` для нового клиента.

Вы также можете использовать метод `anonymize` для переключения на другой проект Engagement. Тогда SDK будет отслеживать события для новой записи клиента в новом проекте, аналогично первой сессии приложения после установки на новое устройство.

#### Examples

```kotlin
Sendsay.anonymize()
```

Переключение на другой проект:

```kotlin
Sendsay.anonymize(
    sendsayProject = SendsayProject(
        baseUrl= "https://api.sendsay.com",
        projectToken= "YOUR PROJECT TOKEN",
        authorization= "Token YOUR API KEY"
    ),
    projectRouteMap = mapOf(
        EventType.TRACK_EVENT to listOf(
            SendsayProject(
                baseUrl= "https://api.sendsay.com",
                projectToken= "YOUR PROJECT TOKEN",
                authorization= "Token YOUR API KEY"
            )
        )
    ),
    advancedAuthToken = "YOUR JWT TOKEN"
)
```

## Сессии

SDK отслеживает сессии автоматически по умолчанию, генерируя два события: `session_start` и `session_end`.

Сессия представляет фактическое время, проведенное в приложении. Она начинается при запуске приложения и заканчивается, когда оно переходит в фоновый режим. Если пользователь возвращается в приложение до истечения тайм-аута сессии, приложение продолжит текущую сессию.

Тайм-аут сессии по умолчанию составляет 60 секунд. Установите `sessionTimeout` в [конфигурации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration#automaticsessiontracking), чтобы указать другой тайм-аут.

### Отслеживание сессии вручную

Чтобы отключить автоматическое отслеживание сессий, установите `automaticSessionTracking` в `false` в [конфигурации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration#automaticsessiontracking).

Используйте методы `trackSessionStart()` и `trackSessionEnd()` для отслеживания сессий вручную.

#### Examples

```kotlin
Sendsay.trackSessionStart()
```

```kotlin
Sendsay.trackSessionEnd()
```

> 👍
>
> The default behavior for manually calling `Sendsay.trackSessionStart()` multiple times can be controlled by the [manualSessionAutoClose](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) flag, which is set to `true` by default. If a previous session is still open (i.e. it has not been manually closed with `Sendsay.trackSessionEnd()`) and `Sendsay.trackSessionStart()` is called again, the SDK will automatically track a `session_end` for the previous session and then tracks a new `session_start` event. To prevent this behavior, set the [manualSessionAutoClose](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) flag to `false`.

## Push-уведомления

Если разработчики [интегрируют функциональность push-уведомлений](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#integration) в свое приложение, SDK автоматически отслеживает push-уведомления по умолчанию.

В [конфигурации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) вы можете отключить автоматическое отслеживание push-уведомлений, установив булево значение свойства `automaticPushNotification` в `false`. Тогда разработчик должен [отслеживать push-уведомления вручную](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#manually-track-push-notifications).

> ❗️
>
> The behavior of push notification tracking may be affected by the tracking consent feature, which in enabled mode requires explicit consent for tracking. Refer to the [consent documentation](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking-consent) for details.

### Track token manually

Use either the `trackPushToken()` (Firebase) or `trackHmsPushToken` (Huawei) method to [manually track the token](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#track-push-token-fcm) for receiving push notifications. The token is assigned to the currently logged-in customer (with the `identifyCustomer` method).

Invoking this method will track a push token immediately regardless of the value of the `tokenTrackFrequency` [configuration parameter](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration).

Each time the app becomes active, the SDK calls `verifyPushStatusAndTrackPushToken` and tracks the token.

#### Arguments

| Name                 | Type    | Description |
| ---------------------| ------- | ----------- |
| token **(required)** | String  | String containing the push notification token. |

#### Example 

Firebase:

```kotlin
Sendsay.trackPushToken("value-of-push-token")
```

Huawei:

```kotlin
Sendsay.trackHmsPushToken("value-of-push-token")
```

> ❗️
>
> Remember to invoke [anonymize](#anonymize) whenever the user signs out to ensure the push notification token is removed from the user's customer profile. Failing to do this may cause multiple customer profiles share the same token, resulting in duplicate push notifications.

### Track push notification delivery manually

Используйте метод `trackDeliveredPush()` для [отслеживания доставки push-уведомлений вручную](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#track-delivered-push-notification).

#### Arguments

| Name      | Type                                   | Description |
| ----------| -------------------------------------- | ----------- |
| data      | [NotificationData](#notificationdata)? | Notification data. |
| timestamp | Double                                 | Unix timestamp (in seconds) specifying when the event was tracked. Specify nil value to use the current time. |

#### NotificationData

| Name                    | Type                          | Description |
| ------------------------| ----------------------------- | ----------- |
| attributes              | HashMap<String, Any>          | Map of data attributes. |
| campaignData            | [CampaignData](#campaigndata) | Campaign data. |
| consentCategoryTracking | String?                       | Consent category. |
| hasTrackingConsent      | Boolean                       | Indicates whether explicit [tracking consent](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking-consent) has been obtained. |
| hasCustomEventType      | Boolean                       | Indicates whether the notification has a custom event type. |
| eventType               | String?                       | Event type for the notification (default: campaign). |
| sentTimestamp           | Double?                       | Unix timestamp (in seconds). Specify nil value to use the current time. |

#### CampaignData

| Name        | Type    | Description |
| ------------| ------- | ----------- |
| source      | String? | UTM source code. |
| campaign    | String? | UTM campaign code. |
| content     | String? | UTM content code. |
| medium      | String? | UTM method code.|
| term        | String? | UTM term code. |
| payload     | String? | Notification payload in JSON format. |
| createdAt   | Double  | Unix timestamp (in seconds). Specify nil value to use the current time. |
| completeUrl | String? | Campaign URL, defaults to null for push notifications.| 

> 📘
>
> Refer to [UTM parameters](https://documentation.bloomreach.com/engagement/docs/utm-parameters) in the campaigns documentation for details. 

#### Example

```kotlin
// create NotificationData from your push payload
val notificationData = NotificationData(
    dataMap = hashMapOf(
        "platform" to "android",
        "subject" to "Subject",
        "type" to "push",
        ...
    ),
    campaignMap = mapOf(
       "utm_campaign" to "Campaign name",
       "utm_medium" to "mobile_push_notification",
       "utm_content" to "en",
       ...
    )
)
Sendsay.trackDeliveredPush(
        data = notificationData
        timestamp = currentTimeSeconds()
)
```

### Track push notification click manually

Используйте метод `trackClickedPush()` для [отслеживания кликов по push-уведомлениям вручную](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#track-clicked-push-notification).

#### Arguments

| Name       | Type                                   | Description |
| -----------| -------------------------------------- | ----------- |
| data       | [NotificationData](#notificationdata)? | Notification data. |
| actionData | [NotificationData](#notificationdata)? | Action data.|
| timestamp  | Double?                                | Unix timestamp (in seconds) specifying when the event was tracked. Specify nil value to use the current time. |

#### Example

```kotlin
// create NotificationData from your push payload
val notificationData = NotificationData(
    dataMap = hashMapOf(
        "platform" to "android",
        "subject" to "Subject",
        "type" to "push",
        ...
    ),
    campaignMap = mapOf(
       "utm_campaign" to "Campaign name",
       "utm_medium" to "mobile_push_notification",
       "utm_content" to "en",
       ...
    )
)
Sendsay.trackClickedPush(
        data = notificationData
        timestamp = currentTimeSeconds()
)
```

## Очистка локальных данных клиента

Ваше приложение должно всегда запрашивать у клиентов согласие на отслеживание использования приложения. Если клиент соглашается на отслеживание событий на уровне приложения, но не на уровне персональных данных, обычно достаточно использовать метод `anonymize()`.

If the customer doesn't consent to any tracking, it's recommended not to initialize the SDK at all.

If the customer asks to delete personalized data, use the `clearLocalCustomerData()` method to delete all information stored locally before SDK is initialized.

The customer may also revoke all tracking consent after the SDK is fully initialized and tracking is enabled. In this case, you can stop SDK integration and remove all locally stored data using the [stopIntegration](#stop-sdk-integration) method.

Invoking this method will cause the SDK to:

* Remove the push notification token for the current customer from local device storage.
* Clear local repositories and caches, including all previously tracked events that haven't been flushed yet.
* Clear all session start and end information.
* Remove the customer record stored locally.
* Clear any previously loaded in-app messages, in-app content blocks, and app inbox messages.
* Clear the SDK configuration from the last invoked initialization.
* Stop handling of received push notifications.
* Stop tracking of deep links and universal links (your app's handling of them isn't affected).

## Остановка интеграции SDK

Your application should always ask the customer for consent to track their app usage. If the customer consents to tracking of events at the application level but not at the personal data level, using the `anonymize()` method is normally sufficient.

If the customer doesn't consent to any tracking before the SDK is initialized, it's recommended that the SDK isn't initialized at all. For the case of deleting personalized data before SDK initialization, see more info in the usage of the [clearLocalCustomerData](#clear-local-customer-data) method.

The customer may also revoke all tracking consent later, after the SDK is fully initialized and tracking is enabled. In this case, you can stop SDK integration and remove all locally stored data by using the `Sendsay.stopIntegration()` method.

Use the `stopIntegration()` method to delete all information stored locally and stop the SDK if it is already running.

Invoking this method will cause the SDK to:

* Remove the push notification token for the current customer from local device storage.
* Clear local repositories and caches, including all previously tracked events that were not flushed yet.
* Clear all session start and end information.
* Remove the customer record stored locally.
* Clear any In-app messages, In-app content blocks, and App inbox messages previously loaded.
* Clear the SDK configuration from the last invoked initialization.
* Stop handling of received push notifications.
* Stop tracking of Deep links and Universal links (your app's handling of them is not affected).

If the SDK is already running, invoking of this method also:

* Stops and disables session start and session end tracking even if your application tries later on.
* Stops and disables any tracking of events even if your application tries later on.
* Stops and disables any flushing of tracked events even if your application tries later on.
* Stops displaying of In-app messages, In-app content blocks, and App inbox messages.
  * Already displayed messages are dismissed.
  * Please validate dismiss behaviour if you [customized](https://documentation.bloomreach.com/engagement/docs/android-sdk-app-inbox#customize-app-inbox) the App Inbox UI layout. 

After invoking the `stopIntegration()` method, the SDK will drop any API method invocation until you [initialize the SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize_the_sdk) again. 

### Use cases

Correct usage of `stopIntegration()` method depends on the use case so please consider all scenarios.

#### Ask the customer for consent

Developers should always respect user privacy, not just to comply with GDPR, but to build trust and create better, more ethical digital experiences.

Permission requests in mobile apps should be clear, transparent, and contextually relevant. Explain why the permission is needed and request it only when necessary, ensuring users can make an informed choice.

You may use system dialog or In-app messages for that purpose.

![](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/gdpr-dialog-example.png)

In the case of the in-app message dialog, you can customize [In-app message action callback](https://documentation.bloomreach.com/engagement/docs/android-sdk-in-app-messages#customize-in-app-message-actions) to handle the user's decision about allowing or denying tracking permission.

```kotlin
Sendsay.inAppMessageActionCallback = object : InAppMessageCallback {  
    // set overrideDefaultBehavior to true to handle URL opening manually
    override var overrideDefaultBehavior = true
    // set trackActions to true to keep tracking of click and close actions
    override var trackActions = true
  
    override fun inAppMessageClickAction(message: InAppMessage, button: InAppMessageButton, context: Context) {
        if (messageIsForGdpr(message)) {
            handleGdprUserResponse(button)
        } else if (button.url != null) {
            openUrl(button)
        }
    }

    override fun inAppMessageCloseAction(
        message: InAppMessage,
        button: InAppMessageButton?,
        interaction: Boolean,
        context: Context
    ) {
        if (messageIsForGdpr(message) && interaction) {
            // regardless from `button` nullability, parameter `interaction` with true tells that user closed message
            Logger.i(this, "Stopping SDK")
            Sendsay.stopIntegration()
        }
    }

    override fun inAppMessageShown(message: InAppMessage, context: Context) {
        // Here goes your code
    }

    override fun inAppMessageError(message: InAppMessage?, errorMessage: String, context: Context) {
        // Here goes your code
    }

    private fun messageIsForGdpr(message: InAppMessage): Boolean {
        // apply your detection for GDPR related In-app
        // our example app is triggering GDPR In-app by custom event tracking so we used it for detection
        // you may implement detection against message title, ID, payload, etc.
        return message.applyEventFilter("event_name", mapOf("property" to "gdpr"), null)
    }

    private fun openUrl(button: InAppMessageButton) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.parse(button.url)
                }
            )
        } catch (e: ActivityNotFoundException) {
            Logger.e(this, "Unable to open URL", e)
        }
    }
}
```

#### Stop the SDK but upload tracked data

The SDK caches data (such as sessions, events, and customer properties) in an internal local database and periodically sends them to Bloomreach Engagement. These data are kept locally if the device has no network, or if you configured SDK to upload them less frequently.

Invoking the `stopIntegration()` method will remove all these locally stored data that may not be uploaded yet. To avoid loss of these data please request to flush them before stopping the SDK:

```kotlin
// Flushing requires that SDK is initialized
Sendsay.configure(...)
// Invoke flush force-fully
Sendsay.flushMode = FlushMode.MANUAL
val flushIsDone = Semaphore(0, true)
Sendsay.flushData {
    flushIsDone.release()
}
// Flushing process is asynchronous, we should wait until it is done
messageShownInvoked.tryAcquire(20, TimeUnit.SECONDS)
// All data are uploaded, we may stop SDK
Sendsay.stopIntegration()
```

#### Stop the SDK and wipe all tracked data

The SDK caches data (such as sessions, events, and customer properties) in an internal local database and periodically sends them to the Bloomreach Engagement app. These data are kept locally if the device has no network, or if you configured SDK to upload them less frequently.

You may face the use case where the customer gets removed from the Bloomreach Enagagement platform and subsequently you want to remove them from local storage too.

Please do not initialize the SDK in this case as, depending on your configuration, the SDK may upload the stored tracked events. This may lead to customer's profile being recreated in Bloomreach Enagagement. This is because stored events may have been tracked for this customer and uploading them will result in the recreation of the customer profile based on the assigned customer IDs.

To prevent this from happening, invoke `stopIntegration()` immediately without initializing the SDK:

```kotlin
Sendsay.stopIntegration()
```

This results in all previously stored data being removed from the device. The next SDK initialization will be considered a fresh new start.

#### Stop the already running SDK

The method `stopIntegration()` can be invoked anytime on a configured and running SDK.

This can be used in case the customer previously consented to tracking but revoked their consent later. You may freely invoke `stopIntegration()` with immediate effect.

```kotlin
// User gave you permission to track
Sendsay.configure(...)

// Later, user decides to stop tracking
Sendsay.stopIntegration()
```

This results in the SDK stopping all internal processes (such as session tracking and push notifications handling) and removing all locally stored data.

Please be aware that `stopIntegration()` stops any further tracking and flushing of data so if you require to upload tracked data to Bloomreach Engagement, then [flush them synchronously](#stop-the-sdk-but-upload-tracked-data) before stopping the SDK.

#### Customer denies tracking consent

It is recommended to ask the customer for tracking consent as soon as possible in your application. If the customer denies consent, please do not initialize the SDK at all.

## Платежи

SDK предоставляет удобный метод `trackPaymentEvent` для отслеживания информации о платеже за продукт или услугу в приложении.

### Отслеживание события платежа

Используйте метод `trackPaymentEvent()` для отслеживания платежей.

#### Arguments

| Name          | Type          | Description |
| --------------| ------------- | ----------- |
| purchasedItem | PurchasedItem | Dictionary of payment properties. |

#### Examples

First, create a `PurchasedItem` containing the basic information about the purchase:

```kotlin
val item = PurchasedItem(
        value = 12.34,
        currency = "EUR",
        paymentSystem = "Virtual",
        productId = "handbag",
        productTitle = "Awesome leather handbag"
)
```

Pass the `PurchasedItem` to `trackPaymentEvent` as follows:

```kotlin
Sendsay.trackPaymentEvent(purchasedItem = item)
```

If your app uses in-app purchases (for example, purchases with in-game gold, coins, etc.), you can track them with `trackPaymentEvent` using `Purchase` and `SkuDetails` objects used in [Google Play Billing Library](https://developer.android.com/google/play/billing/integrate):

```kotlin
val purchase: com.android.billingclient.api.Purchase = ...
val skuDetails: com.android.billingclient.api.SkuDetails = ...
val item = PurchasedItem(
        value = sku.priceAmountMicros / 1000000.0,
        currency = sku.priceCurrencyCode,
        paymentSystem = "Google Play",
        productId = sku.sku,
        productTitle = sku.title,
        receipt = purchase.signature
)

Sendsay.trackPaymentEvent(purchasedItem = item)
```

## Свойства по умолчанию

Вы можете [настроить](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) свойства по умолчанию, которые будут отслеживаться с каждым событием. Обратите внимание, что значение свойства по умолчанию будет перезаписано, если отслеживаемое событие имеет свойство с тем же ключом.

```kotlin
// Create a new SendsayConfiguration instance
val configuration = SendsayConfiguration()
configuration.defaultProperties["thisIsADefaultStringProperty"] = "This is a default string value"
configuration.defaultProperties["thisIsADefaultIntProperty"] = 1

// Start the SDK
Sendsay.init(App.instance, configuration)
```

After initializing the SDK, you can change the default properties by setting `Sendsay.defaultProperties`.
