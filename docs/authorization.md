# Авторизация

Полный справочник по авторизации для Android SDK

SDK обменивается данными с API Engagement через авторизованную HTTP/HTTPS связь. SDK поддерживает два режима авторизации: стандартную **авторизацию по токену** для доступа к публичному API и более безопасную **авторизацию по токену клиента** для доступа к приватному API. Разработчики могут выбрать подходящий режим авторизации для требуемого уровня безопасности.

## Авторизация по токену

Стандартный режим авторизации по токену обеспечивает [доступ к публичному API](https://documentation.bloomreach.com/engagement/reference/authentication#public-api-access) с использованием API-ключа в качестве токена.

Авторизация по токену используется для следующих конечных точек API по умолчанию:

* `POST /track/v2/projects/<projectToken>/customers` для отслеживания данных клиентов
* `POST /track/v2/projects/<projectToken>/customers/events` для отслеживания данных событий
* `POST /track/v2/projects/<projectToken>/campaigns/clicks` для отслеживания событий кампаний
* `POST /data/v2/projects/<projectToken>/consent/categories` для получения согласий
* `POST /webxp/s/<projectToken>/inappmessages?v=1` для получения InApp сообщений
* `POST /webxp/projects/<projectToken>/appinbox/fetch` для получения данных AppInbox
* `POST /webxp/projects/<projectToken>/appinbox/markasread` для отметки сообщения AppInbox как прочитанного
* `POST /campaigns/send-self-check-notification?project_id=<projectToken>` для части потока самопроверки push-уведомлений

Разработчики должны установить токен, используя параметр [конфигурации](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) `authorization` при [инициализации SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize-the-sdk):

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = SendsayConfiguration()
        configuration.authorization = "Token YOUR_API_KEY"
        configuration.projectToken = "YOUR_PROJECT_TOKEN"
        configuration.baseURL = "https://api.exponea.com"
        Sendsay.init(App.instance, configuration)
    }
}
```

## Авторизация по токену клиента

Авторизация по токену клиента является опциональной и обеспечивает [доступ к приватному API](authentication#private-api-access) для выбранных конечных точек Engagement API. [Токен клиента](https://documentation.bloomreach.com/engagement/docs/customer-token) содержит закодированные ID клиентов и подпись. Когда API Bloomreach Engagement получает токен клиента, он сначала проверяет подпись и обрабатывает запрос только если подпись действительна.

Токен клиента кодируется с использованием **JSON Web Token (JWT)**, открытого отраслевого стандарта [RFC 7519](https://tools.ietf.org/html/rfc7519), который определяет компактный и самодостаточный способ безопасной передачи информации между сторонами.

SDK отправляет токен клиента в формате `Bearer <value>`. В настоящее время SDK поддерживает авторизацию по токену клиента для следующих конечных точек Engagement API:

* `POST /webxp/projects/<projectToken>/appinbox/fetch` для получения данных AppInbox
* `POST /webxp/projects/<projectToken>/appinbox/markasread` для отметки сообщения AppInbox как прочитанного

Разработчики могут включить авторизацию по токену клиента, установив параметр [конфигурации](ios-sdk-configuration) `advancedAuthEnabled` в `true` при [инициализации SDK](ios-sdk-setup#initialize-the-sdk):

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = SendsayConfiguration()
        configuration.projectToken = "YOUR_PROJECT_TOKEN"
        configuration.baseURL = "https://api.exponea.com"
        configuration.authorization = "Token YOUR_API_KEY" // !!! базовая авторизация обязательна
        configuration.advancedAuthEnabled = true
        Sendsay.init(App.instance, configuration)
    }
}
```

Дополнительно разработчики должны реализовать интерфейс `com.sendsay.sdk.services.AuthorizationProvider`, убедившись, что метод `getAuthorizationToken` возвращает действительный JWT токен, который кодирует соответствующие ID клиента(ов) и ID приватного API ключа:

```kotlin
class ExampleAuthProvider : AuthorizationProvider {
    override fun getAuthorizationToken(): String? {
        return "YOUR JWT TOKEN"
    }
}
```

