# Android App Links

Включение и отслеживание Android App Links в вашем приложении с помощью Android SDK

Android App Links (иногда называемые Universal Links) позволяют ссылкам, которые вы отправляете клиенту, открываться непосредственно в вашем мобильном приложении без каких-либо перенаправлений, которые могут помешать пользовательскому опыту.

Подробности о том, как работают Android App Links и как они могут улучшить пользовательский опыт, см. в разделе [Universal Links](https://documentation.bloomreach.com/engagement/docs/universal-link) в документации по кампаниям.

Эта страница описывает шаги, необходимые для поддержки и отслеживания входящих Android App Links в вашем приложении с помощью Android SDK.

## Включение Android App Links

Чтобы поддерживать Android App Links в вашем приложении, необходимо создать двустороннюю связь между вашим приложением и веб-сайтом и указать URL-адреса, которые обрабатывает ваше приложение. Для этого необходимо добавить intent filter в манифест Android вашего приложения и разместить файл Digital Asset Link JSON на вашем домене.

### Добавление intent filter в манифест Android

[App Links Assistant в Android Studio](https://developer.android.com/studio/write/app-link-indexing.html#intent) может помочь вам создать intent filters в вашем манифесте и сопоставить существующие URL-адреса с вашего веб-сайта с активностями в вашем приложении. App Links Assistant также добавляет шаблонный код в каждую соответствующую активность для обработки intent.

Альтернативно, вы можете настроить это вручную, следуя инструкциям в разделе [Проверка Android App Links](https://developer.android.com/training/app-links/verify-android-applinks) в официальной документации Android.

Убедитесь, что intent filter содержит атрибут `android:autoVerify="true"`, чтобы сигнализировать системе Android, что она должна проверить ваш Digital Asset Link JSON и автоматически обрабатывать App Links.

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

### Добавление Digital Asset Link JSON на ваш домен

Необходимо объявить связь между вашим веб-сайтом и intent filters, разместив файл [Digital Asset Links](https://developers.google.com/digital-asset-links/v1/getting-started) JSON в следующем местоположении:

```
https://domain.name/.well-known/assetlinks.json
```

Опять же, [App Links Assistant в Android Studio](https://developer.android.com/studio/write/app-link-indexing.html#associatesite) может помочь сгенерировать файл для вас.

Альтернативно, вы можете сделать это вручную, следуя инструкциям в разделе [Объявление связей веб-сайта](https://developer.android.com/training/app-links/verify-android-applinks#web-assoc) в официальной документации Android.

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

## Отслеживание Android App Links

SDK может автоматически определить, является ли intent, который открыл ваше приложение, App Link. Все, что требуется, это вызов `Sendsay.handleCampaignIntent(intent, applicationContext)`.

Параметры App Link автоматически отслеживаются в событиях `session_start`, когда для данного клика Universal Link запускается новая сессия. Если ваше приложение запускает новую сессию, параметры кампании (`utm_source`, `utm_campaign`, `utm_content`, `utm_medium`, `utm_term` и `xnpe_cmp`) отправляются в параметрах сессии, чтобы вы могли приписать новую сессию клику App Link.

Если App Link содержит параметр `xnpe_cmp`, то отслеживается дополнительное событие `campaign`. Параметр `xnpe_cmp` представляет идентификатор кампании, обычно генерируемый для кампаний Email или SMS.

Чтобы отслеживать события сессии с параметрами App Link, необходимо вызвать `Sendsay.handleCampaignIntent` **до** того, как будет вызван метод `onResume` вашей Activity. В идеале сделайте вызов в методе `.onCreate` вашей MainActivity.

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
> Обратите внимание, что `handleCampaignIntent` заботится только об отслеживании. Вы все еще должны прочитать данные из intent и использовать их для определения соответствующего содержимого приложения для отображения. Это выходит за рамки SDK.