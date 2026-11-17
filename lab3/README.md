# Лабораторна робота №3

**Дисципліна:** Технології розробки на платформі Java\
**Тема:** Нескінченні потоки, ледачі операції Stream API, Optional та Stream Gatherers\
**Автор:** Ренат Левченко, група ІП-44\
**Варіант:** 2 (Обробка безпекових подій)

## Мета

Опанувати роботу з нескінченними потоками (`Stream.generate`, `Stream.iterate`), навчитися
контролювати виконання за допомогою операцій короткого замикання (`findFirst`, `takeWhile`),
уникати блокування при роботі з нескінченними джерелами та реалізувати власну stateful проміжну
операцію через `java.util.stream.Gatherer`.

## Розрахунок варіанта

```
V = N mod 3 = 11 mod 3 = 2
```

## Постановка задачі

Пакет `ua.kpi.comsys.streams`. Модель даних:

```java
public record SecurityLog(String ipAddress, String severityLevel,
                          String eventDescription, LocalDateTime timestamp) {}
```

Сервіс:

```java
public class SecurityLogAnalyzer {
    public Optional<SecurityLog> findFirstCriticalEvent(Stream<SecurityLog> stream) { ... }
    public List<SecurityLog> getLogsBeforeDeadline(Stream<SecurityLog> stream, LocalDateTime deadline) { ... }
    public List<String> detectIpBursts(Stream<SecurityLog> stream, int burstThreshold, int maxIpsToReport) { ... }
}
```

Усі методи повинні коректно працювати на нескінченних стрімах і кидати `IllegalArgumentException`
для `null` чи некоректних аргументів.

## Виконана робота

### Огляд методів

| Метод | Реалізація | Чому не зависає |
|---|---|---|
| `findFirstCriticalEvent` | `filter` за рівнем `"CRITICAL"` + `findFirst` | `findFirst` є операцією короткого замикання |
| `getLogsBeforeDeadline` | `takeWhile(log -> log.timestamp().isBefore(deadline))` | `takeWhile` обриває стрім на першій події не раніше дедлайну |
| `detectIpBursts` | власний `Gatherer.ofSequential` | інтегратор повертає `false`, коли звіт заповнено |

### findFirstCriticalEvent

Повертає `Optional<SecurityLog>` з першою подією рівня `"CRITICAL"`. Порівняння виконується як
`"CRITICAL".equals(log.severityLevel())`, тому `null` у полі рівня не спричиняє помилки. Якщо такої
події немає у скінченному стрімі, повертається `Optional.empty()`.

### getLogsBeforeDeadline

Збирає події, доки їхній час строго менший за `deadline`. Перша подія з часом, що дорівнює дедлайну
або пізніша, зупиняє обробку, навіть якщо далі є ранні події. Це відповідає семантиці "збирати, доки
не настане дедлайн" для впорядкованого в часі потоку.

### detectIpBursts та Gatherer

Gatherer створюється через `Gatherer.ofSequential(initializer, integrator)` і зберігає стан
у локальному класі `State`:

| Поле стану | Призначення |
|---|---|
| `currentIp` | IP-адреса поточної неперервної серії |
| `streak` | довжина поточної серії |
| `reported` | множина вже знайдених IP-адрес |

Логіка інтегратора для кожної події:

1. Якщо IP збігається з `currentIp`, лічильник `streak` збільшується, інакше починається нова серія
   зі значенням 1.
2. Коли `streak` досягає `burstThreshold` і IP ще не було у звіті, адреса передається далі
   через `downstream.push(...)`. Кожна IP-адреса потрапляє у звіт лише один раз.
3. Інтегратор повертає `false`, коли кількість знайдених адрес досягла `maxIpsToReport`, або коли
   downstream більше не приймає елементи. Після цього стрім перестає читати джерело.

Приклад для `burstThreshold = 3`:

```
вхід:  A A B A A B B B C C C
серії: A=2, B=1, A=2, B=3, C=3
звіт:  [B, C]
```

### Валідація та Optional

| Аргумент | Умова помилки |
|---|---|
| `stream` | `null` |
| `deadline` | `null` |
| `burstThreshold` | `<= 0` |
| `maxIpsToReport` | `<= 0` |

Перевірка на `null` винесена в допоміжний метод і виконана через
`Optional.ofNullable(value).orElseThrow(() -> new IllegalArgumentException(...))`.
Виклики `Optional.get()` без перевірок у коді відсутні.

## Структура модуля

```
lab3/
├── build.gradle.kts
└── src/
    ├── main/java/ua/kpi/comsys/streams/
    │   ├── SecurityLog.java
    │   └── SecurityLogAnalyzer.java
    └── test/java/ua/kpi/comsys/streams/
        └── SecurityLogAnalyzerTest.java
```

## Тестування

`SecurityLogAnalyzerTest` (JUnit 5) розбито на `@Nested` класи за методами. Анотація `@Timeout(2)`
на рівні класу гарантує, що жоден тест не зависає на нескінченному стрімі.

- **FindFirstCriticalEvent** : пошук у нескінченному стрімі (`Stream.iterate`), вибір першої з
  кількох критичних подій, перевірка, що з `Stream.generate` прочитано рівно стільки елементів,
  скільки потрібно, порожній та скінченний стрім без результату, `null`.
- **GetLogsBeforeDeadline** : збір подій з нескінченного стріму, зупинка на першій пізній події,
  порожній результат, скінченний стрім повністю до дедлайну, `null`-аргументи.
- **DetectIpBursts** : пошук серій у нескінченному стрімі, перервані серії, унікальність IP у звіті,
  довга серія, поріг 1, зупинка читання джерела після заповнення звіту, скінченний стрім,
  некоректні значення `burstThreshold` та `maxIpsToReport`.

## Запуск

```bash
./gradlew :lab3:test
```

Звіт: `lab3/build/reports/tests/test/index.html`.

## Висновки

Реалізовано сервіс аналізу безпекових подій, що безпечно працює з нескінченними потоками завдяки
ледачим операціям та короткому замиканню. Власний Gatherer зберігає стан між елементами потоку,
виявляє серії подій від однієї IP-адреси і сам зупиняє обробку після досягнення ліміту звіту.
