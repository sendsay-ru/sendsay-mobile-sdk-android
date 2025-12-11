# Firebase Cloud Messaging

Интеграция Firebase Cloud Messaging (FCM) позволяет вашему Android-приложению получать [push-уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications) с платформы Engagement. Для этого необходимо:
- настроить проект Firebase,
- реализовать обработку сообщений FCM в вашем приложении,
- настроить интеграцию Firebase Cloud Messaging в веб-приложении Engagement.

> 👍
>
> SDK поддерживает самопроверку настройки push: она отследит push-токен и запросит у Engagement отправку тихого пуша на устройство, для подтверждения его готовности принимать уведомления.
>
> Чтобы включить самопроверку, установите `Sendsay.checkPushSetup = true` **перед** [инициализацией SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize-the-sdk).
>
> Рекомендуем включать самопроверку при первой реализации push-уведомлений или для диагностики.

## Настройка Firebase

Начните с подготовки проекта Firebase. Подробная инструкция доступна в разделе: [Добавление Firebase в ваш Android-проект](https://firebase.google.com/docs/android/setup#console) в официальной документации Firebase.

Краткие шаги:
1. Создайте проект в Firebase Console.
2. Скачайте файл `google-services.json`.
3. Добавьте файл в модуль вашего приложения.
4. Обновите конфигурацию Gradle.

#### Чек-лист настройки Firebase:
- [ ] Файл `google-services.json` размещён в папке **приложения**, например: *my-project/app/google-services.json*.
- [ ] В файл сборки Gradle **приложения** (например, *my-project/app/build.gradle*) добавлено: `apply plugin: 'com.google.gms.google-services'`.
- [ ] Ваш файл сборки Gradle **верхнего уровня** (например, *my-project/build.gradle*) имеет `classpath 'com.google.gms:google-services:X.X.X'` в зависимостях скрипта сборки.

## Реализация Firebase messaging в вашем приложении

SDK не включает собственную реализацию `FirebaseMessagingService`, поэтому вы должны добавить её в приложение вручную. Это необходимо для автоматического отслеживания push-токенов и обработки входящих push-уведомлений.

> 👍
>
> Эта реализация не включена в SDK, чтобы сохранить его как можно меньшим и избежать включения библиотек, которые не являются существенными для его функциональности. Вы можете скопировать приведённый ниже пример кода и использовать его в своём приложении.

### 1. Создайте сервис FCM

   ```kotlin
    import android.app.NotificationManager  
    import android.content.Context  
    import com.sendsay.sdk.Sendsay  
    import com.google.firebase.messaging.FirebaseMessagingService  
    import com.google.firebase.messaging.RemoteMessage

    class MyFirebaseMessagingService: FirebaseMessagingService() {

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
            Sendsay.handleNewToken(applicationContext, token)
        }
    }
   ```
### 2. Зарегистрируйте сервис в AndroidManifest.xml

   ```xml
    <service android:name="MyFirebaseMessagingService" android:exported="false" >  
        <intent-filter> 
            <action android:name="com.google.firebase.MESSAGING_EVENT" />  
        </intent-filter>
    </service>   
   ```

После этого SDK будет автоматически обрабатывать push-уведомления, отправленные с платформы Engagement. Дополнительно доступен вспомогательный метод: `Sendsay.isSendsayPushNotification()`.

### Проверка получения push-токена

Если всё настроено корректно:

- при запуске приложения SDK отслеживает push-токен,
- в самопроверке отображается успешное получение токена,
- в профиле клиента в веб-приложении Engagement появилось свойство `google_push_notification_id`.

Push-токен обновляется согласно правилам Firebase. Полный список триггеров обновления токена смотрите в [документации](https://firebase.google.com/docs/cloud-messaging/android/client#sample-register) Firebase.

### Разрешение на уведомления в Android 13+

Начиная с Android 13 (API 33):
1. Разрешение на уведомления должно быть зарегистрировано в вашем `AndroidManifest.xml`.
2. Пользователь должен явно предоставить разрешение. 
3. SDK регистрирует разрешение, но запросить его должно ваше приложение вызвав: 
```
Sendsay.requestPushAuthorization(context)
``` 

Подробнее — в разделе [Запрос разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#request-notification-permission) документации Engagement.

Если ваш маркетинговый сценарий требует отправлять только обычные push-уведомления, настройте SDK для отслеживания только авторизованных push-токенов: установите [requirePushAuthorization](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) = `true` в конфигурации SDK. 

Подробнее — в разделе [Требование разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#require-notification-permission) документации Engagement.


### Если FCM-токен не обновляется

> ❗️
>
> Если вы интегрируете новый проект Firebase в существующий проект или полностью меняете проект Firebase, вы можете столкнуться с проблемой, при которой сервис `FirebaseMessagingService` не вызывается автоматически.

В этом случае запросите токен вручную:

```kotlin
 import android.app.Application
 import com.sendsay.sdk.Sendsay
 import com.google.firebase.installations.FirebaseMessaging
 
 class SendsayApp : Application() {
     override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Sendsay.handleNewToken(applicationContext, it)
        }
     }
 }
 ```

### Использование методов до инициализации SDK

> ❗️
>
> `Sendsay.handleNewToken` и `Sendsay.handleRemoteMessage` можно вызывать до инициализации SDK, если она уже выполнялась ранее.
В этом случае данные будут отправлены с конфигурацией последней инициализации.
> 
> Чтобы избежать неактуальных конфигураций — инициализируйте SDK в `Application.onCreate()`.

## Настройка интеграции Firebase Cloud Messaging в Engagement

Чтобы Engagement мог отправлять push-уведомления через FCM:

1. **Создайте service account** в Google Cloud: 
    
    Google Cloud > `Service Accounts` > *ваш проект* > **Create Service Account**. Можно использовать роли для определения более детального доступа.

2. **Сгенерируйте новый приватный ключ**: 

    Откройте созданный аккаунт и выберите **Actions** > **Manage Keys** > **Add Key** > **Create new key**. Скачайте файл ключа JSON.

3. **Добавьте интеграцию FCM в Engagement**:

    - В Engagement перейдите в **Data & Assets** > **Integration**.
    - Нажмите на «Add new integration» и выберите **Firebase Cloud Messaging**. 
    - Если вы хотите отправлять пуши через webhooks — выберите **Firebase Service Account Authentication**.

    ![](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/firebase-1.png)

4. **Вставьте JSON-ключ в Service Account JSON Credentials** 

    - Добавьте JSON-ключ из шага 2 на страницу настроек интеграции Firebase Cloud Messaging, в поле **Service Account JSON Credentials**. 
    - Нажмите «Save integration».

    ![](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/firebase-2.png)

5. **Выберите интеграцию** 

    - **Project Settings** > **Channels** > **Push notifications**
    - выберите интеграцию **Firebase Cloud Messaging integration** и нажмите «Save changes».

Теперь Engagement может отправлять push-уведомления на устройства Android.

#### Чек-лист интеграции

- [ ] Самопроверка способна отправить и принять «тихий» push

  ![](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/self-check.png)

- [ ] Приложение получает push-уведомления, отправленные с помощью веб-приложения Engagement. 

Как создавать push-уведомления в веб-приложении Engagement смотрите в разделе документации: [Мобильные push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification).

- [ ] Тестовый пуш из Engagement открывается, а ваш *broadcast receiver* вызывается корректно.

> 👍
>
> Иногда FCM и сервис Engagement запускаются не сразу. Если push не приходит — перезапустите приложение. Если после 2–3 попыток проблема сохраняется — перепроверьте настройку.