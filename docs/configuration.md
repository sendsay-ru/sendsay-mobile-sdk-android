---
title: Конфигурация
excerpt: Полная справочная информация по конфигурации Android SDK
slug: android-sdk-configuration
categorySlug: integrations
parentDocSlug: android-sdk-setup
---

На этой странице собраны все параметры конфигурации Sendsay Android SDK. Android SDK можно настроить через код, используя объект `SendsayConfiguration`, или через файл **sendsay_configuration.json** в папке **assets** вашего приложения.

> 📘
>
> Информацию о том, где и как применять параметры конфигурации, смотрите в разделе [Инициализация SDK](setup.md#инициализация-sdk).

## Параметры конфигурации

* `projectToken` **(обязательный)**
   * Токен проекта. Его можно посмотреть в веб-приложении CDP Sendsay в разделе `Профиль (верхний угол)` > `Настройки аккаунта` > `Основной логин`.

* `authorization` **(обязательный)**
   * Используйте формат `"Token <token>"`, где `<token>` - это API ключ CDP Sendsay.
   * Используйте **публичный** API-ключ CDP Sendsay. Его можно посмотреть в веб-приложении CDP Sendsay в разделе `Подписчики` > `Мобильное приложение` > `Выберите из списка нужное` > `Настройки приложение и импорта` > `Авторизационный токен`.
   * Подробную информацию смотрите в разделе [Управление доступом API мобильных SDK](mobile-sdks-api-access-management).

* `baseURL`
  * Базовый URL мобильного API. По умолчанию: `https://mobi.sendsay.ru/mobi/api/v100`.
  * Укажите своё значение, если используйте пользовательский базовый URL.

* `projectRouteMap`
  * Позволяет отслеживать события в нескольких проектах. Используйте, если отдельные «типы событий» должны отслеживать несколько раз и отправляться в разные проекты.
    Пример:
    ```kotlin
    var projectRouteMap = mapOf<EventType, List<SendsayProject>> (
        EventType.TRACK_CUSTOMER to listOf(
            SendsayProject(
                "https://mobi.sendsay.ru/mobi/api/v100",
                "YOUR_PROJECT_TOKEN",
                "Token YOUR_API_KEY"
            )
        )
    )
    ```
  
* `defaultProperties`
  * Свойства, которые SDK добавляет ко всем событиям отслеживания.
  * По умолчанию: `null`.

* `allowDefaultCustomerProperties`
  * Применяет список `defaultProperties` к событию отслеживания `identifyCustomer`.
  * По умолчанию: `true`.

* `automaticSessionTracking`
  * Включает автоматическое отслеживание событий`session_start` и `session_end`.
  * По умолчанию: `true`.

* `sessionTimeout`
  * Время сессии в секундах.
  * По умолчанию: `60` секунд.
  * Подробнее про отслеживание сессий — в [документации](../docs/tracking#сессии).

  > 📘
  >
  > Сессия — это фактическое время, проведённое в приложении. Она начинается при запуске приложения и заканчивается, когда приложение переходит в фон.

* `automaticPushNotification`
  * Управляет автоматической обработкой push-уведомлений SDK.
  * По умолчанию: `true`.

* `pushIcon`
  * Иконка, отображаемая в push-уведомлении.
  * Подробнее о требованиях к иконкам — в [документации](https://developer.android.com/design/ui/mobile/guides/home-screen/notifications#notification-header) Android.

* `pushAccentColor`
  * Акцентный цвет уведомления (иконка, кнопки), например: `Color.GREEN`.
  * Это **ID цвета**, а не ID ресурса. При использовании цветов из ресурсов нужно указать ресурс, например: `context.resources.getColor(R.color.something)`.
  * Подробнее — в [документации](https://developer.android.com/design/ui/mobile/guides/home-screen/notifications#notification-header) Android.

* `pushChannelName`
  * Имя канала, который будет создан для push-уведомлений.
  * Доступно только для API уровня 26+. 
  * Подробнее — в [документации](https://developer.android.com/training/notify-user/channels) Android.

* `pushChannelDescription`
  * Описание канала для push-уведомлений.
  * Доступно только для API уровня 26+. 
  * Подробнее — в [документации](https://developer.android.com/training/notify-user/channels) Android.

* `pushChannelId`
  * ID канала для push-уведомлений.
  * Доступно только для API уровня 26+. 
  * Подробнее — в [документации](https://developer.android.com/training/notify-user/channels) Android.

* `pushNotificationImportance`
  * Уровень важности уведомлений для канала.
  * Доступно только для API уровня 26+. 
  * Подробнее — в [документации](https://developer.android.com/training/notify-user/channels) Android.

* `tokenTrackFrequency`
  * Частота, с которой SDK отслеживает токен push-уведомлений в CDP Sendsay.
  * По умолчанию: `ON_TOKEN_CHANGE` — отслеживает push-токен, если он отличается от ранее отслеженного
  * Другие возможные значения:
    * `EVERY_LAUNCH` — всегда отслеживает push-токен.
    * `DAILY` — отслеживает push-токен один раз в день.

* `requirePushAuthorization`
  * Определяет, должен ли SDK отслеживать токен push-уведомлений только после выдачи пользователем разрешения. Подробнее о статусе разрешения в [документации](https://developer.android.com/develop/ui/views/notifications/notification-permission) Android.
  * Возможные значения:
    * `true` — токен отслеживается, если разрешение на уведомления выдано. Пустое значение токена отслеживается, если пользователь отклонил разрешение — это полезно для отправки обычных push-уведомлений аудитории, которая разрешает получение уведомлений.
    * `false` — токен отслеживается всегда, независимо от статуса разрешения уведомлений. Это полезно для отправки **тихих push-уведомлений**, которые не требуют разрешения от пользователя.
  * По умолчанию: `false`.

* `maxTries`
  * Количество попыток отправки данных, прежде чем SDK удалит событие из очереди. Полезно в случае недоступности API или других временных ошибок.
  * По умолчанию: `10`.

* `advancedAuthEnabled`
  * Включает расширенную авторизацию для эндпоинтов, перечисленных в разделе [Авторизация токена клиента](../docs/authorization.md#авторизация-по-токену) документации CDP Sendsay.
  * Подробнее — в [документации по авторизации](../docs/authorization.md).

[//]: # (* `inAppContentBlocksPlaceholders`)
[//]: # ( * При включении SDK заранее загрузит [блоки контента]&#40;https://documentation.bloomreach.com/engagement/docs/android-sdk-in-app-content-blocks&#41; в приложении.)

* `allowWebViewCookies`
  * Включает cookie в **WebView**.
  * По умолчанию: `false`
  * > ❗️
    >
    > **Отказ от ответственности**:
    > * Cookie в WebView отключены из соображений безопасности.
    > * Настройка влияет **на все WebView**, а не только на те, что использует SDK.
    > * Включайте её, если полностью понимаете риски, связанные с включением и хранением cookie.

* `manualSessionAutoClose`
    * Определяет, должен ли SDK автоматически отслеживать `session_end` для сессий, которые остаются открытыми, когда `Sendsay.shared.trackSessionStart()` вызывается несколько раз в режиме ручного отслеживания сессий.
    * По умолчанию: `true`.