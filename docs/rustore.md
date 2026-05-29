# RuStore Push Service

Интеграция RuStore Push Service позволяет вашему Android-приложению получать [push-уведомления](push-notifications.md) с платформы CDP Sendsay на устройствах, где доступна доставка push-уведомлений через RuStore.

> 📘
>
> RuStore Push Service может использоваться как отдельный push-провайдер или как дополнительный канал доставки для Android-устройств, на которых Firebase Cloud Messaging (FCM) или Huawei Mobile Services (HMS) недоступны либо не используются.

Чтобы иметь возможность отправлять пуши с платформы CDP Sendsay и получать их в приложении через RuStore, необходимо:
- настроить проект RuStore Push в консоли разработчика RuStore;
- подключить RuStore Push SDK в Android-приложение;
- реализовать `RuStoreMessagingService` в приложении;
- настроить интеграцию RuStore Push Service в веб-приложении CDP Sendsay.

> 👍
>
> SDK поддерживает самопроверку настройки push: она отследит push-токен и запросит у CDP Sendsay отправку тихого пуша на устройство, для подтверждения его готовности принимать уведомления.
>
> Чтобы включить самопроверку, установите `Sendsay.checkPushSetup = true` **перед** [инициализацией SDK](setup.md#инициализация-sdk).
>
> Рекомендуем включать самопроверку при первой реализации push-уведомлений или для диагностики.

## Настройка RuStore Push Service

Для настройки RuStore Push Service:

1. Зарегистрируйте и настройте аккаунт разработчика в [RuStore Console](https://console.rustore.ru/).
2. Добавьте приложение в консоли RuStore.
3. Перейдите в раздел **Push-уведомления** для созданного приложения.
4. Создайте push-проект.
5. Укажите пакет приложения и SHA-256 отпечаток подписи приложения.
6. Создайте **сервисный токен** для отправки push-уведомлений через RuStore.
7. Скопируйте этот **сервисный токен** и **ID проекта** — они понадобятся для интеграции в веб-приложении CDP Sendsay.

> 📘
>
> SHA-256 отпечаток подписи можно получить командой `./gradlew signingReport` в корневой папке Android-проекта.

#### Чек-лист настройки RuStore:
- [ ] Приложение добавлено в RuStore Console.
- [ ] В RuStore-проекте указан корректный `applicationId` приложения.
- [ ] SHA-256 отпечаток подписи совпадает с подписью APK/AAB, который используется для установки приложения.
- [ ] Создан сервисный токен для отправки push-уведомлений.
- [ ] RuStore установлен на тестовом устройстве.
- [ ] Пользователь авторизован в RuStore.
- [ ] Для RuStore разрешена фоновая работа на устройстве.

## Подключение RuStore Push SDK

Добавьте репозиторий RuStore SDK в настройки Gradle проекта:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://artifactory-external.vkpartner.ru/artifactory/maven")
    }
}
```

Добавьте зависимость RuStore Push SDK в модуль приложения:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("ru.rustore.sdk:pushclient:x.y.z")
}
```

> 📘
>
> Актуальную версию зависимости смотрите в официальной документации RuStore Push SDK.

## Инициализация RuStore Push SDK

Для автоматической инициализации `RuStorePushClient` пропишите ваш **project_id** в `AndroidManifest.xml` вашего приложения.
И зарегистрируйте `Application` в `AndroidManifest.xml`, если это ещё не сделано:

```xml
<application
    android:name=".SendsayApp">
    ...
   <meta-data
           android:name="ru.rustore.sdk.pushclient.project_id"
           android:value="YOUR_RUSTORE_PROJECT_ID" />
</application>
```

Или вручную инициализируйте `RuStorePushClient` в `Application.onCreate()`.

```kotlin
import android.app.Application
import ru.rustore.sdk.pushclient.RuStorePushClient

class SendsayApp : Application() {
    override fun onCreate() {
        super.onCreate()

        RuStorePushClient.init(
            application = this,
            projectId = "YOUR_RUSTORE_PROJECT_ID"
        )
    }
}
```

> ❗️
>
> RuStore Push SDK не поддерживает одновременную работу в нескольких процессах. Если приложение использует несколько процессов — инициализируйте SDK только в главном.

## Реализация RuStore Messaging Service в приложении

