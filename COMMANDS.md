# ShiftWeave — команды

## Сборка

```text
./gradlew assembleDebug
./gradlew assembleRelease
```

Windows:

```text
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

## Установка debug APK

```text
.\gradlew.bat installDebug
```

## Unit-тесты

```text
.\gradlew.bat test
.\gradlew.bat testDebugUnitTest
```

## Очистка

```text
.\gradlew.bat clean
.\gradlew.bat --stop
```

## Устройство и логи

```text
adb devices
adb logcat *:E
adb shell am force-stop com.shiftschedule.app
```

## Release-подпись

Release-подпись берётся из Gradle properties:

```text
RELEASE_STORE_FILE=/absolute/path/to/release.keystore
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

Для RuStore release bundle:

```text
.\gradlew.bat bundleRelease
```
