# Shift Schedule (График смен)

Удобное Android-приложение для управления, отслеживания и планирования рабочих смен.

## ✨ Возможности
- 📅 **Множественные графики**: Создание и ведение нескольких расписаний одновременно.
- 🔄 **Шаблоны смен**: Использование встроенных и создание собственных шаблонов.
- 🎨 **Темы оформления**: 11 тем оформления, включая Material You.
- 🔔 **Уведомления**: Настраиваемые напоминания о сменах.
- 💾 **Бэкапы**: Экспорт и импорт всех данных в формате JSON.
- 📱 **Виджет**: Быстрый просмотр текущей смены на главном экране.
- 📊 **Статистика**: Подсчет отработанных часов и смен по месяцам/годам.
- 🌍 **Мультиязычность**: Поддержка системного языка.

## 🛠 Стек технологий
- **UI:** Jetpack Compose, Material 3
- **Архитектура:** MVVM, Clean Architecture elements
- **База данных:** Room
- **Настройки:** DataStore
- **Асинхронность:** Kotlin Coroutines & Flow
- **Навигация:** Compose Navigation

## 🚀 Сборка
1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/K0Teu4/ShiftSchedule.git
   ```
2. Откройте проект в Android Studio (Hedgehog или новее).
3. Дождитесь синхронизации Gradle и запустите на устройстве/эмуляторе.
## Architecture
- `domain/ShiftResolver` is the single source of truth for resolving a shift for a date.
- Calendar, statistics, notifications and the widget use the same resolver semantics.
- Room upgrades are explicit; destructive migration is allowed only on downgrade.
- JSON restore is schema-validated and performed atomically in a Room transaction.
- Release signing credentials are never stored in the repository.

## Release signing
Supply these Gradle properties from a local `~/.gradle/gradle.properties` or CI secret store:

```text
RELEASE_STORE_FILE=/absolute/path/to/release.keystore
RELEASE_STORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

A debug build does not require release signing credentials.
