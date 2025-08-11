# Авторизация

Полный справочник по авторизации для Sendsay Android SDK

SDK обменивается данными с CDP Sendsay через HTTPS. SDK поддерживает стандартную **авторизацию по токену** для доступа к мобильному API.

## Авторизация по токену

Стандартный режим авторизации по токену обеспечивает доступ к публичным эндпоинтам мобильного API с использованием API-ключа в качестве токена.

Разработчики должны установить токен, используя параметр конфигурации `authorization` при инициализации SDK:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val configuration = SendsayConfiguration()
        configuration.authorization = "YOUR_API_KEY"
        configuration.projectToken = "ID вашего аккаунта в Sendsay"
        configuration.baseURL = "https://mobi.sendsay.ru/mobi/api/v100/json"
        Sendsay.init(App.instance, configuration)
    }
}
```