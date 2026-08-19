---
title: Первоначальная настройка SDK
excerpt: Установка и настройка Android SDK
slug: android-sdk-setup
categorySlug: integrations
parentDocSlug: android-sdk
---

## Установка SDK

Sendsay Android SDK можно установить или обновить с помощью [Gradle](https://gradle.org/) или [Maven](https://maven.apache.org/). Для Gradle поддерживаются конфигурации на Kotlin и Groovy.

> 📘
>
> Актуальная версия Sendsay Android SDK всегда доступна в репозитории: https://github.com/sendsay-ru/sendsay-mobile-sdk-android/releases.

### Gradle (Kotlin)

1. В файле **build.gradle.kts** вашего приложения добавьте `com.sendsay.sdk:sdk` внутри секции `dependencies { }`:
   ```kotlin
   implementation("com.sendsay.sdk:sdk:0.1.4")
   ```
2. Пересоберите проект: **Build** > **Rebuild Project**.

### Gradle (Groovy)

1. В файле **build.gradle** вашего приложения добавьте `com.sendsay.sdk:sdk` внутри секции `dependencies { }`:
   ```groovy
   implementation 'com.sendsay.sdk:sdk:0.1.4'
   ```
2. Пересоберите проект: **Build** > **Rebuild Project**.

### Maven

1. В файл **pom.xml** добавьте `com.sendsay.sdk:sdk` внутри секции `<dependencies> </dependencies>`:
   ```xml
   <dependency>
      <groupId>com.sendsay.sdk</groupId>
      <artifactId>sdk</artifactId>
      <version>0.1.4</version>
      <type>aar</type>  <!---> Опционально, если требуется -->
   </dependency>   
   ```
2. Пересоберите приложение через Maven.

## Инициализация SDK

После установки SDK вам нужно импортировать его в проект, указать параметры конфигурации и выполнить инициализацию.

Обязательные параметры конфигурации:
- `projectToken`
- `authorization`
- `baseUrl`

Эти значения можно найти в [личном кабинете](https://app.sendsay.ru/subscribers/apps) CDP Sendsay в разделе **Подписчики** > **Мобильное приложение** > **Настройки приложения**.

SDK можно настроить в [коде](#использование-конфигурации-в-коде) (предпочтительный вариант) или через [JSON-файл конфигурации](#использование-файла-конфигурации).

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
configuration.baseURL = "https://mobi.sendsay.ru/xnpe/v100"

Sendsay.init(this, configuration)
```

### Использование файла конфигурации

Создайте файл **sendsay_configuration.json** в папке **assets** вашего приложения и добавьте минимум:

```json
{
  "projectToken": "ID вашего аккаунта в Sendsay",
  "authorization": "YOUR_API_KEY",
  "baseURL": "https://mobi.sendsay.ru/xnpe/v100"
}
```

Импортируйте SDK:

```kotlin
import com.sendsay.sdk.Sendsay

```

Инициализируйте SDK:

```kotlin
Sendsay.init(this)
```

SDK автоматически прочитает параметры из файла.

> 📘
>
> Обратитесь к [`sendsay_configuration.json`](https://github.com/prosky/sendsay-android-sdk/blob/main/app/src/main/assets/sendsay_configuration.json) в [примере приложения](example-app.md), чтобы посмотреть пример файла конфигурации.

### Где разместить код инициализации SDK

#### В подклассе приложения

Наиболее корректное место — метод `onCreate()` класса [`Application`](https://developer.android.com/reference/android/app/Application): он вызывается один раз при запуске и доступен рано в жизненном цикле приложения.

```kotlin
class MyApplication : Application() {
  override fun onCreate(){
    super.onCreate()

    val configuration = SendsayConfiguration()

    configuration.authorization = "Token jlk5askvxss99asmnbgayrks333"
    configuration.projectToken = "x_123456"
    configuration.baseURL = "https://mobi.sendsay.ru/xnpe/v100"

    // Инициализация SDK
    Sendsay.init(this, configuration)
    // или Sendsay.init(this) при использовании файла конфигурации
  }
}
```

Убедитесь, что вы зарегистрировали пользовательский класс приложения в **AndroidManifest.xml**:

```xml
<application
   android:name=".MyApplication">
   ...
</application>
```

#### В активности

Инициализацию можно выполнять и в `Activity`, но делать это следует как можно раньше, лучше всего — в `onCreate()`.

SDK отслеживает жизненный цикл приложения, включая события `onResume`, поэтому поздняя инициализация может привести к пропуску части событий.

> ❗️
>
> Если SDK уже инициализировался ранее (например, в предыдущем запуске), некоторые методы могут работать до повторной инициализации:
> - `Sendsay.handleCampaignIntent`
> - `Sendsay.handleRemoteMessage`
> - `Sendsay.handleNewToken`
> - `Sendsay.handleNewHmsToken`
>
> В этом случае используется конфигурация предыдущей инициализации. Чтобы всегда применять актуальные настройки, рекомендуется инициализировать SDK в `Application::onCreate()`.

### Инициализация завершена

После инициализации SDK активен и начинает автоматически отслеживать сессии приложения.

## Другая конфигурация SDK

### Расширенная конфигурация

SDK можно дополнительно настроить, указав свойства в объекте `SendsayConfiguration` или файле **sendsay_configuration.json**. 

Полный список доступных параметров конфигурации смотрите в разделе документации [Конфигурация](docs/configuration).

### Уровень логирования

SDK поддерживает следующие уровни логирования, определённые в `com.sendsay.sdk.util.Logger.Level`:

| Уровень логирования  | Описание |
| -----------| ----------- |
| `OFF`    | Отключает всё логирование |
| `ERROR`   | Серьёзные ошибки или критические проблемы |
| `WARN` | Предупреждения и рекомендации + `ERROR` |
| `INFO` | Информационные сообщения + `WARN` + `ERROR` |
| `DEBUG` | Отладочная информация + `INFO` + `WARN` + `ERROR`  |
| `VERBOSE` | Информация обо всех действиях SDK + `DEBUG` + `INFO` + `WARN` + `ERROR`. |

По умолчанию используется уровень `INFO`. При разработке или отладке может быть полезно установить уровень `VERBOSE`.

Чтобы изменить уровень:

```kotlin
Sendsay.loggerLevel = Logger.Level.VERBOSE
```

## Устранение неисправностей

### Ошибка сборки "Manifest merger failed"

В новом проекте Android Studio может возникнуть конфликт правил [резервного копирования]((https://developer.android.com/guide/topics/data/autobackup)), поскольку и приложение, и SDK содержат собственные backup_rules.

```
Manifest merger failed : Attribute application@fullBackupContent value=(@xml/backup_rules) from AndroidManifest.xml:8:9-54
	is also present at [com.sendsay.sdk:sdk:0.1.4] AndroidManifest.xml:15:9-70 value=(@xml/sendsay_default_backup_rules).
```

Вам необходимо [управлять файлами манифеста](https://developer.android.com/build/manage-manifests) и обеспечить их правильное слияние.

Варианты решения ошибки:

#### 1. Использовать правила резервного копирования SDK

  ```xml
  <application
      android:allowBackup="true"
      ...
      >
  </application>
  ```
  (Удалите `android:fullBackupContent="@xml/backup_rules"`)

#### 2. Использовать собственные правила и заменить правила SDK

Измените настройки в **app/src/main/res/xml/backup_rules.xml**:

  ```xml
  <application
      android:allowBackup="false"
      android:fullBackupContent="@xml/backup_rules"
      tools:replace="android:fullBackupContent"
      ...
      >
  </application>
  ```

#### 3. Отключить автоматическое резервное копирование

  ```xml
  <application
      android:allowBackup="false"
      ...
      >
  </application>
  ```
  (Удалите `android:fullBackupContent="@xml/backup_rules"`)