SDK не включает собственную реализацию `RuStoreMessagingService`, поэтому вы должны добавить её в приложение вручную. Это необходимо для автоматического отслеживания push-токенов и обработки входящих push-уведомлений.

> 👍
>
> Эта реализация не включена в SDK, чтобы сохранить его как можно меньшим и избежать включения библиотек, которые не являются существенными для его функциональности. Вы можете скопировать приведённый ниже пример кода и использовать его в своём приложении.

### 1. Создайте сервис RuStore

   ```kotlin
   import android.app.NotificationManager
   import android.content.Context
   import com.sendsay.sdk.Sendsay
   import ru.rustore.sdk.pushclient.common.exception.RuStorePushClientException
   import ru.rustore.sdk.pushclient.messaging.model.RemoteMessage
   import ru.rustore.sdk.pushclient.messaging.service.RuStoreMessagingService
   
   class MyRuStoreMessagingService : RuStoreMessagingService() {
   
       private val notificationManager by lazy {
           getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
       }
   
       override fun onMessageReceived(message: RemoteMessage) {
           super.onMessageReceived(message)
           if (!Sendsay.handleRemoteMessage(applicationContext, message.data, notificationManager)) {
               // push-уведомление от другого провайдера push-уведомлений
           }
       }
   
       override fun onNewToken(token: String) {
           super.onNewToken(token)
           Sendsay.handleNewRuStoreToken(applicationContext, token)
       }
   
       override fun onDeletedMessages() {
           super.onDeletedMessages()
           // один или несколько push не были доставлены на устройство;
           // при необходимости синхронизируйте состояние приложения с сервером
       }
   
       override fun onError(errors: List<RuStorePushClientException>) {
           super.onError(errors)
           // обработайте ошибки RuStore Push SDK при необходимости
       }
   }
   ```

> ❗️
>
> Проверьте название метода для передачи RuStore-токена в вашей версии Sendsay SDK. 
> Если метод `Sendsay.handleNewRuStoreToken(...)` отсутствует, используйте актуальный метод SDK для регистрации RuStore push-токена или обновите SDK до версии с поддержкой RuStore.
>
>> А также можете обратиться к примеру реализации, находящимся в пакете [**example/app**](../app/src/rsm/java/com/sendsay/example/services/SendsayRsmMessageService.kt)

### 2. Зарегистрируйте сервис в AndroidManifest.xml

```xml
<service
    android:name=".MyRuStoreMessagingService"
    android:exported="true"
    tools:ignore="ExportedService">
    <intent-filter>
        <action android:name="ru.rustore.sdk.pushclient.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

> ❗️
>
> Для сервиса RuStore в манифесте должен быть указан `android:exported="true"`. Если используется атрибут `tools:ignore`, убедитесь, что в корневом теге `manifest` подключён namespace `xmlns:tools="http://schemas.android.com/tools"`.

После этого SDK будет автоматически обрабатывать push-уведомления, отправленные с платформы CDP Sendsay. Дополнительно доступен вспомогательный метод: `Sendsay.isSendsayPushNotification()`.

### Проверка получения push-токена

Если всё настроено корректно:

- при запуске приложения RuStore Push SDK получает push-токен;
- приложение передаёт RuStore push-токен в Sendsay SDK;
- в самопроверке отображается успешное получение токена;
- в профиле клиента в веб-приложении CDP Sendsay появилось свойство `rustore_push_notification_id`.

Push-токен создаётся и обновляется согласно правилам RuStore Push SDK. Полный список условий и ошибок смотрите в [документации RuStore Push SDK](https://www.rustore.ru/help/sdk/push-notifications/kotlin-java/).

### Разрешение на уведомления в Android 13+

Начиная с Android 13 (API 33):
1. Разрешение на уведомления должно быть зарегистрировано в вашем `AndroidManifest.xml`.
2. Пользователь должен явно предоставить разрешение.
3. SDK регистрирует разрешение, но запросить его должно ваше приложение вызвав:

```
Sendsay.requestPushAuthorization(context)
```

Подробнее — в разделе [Запрос разрешения на уведомления](push-notifications.md#запрос-разрешения-на-уведомления) документации CDP Sendsay.

Если ваш маркетинговый сценарий требует отправлять только обычные push-уведомления, настройте SDK для отслеживания только авторизованных push-токенов: установите [requirePushAuthorization](push-notifications.md#запрос-разрешения-на-уведомления) = `true` в конфигурации SDK.

Подробнее — в разделе [Требование разрешения на уведомления](push-notifications.md#запрос-разрешения-на-уведомления) документации CDP Sendsay.

### Если RuStoreMessagingService не вызывается

> ❗️
>
> Если вы интегрируете SDK в существующий проект, вы можете столкнуться с проблемой, при которой `RuStoreMessagingService` не вызывается автоматически.

Проверьте, что:

- приложение установлено на устройство с тем же `applicationId`, который указан в RuStore Console;
- подпись установленного приложения совпадает с SHA-256 отпечатком в RuStore Console;
- RuStore установлен на устройстве;
- пользователь авторизован в RuStore;
- приложению RuStore разрешена фоновая работа;
- сервис зарегистрирован в `AndroidManifest.xml` с action `ru.rustore.sdk.pushclient.MESSAGING_EVENT`;
- `RuStorePushClient` инициализирован до запроса токена.

Если токен не приходит автоматически, запросите его вручную после инициализации RuStore Push SDK:

```kotlin
import android.app.Application
import com.sendsay.sdk.Sendsay
import ru.rustore.sdk.pushclient.RuStorePushClient

