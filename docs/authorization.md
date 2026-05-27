# Авторизация

SDK обменивается данными с CDP Sendsay через HTTPS. Android SDK поддерживает стандартную **авторизацию по токену** для доступа к мобильному API.

## Авторизация по токену

В режиме авторизации по токену SDK использует ваш API-ключ как токен и получает доступ к публичным эндпоинтам мобильного API.

Токен передаётся в параметре конфигурации `authorization` при инициализации SDK:

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