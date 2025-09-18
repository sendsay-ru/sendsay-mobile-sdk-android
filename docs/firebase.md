# Firebase Cloud Messaging

Интеграция Firebase Cloud Messaging в ваше приложение для поддержки push-уведомлений Engagement на Android-устройствах

Чтобы иметь возможность отправлять [push-уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications) с платформы Engagement и получать их в вашем приложении на Android-устройствах, необходимо настроить проект Firebase, реализовать Firebase messaging в вашем приложении и настроить интеграцию Firebase Cloud Messaging в веб-приложении Engagement.

> 👍
>
> SDK предоставляет функцию самопроверки настройки push-уведомлений, чтобы помочь разработчикам успешно настроить push-уведомления. Самопроверка попытается отследить push-токен, запросить у backend'а Engagement отправку беззвучного push-уведомления на устройство и проверить, готово ли приложение открывать push-уведомления.
>
> Чтобы включить проверку настройки, установите `Sendsay.checkPushSetup = true` перед [инициализацией SDK](https://documentation.bloomreach.com/engagement/docs/android-sdk-setup#initialize-the-sdk).
>
> Мы предлагаем включить функцию самопроверки при первой реализации push-уведомлений или если вам нужно провести диагностику.

## Настройка Firebase

Сначала необходимо настроить проект Firebase. Для пошаговых инструкций обратитесь к разделу [Добавление Firebase в ваш Android-проект](https://firebase.google.com/docs/android/setup#console) в официальной документации Firebase.

Вкратце, вы создадите проект с помощью консоли Firebase, скачаете сгенерированный файл конфигурации `google-services.json` и добавите его в свое приложение, а также обновите скрипты сборки Gradle в вашем приложении.

#### Чек-лист:
- [ ] Файл `google-services.json`, скачанный из консоли Firebase, находится в папке вашего **приложения**, например, *my-project/app/google-services.json*.
- [ ] Файл сборки Gradle вашего **приложения** (например, *my-project/app/build.gradle*) содержит `apply plugin: 'com.google.gms.google-services'`.
- [ ] Ваш **верхнего уровня** файл сборки Gradle (например, *my-project/build.gradle*) имеет `classpath 'com.google.gms:google-services:X.X.X'` в зависимостях скрипта сборки.

## Реализация Firebase messaging в вашем приложении

Далее необходимо создать и зарегистрировать сервис, который расширяет `FirebaseMessagingService`. Автоматическое отслеживание SDK зависит от того, что ваше приложение предоставляет эту реализацию.

> 👍
>
> Эта реализация не включена в SDK, чтобы сохранить его как можно меньшим и избежать включения библиотек, которые не являются существенными для его функциональности. Вы можете скопировать приведенный ниже пример кода и использовать его в своем приложении.

1. Создайте сервис:
   ```kotlin
    import android.app.NotificationManager  
    import android.content.Context  
    import ru.sendsay.sdk.Sendsay  
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
2. Зарегистрируйте сервис в `AndroidManifest.xml`:
   ```xml
    <service android:name="MyFirebaseMessagingService" android:exported="false" >  
        <intent-filter> 
            <action android:name="com.google.firebase.MESSAGING_EVENT" />  
        </intent-filter>
    </service>   
   ```

SDK будет обрабатывать только сообщения push-уведомлений, отправленные с платформы Engagement. Также предоставляется вспомогательный метод `Sendsay.isSendsayPushNotification()`.

Если вы запустите приложение, SDK должен отследить push-токен на платформе Engagement. Если вы включили самопроверку, она сообщит вам об этом. Альтернативно, вы можете найти клиента в веб-приложении Engagement и проверить свойство клиента `google_push_notification_id`.

Push-токен обычно генерируется при первом запуске приложения, но у него есть свой жизненный цикл. Ваша реализация `FirebaseMessagingService` срабатывает только если токен создан или его значение изменилось. Пожалуйста, проверьте свои ожидания в соответствии с определенными [триггерами обновления токена](https://firebase.google.com/docs/cloud-messaging/android/client#sample-register)

> ❗️
>
> Начиная с Android 13 (уровень API 33), разрешение на уведомления времени выполнения должно быть зарегистрировано в вашем `AndroidManifest.xml` и также должно быть предоставлено пользователем для того, чтобы ваше приложение могло показывать push-уведомления. SDK заботится о регистрации разрешения. Однако ваше приложение должно запросить разрешение на уведомления у пользователя, вызвав `Sendsay.requestPushAuthorization(context)`. Обратитесь к разделу [Запрос разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#request-notification-permission) для получения подробностей.
>
> Если ваш маркетинговый поток строго требует использования обычных push-уведомлений, настройте SDK для отслеживания только авторизованных push-токенов, установив [requirePushAuthorization](https://documentation.bloomreach.com/engagement/docs/android-sdk-configuration) в `true`. Обратитесь к разделу [Требование разрешения на уведомления](https://documentation.bloomreach.com/engagement/docs/android-sdk-push-notifications#require-notification-permission) для получения подробностей.

> ❗️
>
> Если вы интегрируете новый проект Firebase в существующий проект или полностью меняете проект Firebase, вы можете столкнуться с проблемой, что ваш 'FirebaseMessagingService' не вызывается автоматически.
>
> Чтобы получить свежий FCM токен, рассмотрите возможность запроса токена вручную как можно скорее после инициализации Firebase:
>
> ```kotlin
> import android.app.Application
> import ru.sendsay.sdk.Sendsay
> import com.google.firebase.installations.FirebaseMessaging
> 
> class SendsayApp : Application() {
>     override fun onCreate() {
>        super.onCreate()
>        FirebaseMessaging.getInstance().token.addOnSuccessListener {
>            Sendsay.handleNewToken(applicationContext, it)
>        }
>     }
> }
> ```

> ❗️
>
> Методы `Sendsay.handleNewToken` и `Sendsay.handleRemoteMessage` могут использоваться до инициализации SDK, если предыдущая инициализация была выполнена. В таком случае каждый метод будет отслеживать события с конфигурацией последней инициализации. Рассмотрите возможность инициализации SDK в `Application::onCreate`, чтобы убедиться, что свежая конфигурация применяется в случае обновления приложения.

## Настройка интеграции Firebase Cloud Messaging в Engagement

Наконец, необходимо настроить интеграцию Firebase Cloud Messaging в Engagement, чтобы платформа могла использовать ее для отправки push-уведомлений.

Настройка требует использования приватного ключа из Service Account, который вы создаете в Google Cloud, а затем копируете этот ключ в аутентификацию интеграции в Bloomreach Engagement.

Следуйте шагам ниже:

1. **Создайте service account.** Чтобы создать новый service account в Google Cloud, перейдите в `Service Accounts` и выберите свой проект. На странице Service Accounts выберите `Create Service Account`. Можно использовать роли для определения более детального доступа.

2. **Сгенерируйте новый приватный ключ**. Найдите FCM service account, который вы создали на предыдущем шаге, затем выберите `Actions` > `Manage Keys`. Выберите `Add Key` > `Create new key`. Скачайте файл ключа JSON.

3. **Добавьте интеграцию Firebase Cloud Messaging** в ваш проект Engagement. В Engagement перейдите в `Data & Assets` > `Integration`. Нажмите на `Add new integration` и выберите `Firebase Cloud Messaging` для отправки push-уведомлений через узел push-уведомлений. Обратите внимание, что если вы хотите отправлять push-уведомления через webhooks, вместо этого необходимо выбрать `Firebase Service Account Authentication`.
![](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/firebase-1.png)

4. **Вставьте ключ из шага 2** на страницу настроек интеграции Firebase Cloud Messaging в поле `Service Account JSON Credentials`. Нажмите на `Save integration`.
![](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/firebase-2.png)

5. **Выберите интеграцию Firebase Cloud Messaging** в `Project Settings` > `Channels` > `Push notifications` > `Firebase Cloud Messaging integration`. Нажмите на `Save changes`.

Платформа Engagement теперь должна быть способна отправлять push-уведомления на Android-устройства через узел push-уведомлений.

#### Чек-лист

- [ ] Если вы запустите приложение, самопроверка должна быть способна отправить и получить беззвучное push-уведомление.
  ![](https://raw.githubusercontent.com/exponea/exponea-android-sdk/main/Documentation/images/self-check.png)
- [ ] Теперь вы должны быть способны отправлять push-уведомления с помощью веб-приложения Engagement и получать их в вашем приложении. Обратитесь к разделу [Мобильные push-уведомления](https://documentation.bloomreach.com/engagement/docs/mobile-push-notifications#creating-a-new-notification), чтобы узнать, как создавать push-уведомления в веб-приложении Engagement.
- [ ] Отправьте тестовое push-уведомление из Engagement на устройство и нажмите на него. Ваш broadcast receiver должен быть вызван.

> 👍
>
> Сервису Engagement для отправки push-уведомлений и соединению Firebase может потребоваться минута, чтобы правильно запуститься. Если отправка push-уведомления не удается, попробуйте перезапустить приложение. Если проблема сохраняется после 2-3 попыток, пересмотрите вашу настройку.

Теперь вы должны быть способны использовать push-уведомления Engagement. Вы можете отключить самопроверку или оставить ее включенной для проверки настройки push-уведомлений при каждом запуске отладочной сборки.