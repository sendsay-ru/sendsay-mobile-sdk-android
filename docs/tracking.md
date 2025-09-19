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
| eventType **(обязательно)**  | String         | Название типа события, например `screen_view`. |

#### SSEC (События модуля "Продажи")

Обратитесь к документации [События модуля "Продажи"](https://docs.sendsay.ru/ecom/how-to-configure-data-transfer).
Там указаны требования к обязательным полям [TrackSSECDataCore](../sdk/src/main/java/com/sendsay/sdk/models/TrackSSECBuilders.kt) для заполнения к каждому типу [TrackingSSECType](../sdk/src/main/java/com/sendsay/sdk/models/TrackingSSECType.kt)

## Пример с помощью TrackSSECDataBuilder(рекомендуется, чтобы избежать ошибок):
```kotlin
fun trackClearBasket() {
    // Получение текущего времени с использованием SimpleDateFormat
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val currentDateTime = dateFormat.format(Date())

    // Создание объекта трекинга SSEC с помощью TrackSSEC
    val data = TrackSSEC
        .basketClear()    // Конструктор для удобного создания события BASKET_CLEAR
        .setProduct(dateTime = currentDateTime) // Указываем обязательные поля (смотри документацию)
        .setItems(listOf(OrderItem(id = "-1")))
        .buildData()        // Завершаем создание объекта трекинга SSEC
    try {
    // Отправка события SSEC
        Sendsay.trackSSECEvent(TrackingSSECType.BASKET_CLEAR, data)
    } catch (e: IllegalArgumentException) {
        Log.e(TrackingSSECType.BASKET_CLEAR.name, e.stackTrace.toString())
    }
}
```

## Пример с помощью JSON конвертера в TrackSSECData:
```kotlin
fun trackClearBasketJson() {
    // Получение текущего времени с использованием SimpleDateFormat
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val currentDateTime = dateFormat.format(Date())

    // Пример JSON, который можем отправить в качестве SSEC
    val jsonString = """
    {"dt":"$currentDateTime",
        "items": [
          {
            "id": -1
          }
        ]
      }
    """.trimIndent()

    // Создание объекта трекинга SSEC через конвертер в TrackSSECData
    val jsonToSsecExample: TrackSSECData =
        SendsayGson.instance.fromJson(jsonString, TrackSSECData::class.java)
    try {
    // Отправка события SSEC
        Sendsay.trackSSECEvent(TrackingSSECType.BASKET_CLEAR, jsonToSsecExample)
    } catch (e: IllegalArgumentException) {
        Log.e(TrackingSSECType.BASKET_CLEAR.name, e.stackTrace.toString())
    }
}
```

## Еще пример с передачей "списка заказа", объекта [OrderItem](../sdk/src/main/java/com/sendsay/sdk/models/OrderItem.kt)
```kotlin
fun trackBasket() {
    // Генерация случайного transaction_id без отрицательных чисел
    val randomTransactionId = Random.nextLong().absoluteValue.toString()

    // Получение текущего времени с использованием SimpleDateFormat
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val currentDateTime = dateFormat.format(Date())
    
    // Описание "списка заказов"
    val orderN = OrderItem(
        id = "product1",
        qnt = 1,
        price = 7.88,
        available = 1,
        name = "name",
        oldPrice = 5.99,
        picture = listOf(),
        url = "url",
        model = "model",
        vendor = "vendor",
        categoryId = 777,
        category = "category_name",
    )
    val orders = listOf(orderN, orderN, orderN,)

    // Создание объекта трекинга SSEC с помощью TrackSSEC
    val orderData = TrackSSEC
        .basketAdd()       // Конструктор для удобного создания события BASKET_ADD
        .setTransaction(   // Указываем обязательные поля (смотри документацию)
            id = randomTransactionId, 
            dt = currentDateTime, sum = 100.9
        )
        .setItems(orders)
        .build()
    try {
        Sendsay.trackSSECEvent(TrackingSSECType.BASKET_ADD, orderData)
    } catch (e: IllegalArgumentException) {
        Log.e(TrackingSSECType.BASKET_ADD.name, e.stackTrace.toString())
    }
}
```

#### CCE (Пользовательские события) (в разработке)

```kotlin
    jsonString = """{
        "cce": {"any-key": "test-value"},
    }""".trimIndent()
    
/// ......

    val propertiesList = PropertiesList(properties)
    Sendsay.trackEvent(
        properties = propertiesList,
        timestamp = null,
        eventType = "custom_event_name"
    )
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

#### Передача данных в профиль клиента (member.set)

Обратитесь к документации [метода member.set SendsayAPI](https://sendsay.ru/api/api.html#%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9E%D0%B1%D0%BD%D0%BE%D0%B2%D0%B8%D1%82%D1%8C-%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D0%B5-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9A%D0%94).

#### Примеры

Сначала создайте словарь `CustomerIds`, содержащий как минимум один идентификатор клиента:

```kotlin
val customerIds = CustomerIds().withId("registered","jane.doe@example.com")
```

По желанию создайте словарь с дополнительными свойствами клиента:

```kotlin

ТУТ поменять на member.set

val properties = PropertiesList(
    hashMapOf(
        Pair("first_name", "Jane"),
        Pair("last_name", "Doe"),
        Pair("age", 32),
        Pair("phone", "+79991112233")
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

### Anonymize

Используйте метод `anonymize()` для удаления всей информации, сохраненной локально, и сброса текущего состояния SDK. Типичный случай использования - когда пользователь выходит из приложения.

Вызов этого метода приведет к тому, что SDK:

* Удалит токен push-уведомлений для текущего клиента из локального хранилища устройства и профиля клиента в CDP Sendsay.
* Очистит локальные репозитории и кэши, исключая отслеженные события.
* Отследит новое начало сессии, если включен `automaticSessionTracking`.
* Создаст новую запись клиента в Sendsay (генерируется новый `cookie`).
* Назначит предыдущий токен push-уведомлений новой записи клиента.
* Отследит новое событие `installation` для нового клиента.

#### Примеры

```kotlin
Sendsay.anonymize()
```

## Сессии

SDK отслеживает сессии автоматически по умолчанию, генерируя два события: `session_start` и `session_end`.

Сессия представляет фактическое время, проведенное в приложении. Она начинается при запуске приложения и заканчивается, когда оно переходит в фоновый режим. Если пользователь возвращается в приложение до истечения тайм-аута сессии, приложение продолжит текущую сессию.

Тайм-аут сессии по умолчанию составляет 60 секунд. Установите `sessionTimeout` в конфигурации SDK, чтобы указать другой тайм-аут.

### Отслеживание сессии вручную

Чтобы отключить автоматическое отслеживание сессий, установите `automaticSessionTracking` в `false`.

Используйте методы `trackSessionStart()` и `trackSessionEnd()` для отслеживания сессий вручную.

#### Примеры

```kotlin
Sendsay.trackSessionStart()
```

```kotlin
Sendsay.trackSessionEnd()
```


## Очистка локальных данных клиента

Если клиент просит удалить свои данные, используйте метод `clearLocalCustomerData()` для удаления всей информации, хранящейся локально до инициализации SDK.

Вызов этого метода приведет к тому, что SDK:

* Удалит токен push-уведомлений для текущего клиента из локального хранилища устройства.
* Очистит локальные репозитории и кэши, включая все ранее отслеженные события, которые еще не были отправлены.
* Очистит всю информацию о начале и окончании сессий.
* Удалит запись клиента, хранящуюся локально.
* Очистит конфигурацию SDK от последней вызванной инициализации.
* Прекратит обработку полученных push-уведомлений.
* Прекратит отслеживание глубоких ссылок и универсальных ссылок (обработка их вашим приложением не затрагивается).
