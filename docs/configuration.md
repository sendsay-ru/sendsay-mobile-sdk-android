---
title: Конфигурация
excerpt: Полный справочник по конфигурации Android SDK
slug: android-sdk-configuration
categorySlug: integrations
parentDocSlug: android-sdk-setup
---

На этой странице представлен обзор всех параметров конфигурации SDK. Вы можете настроить SDK в коде, используя объект `SendsayConfiguration`, или в файле с именем `sendsay_configuration.json` внутри папки `assets` вашего приложения. 

> 📘
>
> Обратитесь к разделу [Инициализация SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize-the-sdk) для получения инструкций.

## Параметры конфигурации

* `projectToken` **(обязательный)**
   * Токен вашего проекта. Вы можете найти его в веб-приложении Engagement в разделе `Project settings` > `Access management` > `API`.

* `authorization` **(обязательный)**
   * Формат `"Token <token>"`, где `<token>` - это API ключ Engagement.
   * Токен должен быть **публичным** ключом Engagement. Подробности см. в [Управление доступом API мобильных SDK](mobile-sdks-api-access-management).
   * Для получения дополнительной информации обратитесь к [документации Sendsay API](https://docs.exponea.com/reference#access-keys).

* `baseURL`
  * Базовый URL вашего API, который можно найти в веб-приложении Engagement в разделе `Project settings` > `Access management` > `API`.
  * Значение по умолчанию `https://api.exponea.com`.
  * Если у вас есть пользовательский базовый URL, вы должны установить это свойство.

* `projectRouteMap`
  * Если вам нужно отслеживать события в нескольких проектах, вы можете определить информацию о проекте для "типов событий", которые должны отслеживаться несколько раз.
    Пример:
    ```kotlin
    var projectRouteMap = mapOf<EventType, List<SendsayProject>> (
        EventType.TRACK_CUSTOMER to listOf(
            SendsayProject(
                "https://api.exponea.com",
                "YOUR_PROJECT_TOKEN",
                "Token YOUR_API_KEY"
            )
        )
    )
    ```
  
* `defaultProperties`
  * Список свойств, которые будут добавлены ко всем событиям отслеживания.
  * Значение по умолчанию: `nil`

* `allowDefaultCustomerProperties`
  * Флаг для применения списка `defaultProperties` к событию отслеживания `identifyCustomer`
  * Значение по умолчанию: `true`

* `automaticSessionTracking`
  * Флаг для управления автоматическим отслеживанием событий `session_start` и `session_end`.
  * Значение по умолчанию: `true`

* `sessionTimeout`
  * Сессия - это фактическое время, проведенное в приложении. Она начинается при запуске приложения и заканчивается, когда приложение переходит в фоновый режим.
  * Это значение используется для расчета времени сессии.
  * Значение по умолчанию: `60` секунд.
  * Узнайте больше об [отслеживании сессий](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking#session)

* `automaticPushNotification`
  * Управляет тем, будет ли SDK автоматически обрабатывать push-уведомления.
  * Значение по умолчанию: `true`

* `pushIcon`
  * Иконка, которая будет отображаться в push-уведомлении.
  * Подробности см. на https://developer.android.com/design/ui/mobile/guides/home-screen/notifications#notification-header.

* `pushAccentColor`
  * Акцентный цвет push-уведомления. Изменяет цвет маленькой иконки и кнопок уведомления. Например: `Color.GREEN`.
  * Это ID цвета, а не ID ресурса. При использовании цветов из ресурсов вы должны указать ресурс, например: `context.resources.getColor(R.color.something)`.
  * Подробности см. на https://developer.android.com/design/ui/mobile/guides/home-screen/notifications#notification-header.

* `pushChannelName`
  * Имя канала, который будет создан для push-уведомлений.
  * Доступно только для API уровня 26+. Подробности см. на https://developer.android.com/training/notify-user/channels.

* `pushChannelDescription`
  * Описание канала, который будет создан для push-уведомлений.
  * Доступно только для API уровня 26+. Подробности см. на https://developer.android.com/training/notify-user/channels.

* `pushChannelId`
  * ID канала для push-уведомлений.
  * Доступно только для API уровня 26+. Подробности см. на https://developer.android.com/training/notify-user/channels.

* `pushNotificationImportance`
  * Важность уведомлений для канала уведомлений.
  * Доступно только для API уровня 26+. Подробности см. на https://developer.android.com/training/notify-user/channels.

* `tokenTrackFrequency`
  * Указывает частоту, с которой SDK должен отслеживать токен push-уведомлений в Engagement.
  * Значение по умолчанию: `ON_TOKEN_CHANGE`
  * Возможные значения:
    * `ON_TOKEN_CHANGE` - отслеживает push-токен, если он отличается от ранее отслеженного
    * `EVERY_LAUNCH` - всегда отслеживает push-токен
    * `DAILY` - отслеживает push-токен один раз в день

* `requirePushAuthorization`
  * Флаг, указывающий, должен ли SDK проверять [статус разрешения push-уведомлений](https://developer.android.com/develop/ui/views/notifications/notification-permission) и отслеживать push-токен только в случае, если пользователь предоставил разрешение на получение push-уведомлений.
  * Возможные значения:
    * `true` - отслеживает push-токен только в случае, если пользователь предоставил разрешение на получение push-уведомлений. Пустое значение токена отслеживается, если пользователь отклонил разрешение. Это полезно для отправки обычных push-уведомлений целевой аудитории, которая разрешает получение уведомлений.
    * `false` - отслеживает push-токен независимо от статуса разрешения уведомлений. Это полезно для отправки тихих push-уведомлений, которые не требуют разрешения от пользователя.
  * Значение по умолчанию: `false`

* `maxTries`
  * Управляет количеством попыток SDK отправить событие перед прерыванием. Полезно, например, в случае недоступности API или возникновения других временных ошибок.
  * SDK будет считать данные отправленными, если это число превышено, и удалит данные из очереди.
  * Значение по умолчанию: `10`

* `advancedAuthEnabled`
  * Если установлено, для связи с API Engagement, перечисленными в [Авторизация токена клиента](https://documentation.bloomreach.com/engagement/docs/android-sdk-authorization#customer-token-authorization), используется расширенная авторизация.
  * Подробности см. в [документации по авторизации](https://documentation.bloomreach.com/engagement/docs/android-sdk-authorization).

* `inAppContentBlocksPlaceholders`
  * Если установлено, все [блоки внутриприложенческого контента](https://documentation.bloomreach.com/engagement/docs/android-sdk-in-app-content-blocks) будут предварительно загружены сразу после инициализации SDK.

* `allowWebViewCookies`
  * Флаг для включения или отключения cookies в WebViews.
  * Значение по умолчанию: `false`
  * > ❗️
    >
    > **Отказ от ответственности**:
    > * В целях безопасности cookies по умолчанию отключены в WebViews.
    > * Эта настройка влияет на все WebViews в приложении, НЕ ТОЛЬКО на те, которые используются SDK.
    > * НЕ ИЗМЕНЯЙТЕ ЭТУ НАСТРОЙКУ, если вы не знаете рисков, связанных с включением и хранением cookies.
    > * Изменив эту настройку и включив cookies в WebViews, вы берете на себя полную ответственность за любые уязвимости безопасности или инциденты, вызванные ими.

* `manualSessionAutoClose`
    * Определяет, автоматически ли SDK отслеживает `session_end` для сессий, которые остаются открытыми, когда `Sendsay.trackSessionStart()` вызывается несколько раз в режиме ручного отслеживания сессий.
    * Значение по умолчанию: `true`