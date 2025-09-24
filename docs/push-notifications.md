# Push-уведомления

CDP Sendsay позволяет отправлять push-уведомления пользователям вашего приложения. Мобильное приложение обрабатывает push-сообщение с помощью SDK и отображает уведомление на устройстве клиента.

Push-уведомления также могут быть беззвучными, используемыми только для обновления интерфейса приложения или запуска какой-либо фоновой задачи.


## Интеграция

В этом разделе описаны шаги для добавления минимальной функциональности push-уведомлений в ваше приложение.

### Стандартная интеграция (Firebase)

Чтобы иметь возможность отправлять push-уведомления и получать их в вашем приложении на Android-устройствах, необходимо:

1. Настроить проект Firebase.
2. Реализовать Firebase messaging в вашем приложении.
3. Настроить интеграцию Firebase Cloud Messaging в настройках мобильного приложения в личном кабинете Sendsay.


### Интеграция с Huawei

Чтобы иметь возможность отправлять push-уведомления и получать их в вашем приложении на устройствах Huawei, необходимо:

1. Настроить Huawei Mobile Services (HMS)
2. Реализовать HMS в вашем приложении.
3. Настроить интеграцию Huawei Push Service  в настройках мобильного приложения в личном кабинете Sendsay.


### Запрос разрешения на уведомления

Начиная с Android 13 (уровень API 33), новое разрешение времени выполнения `POST_NOTIFICATIONS` должно быть зарегистрировано в вашем `AndroidManifest.xml` и также должно быть предоставлено пользователем для того, чтобы ваше приложение могло показывать push-уведомления.

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

Если вам необходимы только обычные push-уведомления настройте SDK для отслеживания только авторизованных push-токенов, установив `requirePushAuthorization` в `true`. SDK будет отслеживать push-токены только если пользователь предоставил разрешение на push-уведомления, иначе push-токен будет удален. Если вы оставите `requirePushAuthorization` со значением `false` (значение по умолчанию), SDK будет отслеживать push-токен независимо от разрешения пользователя. Эти токены могут использоваться только для отправки беззвучных push-уведомлений.

## Настройка

В этом разделе описаны настройки, которые вы можете реализовать после интеграции минимальной функциональности push-уведомлений.

### Настройка автоматического отслеживания push-уведомлений

По умолчанию SDK отслеживает push-уведомления автоматически. В конфигурации SDK вы можете установить желаемую частоту, используя свойство `tokenTrackFrequency` (значение по умолчанию `ON_TOKEN_CHANGE`). Вы также можете отключить автоматическое отслеживание push-уведомлений, установив логическое значение свойства `automaticPushNotification` в `false`.

Если включено `automaticPushNotification`, SDK будет отображать push-уведомления и отслеживать событие "campaign" для каждого доставленного/открытого push-уведомления с соответствующими свойствами.

### Реагирование на push-уведомления

При создании push-рассылки вы можете выбрать одно из трех различных действий, которые будут выполнены при нажатии на уведомление или дополнительные кнопки, отображаемые с уведомлением.

#### Открыть приложение

Действие "Открыть приложение" генерирует intent с действием `com.sendsay.sdk.action.PUSH_CLICKED`. SDK автоматически реагирует на него, открывая ваше приложение.

#### Deep Link

Действие "Deep Link" создает intent "view", который содержит URL, указанный при настройке push-рассылки. Чтобы отреагировать на этот intent, создайте intent filter для активности, которая его обрабатывает, в файле манифеста Android. Подробности см. в разделе [Создание глубоких ссылок на содержимое приложения](https://developer.android.com/training/app-links/deep-linking) в официальной документации Android.

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
> Если intent deep link отправляется в Activity, которая в данный момент активна, поведением по умолчанию является то, что intent доставляется в метод [onNewIntent](https://developer.android.com/reference/android/app/Activity#onNewIntent(android.content.Intent)), а не в [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)).

#### Открыть веб-браузер

Действие "Открыть веб-браузер" обрабатывается автоматически SDK, и от разработчика не требуется никакой работы для его обработки.

### Обработка дополнительной полезной нагрузки данных

При создании push-рассылки вы можете настроить ее так, чтобы она содержала дополнительные данные. При поступлении уведомления SDK вызовет `pushNotificationsDelegate`, который вы можете установить в объекте `Sendsay`.

``` kotlin
Sendsay.pushNotificationsDelegate = object : PushNotificationDelegate {
    override fun onSilentPushNotificationReceived(notificationData: Map<String, Any>) {
        // обработать дополнительные данные беззвучного push-уведомления
    }

    override fun onPushNotificationReceived(notificationData: Map<String, Any>) {
        // обработать дополнительные данные обычного push-уведомления
    }

    override fun onPushNotificationOpened(
        action: SendsayNotificationActionType,
        url: String?,
        notificationData: Map<String, Any>
    ) {
        // обработать дополнительные данные действия нажатого push-уведомления
    }
}
```

Обратите внимание, что если SDK ранее получил какие-либо дополнительные данные, пока не был прикреплен слушатель к обратному вызову, он отправит все полученные и нажатые данные push-уведомлений, как только слушатель будет прикреплен.

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
        val data = intent.getParcelableExtra<NotificationData>(SendsayExtras.EXTRA_DATA)  
        val actionInfo = intent.getSerializableExtra(SendsayExtras.EXTRA_ACTION_INFO) as? NotificationAction  
        val customData = intent.getSerializableExtra(SendsayExtras.EXTRA_CUSTOM_DATA) as Map<String, String>  
        // Обработать данные push по необходимости
    }  
}
```

### Беззвучные push-уведомления

CDP Sendsay позволяет отправить беззвучные push-уведомления, которые не отображаются пользователю. SDK отслеживает событие `campaign`, когда доставляется беззвучное push-уведомление. Беззвучные push-уведомления не могут быть открыты, но если вы настроили дополнительные данные в полезной нагрузке, SDK вызовет `Sendsay.pushNotificationsDelegate`, как описано в разделе [Обработка дополнительной полезной нагрузки данных](#обработка-дополнительной-полезной-нагрузки-данных).

### Звук уведомления push

Полученные push-уведомления, обрабатываемые `Sendsay.handleRemoteMessage()`, будут воспроизводить звук по умолчанию или настроенный звук при отображении уведомления.

Чтобы использовать звук по умолчанию для уведомления, оставьте пустым или введите `default`.

Чтобы использовать пользовательский звук для уведомления, вы должны создать звуковой файл, который [поддерживает Android](https://developer.android.com/media/platform/supported-formats#audio-formats). Включите звуковой файл в ваши 'raw' ресурсы Android.

Как только пользовательский звук будет размещен в вашем приложении, используйте имя файла звукового файла в качестве значения в настройках вашей push-рассылки. Убедитесь, что вы вводите точное имя файла (с учетом регистра) без расширения.

> ❗️
>
> Воспроизведение звука уведомления может быть затронуто важностью канала уведомлений, настройкой поведения уведомлений приложения пользователя или активным режимом "Не беспокоить". Подробнее читайте в [обзоре уведомлений](https://developer.android.com/develop/ui/views/notifications) в документации Android.


### Пользовательская обработка данных push-уведомлений

Если предоставленный метод `Sendsay.handleRemoteMessage` не соответствует требованиям вашего приложения, или вы решили отключить автоматические push-уведомления, вы должны обрабатывать push-уведомления и обрабатывать их полезную нагрузку самостоятельно.

Полезная нагрузка уведомлений генерируется Sendsay и содержит все данные для платформ Android, iOS и web. Поэтому сама полезная нагрузка может быть сложной.

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