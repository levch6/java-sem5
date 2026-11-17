# Лабораторна робота №2

**Дисципліна:** Технології розробки на платформі Java\
**Тема:** Збірка проєкту за допомогою Gradle та робота з Java Collections Framework\
**Автор:** Ренат Левченко, група ІП-44\
**Варіант:** 2 (Частотний аналізатор тексту)

## Мета

Навчитися конфігурувати систему збірки Gradle із використанням Version Catalogs, застосовувати
стандартні колекції Java та обробляти граничні випадки.

## Розрахунок варіанта

```
V = N mod 3 = 11 mod 3 = 2
```

## Постановка задачі

Реалізувати клас `WordCounter` у пакеті `ua.kpi.comsys.collections`:

```java
public class WordCounter {
    public Map<String, Integer> countWords(List<String> textLines) { ... }
    public Set<String> getUniqueWords(List<String> textLines) { ... }
}
```

Правила обробки тексту:

- рядок розбивається на слова, словом вважається послідовність буквено-цифрових символів латиниці;
- усі слова приводяться до нижнього регістру;
- порожні слова після розбиття ігноруються;
- для `textLines == null` обидва методи кидають `IllegalArgumentException`.

Сторонні бібліотеки для колекцій заборонені, лише `java.util.*`.

## Виконана робота

### Алгоритм

1. Перевірка аргументу: для `null` кидається `IllegalArgumentException("textLines cannot be null")`.
2. Кожен рядок розбивається за регулярним виразом `[^A-Za-z0-9]+`. Отже роздільником є будь-яка
   послідовність символів, що не є латинською літерою чи цифрою (пробіли, пунктуація, кирилиця тощо).
3. Порожні токени (наприклад, коли рядок починається з роздільника) відкидаються.
4. Токени приводяться до нижнього регістру через `toLowerCase(Locale.ROOT)`, щоб результат
   не залежав від локалі системи.
5. `null`-рядки всередині списку пропускаються.

Скомпільований `Pattern` зберігається в статичній константі, щоб не компілювати регулярний вираз
при кожному виклику.

### Методи

| Метод | Колекція | Опис |
|---|---|---|
| `countWords` | `HashMap<String, Integer>` | лічильник оновлюється через `Map.merge(word, 1, Integer::sum)` |
| `getUniqueWords` | `HashSet<String>` | множина всіх слів, дублікати відкидаються автоматично |

Обидва методи використовують спільний приватний метод `extractWords`, тому правила розбиття
однакові, а множина з `getUniqueWords` завжди збігається з ключами `countWords`.

### Приклад

Вхідні дані:

```
"Hello, world!  Hello...world?"
"java21 and JAVA21"
```

Результат `countWords`:

```
{hello=2, world=2, java21=2, and=1}
```

## Gradle

- Проєкт збирається командою `./gradlew build`, тести запускаються через `./gradlew test`.
- Залежності керуються через каталог версій `gradle/libs.versions.toml` у корені репозиторію:

```toml
[versions]
junit = "5.13.4"

[libraries]
junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }
```

- У `lab2/build.gradle.kts` залежності підключаються через типізовані аксесори `libs.junit.*`,
  а версія JUnit задається один раз через BOM.
- Версія Java задана через toolchain (JDK 25).

## Структура модуля

```
lab2/
├── build.gradle.kts
└── src/
    ├── main/java/ua/kpi/comsys/collections/
    │   └── WordCounter.java
    └── test/java/ua/kpi/comsys/collections/
        └── WordCounterTest.java
```

## Тестування

`WordCounterTest` (JUnit 5) покриває:

- підрахунок слів у кількох рядках;
- ігнорування регістру;
- розбиття за пунктуацією, кількома пробілами та табуляцією;
- слова з цифрами та окремі числа;
- кирилицю як роздільник;
- порожній список, порожні рядки та рядки лише з розділових знаків;
- `null`-рядки всередині списку;
- `null` як аргумент для обох методів;
- відповідність результату `getUniqueWords` ключам `countWords`.

## Запуск

```bash
./gradlew :lab2:build
./gradlew :lab2:test
```

Звіт: `lab2/build/reports/tests/test/index.html`.

## Висновки

Налаштовано багатомодульну збірку Gradle з каталогом версій, реалізовано частотний аналіз тексту
на стандартних колекціях `HashMap` та `HashSet`. Граничні випадки (порожні дані, `null`, різні
роздільники) оброблено та перевірено тестами.
