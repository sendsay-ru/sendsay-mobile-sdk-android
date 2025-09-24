---
title: Пример приложения
excerpt: Сборка, запуск и навигация по примеру приложения, включенному в Android SDK
slug: android-sdk-example-app
categorySlug: integrations
parentDocSlug: android-sdk
---

Android SDK Sendsay включает пример приложения, который вы можете использовать в качестве эталонной реализации. Вы можете собрать и запустить приложение, протестировать функции Engagement и сравнить код и поведение вашей реализации с ожидаемым поведением и кодом в примере приложения.

## Предварительные требования

У вас должно быть установлено следующее программное обеспечение для сборки и запуска примера приложения:

- [Android Studio](https://developer.android.com/studio) с настроенным [виртуальным устройством](https://developer.android.com/studio/run/managing-avds)

## Сборка и запуск примера приложения

1. Клонируйте репозиторий [sendsay-android-sdk](https://github.com/sendsay/sendsay-android-sdk) на GitHub:
   ```shell
   git clone https://github.com/sendsay/sendsay-android-sdk.git
   ```
2. Откройте проект `sendsay-android-sdk` в Android Studio.
3. Откройте файл `sdk/build.gradle` и найдите следующую строку:
   ```
   apply from: 'publish-maven.gradle'
   ```
   Закомментируйте её, чтобы она выглядела так:
   ```
   //apply from: 'publish-maven.gradle'
   ```
4. Запустите пример приложения в эмуляторе Android (`Run` > `Run 'app'` или Ctrl + R).

> 📘
>
> Чтобы включить push-уведомления в примере приложения, вы также должны настроить [интеграцию Firebase](https://documentation.bloomreach.com/engagement/docs/android-sdk-firebase) или [интеграцию Huawei](https://documentation.bloomreach.com/engagement/docs/android-sdk-huawei) в веб-приложении Sendsay.

## Навигация по примеру приложения

![Экраны примера приложения: конфигурация, получение данных, отслеживание, отслеживание событий](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/android-example-app-1.png)

Когда вы запустите приложение в симуляторе, вы увидите экран **Authentication** (Аутентификация). Введите ваш [токен проекта, API токен и базовый URL API](mobile-sdks-api-access-management). Опционально введите адрес электронной почты hard ID в поле `Registered`, чтобы идентифицировать клиента. Затем нажмите `Authenticate`, чтобы [инициализировать SDK](ios-sdk-setup#initialize-the-sdk).
> [`AuthenticationActivity.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/java/com/sendsay/example/view/AuthenticationActivity.kt)

> 👍
>
> Убедитесь, что добавили префикс "Token " к вашему API ключу, например:
> `Token 0b7uuqicb0fwuv1tqz7ubesxzj3kc3dje3lqyqhzd94pgwnypdiwxz45zqkhjmbf`.

Приложение предоставляет несколько экранов, доступных через нижнюю навигацию, для тестирования различных функций SDK:

- Экран **Fetch** (Получение) позволяет получать рекомендации и согласия, а также открывать inbox приложения.
  > [`FetchFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/java/com/sendsay/example/view/fragments/FetchFragment.kt)
  
- Экран **Track** (Отслеживание) позволяет тестировать отслеживание различных событий и свойств. Кнопки `Custom Event` и `Identify Customer` ведут к отдельным экранам для ввода тестовых данных.
  > [`TrackFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/TrackFragment.kt)
  > [`TrackCustomAttributesDialog.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/dialogs/TrackCustomAttributesDialog.kt)
  > [`TrackCustomEventDialog.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/dialogs/TrackCustomEventDialog.kt)

- Экран **Manual Flush** (Ручной сброс) позволяет запустить ручной сброс данных.
  > [`FlushFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/FlushFragment.kt)

- Экран **Anonymize** (Анонимизация) позволяет анонимизировать текущего пользователя.
  > [`AnonymizeFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/AnonymizeFragment.kt)

- Экран **InAppCB** отображает блоки контента внутри приложения. Используйте ID плейсхолдеров `example_top`, `ph_x_example_iOS`, `example_list`, `example_carousel` и `example_carousel_and` в настройках ваших блоков контента внутри приложения.
  > [`InAppContentBlocksFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/InAppContentBlocksFragment.kt)
  > [`fragment_inapp_content_blocks.xml`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/res/layout/fragment_inapp_content_blocks.xml)

Попробуйте различные функции в приложении, затем найдите профиль клиента в веб-приложении Engagement (в разделе `Data & Assets` > `Customers`), чтобы увидеть свойства и события, отслеживаемые SDK.

Если вы оставили поле `Registered` пустым, клиент отслеживается анонимно с использованием soft ID cookie. Вы можете найти значение cookie в логах и найти соответствующий профиль в веб-приложении Engagement.

Если вы ввели hard ID (используйте адрес электронной почты в качестве значения) в поле `Registered`, клиент идентифицируется и может быть найден в веб-приложении Engagement по его адресу электронной почты.

> 📘
>
> Обратитесь к разделу [Идентификация клиентов](https://documentation.bloomreach.com/engagement/docs/customer-identification) для получения дополнительной информации о soft ID и hard ID.

![Экраны примера приложения: идентификация, сброс, анонимизация, блоки контента](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/android-example-app-2.png)

## Устранение неполадок

Если у вас возникнут проблемы при сборке примера приложения, следующее может помочь:

- В Android Studio выберите `Build` > `Clean Project`, затем `Build` > `Rebuild Project`.
