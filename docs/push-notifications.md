# Push-уведомления

CDP Sendsay позволяет отправлять push-уведомления пользователям вашего мобильного приложения. SDK принимает push-сообщение, обрабатывает его и отображает уведомление на устройстве клиента.

Помимо обычных push-уведомлений, поддерживаются **беззвучные push-уведомления**, которые используются для фоновых задач или обновления интерфейса без взаимодействия с пользователем.


## Интеграция

Ниже перечислены базовые шаги, необходимые для включения push-уведомлений.

### Стандартная интеграция (Firebase)

Чтобы отправлять push-уведомления на Android-устройства:

1. Настройте проект Firebase.
2. Реализуйте Firebase messaging в приложении.
3. Подключите интеграцию **Firebase Cloud Messaging** в настройках мобильного приложения в личном кабинете Sendsay.


### Интеграция с Huawei

Для устройств, использующих **Huawei Mobile Services (HMS)**:

1. Настройте Huawei Mobile Services (HMS).
2. Реализуйте HMS в приложении.
3. Подключите интеграцию **Huawei Push Service** в настройках мобильного приложения в личном кабинете Sendsay.


### Запрос разрешения на уведомления

Начиная с Android 13 (API 33), необходимо запрашивать разрешение времени выполнения `POST_NOTIFICATIONS` и регистрировать его в `AndroidManifest.xml`. Без этого приложение не может показывать push-уведомления.

Диалог разрешения времени выполнения должен быть запущен из приложения. Вы можете использовать API SDK для этой цели:

```kotlin
Sendsay.requestPushAuthorization(requireContext()) { granted ->
    Logger.i(this, "Push notifications are allowed: $granted")
}
```

Логика поведения вызова:

* Для Android API <33:
  * Разрешение не требуется, всегда возвращается `true`.
* Для Android API 33+:
  * Если разрешение уже выдано > возвращает `true`, без показа диалога.
  * Если нет > показывает системный диалог и возвращает решение пользователя.

#### Требование разрешения на уведомления

Некоторые сценарии требуют отправлять только **обычные push-уведомления**. На Android 13+ такие уведомления можно показать только если пользователь дал согласие.

Если ваш маркетинговый сценарий требует отправлять только обычные push-уведомления, настройте SDK для отслеживания только авторизованных push-токенов: установите `requirePushAuthorization` = `true` в конфигурации SDK. Так push-токены будут отслеживать только если пользователь предоставил разрешение на push-уведомления, иначе push-токен будет удален.

Если оставить значение false (по умолчанию), SDK будет отслеживать push-токен даже без разрешения — но такие токены подходят **только для беззвучных push-уведомлений**.

## Настройка

После базовой интеграции можно настроить дополнительные параметры поведения push-уведомлений.

### Настройка автоматического отслеживания push-уведомлений

По умолчанию SDK:
- автоматически отслеживает push-токены,
- отображает полученные уведомления,
- отслеживает событие `campaign` при доставке и открытии уведомления.

Отслеживанием можно управлять с помощью свойств:

- `tokenTrackFrequency` — выбирает частоту отслеживания токена (`ON_TOKEN_CHANGE` по умолчанию),
- `automaticPushNotification` — включает или выключает (по умолчанию — `true`) автоматическую обработку push-уведомлений.

### Реагирование на push-уведомления

При создании push-уведомления вы можете выбрать действие, выполняемое при нажатии:

#### 1. Открыть приложение

Действие генерирует intent `com.sendsay.sdk.action.PUSH_CLICKED`, а SDK автоматически реагирует на него, открывая приложение.

#### 2. Deep Link

Действие создаёт intent `view` c URL, который указывается при настройке рассылки. 

Чтобы отреагировать на intent, создайте intent filter в файле манифеста Android:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <!-- Принимает URI, которые начинаются с "my-schema://"-->
    <data android:scheme="my-schema" />
</intent-filter>
```
Подробнее — в разделе [Создание глубоких ссылок на содержимое приложения](https://developer.android.com/training/app-links/deep-linking) официальной документации Android.

> ❗️
>
> Если Activity уже активна, deep link попадёт в метод [onNewIntent](https://developer.android.com/reference/android/app/Activity#onNewIntent(android.content.Intent)), а не в [onCreate](https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle)).

#### 3. Открыть веб-браузер

Действие обрабатывается SDK автоматически, дополнительного кода не требуется.

### Обработка дополнительной полезной нагрузки данных

Вы можете добавить в рассылку дополнительные данные: при поступлении уведомления SDK вызовет `pushNotificationsDelegate`, который вы можете установить в объекте `Sendsay`.

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
SDK автоматически отправит все полученные ранее данные, если делегат был прикреплён позже.

### Пользовательская обработка действий уведомлений

Когда пользователь нажимает на push-уведомление или одну из его кнопок, SDK автоматически выполняет предустановленное действие — открывает приложение, deep link или браузер, в зависимости от настроек вашей push-рассылки.

Если вам требуется выполнить дополнительную обработку при нажатии, вы можете создать собственный **receiver**. 

SDK транслирует действия: 
- `com.sendsay.sdk.action.PUSH_CLICKED`, 
- `com.sendsay.sdk.action.PUSH_DEEPLINK_CLICKED`,
- `com.sendsay.sdk.action.PUSH_URL_CLICKED`. 

Укажите эти действия в *intent-filter*, чтобы ваш **receiver** получил уведомление о клике.

#### Регистрация в AndroidManifest.xml

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

#### Пример класса receiver

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

CDP Sendsay позволяет отправлять **беззвучные push-уведомления** — это push-сообщения, которые **не отображаются пользователю**. Они предназначены для выполнения фоновых задач.

SDK отслеживает событие `campaign` для каждого доставленного беззвучного push-уведомления. В отличие от обычных уведомлений, беззвучные push-уведомления нельзя открыть, но если в беззвучном пуше присутствуют дополнительные данные в полезной нагрузке, SDK вызовет `Sendsay.pushNotificationsDelegate`, как описано в разделе [Обработка дополнительной полезной нагрузки данных](#обработка-дополнительной-полезной-нагрузки-данных).

### Звук push-уведомления

Push-уведомления, которые проходят через `SendsayNotificationService`.process(), могут воспроизводить стандартный звук или ваш собственный звуковой файл.

Чтобы использовать стандартный звук уведомлений оставьте поле звука пустым или укажите значение `default`.

Чтобы использовать настраиваемый звук:
1. Создайте звуковой файл в формате, который [поддерживает Android](https://developer.android.com/media/platform/supported-formats#audio-formats). 
2. Включите звуковой файл в ваши `raw` ресурсы Android.
3. При создании push-рассылки укажите имя файла (с учётом регистра, **без расширения**).

> ❗️
>
> Воспроизведение звука уведомления зависит не только от настроек push-рассылки. На него также могут повлиять:
> - важность канала уведомлений,
> - индивидуальные настройки уведомлений для вашего приложения в системе,
> - активный режим «Не беспокоить»,
> Подробнее — в [обзоре уведомлений](https://developer.android.com/develop/ui/views/notifications) документации Android. 

### Пользовательская обработка данных push-уведомлений

Если метод `Sendsay.handleRemoteMessage` не подходит под требования вашего приложения или автоматическая обработка push-уведомлений отключена, вы можете обрабатывать уведомления вручную.

Полезная нагрузка push-уведомлений формируется CDP Sendsay и может содержать данные для платформ Android, iOS и Web. Поэтому структура полезной нагрузки может быть сложной. Формат данных представлен в виде JSON-объекта.

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