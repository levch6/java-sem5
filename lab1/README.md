# Лабораторна робота №1

**Дисципліна:** Технології розробки на платформі Java\
**Тема:** Сучасні можливості Java та підхід Data-Oriented Programming (DOP)\
**Автор:** Ренат Левченко, група ІП-44\
**Варіант:** 2 (Система сповіщень)

## Мета

Навчитися моделювати незмінні дані за допомогою Records та Sealed Types, розділяти дані й алгоритми,
реалізовувати вичерпну бізнес-логіку через Switch Expressions та Pattern Matching, а також
використовувати Text Blocks.

## Розрахунок варіанта

Варіант визначається остачею від ділення номера студента на 3:

```
V = N mod 3 = 11 mod 3 = 2
```

## Постановка задачі

Реалізувати ієрархію сповіщень і окремий клас-обробник, який перетворює сповіщення на текстовий звіт.
Ієрархія даних:

```
+ Notification
|--- TextMsg(String sender, String text)
|--- ImageMsg(String sender, String url, int sizeKb)
|--- AudioMsg(String sender, int durationSeconds)
```

Обмеження: дані незмінні, логіка винесена в окремий клас, обробка виконується switch expression
з pattern matching без гілки `default`, результат формується через Text Blocks.

## Виконана робота

Усі класи розміщено в пакеті `ua.kpi.comsys.dop`.

### Модель даних

- `Notification` : sealed interface з `permits TextMsg, ImageMsg, AudioMsg`. Компілятор знає всі
  можливі реалізації, тому switch по цьому типу може бути вичерпним без `default`.
- `TextMsg`, `ImageMsg`, `AudioMsg` : records, що реалізують `Notification`. Records є незмінними
  за замовчуванням: усі поля `private final`, сетерів немає.

### Валідація

Перевірки виконуються в компактних конструкторах records, тому некоректний об'єкт неможливо створити.

| Record | Поле | Умова помилки |
|---|---|---|
| `TextMsg` | `sender`, `text` | `null`, порожній або пробільний рядок |
| `ImageMsg` | `sender`, `url` | `null`, порожній або пробільний рядок |
| `ImageMsg` | `sizeKb` | `sizeKb <= 0` |
| `AudioMsg` | `sender` | `null`, порожній або пробільний рядок |
| `AudioMsg` | `durationSeconds` | `durationSeconds <= 0` |

В усіх випадках кидається `IllegalArgumentException`.

### Обробник

`NotificationProcessor.process(Notification)`:

- для `null` кидає `IllegalArgumentException("Notification cannot be null")`;
- використовує switch expression з record patterns (`case TextMsg(String sender, String text) -> ...`),
  що одразу деконструюють record на компоненти;
- кожна гілка повертає Text Block, заповнений через `String.formatted(...)`.

Формати результату:

```
Notification: TEXT
From: <sender>
Message: <text>
```

```
Notification: IMAGE
From: <sender>
URL: <url> (<sizeKb> KB)
```

```
Notification: AUDIO
From: <sender>
Duration: <durationSeconds> seconds
```

Приклад для `new AudioMsg("carol", 42)`:

```
Notification: AUDIO
From: carol
Duration: 42 seconds
```

## Структура модуля

```
lab1/
├── build.gradle.kts
└── src/
    ├── main/java/ua/kpi/comsys/dop/
    │   ├── Notification.java
    │   ├── TextMsg.java
    │   ├── ImageMsg.java
    │   ├── AudioMsg.java
    │   └── NotificationProcessor.java
    └── test/java/ua/kpi/comsys/dop/
        ├── NotificationProcessorTest.java
        └── NotificationValidationTest.java
```

## Тестування

Тести написано на JUnit 5.

- `NotificationProcessorTest` : перевірка точного тексту для кожної гілки switch (`TEXT`, `IMAGE`,
  `AUDIO`) та винятку з повідомленням для `null`.
- `NotificationValidationTest` : параметризовані тести (`@ParameterizedTest`, `@NullAndEmptySource`,
  `@ValueSource`) для всіх полів усіх records, включно з граничними значеннями `0`, `-1` та
  `Integer.MIN_VALUE`, а також перевірка створення коректних об'єктів.

## Запуск

```bash
./gradlew :lab1:test
```

Звіт: `lab1/build/reports/tests/test/index.html`.

## Висновки

Під час роботи змодельовано предметну область за допомогою sealed interface та records, реалізовано
валідацію даних у компактних конструкторах і вичерпну обробку через switch expression з record patterns.
Відсутність гілки `default` дозволяє компілятору перевіряти, що оброблено всі типи сповіщень.
