# Технології розробки на платформі Java

Лабораторні роботи з дисципліни "Технології розробки на платформі Java", КПІ ім. Ігоря Сікорського.

**Автор:** Ренат Левченко\
**Група:** ІП-44\
**Номер у списку групи:** 11\
**Варіант:** `V = 11 mod 3 = 2`

## Зміст

| № | Тема | Модуль | Пакет |
|---|---|---|---|
| 1 | Data-Oriented Programming: records, sealed types, pattern matching | [`lab1`](lab1/README.md) | `ua.kpi.comsys.dop` |
| 2 | Gradle, Version Catalog, Java Collections Framework | [`lab2`](lab2/README.md) | `ua.kpi.comsys.collections` |
| 3 | Нескінченні потоки, Optional, Stream Gatherers | [`lab3`](lab3/README.md) | `ua.kpi.comsys.streams` |

## Короткий опис робіт

**Лабораторна робота №1. Система сповіщень.**
Ієрархія `Notification` на основі sealed interface та records (`TextMsg`, `ImageMsg`, `AudioMsg`)
з валідацією в конструкторах. Обробник `NotificationProcessor` формує текстовий звіт через switch
expression з record patterns та Text Blocks.

**Лабораторна робота №2. Частотний аналізатор тексту.**
Клас `WordCounter` рахує кількість входжень слів та повертає множину унікальних слів. Використано
лише стандартні колекції `java.util`. Залежності керуються через Gradle Version Catalog.

**Лабораторна робота №3. Обробка безпекових подій.**
Клас `SecurityLogAnalyzer` працює з нескінченними потоками логів: пошук першої критичної події,
збір подій до дедлайну та виявлення серій подій від однієї IP-адреси за допомогою власного `Gatherer`.

## Структура репозиторію

```
.
├── gradle/
│   ├── libs.versions.toml    # version catalog
│   └── wrapper/
├── lab1/                     # Лабораторна робота №1
├── lab2/                     # Лабораторна робота №2
├── lab3/                     # Лабораторна робота №3
├── settings.gradle.kts
└── README.md
```

Кожна лабораторна робота є окремим модулем Gradle зі своїм `build.gradle.kts` та `README.md`.
Модулі підключені в `settings.gradle.kts`. Версії залежностей задані в спільному каталозі
`gradle/libs.versions.toml`, тому оновлення версії JUnit виконується в одному місці.

## Технології

| Інструмент | Версія |
|---|---|
| Java (toolchain) | 25 |
| Gradle (wrapper) | 9.6 |
| JUnit | 5.13.4 |

## Вимоги

- JDK 25
- Gradle окремо встановлювати не потрібно, використовується wrapper (`./gradlew`)

## Збірка та тести

Усі модулі:

```bash
./gradlew build
```

Окремий модуль:

```bash
./gradlew :lab1:test
./gradlew :lab2:test
./gradlew :lab3:test
```

Повторний запуск тестів без кешу Gradle:

```bash
./gradlew test --rerun-tasks
```

Звіти тестів: `labN/build/reports/tests/test/index.html`.

## Робота в IntelliJ IDEA

Відкрити кореневу папку репозиторію як Gradle-проєкт. IDE автоматично імпортує всі три модулі.
Тести можна запускати як з вікна Gradle, так і безпосередньо з тестових класів.

## Ліцензія

Проєкт поширюється за ліцензією MIT. Текст ліцензії: [LICENSE](LICENSE).