Вы должны зарегистрировать свой `AuthorizationProvider` в файле `AndroidManifest.xml` вашего приложения как:

```xml
<application
    ...
    <meta-data
        android:name="SendsayAuthProvider"
        android:value="com.your.app.security.ExampleAuthProvider"
        />
</application>
```

> ❗️
>
> Токены клиентов должны генерироваться стороной, которая может безопасно проверить личность клиента. Обычно это означает, что токены клиентов должны генерироваться во время процедуры входа в backend приложения. Когда личность клиента проверена (с использованием пароля, сторонней аутентификации, Single Sign-On и т.д.), backend приложения должен сгенерировать токен клиента и отправить его на устройство, на котором работает SDK.

> 📘
>
> Обратитесь к разделу [Генерация токена клиента](customer-token#generating-customer-token) в документации по токенам клиентов для пошаговых инструкций по генерации JWT токена клиента.

### Устранение неполадок

Если вы реализуете `AuthorizationProvider`, но он не работает как ожидается, проверьте логи на следующее:
1. Если вы включаете авторизацию по токену клиента, установив флаг конфигурации `advancedAuthEnabled` в `true`, но SDK не может найти реализацию провайдера, он запишет следующее сообщение:
`Advanced auth has been enabled but provider has not been found`
2. Если вы регистрируете класс `SendsayAuthProvider` в `AndroidManifest.xml`, но SDK не может его найти, он запишет следующее сообщение:
`Registered <your class> class has not been found with detailed info`
2. Если вы регистрируете класс `SendsayAuthProvider` в `AndroidManifest.xml`, который не реализует интерфейс `AuthorizationProvider`, он запишет следующее сообщение:
`Registered <your class> class has to implement com.sendsay.sdk.services.AuthorizationProvider`

### Асинхронная реализация AuthorizationProvider

Значение токена клиента запрашивается для каждого HTTP вызова во время выполнения. Метод `getAuthorizationToken()` написан для синхронного использования, но вызывается в фоновом потоке. Поэтому вы можете заблокировать любое асинхронное получение токена (например, другой HTTP вызов) и ждать результата, блокируя этот поток. Если получение токена не удается, вы можете вернуть значение NULL, но запрос автоматически завершится неудачей.

```kotlin
class ExampleAuthProvider : AuthorizationProvider {
    override fun getAuthorizationToken(): String? = runBlocking {
        return@runBlocking suspendCoroutine { done ->
            retrieveTokenAsync(
                    success = {token -> done.resume(token)},
                    error = {error -> done.resume(null)}
            )
        }
    }
}
```

> 👍
>
> Различные сетевые библиотеки поддерживают разные подходы, но принцип остается тем же - не стесняйтесь блокировать вызов метода `getAuthorizationToken`.

### Политика получения токена клиента

Значение токена клиента запрашивается для каждого HTTP вызова, который его требует.

Обычно JWT токены имеют свое собственное время истечения срока действия и могут использоваться несколько раз. SDK не хранит токен в каком-либо кэше. Разработчики могут реализовать свой собственный кэш токенов по своему усмотрению. Например:

```kotlin
class ExampleAuthProvider : AuthorizationProvider {

    private var tokenCache: String? = null

    override fun getAuthorizationToken(): String? = runBlocking {
         if (tokenCache.isNullOrEmpty()) {
             tokenCache = suspendCoroutine { done ->
                 retrieveTokenAsync(
                     success = {token -> done.resume(token)},
                     error = {error -> done.resume(null)}
                 )
             }
         }
         return@runBlocking tokenCache
     }
}
```

> ❗️
>
> Пожалуйста, рассмотрите возможность более безопасного хранения вашего кэшированного токена. Android предлагает множество опций, таких как [KeyStore](https://developer.android.com/training/articles/keystore) или [Encrypted Shared Preferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences).

> ❗️
>
> Токен клиента действителен до истечения срока действия и назначается текущим ID клиентов. Имейте в виду, что если ID клиентов изменяются (из-за вызова методов `identifyCustomer` или `anonymize`), токен клиента может стать недействительным для будущих HTTP запросов, вызванных для новых ID клиентов.