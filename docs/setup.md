---
title: Первоначальная настройка SDK
excerpt: Установка и настройка Android SDK
slug: android-sdk-setup
categorySlug: integrations
parentDocSlug: android-sdk
---

## Установка SDK

Sendsay Android SDK можно установить или обновить с помощью [Gradle](https://gradle.org/) или [Maven](https://maven.apache.org/). В случае Gradle вы можете использовать Kotlin или Groovy для файлов конфигурации сборки.

> 📘
>
> Обратитесь к https://github.com/exponea/exponea-android-sdk/releases для получения последней версии Sendsay Android SDK.

### Gradle (Kotlin)

1. В файле `build.gradle.kts` вашего приложения добавьте `com.sendsay.sdk:sdk` внутри секции `dependencies { }`:
   ```kotlin
   implementation("com.sendsay.sdk:sdk:4.5.0")
   ```
2. Пересоберите ваш проект (`Build` > `Rebuild Project`).

### Gradle (Groovy)

1. В файле `build.gradle` вашего приложения добавьте `com.sendsay.sdk:sdk` внутри секции `dependencies { }`:
   ```groovy
   implementation 'com.sendsay.sdk:sdk:4.5.0'
   ```
2. Пересоберите ваш проект (`Build` > `Rebuild Project`).

### Maven

1. В файле `pom.xml` вашего приложения добавьте `com.sendsay.sdk:sdk` внутри секции `<dependencies> </dependencies>`:
   ```xml
   <dependency>
      <groupId>com.sendsay.sdk</groupId>
      <artifactId>sdk</artifactId>
      <version>4.5.0</version>
   </dependency>   
   ```
2. Пересоберите ваше приложение с помощью Maven.

## Инициализация SDK

Теперь, когда вы установили SDK в свой проект, вы должны импортировать, настроить и инициализировать SDK в коде вашего приложения.

Обязательными параметрами конфигурации являются `projectToken`, `authorization` и `baseURL`. Вы можете найти их в личном кабинете CDP Sendsay в разделе `Подписчики` > `Мобильное приложение` > `Настройки приложения`.

Вы можете настроить SDK в [коде](#using-configuration-in-code) (предпочтительно) или используя [файл конфигурации JSON](#using-a-configuration-file).

### Использование конфигурации в коде

Импортируйте SDK:

```kotlin
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.SendsayConfiguration

```

Инициализируйте SDK:

```kotlin
val configuration = SendsayConfiguration()

configuration.authorization = "YOUR_API_KEY"
configuration.projectToken = "ID вашего аккаунта в Sendsay"
configuration.baseURL = "https://mobi.sendsay.ru/mobi/api/v100/json"

Sendsay.init(this, configuration)
```

### Использование файла конфигурации

Создайте файл `sendsay_configuration.json` в папке `assets` вашего приложения как минимум со следующими свойствами конфигурации:

```json
{
  "projectToken": "ID вашего аккаунта в Sendsay",
  "authorization": "YOUR_API_KEY",
  "baseURL": "https://mobi.sendsay.ru/mobi/api/v100/json"
}
```

Импортируйте SDK в ваш код:

```kotlin
import com.sendsay.sdk.Sendsay

```

Инициализируйте SDK:

```kotlin
Sendsay.init(this)
```

SDK прочитает параметры конфигурации из файла конфигурации.

> 📘
>
> Обратитесь к [`sendsay_configuration.json`](https://github.com/exponea/exponea-android-sdk/blob/main/app/src/main/assets/exponea_configuration.json) в [примере приложения](https://documentation.bloomreach.com/engagement/docs/android-sdk-example-app) для примера файла конфигурации.

### Где разместить код инициализации SDK

#### В подклассе приложения

Метод `onCreate()` вашего [`Application`](https://developer.android.com/reference/android/app/Application) - лучшее место для инициализации - он вызывается только один раз и очень рано в жизненном цикле приложения. Application - это класс для поддержания глобального состояния приложения.

Он должен выглядеть аналогично примеру ниже:

```kotlin
class MyApplication : Application() {
  override fun onCreate(){
    super.onCreate()

    val configuration = SendsayConfiguration()

    configuration.authorization = "Token jlk5askvxss99asmnbgayrks333"
    configuration.projectToken = "x_123456"
    configuration.baseURL = "https://mobi.sendsay.ru/mobi/api/v100/json"

    // Инициализация SDK
    Sendsay.init(this, configuration)
    // или Sendsay.init(this) при использовании файла конфигурации
  }
}
```
Убедитесь, что вы зарегистрировали ваш пользовательский класс приложения в `AndroidManifest.xml`:
```xml
<application
   android:name=".MyApplication">
   ...
</application>
```

#### В активности

Вы также можете инициализировать SDK из любой `Activity`, но важно делать это как можно раньше, предпочтительно в методе `onCreate()` вашей активности.

SDK подключается к жизненному циклу приложения для отслеживания сессий (среди прочего), поэтому вы должны отслеживать обратные вызовы `onResume` активностей. Если вам нужно инициализировать SDK после возобновления активности, делайте это с контекстом текущей активности.

> ❗️
>
> Некоторые методы API могут использоваться до инициализации SDK, если предыдущая инициализация была выполнена.
> Эти методы API:
> - `Sendsay.handleCampaignIntent`
> - `Sendsay.handleRemoteMessage`
> - `Sendsay.handleNewToken`
> - `Sendsay.handleNewHmsToken`
>
> В таком случае каждый метод будет отслеживать события с конфигурацией последней инициализации. Рассмотрите инициализацию SDK в `Application::onCreate`, чтобы убедиться, что свежая конфигурация применяется в случае обновления приложения.

### Готово!

На данном этапе SDK активен и теперь должен отслеживать сессии в вашем приложении.

## Другая конфигурация SDK

### Расширенная конфигурация

SDK можно дополнительно настроить, установив дополнительные свойства объекта `SendsayConfiguration` или файла `sendsay_configuration.json`. Для полного списка доступных параметров конфигурации обратитесь к документации [Конфигурация](docs/configuration).

### Уровень логирования

SDK поддерживает следующие уровни логирования, определенные в `com.sendsay.sdk.util.Logger.Level`:

| Уровень логирования  | Описание |
| -----------| ----------- |
| `OFF`    | Отключает все логирование |
| `ERROR`   | Серьезные ошибки или критические проблемы |
| `WARN` | Предупреждения и рекомендации + `ERROR` |
| `INFO` | Информационные сообщения + `WARN` + `ERROR` |
| `DEBUG` | Отладочная информация + `INFO` + `WARN` + `ERROR`  |
| `VERBOSE` | Информация обо всех действиях SDK + `DEBUG` + `INFO` + `WARN` + `ERROR`. |

По умолчанию уровень логирования - `INFO`. При разработке или отладке установка уровня логирования на `VERBOSE` может быть полезной.

Вы можете установить уровень логирования во время выполнения следующим образом:

```kotlin
Sendsay.loggerLevel = Logger.Level.VERBOSE
```

## Устранение неисправностей

### Ошибка сборки "Manifest merger failed"

Вы можете получить ошибку сборки, похожую на следующую, особенно в новом проекте "empty activity" по умолчанию, созданном Android Studio:

```
Manifest merger failed : Attribute application@fullBackupContent value=(@xml/backup_rules) from AndroidManifest.xml:8:9-54
	is also present at [com.sendsay.sdk:sdk:4.5.0] AndroidManifest.xml:15:9-70 value=(@xml/sendsay_default_backup_rules).
```

SDK и новое приложение, созданное Android Studio, оба включают [функцию автоматического резервного копирования](https://developer.android.com/guide/topics/data/autobackup) в `AndroidManifest.xml`, но каждое со своими правилами резервного копирования. Вам как разработчику необходимо [управлять файлами манифеста](https://developer.android.com/build/manage-manifests) и обеспечить их правильное слияние.

Ваши варианты включают:
- Использовать правила резервного копирования SDK:
  ```xml
  <application
      android:allowBackup="true"
      ...
      >
  </application>
  ```
  (Удалите `android:fullBackupContent="@xml/backup_rules"`)
- Определить свои собственные правила резервного копирования в `app/src/main/res/xml/backup_rules.xml` и указать, что они должны заменить правила резервного копирования SDK:
  ```xml
  <application
      android:allowBackup="false"
      android:fullBackupContent="@xml/backup_rules"
      tools:replace="android:fullBackupContent"
      ...
      >
  </application>
  ```
- Отключить автоматическое резервное копирование:
  ```xml
  <application
      android:allowBackup="false"
      ...
      >
  </application>
  ```
  (Удалите `android:fullBackupContent="@xml/backup_rules"`)