class SendsayApp : Application() {
    override fun onCreate() {
        super.onCreate()

        RuStorePushClient.init(
            application = this,
            projectId = "YOUR_RUSTORE_PROJECT_ID"
        )

        RuStorePushClient.getToken()
            .addOnSuccessListener { token ->
                Sendsay.handleNewRuStoreToken(applicationContext, token)
            }
            .addOnFailureListener {
                // обработайте ошибку получения токена при необходимости
            }
    }
}
```

### Использование методов до инициализации SDK

> ❗️
>
> Методы обработки RuStore push-токена и `Sendsay.handleRemoteMessage` можно вызывать до инициализации SDK, если она уже выполнялась ранее.
> В этом случае данные будут отправлены с конфигурацией последней инициализации.
>
> Чтобы избежать неактуальных конфигураций — инициализируйте SDK в `Application.onCreate()`.

## Настройка интеграции RuStore Push Service в CDP Sendsay

Чтобы CDP Sendsay мог отправлять push-уведомления через RuStore Push Service:

1. **Получите Project ID и сервисный токен в RuStore Console**:

    - Откройте нужное приложение в RuStore Console.
    - Перейдите в раздел **Push-уведомления**.
    - Скопируйте **ID проекта**.
    - Создайте и скопируйте сервисный токен для отправки push-уведомлений.

2. **Добавьте интеграцию RuStore Push Service в CDP Sendsay**:

    - В CDP Sendsay перейдите в **Подписчики** > **Мобильное приложение** > **Выберите из списка нужное** > **Настройки приложение и импорта**
    - Нажмите **Подключить** напротив надписи **RuStore**.

3. **Укажите параметры RuStore Push Service**:

    - В поле **Project ID** укажите ID проекта из RuStore Console.
    - В поле **Service token** укажите сервисный токен RuStore.
    - Нажмите «Сохранить».

Теперь CDP Sendsay может отправлять push-уведомления на Android-устройства через системы RuStore.

#### Чек-лист интеграции

- [ ] `RuStoreMessagingService` создан и зарегистрирован в `AndroidManifest.xml`.
- [ ] Задан project_id через <meta-data> в AndroidManifest.xml или RuStore Push SDK инициализирован в `Application.onCreate()`.
- [ ] Приложение получает RuStore push-токен.
- [ ] RuStore push-токен передаётся в Sendsay SDK.
- [ ] Самопроверка способна отправить и принять «тихий» push.
- [ ] Приложение получает push-уведомления, отправленные с помощью веб-приложения CDP Sendsay.
- [ ] Тестовый пуш из CDP Sendsay открывается, а ваш *broadcast receiver* вызывается корректно.

Как создавать push-уведомления в веб-приложении CDP Sendsay смотрите в разделе документации: [Мобильные push-уведомления](https://docs.sendsay.ru/other-channels/mobile-push/how-to-create-mobile-push-campaign).

> 👍
>
> Иногда RuStore Push SDK и сервис CDP Sendsay запускаются не сразу. Если push не приходит — перезапустите приложение. Если после 2–3 попыток проблема сохраняется — перепроверьте настройку приложения, подпись, `applicationId`, авторизацию пользователя в RuStore и разрешение фоновой работы RuStore.
