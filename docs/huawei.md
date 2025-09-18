# Huawei Mobile Services

Интеграция Huawei Mobile Services в ваше приложение для поддержки push-уведомлений Engagement на устройствах Huawei

> 📘
>
> Новые телефоны, произведенные Huawei, поставляются с [Huawei Mobile Services (HMS)](https://developer.huawei.com/consumer/en/hms/) - сервисом, который доставляет push-уведомления _вместо_ Firebase Cloud Messaging (FCM) от Google.

Чтобы иметь возможность отправлять [push-уведомления](https://documentation.bloomreach.com/engagement/docs/android-push-notifications) с платформы Engagement и получать их в вашем приложении на устройствах Huawei, необходимо настроить Huawei Mobile Services (HMS), реализовать HMS в вашем приложении и настроить интеграцию Huawei Push Service в веб-приложении Engagement.

> 👍
>
> SDK предоставляет функцию самопроверки настройки push-уведомлений, чтобы помочь разработчикам успешно настроить push-уведомления. Самопроверка попытается отследить push-токен, запросить у backend'а Engagement отправку беззвучного push-уведомления на устройство и проверить, готово ли приложение открывать push-уведомления.
>
> Чтобы включить проверку настройки, установите `Sendsay.checkPushSetup = true` перед [инициализацией SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize-the-sdk).
>
> Мы предлагаем включить функцию самопроверки при первой реализации push-уведомлений или если вам нужно провести диагностику.

## Настройка Huawei Mobile Services

Сначала необходимо настроить Huawei Mobile Services:

1. Зарегистрируйте и настройте [аккаунт разработчика Huawei](https://developer.huawei.com/consumer/en/console).
2. Создайте проект и приложение в AppGallery Connect.
3. Сгенерируйте и настройте сертификат подписи.
4. Включите Push Kit в API AppGallery Connect.
5. Обновите скрипты Gradle и добавьте сгенерированный `agconnect-services.json` в ваше приложение.
6. Настройте информацию о подписи в вашем приложении.

> 📘
>
> Для подробных инструкций обратитесь к разделу [Подготовка к интеграции HUAWEI HMS Core](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0) в официальной документации HMS.

## Реализация HMS Message Service в вашем приложении

Далее необходимо создать и зарегистрировать сервис, который расширяет `HmsMessageService`. Автоматическое отслеживание SDK зависит от того, что ваше приложение предоставляет эту реализацию.

> 👍
>
> Эта реализация не включена в SDK, чтобы сохранить его как можно меньшим и избежать включения библиотек, которые не являются существенными для его функциональности. Вы можете скопировать приведенный ниже пример кода и использовать его в своем приложении.

1. Создайте сервис:
    ``` kotlin
    import android.app.NotificationManager  
    import android.content.Context  
    import ru.sendsay.sdk.Sendsay  
    import com.huawei.hms.push.HmsMessageService  
    import com.huawei.hms.push.RemoteMessage

    class MyHmsMessagingService: HmsMessageService() {

        private val notificationManager by lazy {
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        override fun onMessageReceived(message: RemoteMessage) {
            super.onMessageReceived(message)
            if (!Sendsay.handleRemoteMessage(applicationContext, message.dataOfMap, notificationManager)) {
                // push-уведомление от другого провайдера push-уведомлений
            }
        }

        override fun onNewToken(token: String) {
            super.onNewToken(token)
            Sendsay.handleNewHmsToken(applicationContext, token)
        }
    }
    ```

2. Зарегистрируйте сервис в `AndroidManifest.xml`:
    ```xml
    <service android:name="MyHmsMessagingService" android:exported="false">  
        <intent-filter> 
            <action android:name="com.huawei.push.action.MESSAGING_EVENT"/>  
        </intent-filter>
    </service>  
    <meta-data  android:name="push_kit_auto_init_enabled" android:value="true"/>
    ```

SDK будет обрабатывать только сообщения push-уведомлений, отправленные с платформы Engagement. Также предоставляется вспомогательный метод `Sendsay.isSendsayPushNotification()`.

Если вы запустите приложение, SDK должен отследить push-токен на платформе Engagement. Если вы включили самопроверку, она сообщит вам об этом. Альтернативно, вы можете найти клиента в веб-приложении Engagement и проверить свойство клиента `huawei_push_notification_id`.

Push-токен обычно генерируется при первом запуске приложения, но у него есть свой жизненный цикл. Ваша реализация `HmsMessageService` срабатывает только если токен создан или его значение изменилось. Пожалуйста, проверьте свои ожидания в соответствии с определенными [триггерами обновления токена](https://developer.huawei.com/consumer/en/doc/HMSCore-Guides/android-client-dev-0000001050042041#section487774626)

> ❗️
>
> Начиная с Android 13 (уровень API 33), разрешение на уведомления времени выполнения должно быть зарегистрировано в вашем `AndroidManifest.xml` и также должно быть предоставлено пользователем для того, чтобы ваше приложение могло показывать push-уведомления. SDK заботится о регистрации разрешения. Однако ваше приложение должно запросить разрешение на уведомления у пользователя, вызвав `Sendsay.requestPushAuthorization(context)`. Обратитесь к разделу [Запрос разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#request-notification-permission) для получения подробностей.
>
> Если ваш маркетинговый поток строго требует использования обычных push-уведомлений, настройте SDK для отслеживания только авторизованных push-токенов, установив [requirePushAuthorization](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) в `true`. Обратитесь к разделу [Требование разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#require-notification-permission) для получения подробностей.

> ❗️
>
> Если вы интегрируете SDK в существующий проект, вы можете столкнуться с проблемой, что ваш `HmsMessageService` не вызывается автоматически.
>
> Чтобы получить свежий push-токен, рассмотрите возможность запроса токена вручную как можно скорее после запуска приложения:
>
> Обратитесь к разделу [Получение и удаление Push Token](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-client-dev-0000001050042041) в документации HMS для инструкций о том, как получить текущий push-токен.

> ❗️
>
> Методы `Sendsay.handleNewToken` и `Sendsay.handleRemoteMessage` могут использоваться до инициализации SDK, если предыдущая инициализация была выполнена. В таком случае каждый метод будет отслеживать события с конфигурацией последней инициализации. Рассмотрите возможность инициализации SDK в `Application::onCreate`, чтобы убедиться, что свежая конфигурация применяется в случае обновления приложения.

## Настройка интеграции Huawei Push Service в Engagement

1. В Huawei App Gallery Connect перейдите в `Project settings` > `App information` > `OAuth 2.0 client ID`. Найдите `Client ID` и `Client secret` и скопируйте их значения. Вы будете использовать их для настройки интеграции Huawei Push Service в Engagement.
   ![HMS - Client ID и Client secret](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/huawei1.png)

2. Откройте веб-приложение Engagement и перейдите в `Data & Assets` > `Integrations`. Нажмите `+ Add new integration`.

3. Найдите `Huawei Push Service` и нажмите `+ Add integration`.
   ![Интеграции Engagement - Выберите интеграцию Firebase Cloud Messaging](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/huawei2.png)

4. Введите значения `Client ID` и `Client secret`, которые вы скопировали на шаге 1. Нажмите `Save integration` для завершения.
   ![Интеграции Engagement - Настройте интеграцию Firebase Cloud Messaging](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/huawei3.png)

5. Перейдите в `Settings` > `Project settings` > `Channels` > `Push notifications` > `Android Notifications` и установите `Huawei integration` в `Huawei Push Service`.