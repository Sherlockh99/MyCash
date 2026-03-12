# MyCash

Мобільний додаток для ведення особистих фінансів на Android | Android приложение для личных финансов | Personal finance Android app

---

## 🇷🇺 Русский

Android-приложение для ведения личных финансов: учёт доходов, расходов, переводов между счетами, планирование бюджета и отчётность.

### Возможности

| Функция | Описание |
|---------|----------|
| **Счета** | Несколько счетов (карты, наличные и т.д.), опциональный начальный баланс |
| **Категории** | Двухуровневая иерархия: категория → подкатегория, отдельно для доходов и расходов |
| **Операции** | Доход, расход, перевод; дата, сумма, счёт, комментарий |
| **Периодические операции** | Ежемесячные шаблоны; автоматическое создание при первом запуске в новый день |
| **Планирование бюджета** | Месячные лимиты по подкатегориям; план vs факт |
| **Отчёты** | Круговая диаграмма по категориям/подкатегориям; таблица план vs факт |
| **Резервное копирование** | Экспорт/импорт JSON; ручной и автоматический ежедневный бекап (WorkManager) |
| **Настройки** | Язык (украинский, русский, английский), светлая/тёмная тема |

**Валюта:** только UAH (гривна).

### Технологии

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Архитектура | MVVM |
| Навигация | Navigation Compose |
| База данных | Room (SQLite) |
| Асинхронность | Kotlin Coroutines, Flow |
| Настройки | DataStore |
| Фоновые задачи | WorkManager |
| Сериализация | Gson |

**Версии:** minSdk 24, targetSdk 36, Kotlin 2.0.21, AGP 9.0.1.

### Сборка и установка

**Требования:** Android Studio (или совместимая IDE), JDK 11+, Android SDK.

```bash
./gradlew assembleDebug      # Сборка debug APK
./gradlew installDebug       # Установка на устройство/эмулятор
./gradlew assembleRelease    # Сборка release
```

В Android Studio: откройте проект, дождитесь синхронизации Gradle, затем запустите на устройстве или эмуляторе (▶️ Run).

### Навигация в приложении

- **Главная** — общий баланс, последние операции, быстрые переходы к счетам и категориям
- **Операции** — список и фильтры
- **Планирование** — бюджет
- **Отчёты** — диаграммы и план vs факт
- **Настройки** — язык, тема, резервное копирование

### Документация

| Файл | Описание |
|------|----------|
| **SPECIFICATION.md** | Полное техническое задание (украинский) |
| **gradle/libs.versions.toml** | Версии зависимостей |

---

## 🇺🇦 Українська

Мобільний додаток для ведення особистих фінансів на Android: облік доходів, витрат, переказів між рахунками, планування бюджету та звітність.

### Можливості

| Функція | Опис |
|---------|------|
| **Рахунки** | Декілька рахунків (картки, готівка тощо), опціональний початковий баланс |
| **Категорії** | Дворівнева ієрархія: категорія → підкатегорія, окремо для доходів та витрат |
| **Операції** | Дохід, витрата, переказ; дата, сума, рахунок, коментар |
| **Періодичні операції** | Щомісячні шаблони; автоматичне створення при першому запуску в новий день |
| **Планування бюджету** | Місячні ліміти по підкатегоріях; план vs факт |
| **Звіти** | Кругова діаграма по категоріях/підкатегоріях; таблиця план vs факт |
| **Резервне копіювання** | Експорт/імпорт JSON; ручний та автоматичний щоденний бекап (WorkManager) |
| **Налаштування** | Мова (українська, російська, англійська), світла/темна тема |

**Валюта:** лише UAH (гривня).

### Технології

| Компонент | Технологія |
|-----------|------------|
| Мова | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Архітектура | MVVM |
| Навігація | Navigation Compose |
| База даних | Room (SQLite) |
| Асинхронність | Kotlin Coroutines, Flow |
| Налаштування | DataStore |
| Фонові завдання | WorkManager |
| Серіалізація | Gson |

**Версії:** minSdk 24, targetSdk 36, Kotlin 2.0.21, AGP 9.0.1.

### Збірка та встановлення

**Вимоги:** Android Studio (або сумісна IDE), JDK 11+, Android SDK.

```bash
./gradlew assembleDebug      # Збірка debug APK
./gradlew installDebug       # Встановлення на пристрій/емулятор
./gradlew assembleRelease    # Збірка release
```

В Android Studio: відкрийте проєкт, дочекайтесь синхронізації Gradle, потім запустіть на пристрої або емуляторі (▶️ Run).

### Навігація в додатку

- **Головна** — загальний баланс, останні операції, швидкі переходи до рахунків та категорій
- **Операції** — список та фільтри
- **Планування** — бюджет
- **Звіти** — діаграми та план vs факт
- **Налаштування** — мова, тема, резервне копіювання

### Документація

| Файл | Опис |
|------|------|
| **SPECIFICATION.md** | Повне технічне завдання |
| **gradle/libs.versions.toml** | Версії залежностей |

---

## 🇬🇧 English

Android app for personal finance management: track income, expenses, transfers between accounts, budget planning, and reporting.

### Features

| Feature | Description |
|---------|-------------|
| **Accounts** | Multiple accounts (cards, cash, etc.), optional initial balance |
| **Categories** | Two-level hierarchy: category → subcategory, separate for income and expenses |
| **Transactions** | Income, expense, transfer; date, amount, account, comment |
| **Recurring Transactions** | Monthly templates; auto-created on first app launch each day |
| **Budget Planning** | Per-subcategory monthly limits; plan vs actual |
| **Reports** | Pie chart by category/subcategory; plan vs actual table |
| **Backup** | JSON export/import; manual and optional daily WorkManager backup |
| **Settings** | Language (Ukrainian, Russian, English), light/dark theme |

**Currency:** UAH only.

### Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM |
| Navigation | Navigation Compose |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines, Flow |
| Preferences | DataStore |
| Background | WorkManager |
| Serialization | Gson |

**Versions:** minSdk 24, targetSdk 36, Kotlin 2.0.21, AGP 9.0.1.

### Build & Install

**Requirements:** Android Studio (or compatible IDE), JDK 11+, Android SDK.

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew installDebug       # Install on device/emulator
./gradlew assembleRelease    # Build release
```

In Android Studio: open the project, wait for Gradle sync, then run on device or emulator (▶️ Run).

### App Navigation

- **Dashboard** — total balance, recent transactions, shortcuts to accounts and categories
- **Transactions** — list and filters
- **Planning** — budget
- **Reports** — charts and plan vs actual
- **Settings** — language, theme, backup

### Documentation

| File | Description |
|------|-------------|
| **SPECIFICATION.md** | Full specification (Ukrainian) |
| **gradle/libs.versions.toml** | Dependency versions |

---

## Структура проєкта | Project Structure

```
MyCash/
├── app/
│   ├── src/main/
│   │   ├── java/com/sh/mycash/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MyCashApplication.kt
│   │   │   ├── data/local/         # Room entities, DAOs, AppDatabase
│   │   │   ├── data/repository/
│   │   │   ├── data/backup/        # JSON export/import
│   │   │   ├── ui/screens/
│   │   │   ├── ui/navigation/
│   │   │   ├── ui/theme/
│   │   │   └── worker/             # BackupWorker
│   │   ├── res/values/, values-en/, values-ru/, values-uk/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── SPECIFICATION.md
```

---

## License | Ліцензія

Проєкт розробляється в особистих/освітніх цілях.  
The project is developed for personal/educational purposes.
