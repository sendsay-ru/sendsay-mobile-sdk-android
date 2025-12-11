---
title: Отслеживание
excerpt: Отслеживайте клиентов и события с помощью Android SDK
slug: android-sdk-tracking
categorySlug: integrations
parentDocSlug: android-sdk
---

Вы можете передавать данные и события в CDP Sendsay, чтобы лучше понимать поведение клиентов в мобильном приложении.

По умолчанию SDK автоматически отслеживает ряд базовых событий, включая:

* установку приложения (после первой установки и после вызова [anonymize](#anonymize)),
* начало и окончание пользовательской сессии.

Помимо автоматического трекинга вы можете отправлять собственные события, связанные с вашими бизнес-процессами.

## События

### Отслеживание событий

Для отслеживания пользовательских событий используйте метод `trackEvent()`.

Вы можете использовать любое удобное и описательное название события.

#### Аргументы

| Имя                       | Тип                                   | Описание |
| ------------------------- | -------------- | ----------- |
| properties                | PropertiesList | Словарь свойств события. |
| eventType **(обязательно)**  | String         | Тип события, например, `screen_view`. |

#### SSEC (События модуля "Продажи")

Подробное описание доступно в документации: [События модуля "Продажи"](https://docs.sendsay.ru/ecom/how-to-configure-data-transfer).

Там указаны требования к обязательным полям [TrackSSECDataCore](../sdk/src/main/java/com/sendsay/sdk/models/TrackSSECBuilders.kt) для заполнения к каждому типу [TrackingSSECType](../sdk/src/main/java/com/sendsay/sdk/models/TrackingSSECType.kt)

## Пример (рекомендуемый способ — TrackSSECDataBuilder)

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

## Пример JSON > TrackSSECData

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

## Пример с передачей списка заказов (объект [OrderItem](../sdk/src/main/java/com/sendsay/sdk/models/OrderItem.kt))

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
    val jsonString = """{
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

##### Пример 1. Отслеживание просмотра экрана

Предположим, вам нужно понимать, какие экраны приложения чаще всего просматривают пользователи. Для этого можно отправлять событие `screen_view` и передавать название экрана в свойстве `screen_name`.

Сначала создайте словарь с нужными свойствами:

```kotlin
val properties = PropertiesList(
    hashMapOf(
        Pair("screen_name", "dashboard"),
        Pair("other_property", 123.45)
    )
)
```

Затем передайте данные в `trackEvent()` (вместе с `eventType` (`screen_view`)):

```kotlin
Sendsay.trackEvent(
    properties = properties,
    timestamp = null,
    eventType =  "screen_view"
)
```

##### Пример 2. Сложные свойства с вложенным JSON

Если вы хотите отправить более детализированное событие — например, результат покупки, список товаров и итоговую стоимость — можно использовать вложенную структуру в параметре properties:

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

Без идентификации SDK связывает события с анонимным клиентом через автоматически сгенерированный cookie.

После идентификации клиента (по email, телефону, csid и т. д.) события будут связаны с этим идентификатором.

### Идентификация

Используйте метод `identifyCustomer()` для передачи уникальных идентификаторов клиента (email, телефон, csid) и, при необходимости, дополнительных свойств (имя, возраст и др.).

> ❗️
>
> SDK сохраняет данные клиента в локальном кэше. Чтобы очистить кэш, используйте [anonymize](#anonymize). Если профиль клиента был удалён из CDP Sendsay, то при следующей инициализации SDK может заново создать профиль по локальным данным.

#### Аргументы

| Имя                         | Тип            | Описание |
| --------------------------- | -------------- | ----------- |
| customerIds **(обязательно)**  | CustomerIds    | Словарь уникальных идентификаторов клиента. Принимаются только идентификаторы, определённые в проекте Engagement. |
| properties                  | PropertiesList | Словарь свойств клиента. |

#### Передача данных в профиль клиента (member.set)

Подробное описание доступно в документации: [Метод member.set SendsayAPI](https://sendsay.ru/api/api.html#%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9E%D0%B1%D0%BD%D0%BE%D0%B2%D0%B8%D1%82%D1%8C-%D0%B4%D0%B0%D0%BD%D0%BD%D1%8B%D0%B5-%D0%BF%D0%BE%D0%B4%D0%BF%D0%B8%D1%81%D1%87%D0%B8%D0%BA%D0%B0-%D0%9A%D0%94).

#### Примеры

В примере создаются два словаря — с основным индентификатором и дополнительными свойствами клиента:

```kotlin
val customerIds = CustomerIds().withId("registered","jane.doe@example.com")
```

```kotlin
val properties = PropertiesList(
    hashMapOf(
        "member_set" to hashMapOf(
            "datakey" to [
                ["propertyName", "set/update/delete"+".copy"(optional), "value/newValue"], // пример возможных(не всех) значений установки/обновления/удаления строкового свойства
                ["anotherProperty", "update.copy", 123] // рабойчи пример обновления числового свойства
            ]
        )
    )
)
```

Созданные словари нужно передать в `identifyCustomer()`:

```kotlin
Sendsay.identifyCustomer(
    customerIds = customerIds,
    properties = properties
)
```

### Anonymize

Используйте метод `anonymize()` для удаления всей информации, сохранённой локально, и сброса текущего состояния SDK, например, когда пользователь выходит из приложения.

Вызов `anonymize()` приведёт к тому, что SDK:

* удалит push-токен для текущего клиента из локального хранилища устройства и профиля клиента в CDP Sendsay;
* очистит локальные репозитории и кэши (кроме уже отслеженных событий);
* отследит новое начало сессии, если включён `automaticSessionTracking`;
* создаст новую запись клиента в Sendsay (генерируется новый `cookie`);
* назначит предыдущий push-токен новой записи клиента;
* отследит новое событие `installation`.

#### Пример

```kotlin
Sendsay.anonymize()
```

## Сессии

SDK отслеживает сессии по умолчанию, генерируя два события: 
- `session_start`,
- `session_end`.

Сессия начинается при запуске приложения и завершается при переходе в фон или по времени неактивности. Если пользователь возвращается в приложение до истечения тайм-аута сессии, приложение продолжит текущую сессию.

Тайм-аут сессии по умолчанию: **60 секунд**. Его можно изменить через `sessionTimeout` в конфигурации SDK.

### Отслеживание сессии вручную

Чтобы отключить автоматическое отслеживание сессий, установите `automaticSessionTracking` = `false`.

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

Вызов `clearLocalCustomerData()` приведёт к тому, что SDK:

* удалит push-токен для текущего клиента из локального хранилища устройства;
* очистит локальные репозитории и кэши, включая все ранее отслеженные события, которые ещё не были отправлены;
* очистит всю информацию о начале и окончании сессий;
* удалит запись клиента, хранящуюся локально;
* очистит конфигурацию SDK от последней вызванной инициализации;
* прекратит обработку полученных push-уведомлений,
* прекратит отслеживание глубоких ссылок и универсальных ссылок (обработка вашим приложением не затрагивается).
