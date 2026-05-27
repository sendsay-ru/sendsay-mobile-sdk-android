# Android App Links

Включение и отслеживание Android App Links в вашем приложении с помощью Android SDK

Android App Links позволяют ссылкам, которые вы отправляете через CDP Sendsay, открываться напрямую в вашем мобильном приложении без каких-либо перенаправлений, которые могут ухудшить пользовательский опыт.

Ниже описаны шаги, необходимые для поддержки и отслеживания App Links в вашем приложении с помощью Android SDK Sendsay.

> 📘
>
> Подробнее о том, как работают App Links и зачем они нужны, смотрите в разделе [Universal Links](app-links.md) документации.

## Включение Android App Links

Для поддержки App Links необходимо создать двустороннюю связь между вашим приложением и вашим доменом, а также указать URL-адреса, которые приложение будет обрабатывать.

Для этого нужно:
1. Добавить `intent-filter` в `AndroidManifest.xml`.
2. Разместить файл **Digital Asset Links** JSON на вашем веб-сайте.

### Добавление intent filter в манифест Android

Вы можете использовать [App Links Assistant в Android Studio](https://developer.android.com/studio/write/app-link-indexing.html#intent), который поможет: 
- добавить `intent-filter`,
- сопоставить URL-адреса сайта с активностями в приложении,
- сгенерировать обработчик `intent`.

Вы можете настроить это вручную, следуя инструкциям в разделе [Проверка Android App Links](https://developer.android.com/training/app-links/verify-android-applinks) в официальной документации Android.

Убедитесь, что ваш `intent filter` содержит атрибут: 
```xml
android:autoVerify="true"
``` 
Этот атрибут сообщает Android, что система должна проверить ваш JSON-файл **Digital Asset Links** и автоматически обрабатывать App Links.

Пример:

```xml
<activity ...>

    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="www.your-domain.name" />
    </intent-filter>

</activity>
```

### Размещение файла Digital Asset Link JSON на сайте

Файл [Digital Asset Links](https://developers.google.com/digital-asset-links/v1/getting-started) JSON должен находиться по адресу:

```
https://domain.name/.well-known/assetlinks.json
```

[App Links Assistant](https://developer.android.com/studio/write/app-link-indexing.html#associatesite) в Android Studio может сгенерировать файл автоматически, либо вы можете создать его вручную, следуя инструкции: [Объявление связей веб-сайта](https://developer.android.com/training/app-links/verify-android-applinks#web-assoc) в официальной документации Android.

Пример:

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "your.package.name",
    "sha256_cert_fingerprints":["SHA256 fingerprint of your app's signing certificate"]
  }
}]
```
После добавления `intent filter` и размещения файла, App Links должны открывать ваше приложение.

> 👍
>
> Самый простой способ протестировать App Links — отправить себе письмо или сообщение со ссылкой и открыть её в браузере на устройстве. 

## Отслеживание Android App Links

Когда приложение открывается через App Link, Android передаёт его через `intent`. Android SDK Sendsay может автоматически определить, является ли входящий `intent` App Link, и корректно отследить параметры кампании.

Чтобы это работало, вызовите:
```
Sendsay.handleCampaignIntent(intent, applicationContext)
```

### Важные моменты отслеживания

- Параметры App Link автоматически включаются в событие `session_start`, если клик по ссылке открывает новую сессию.
- Параметры кампании (`utm_source`, `utm_campaign`, `utm_content`, `utm_medium`, `utm_term`, `xnpe_cmp`) будут переданы в CDP Sendsay как часть параметров сессии.
- Если App Link содержит параметр `xnpe_cmp` (идентификатор, обычно генерируется для Email или SMS кампаний), дополнительно отслеживается событие `campaign`.

### Где вызывать handleCampaignIntent

Чтобы отслеживать события сессии с параметрами App Link, необходимо вызвать `Sendsay.handleCampaignIntent` **до** того, как будет вызван метод `onResume` вашей Activity. 

Рекомендуемое место — метод `.onCreate` вашей MainActivity.

Пример:

```kotlin
 class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sendsay.handleCampaignIntent(intent, applicationContext)
    }
}
```

> 👍
>
> `handleCampaignIntent` отвечает только за отслеживание. Логика навигации — что именно открыть в приложении — остаётся на стороне приложения и должна быть реализована отдельно.