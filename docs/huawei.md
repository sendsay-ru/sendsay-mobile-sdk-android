# Huawei Mobile Services

Интеграция Huawei Mobile Services (HMS) позволяет вашему Android-приложению получать [push-уведомления](push-notifications.md) с платформы CDP Sendsay на устройствах c Huawei Core, которые не используют Google Firebase Cloud Messaging.

> 📘
>
> Новые устройства Huawei поставляются с [Huawei Mobile Services (HMS)](https://developer.huawei.com/consumer/en/hms/) - сервисом, который доставляет push-уведомления *вместо* Firebase Cloud Messaging (FCM) от Google.

Чтобы иметь возможность отправлять пуши с платформы CDP Sendsay и получать их в приложении на устройствах Huawei, необходимо:
- настроить Huawei Mobile Services (HMS), 
- реализовать HMS в приложении,
- настроить интеграцию Huawei Push Service в веб-приложении CDP Sendsay.

> 👍
>
> SDK поддерживает самопроверку настройки push: она отследит push-токен и запросит у CDP Sendsay отправку тихого пуша на устройство, для подтверждения его готовности принимать уведомления.
>
> Чтобы включить самопроверку, установите `Sendsay.checkPushSetup = true` **перед** [инициализацией SDK](setup.md#инициализация-sdk).
>
> Рекомендуем включать самопроверку при первой реализации push-уведомлений или для диагностики.

## Настройка Huawei Mobile Services

Для настройки Huawei Mobile Services:

1. Зарегистрируйте и настройте [аккаунт разработчика Huawei](https://developer.huawei.com/consumer/en/console).
2. Создайте проект и приложение в *AppGallery Connect*.
3. Сгенерируйте и настройте сертификат подписи.
4. Включите **Push Kit** в *API AppGallery Connect*.
5. Обновите скрипты Gradle и добавьте сгенерированный `agconnect-services.json` в приложение.
6. Настройте информацию о подписи в вашем приложении.

> 📘
>
> Подробнее — в разделе [Подготовка к интеграции HUAWEI HMS Core](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0) документации HMS.

## Реализация HMS Message Service в приложении

SDK не включает собственную реализацию `HmsMessageService`, поэтому вы должны добавить её в приложение вручную. Это необходимо для автоматического отслеживания push-токенов и обработки входящих push-уведомлений.

> 👍
>
> Эта реализация не включена в SDK, чтобы сохранить его как можно меньшим и избежать включения библиотек, которые не являются существенными для его функциональности. Вы можете скопировать приведённый ниже пример кода и использовать его в своём приложении.


### 1. Создайте сервис HMS

   ```kotlin
   import android.app.NotificationManager  
   import android.content.Context  
   import com.sendsay.sdk.Sendsay  
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

2. ### Зарегистрируйте сервис в AndroidManifest.xml

    ```xml
    <service android:name="MyHmsMessagingService" android:exported="false">  
        <intent-filter> 
            <action android:name="com.huawei.push.action.MESSAGING_EVENT"/>  
        </intent-filter>
    </service>  
    <meta-data  android:name="push_kit_auto_init_enabled" android:value="true"/>
    ```

После этого SDK будет автоматически обрабатывать push-уведомления, отправленные с платформы CDP Sendsay. Дополнительно доступен вспомогательный метод: `Sendsay.isSendsayPushNotification()`.

### Проверка получения push-токена

Если всё настроено корректно:

- при запуске приложения SDK отслеживает push-токен,
- в самопроверке отображается успешное получение токена,
- в профиле клиента в веб-приложении CDP Sendsay появилось свойство `huawei_push_notification_id`.

Push-токен генерируется при первом запуске приложения, но у него есть свой жизненный цикл. Ваша реализация `HmsMessageService` срабатывает, если токен создан или его значение изменилось. Полный список триггеров обновления токена смотрите в [документации](https://developer.huawei.com/consumer/en/doc/HMSCore-Guides/android-client-dev-0000001050042041#section487774626).

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

### Если HmsMessageService не вызывается

> ❗️
>
> Если вы интегрируете SDK в существующий проект, вы можете столкнуться с проблемой, при которой `HmsMessageService` не вызывается автоматически.

В этом случае запросите токен вручную. Подробнее — в разделе[Получение и удаление Push Token](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-client-dev-0000001050042041) в документации HMS.

### Использование методов до инициализации SDK

> ❗️
>
> `Sendsay.handleNewToken` и `Sendsay.handleRemoteMessage` можно вызывать до инициализации SDK, если она уже выполнялась ранее.
В этом случае данные будут отправлены с конфигурацией последней инициализации.
> 
> Чтобы избежать неактуальных конфигураций — инициализируйте SDK в `Application.onCreate()`.

## Настройка интеграции Huawei Push Service в CDP Sendsay

Чтобы CDP Sendsay мог отправлять push-уведомления через Huawei Push Service:

1. **Получите Client ID и Client Secret**:

    - В **Huawei App Gallery Connect** перейдите в **Project settings** > **App information** > **OAuth 2.0 client ID**. 
    - Скопируйте `Client ID` и `Client secret`. Они понадобятся для дальнейшей настройки интеграции.

   ![HMS - Client ID и Client secret](https://raw.githubusercontent.com/sendsay-ru/sendsay-mobile-sdk-android/main/Documentation/images/huawei1.png)

2. **Добавьте интеграцию**:

    - В CDP Sendsay перейдите в **Подписчики** > **Мобильное приложение** > **Выберите из списка нужное** > **Настройки приложение и импорта**
    - Нажмите **Подключить** напротив надписи **Huawei**.

   ![Интеграции CDP Sendsay - Выберите интеграцию Firebase Cloud Messaging](https://raw.githubusercontent.com/sendsay-ru/sendsay-mobile-sdk-android/main/Documentation/images/huawei2.png)

3. Введите значения `Client ID` и `Client secret` и нажмите «Сохранить».

   ![Интеграции CDP Sendsay - Настройте интеграцию Firebase Cloud Messaging](https://raw.githubusercontent.com/sendsay-ru/sendsay-mobile-sdk-android/main/Documentation/images/huawei3.png)

Теперь CDP Sendsay может отправлять push-уведомления на устройства Android использующих системы Huawei.
