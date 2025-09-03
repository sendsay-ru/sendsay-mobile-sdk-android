# Push-уведомления

Включение push-уведомлений в вашем приложении с помощью Android SDK

Engagement позволяет отправлять push-уведомления пользователям вашего приложения с помощью [сценариев](https://documentation.bloomreach.com/engagement/docs/scenarios-1). Мобильное приложение обрабатывает push-сообщение с помощью SDK и отображает уведомление на устройстве клиента.

Push-уведомления также могут быть беззвучными, используемыми только для обновления интерфейса приложения или запуска какой-либо фоновой задачи.

> 📘
>
> Обратитесь к разделу [Мобильные push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification), чтобы узнать, как создавать push-уведомления в веб-приложении Engagement.

> 📘
>
> Также см. [FAQ по мобильным push-уведомлениям](https://support.bloomreach.com/hc/en-us/articles/18152713374877-Mobile-Push-Notifications-FAQ) в Центре поддержки Bloomreach.

## Интеграция

В этом разделе описаны шаги для добавления минимальной функциональности push-уведомлений (получение уведомлений-предупреждений) в ваше приложение.

### Стандартная интеграция (Firebase)

Чтобы иметь возможность отправлять [push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications) с платформы Engagement и получать их в вашем приложении на Android-устройствах, необходимо:

1. Настроить проект Firebase.
2. Реализовать Firebase messaging в вашем приложении.
3. Настроить интеграцию Firebase Cloud Messaging в веб-приложении Engagement.

> 📘
>
> Следуйте инструкциям в разделе [Firebase Cloud Messaging](https://documentation.bloomreach.com/engagement/docs/android-sdk-firebase).

> 👍
>
> Обратите внимание, что после того, как Google объявил устаревшим и удалил устаревший API FCM в июне 2024 года, Bloomreach Engagement теперь использует Firebase HTTP v1 API. Обратитесь к разделу [Обновление Firebase до HTTP v1 API](https://support.bloomreach.com/hc/en-us/articles/18931691055133-Firebase-upgrade-to-HTTP-v1-API) в Центре поддержки Bloomreach для получения информации об обновлении.
>
> Если ваш проект Engagement использует устаревшую версию интеграции Firebase, вы должны [прочитать и перенастроить интеграцию FCM следуя текущим инструкциям](https://documentation.bloomreach.com/engagement/docs/android-sdk-firebase#configure-the-firebase-cloud-messaging-integration-in-engagement).
>
> ![](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/fcm-deprecated.png)

### Интеграция с Huawei

Чтобы иметь возможность отправлять [push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications) с платформы Engagement и получать их в вашем приложении на устройствах Huawei, необходимо:

1. Настроить Huawei Mobile Services (HMS)
2. Реализовать HMS в вашем приложении.
3. Настроить интеграцию Huawei Push Service в веб-приложении Engagement.

> 📘
>
> Следуйте инструкциям в разделе [Huawei Mobile Services](https://documentation.bloomreach.com/engagement/docs/android-sdk-huawei).

### Запрос разрешения на уведомления

Начиная с Android 13 (уровень API 33), новое разрешение времени выполнения `POST_NOTIFICATIONS` должно быть зарегистрировано в вашем `AndroidManifest.xml` и также должно быть предоставлено пользователем для того, чтобы ваше приложение могло показывать push-уведомления.

SDK уже регистрирует разрешение `POST_NOTIFICATIONS`.

Диалог разрешения времени выполнения для запроса у пользователя предоставления разрешения должен быть запущен из вашего приложения. Вы можете использовать API SDK для этой цели:

```kotlin
Sendsay.requestPushAuthorization(requireContext()) { granted ->
    Logger.i(this, "Push notifications are allowed: $granted")
}
```

Поведение этого обратного вызова следующее:

* Для уровня Android API <33:
  * Разрешение не требуется, автоматически возвращает `true`.
* Для уровня Android API 33+:
  * Показывает диалог, возвращает решение пользователя (`true`/`false`).
  * В случае ранее предоставленного разрешения, не показывает диалог, возвращает `true`.

#### Требование разрешения на уведомления

На Android 13 и выше приложение может не иметь возможности отправлять обычные push-уведомления, даже если только что сгенерированный токен (при запуске приложения) действителен, если пользователь не предоставил разрешение на получение уведомлений.

Если ваш маркетинговый поток строго требует использования обычных push-уведомлений, настройте SDK для отслеживания только авторизованных push-токенов, установив [requirePushAuthorization](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) в `true`. SDK будет отслеживать push-токены только если пользователь предоставил разрешение на push-уведомления, иначе push-токен будет удален. Если вы оставите `requirePushAuthorization` со значением `false` (значение по умолчанию), SDK будет отслеживать push-токен независимо от разрешения пользователя. Эти токены могут использоваться только для отправки беззвучных push-уведомлений.

## Настройка

В этом разделе описаны настройки, которые вы можете реализовать после интеграции минимальной функциональности push-уведомлений.

### Настройка автоматического отслеживания push-уведомлений

По умолчанию SDK отслеживает push-уведомления автоматически. В [конфигурации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) вы можете установить желаемую частоту, используя свойство `tokenTrackFrequency` (значение по умолчанию `ON_TOKEN_CHANGE`). Вы также можете отключить автоматическое отслеживание push-уведомлений, установив логическое значение свойства `automaticPushNotification` в `false`.

Если включено `automaticPushNotification`, SDK будет отображать push-уведомления от Engagement и отслеживать событие "campaign" для каждого доставленного/открытого push-уведомления с соответствующими свойствами.

### Реагирование на push-уведомления

При [создании push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification) в веб-приложении Engagement вы можете выбрать из трех различных действий, которые будут выполнены при нажатии на уведомление или дополнительные кнопки, отображаемые с уведомлением.

#### Открыть приложение

Действие "Открыть приложение" генерирует intent с действием `com.sendsay.sdk.action.PUSH_CLICKED`. SDK автоматически реагирует на него, открывая активность запуска вашего приложения.

> ❗️
>
> Предыдущие версии SDK (<=2.9.7) требовали создания собственного broadcast receiver для обработки действия открытия, но поскольку notification trampolining (открытие активности из receiver'а или сервиса) больше не разрешено начиная с Android S, ваш intent активности будет открыт напрямую из уведомления, и этот receiver больше не нужен на стороне приложения.

#### Глубокая ссылка

Действие "Глубокая ссылка" создает intent "view", который содержит URL, указанный при настройке действия в Engagement. Чтобы отреагировать на этот intent, создайте intent filter для активности, которая его обрабатывает, в файле манифеста Android. Подробности см. в разделе [Создание глубоких ссылок на содержимое приложения](https://developer.android.com/training/app-links/deep-linking) в официальной документации Android.

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <!-- Принимает URI, которые начинаются с "my-schema://"-->
    <data android:scheme="my-schema" />
</intent-filter>
```

> ❗️
>
> Если intent глубокой ссылки отправляется в Activity, которая в данный момент активна, поведением по умолчанию является то, что intent доставляется в метод [onNewIntent](https://developer.android.com/reference/android/app/Activity#onNewIntent(android.content.Intent)), а не в [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)).

#### Открыть веб-браузер

Действие "Открыть веб-браузер" обрабатывается автоматически SDK, и от разработчика не требуется никакой работы для его обработки.

### Обработка дополнительной полезной нагрузки данных

При [создании push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification) в веб-приложении Engagement вы можете настроить его так, чтобы оно содержало дополнительные данные. При поступлении уведомления SDK вызовет `pushNotificationsDelegate`, который вы можете установить в объекте `Sendsay`.

``` kotlin
Sendsay.pushNotificationsDelegate = object : PushNotificationDelegate {
    override fun onSilentPushNotificationReceived(notificationData: Map<String, Any>) {
        // обработать дополнительные данные беззвучного push-уведомления
    }

    override fun onPushNotificationReceived(notificationData: Map<String, Any>) {
        // обработать дополнительные данные обычного push-уведомления
    }

    override fun onPushNotificationOpened(
        action: ExponeaNotificationActionType,
        url: String?,
        notificationData: Map<String, Any>
    ) {
        // обработать дополнительные данные действия нажатого push-уведомления
    }
}
```

Обратите внимание, что если SDK ранее получил какие-либо дополнительные данные, пока не был прикреплен слушатель к обратному вызову, он отправит все полученные и нажатые данные push-уведомлений, как только слушатель будет прикреплен.

> ❗️
>
> Использование `notificationDataCallback` теперь устарело. Пожалуйста, рассмотрите использование нового `pushNotificationsDelegate` с этими преимуществами:
> * несколько полученных push-уведомлений сохраняются до установки слушателя
> * действия нажатых push-уведомлений также доставляются слушателю (с функцией сохранения нескольких записей тоже)
> * вы можете определить, является ли полученное уведомление беззвучным или было показано пользователю

При [создании push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification) в веб-приложении Engagement вы можете настроить его так, чтобы оно содержало дополнительные данные. При поступлении уведомления SDK вызовет `notificationCallback`, который вы можете установить в объекте `Sendsay`. Дополнительные данные предоставляются как `Map<String, String>`.

``` kotlin
Sendsay.notificationDataCallback = {
     extra -> // обработать дополнительные данные
}
```

Обратите внимание, что если SDK ранее получил какие-либо дополнительные данные, пока не был прикреплен слушатель к обратному вызову, он отправит последние полученные данные push-уведомления, как только слушатель будет прикреплен.

> 👍
>
> Обратный вызов `Sendsay.notificationDataCallback` будет вызван после прикрепления слушателя (следующий запуск приложения) и инициализации SDK. Если вам нужно отреагировать на уведомление, полученное немедленно, реализуйте собственный `FirebaseMessagingService` и установите обратный вызов данных уведомления в функции `onMessageReceived` перед вызовом `Sendsay.handleRemoteMessage`.

> ❗️
>
> Поведение `trackDeliveredPush` может быть затронуто функцией согласия на отслеживание, которая в включенном режиме учитывает требование явного согласия на отслеживание. Подробнее читайте в [документации по согласию на отслеживание](./TRACKING_CONSENT.md).

### Пользовательская обработка действий уведомлений

Когда пользователь нажимает на уведомление или его кнопки, SDK автоматически выполняет настроенное действие (открыть приложение, браузер и т.д.). Если вам требуется дополнительная обработка при возникновении этого события, вы можете создать receiver для этой цели. SDK транслирует действия `com.sendsay.sdk.action.PUSH_CLICKED`, `com.sendsay.sdk.action.PUSH_DEEPLINK_CLICKED` и `com.sendsay.sdk.action.PUSH_URL_CLICKED`, и вы можете указать их в intent filter для реагирования на них.

Регистрация в `AndroidManifest.xml`:

``` xml
<receiver
    android:name="MyReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="com.sendsay.sdk.action.PUSH_CLICKED" />
        <action android:name="com.sendsay.sdk.action.PUSH_DEEPLINK_CLICKED" />  
        <action android:name="com.sendsay.sdk.action.PUSH_URL_CLICKED" />
    </intent-filter>
</receiver>
```

Класс Receiver:

```kotlin
class MyReceiver : BroadcastReceiver() {  
  
  // Реагировать на нажатие действия push
  override fun onReceive(context: Context, intent: Intent) {  
        // Извлечь данные push
        val data = intent.getParcelableExtra<NotificationData>(ExponeaExtras.EXTRA_DATA)  
        val actionInfo = intent.getSerializableExtra(ExponeaExtras.EXTRA_ACTION_INFO) as? NotificationAction  
        val customData = intent.getSerializableExtra(ExponeaExtras.EXTRA_CUSTOM_DATA) as Map<String, String>  
        // Обработать данные push по необходимости
    }  
}
```

### Беззвучные push-уведомления

Веб-приложение Engagement позволяет настроить беззвучные push-уведомления, которые не отображаются пользователю. SDK отслеживает событие `campaign`, когда доставляется беззвучное push-уведомление. Беззвучные push-уведомления не могут быть открыты, но если вы настроили дополнительные данные в полезной нагрузке, SDK вызовет `Sendsay.notificationDataCallback`, как описано в разделе [Обработка дополнительной полезной нагрузки данных](#обработка-дополнительной-полезной-нагрузки-данных).

### Звук уведомления push

Полученные push-уведомления, обрабатываемые `Sendsay.handleRemoteMessage()`, будут воспроизводить звук по умолчанию или настроенный звук при отображении уведомления.

Чтобы использовать звук по умолчанию для уведомления, оставьте пустым или введите `default` в качестве значения для `Media > Sound` в вашем сценарии push-уведомления в веб-приложении Engagement.
![Настройка звука для push-уведомления в Engagement](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/push-sound-config.png)

Чтобы использовать пользовательский звук для уведомления, вы должны создать звуковой файл, который [поддерживает Android](https://developer.android.com/media/platform/supported-formats#audio-formats). Включите звуковой файл в ваши 'raw' ресурсы Android.

Как только пользовательский звук будет размещен в вашем приложении, введите имя файла звукового файла в качестве значения для `Media > Sound` в вашем сценарии push-уведомления в веб-приложении Engagement. Убедитесь, что вы вводите точное имя файла (с учетом регистра) без расширения.

> ❗️
>
> Воспроизведение звука уведомления может быть затронуто важностью канала уведомлений, настройкой поведения уведомлений приложения пользователя или активным режимом "Не беспокоить". Подробнее читайте в [обзоре уведомлений](https://developer.android.com/develop/ui/views/notifications) в документации Android.

### Ручное отслеживание push-уведомлений

Если вы отключите [автоматическое отслеживание push-уведомлений](#настройка-автоматического-отслеживания-push-уведомлений) или если хотите отслеживать push-уведомления от других провайдеров, вы можете вручную отслеживать события, связанные с push-уведомлениями.

#### Отслеживание push-токена (FCM)

Используйте метод [`trackPushToken`](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking#track-token-manually) для ручного отслеживания FCM push-токена:

``` kotlin
Sendsay.trackPushToken(
        token = "382d4221-3441-44b7-a676-3eb5f515157f"
)
```

Вызов этого метода отследит push-токен немедленно, независимо от [конфигурации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) для `tokenTrackFrequency`.

#### Отслеживание доставленного push-уведомления

Используйте метод [`trackDeliveredPush`](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking#track-push-notification-delivery-manually) для ручного отслеживания доставленного push-уведомления:

```kotlin
// создать NotificationData из вашей push полезной нагрузки
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

> ❗️
>
> Поведение `trackDeliveredPush` может быть затронуто функцией согласия на отслеживание, which, когда включена, требует явного согласия на отслеживание. Подробнее читайте в [документации по согласию на отслеживание](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking-consent).

#### Отслеживание нажатого push-уведомления

Используйте метод [`trackClickedPush`](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking#track-push-notification-click-manually) для ручного отслеживания нажатого push-уведомления:

``` kotlin
// создать NotificationData из вашей push полезной нагрузки
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

> Поведение `trackClickedPush` может быть затронуто функцией согласия на отслеживание, которая, когда включена, требует явного согласия на отслеживание. Подробнее читайте в [документации по согласию на отслеживание](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking-consent).

### Пользовательская обработка данных push-уведомлений

Если предоставленный метод `Sendsay.handleRemoteMessage` не соответствует требованиям вашего приложения, или вы решили отключить автоматические push-уведомления, вы должны обрабатывать push-уведомления и обрабатывать их полезную нагрузку самостоятельно.

Полезная нагрузка уведомлений генерируется из (возможно сложных) сценариев на платформе Engagement и содержит все данные для платформ Android, iOS и web. Поэтому сама полезная нагрузка может быть сложной.

Полезная нагрузка уведомлений использует структуру данных JSON.

#### Пример полезной нагрузки

```json
{
    "notification_id": 123,
    "url": "https://example.com/main_action",
    "title": "Notification title",
    "action": "app|browser|deeplink|self-check",
    "message": "Notification message",
    "image": "https://example.com/image.jpg",
    "actions": [
        {"title": "Action 1", "action": "app|browser|deeplink", "url": "https://example.com/action1"}
    ],
    "sound": "default",
    "aps": {
        "alert": {"title": "Notification title", "body": "Notification message"},
        "mutable-content": 1
    },
    "attributes": {
        "event_type": "campaign",
        "campaign_id": "123456",
        "campaign_name": "Campaign name",
        "action_id": 1,
        "action_type": "mobile notification",
        "action_name": "Action 1",
        "campaign_policy": "policy",
        "consent_category": "General consent",
        "subject": "Subject",
        "language": "en",
        "platform": "ios|android",
        "sent_timestamp": 1631234567.89,
        "recipient": "user@example.com"
    },
    "url_params": {"param1": "value1", "param2": "value2"},
    "source": "xnpe_platform",
    "silent": false,
    "has_tracking_consent": true,
    "consent_category_tracking": "Tracking consent name"
}
```

## Устранение неполадок

> 📘
>
> Обратитесь к [FAQ по мобильным push-уведомлениям](https://support.bloomreach.com/hc/en-us/articles/18152713374877-Mobile-Push-Notifications-FAQ) в нашем портале поддержки для получения часто задаваемых вопросов, связанных с push-уведомлениями.

Если push-уведомления работают не так, как ожидается в вашем приложении, рассмотрите следующие частые проблемы и их возможные решения:

### Нажатие на push-уведомление не открывает приложение на устройствах Xiaomi Redmi

Xiaomi MIUI обрабатывает оптимизацию батареи по-своему, что иногда может повлиять на поведение push-уведомлений.

Если оптимизация батареи включена для устройств, работающих под управлением MIUI, это может привести к тому, что push-уведомления перестанут показываться или не будут работать после нажатия. К сожалению, мы ничего не можем сделать с нашей стороны, чтобы предотвратить это, но вы можете попробовать это для решения проблем:

- Отключите любые оптимизации батареи в `Настройки` > `Батарея и производительность`.
- Установите опцию "Без ограничений" в настройках экономии батареи для вашего приложения.
- И (вероятно) самое главное, отключите `Оптимизацию памяти и MIUI` в `Параметрах разработчика`.

### Токен push-уведомления отсутствует после анонимизации

Ваше приложение может использовать `Sendsay.anonymize()` как функцию выхода.

Имейте в виду, что вызов метода `anonymize` удалит токен push-уведомления из хранилища. Ваше приложение должно получить действительный токен вручную перед использованием любых функций push-уведомлений. Вы можете сделать это непосредственно after `anonymize` или до или после `identifyCustomer`, в зависимости от использования push-уведомлений.

> 📘
>
> Обратитесь к разделам [Firebase Cloud Messaging](https://documentation.bloomreach.com/engagement/docs/android-sdk-firebase) и [Huawai Mobile Services](https://documentation.bloomreach.com/engagement/docs/android-sdk-huawei) для получения информации о том, как получить действительный токен push-уведомления.

### Несколько профилей клиентов имеют один и тот же назначенный токен push-уведомления

Скорее всего, это происходит потому, что ваше приложение не вызывает [`anonymize`](https://documentation.bloomreach.com/engagement/docs/android-sdk-tracking#anonymize), когда пользователь выходит из системы.

Важно вызывать `anonymize`, когда пользователь выходит из вашего приложения, чтобы убедиться, что токен push-уведомления удален из профиля этого пользователя. Токен будет назначен пользователю, который войдет в систему следующим. Если ваше приложение не вызывает `anonymize` при выходе одного пользователя и входе другого пользователя, оба пользователя будут иметь один и тот же токен в своих профилях.

### События нажатия push-уведомлений слишком редки в продакшене (низкая конверсия)

Android 8 (уровень API 26) представил концепции "канал уведомлений" и "уровень важности". Уровень важности определяет визуальное и звуковое поведение уведомлений канала, как [определено системой Android](https://developer.android.com/develop/ui/views/notifications/channels#importance).

Разработчики устанавливают уровень важности по умолчанию при создании канала уведомлений. Однако пользователи имеют возможность настроить, насколько видимым и навязчивым является канал, переопределяя его важность по умолчанию. Это может привести к тому, что уведомления вообще не будут показаны пользователю, даже если вы установили важность канала по умолчанию в `IMPORTANCE_MAX`.

Если пользователь переопределяет важность канала уведомлений с `IMPORTANCE_NONE`, уведомления не будут показаны. Это означает, что событие "campaign" с `status=clicked` не будет отслежено, поскольку на уведомление нельзя нажать или взаимодействовать с ним.

SDK не может этого избежать. Однако он все равно будет отслеживать событие "campaign" с `status=delivered` и следующими дополнительными свойствами, которые могут быть полезны при анализе и фильтрации:

* **state** содержит значение `shown`, если уведомление было успешно показано клиенту. Если нет, значение `not_shown`.
* **notification_importance** содержит читаемое значение текущей важности канала уведомлений. Возможные значения:
  * **importance_none** отражает значение `NotificationManager.IMPORTANCE_NONE`.
  * **importance_min** отражает значение `NotificationManager.IMPORTANCE_MIN`.
  * **importance_low** отражает значение `NotificationManager.IMPORTANCE_LOW`.
  * **importance_default** отражает значение `NotificationManager.IMPORTANCE_DEFAULT`.
  * **importance_high** отражает значение `NotificationManager.IMPORTANCE_HIGH`.
  * **importance_max** отражает значение `NotificationManager.IMPORTANCE_MAX`.
  * **importance_unspecified** отражает значение `NotificationManager.IMPORTANCE_UNSPECIFIED`.
  * **importance_unknown** отслеживается в случае, если канал уведомлений не может быть найден из-за неправильной регистрации или старой версии Android, которая не поддерживает каналы уведомлений.
  * **importance_unsupported** отслеживается в случае, если канал уведомлений существует, но функция `importance` не поддерживается Android (до уровня API 26).