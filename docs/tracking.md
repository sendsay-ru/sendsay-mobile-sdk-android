---
title: Отслеживание
excerpt: Отслеживайте клиентов и события с помощью Android SDK
slug: android-sdk-tracking
categorySlug: integrations
parentDocSlug: android-sdk
---

Вы можете передавать данные и события в CDP Sendsay, чтобы узнать больше о ваших клиентах.

По умолчанию SDK автоматически отслеживает определенные события, включая:

* Установку (после установки приложения и после вызова [anonymize](#anonymize))
* Начало и окончание пользовательской сессии

Кроме того, вы можете отслеживать пользовательские события, связанные с вашими бизнес-процессами.

## События

### Отслеживание событий

Используйте метод `trackEvent()` для отслеживания пользовательских событий, связанных с вашими бизнес-процессами.

Вы можете использовать любое имя для типа пользовательского события. Мы рекомендуем использовать описательное и удобочитаемое имя.


#### Аргументы

| Имя                       | Тип                                   | Описание |
| ------------------------- | -------------- | ----------- |
| properties                | PropertiesList | Словарь свойств события. |
| timestamp                 | Double         | Unix timestamp (в секундах), указывающий, когда событие было отслежено. Укажите значение `nil` для использования текущего времени. |
| eventType **(обязательно)**  | String         | Название типа события, например `screen_view`. |

#### SSEC (События модуля "Продажи")

Обратитесь к документации [События модуля "Продажи"](https://docs.sendsay.ru/ecom/how-to-configure-data-transfer).

```kotlin
тут пример ssec-события
```

#### CCE (Пользовательские события)

```kotlin
тут пример cce-события
```

#### Примеры

Представьте, что вы хотите отслеживать, какие экраны просматривает клиент. Вы можете создать пользовательское событие `screen_view` для этого и передавать его в Пользовательские события CDP Sendsay

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

Без дополнительной идентификации события отслеживаются для анонимного клиента, определяемого только по т.н. cookie (автоматически сгенеренный идентификтор). Когда клиент идентифицируется по собственному идентификатору (email, телефон, csid), события будут ассоциированы с переданным основным идентификатором.


### Идентификация

Используйте метод `identifyCustomer()` для идентификации клиента по его уникальному для ваших бизнес-процессов идентификатору (email, телефон, csid).

По умолчанию используется ключ `registered`, и его значение – это обычно адрес электронной почты клиента. Однако ваши настройки мобильного приложения могут определять другой основной идентификатор.

По желанию вы можете передавать в CDP Sendsay дополнительные свойства клиента, такие как имя и фамилия, возраст и т.д.

> ❗️
>
> SDK сохраняет данные клиента, включая идентификаторы, переданные через `identifyCustomer()`, в локальном кэше на устройстве. Очистка локального кэша требует вызова [anonymize](#anonymize).
> Если профиль клиента удален из базы данных CDP Sendsay, последующая инициализация SDK в приложении может привести к повторному созданию профиля клиента из локально кэшированных данных.

#### Аргументы

| Имя                         | Тип            | Описание |
| --------------------------- | -------------- | ----------- |
| customerIds **(обязательно)**  | CustomerIds    | Словарь уникальных идентификаторов клиента. Принимаются только идентификаторы, определенные в проекте Engagement. |
| properties                  | PropertiesList | Словарь свойств клиента. |
| timestamp                   | Double         | Unix timestamp (в секундах), указывающий, когда свойства клиента были обновлены. Укажите значение `nil` для использования текущего времени. |

#### Передача данных в профиль клиента (member.set)

Обратитесь к документации [метода member.set SendsayAPI](https://sendsay.ru/api/api.html#%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9E%D0%B1%D0%BD%D0%BE%D0%B2%D0%B8%D1%82%D1%8C-%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D0%B5-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9A%D0%94).

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
