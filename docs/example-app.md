---
title: Пример приложения
excerpt: Сборка, запуск и навигация по примеру приложения, включённому в Android SDK
slug: android-sdk-example-app
categorySlug: integrations
parentDocSlug: android-sdk
---

Android SDK Sendsay включает пример приложения, который можно использовать как эталонную реализацию. Вы можете собрать и запустить его, протестировать функции Engagement и сравнить свой код с ожидаемым поведением и кодом в примере.

## Требования

Для сборки и запуска примера приложения установите:

- [Android Studio](https://developer.android.com/studio). 
- Настроенное [виртуальное устройство](https://developer.android.com/studio/run/managing-avds).

## Сборка и запуск примера приложения

1. Клонируйте репозиторий [sendsay-mobile-sdk-android](https://github.com/sendsay-ru/sendsay-mobile-sdk-android) на GitHub:
   ```shell
   git clone https://github.com/sendsay-ru/sendsay-mobile-sdk-android.git
   ```
2. Откройте проект **sendsay-mobile-sdk-android** в Android Studio.
3. Откройте файл **sdk/build.gradle** и найдите строку:
   ```
   apply from: 'publish-maven.gradle'
   ```
   Закомментируйте её:
   ```
   //apply from: 'publish-maven.gradle'
   ```
4. Запустите пример приложения на эмуляторе (**Run** > **Run 'app'** или Ctrl + R).

> 📘
>
> Чтобы протестировать push-уведомления, настроите интеграцию через[Firebase](https://documentation.bloomreach.com/engagement/docs/android-sdk-firebase) или [Huawei](https://documentation.bloomreach.com/engagement/docs/android-sdk-huawei) в веб-приложении Sendsay.

## Навигация по примеру приложения

![Экраны примера приложения: конфигурация, получение данных, отслеживание, отслеживание событий](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/android-example-app-1.png)

После запуска откроется экран **Authentication** (Аутентификация). Введите [токен проекта, API токен и базовый URL API](mobile-sdks-api-access-management). Опционально укажите hard ID (email) в поле **Registered**, чтобы идентифицировать клиента. Нажмите «Authenticate», чтобы [инициализировать SDK](ios-sdk-setup#initialize-the-sdk).
> [`AuthenticationActivity.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/java/com/sendsay/example/view/AuthenticationActivity.kt)

> 👍
>
> Убедитесь, что добавили префикс **"Token "** к вашему API ключу, например:
> `Token 0b7uuqicb0fwuv1tqz7ubesxzj3kc3dje3lqyqhzd94pgwnypdiwxz45zqkhjmbf`.

Приложение содержит несколько экранов, доступных через нижнюю навигацию:

- **Fetch** — получение рекомендаций, согласий и открытие почтового ящика.
  > [`FetchFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/java/com/sendsay/example/view/fragments/FetchFragment.kt)
  
- **Track** — тестирование отслеживания событий и свойств. Кнопки «Custom Event» и «Identify Customer» открывают отдельные формы для ввода данных.
  > [`TrackFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/TrackFragment.kt)
  > [`TrackCustomAttributesDialog.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/dialogs/TrackCustomAttributesDialog.kt)
  > [`TrackCustomEventDialog.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/dialogs/TrackCustomEventDialog.kt)

- **Manual Flush** — ручная отправка кэшированных данных в Sendsay.
  > [`FlushFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/FlushFragment.kt)

- **Anonymize** — сброс клиентского состояния и создание нового анонимного профиля.
  > [`AnonymizeFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/AnonymizeFragment.kt)

- **InAppCB** — отображение блоков контента внутри приложения. Используйте ID плейсхолдеров: `example_top`, `ph_x_example_iOS`, `example_list`, `example_carousel` и `example_carousel_and`.
  > [`InAppContentBlocksFragment.kt`](https://github.com/sendsay/sendsay-android-sdk/blob/bf48aba5a58e5632bdc5d963c18ee24d7e200ec9/app/src/main/java/com/sendsay/example/view/fragments/InAppContentBlocksFragment.kt)
  > [`fragment_inapp_content_blocks.xml`](https://github.com/sendsay/sendsay-android-sdk/blob/main/app/src/main/res/layout/fragment_inapp_content_blocks.xml)

После тестирования действий в приложении перейдите в раздел **Data & Assets** > **Customers** веб-интерфейса Engagement, чтобы увидеть:
- события, отправленные SDK,
- свойства клиента,
- идентификаторы (soft ID / hard ID).

Если поле **Registered** оставлено пустым — создаётся анонимный профиль (soft ID cookie). Если указан hard ID — профиль связывается с этим идентификатором (например, email).

> 📘
>
> Подробнее о soft ID и hard ID — в разделе [Идентификация клиентов](https://documentation.bloomreach.com/engagement/docs/customer-identification) документации Engagement.

![Экраны примера приложения: идентификация, сброс, анонимизация, блоки контента](https://raw.githubusercontent.com/sendsay/sendsay-android-sdk/main/Documentation/images/android-example-app-2.png)

## Устранение неполадок

Если возникают проблемы со сборкой или запуском:

1. В Android Studio выберите **Build** > **Clean Project**, 
2. Затем **Build** > **Rebuild Project**.
