# 15 — Streams API

## What is the Streams API?

The **Streams API** (introduced in Java 8) allows you to process collections of data in a **declarative, functional** style — similar to SQL queries on data.

### Traditional vs Streams

```java
// ─── Traditional (imperative) ─────────────────────
List<String> result = new ArrayList<>();
for (String name : customers) {
    if (name.startsWith("A")) {
        result.add(name.toUpperCase());
    }
}
Collections.sort(result);

// ─── Streams (declarative) ────────────────────────
List<String> result = customers.stream()
    .filter(name -> name.startsWith("A"))
    .map(String::toUpperCase)
    .sorted()
    .collect(Collectors.toList());
```

> Streams describe **what** you want, not **how** to do it.

---

## Stream Pipeline — The Big Picture

Every stream operation follows a **pipeline** pattern:

```
  ┌──────────┐     ┌────────────────────────────┐     ┌───────────────┐
  │  SOURCE   │────►│  INTERMEDIATE OPERATIONS   │────►│  TERMINAL     │
  │           │     │  (lazy — not executed yet)  │     │  OPERATION    │
  └──────────┘     └────────────────────────────┘     │  (triggers    │
                                                       │   execution)  │
  Collection        filter(), map(), sorted(),         └───────────────┘
  Array              distinct(), limit(), skip(),       collect()
  File               flatMap(), peek()                  forEach()
  Generator                                             reduce()
                                                        count(), min(), max()
                                                        toArray()
                                                        findFirst(), anyMatch()
```

### Flow Diagram

```
  Source: [5, 12, 3, 8, 20, 1, 15]
            │
            ▼
  filter(n -> n > 5)      ──► [12, 8, 20, 15]       (intermediate)
            │
            ▼
  map(n -> n * 2)         ──► [24, 16, 40, 30]       (intermediate)
            │
            ▼
  sorted()                ──► [16, 24, 30, 40]       (intermediate)
            │
            ▼
  collect(toList())       ──► [16, 24, 30, 40]       (terminal — DONE!)
```

> **Key insight:** Intermediate operations are **lazy** — they don't execute until a terminal operation is called.

---

## Creating Streams

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.IntStream;

public class CreatingStreams {
    public static void main(String[] args) {

        // 1. From a Collection
        List<String> names = List.of("Ali", "Sara", "Hassan");
        Stream<String> stream1 = names.stream();

        // 2. From an Array
        String[] arr = {"A", "B", "C"};
        Stream<String> stream2 = Arrays.stream(arr);

        // 3. Using Stream.of()
        Stream<String> stream3 = Stream.of("X", "Y", "Z");

        // 4. Using Stream.generate() — infinite stream
        Stream<Double> randoms = Stream.generate(Math::random).limit(5);

        // 5. Using Stream.iterate() — infinite stream
        Stream<Integer> counting = Stream.iterate(0, n -> n + 2).limit(10);
        // 0, 2, 4, 6, 8, 10, 12, 14, 16, 18

        // 6. IntStream, LongStream, DoubleStream (primitive streams)
        IntStream intStream = IntStream.range(1, 11);      // 1..10
        IntStream intStream2 = IntStream.rangeClosed(1, 10); // 1..10

        // 7. From a String (characters)
        "Hello".chars().forEach(c -> System.out.print((char) c + " "));
        // H e l l o
    }
}
```

---

## Intermediate Operations (Lazy)

These return a **new Stream** and are NOT executed until a terminal operation is called.

### filter() — Select Elements

```java
// Keep only elements that match a condition (Predicate)

List<Integer> balances = List.of(500, 12000, 3000, 25000, 800, 50000);

List<Integer> highBalances = balances.stream()
    .filter(b -> b >= 10000)    // keep balances >= 10,000
    .collect(Collectors.toList());

System.out.println(highBalances);  // [12000, 25000, 50000]
```

```
  [500, 12000, 3000, 25000, 800, 50000]
         │                         │
  filter(b -> b >= 10000)
         │                         │
         ▼                         ▼
  [12000,       25000,        50000]
```

### map() — Transform Elements

```java
// Transform each element to something else (Function)

List<String> names = List.of("ali khan", "sara ahmed", "hassan raza");

List<String> upperNames = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

System.out.println(upperNames);  // [ALI KHAN, SARA AHMED, HASSAN RAZA]
```

```
  ["ali khan", "sara ahmed", "hassan raza"]
        │           │            │
  map(String::toUpperCase)
        │           │            │
        ▼           ▼            ▼
  ["ALI KHAN", "SARA AHMED", "HASSAN RAZA"]
```

### map() with Object Transformation

```java
// Extract a field from objects
List<String> accountIds = accounts.stream()
    .map(Account::getAccountId)     // Account → String
    .collect(Collectors.toList());
```

### sorted() — Sort Elements

```java
List<String> names = List.of("Hassan", "Ali", "Sara", "Fatima");

// Natural order
List<String> sorted = names.stream()
    .sorted()
    .collect(Collectors.toList());
// [Ali, Fatima, Hassan, Sara]

// Custom comparator
List<String> byLength = names.stream()
    .sorted(Comparator.comparingInt(String::length))
    .collect(Collectors.toList());
// [Ali, Sara, Hassan, Fatima]

// Reverse order
List<String> reversed = names.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());
// [Sara, Hassan, Fatima, Ali]
```

### distinct() — Remove Duplicates

```java
List<String> cities = List.of("Karachi", "Lahore", "Karachi", "Islamabad", "Lahore");

List<String> unique = cities.stream()
    .distinct()
    .collect(Collectors.toList());
// [Karachi, Lahore, Islamabad]
```

### limit() and skip() — Pagination

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// First 3
List<Integer> firstThree = numbers.stream()
    .limit(3)
    .collect(Collectors.toList());
// [1, 2, 3]

// Skip first 5, take next 3
List<Integer> page = numbers.stream()
    .skip(5)
    .limit(3)
    .collect(Collectors.toList());
// [6, 7, 8]
```

```
  [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
                    │
  skip(5) ──────────┘
  [6, 7, 8, 9, 10]
       │
  limit(3)
  [6, 7, 8]
```

### flatMap() — Flatten Nested Structures

```java
// When each element maps to a stream, flatMap merges them into one stream

List<List<String>> nestedAccounts = List.of(
    List.of("ACC-01", "ACC-02"),
    List.of("ACC-03"),
    List.of("ACC-04", "ACC-05", "ACC-06")
);

List<String> allAccounts = nestedAccounts.stream()
    .flatMap(List::stream)          // flatten
    .collect(Collectors.toList());
// [ACC-01, ACC-02, ACC-03, ACC-04, ACC-05, ACC-06]
```

```
  flatMap — Flattening:

  BEFORE:  [[A, B], [C], [D, E, F]]
                    │
  flatMap(List::stream)
                    │
  AFTER:   [A, B, C, D, E, F]
```

### peek() — Debug / Inspect

```java
// peek() lets you see elements as they pass through (useful for debugging)

List<String> result = names.stream()
    .filter(n -> n.length() > 3)
    .peek(n -> System.out.println("After filter: " + n))
    .map(String::toUpperCase)
    .peek(n -> System.out.println("After map: " + n))
    .collect(Collectors.toList());
```

---

## Terminal Operations (Trigger Execution)

### collect() — Gather Results

```java
import java.util.stream.Collectors;

List<String> names = List.of("Ali", "Sara", "Hassan", "Ali", "Fatima");

// To List
List<String> list = names.stream().collect(Collectors.toList());

// To Set (removes duplicates)
Set<String> set = names.stream().collect(Collectors.toSet());

// To Map
Map<String, Integer> nameLengths = names.stream()
    .distinct()
    .collect(Collectors.toMap(
        name -> name,               // key
        name -> name.length()       // value
    ));
// {Ali=3, Sara=4, Hassan=6, Fatima=6}

// Joining strings
String joined = names.stream()
    .distinct()
    .collect(Collectors.joining(", "));
// "Ali, Sara, Hassan, Fatima"

// Grouping
Map<Integer, List<String>> byLength = names.stream()
    .distinct()
    .collect(Collectors.groupingBy(String::length));
// {3=[Ali], 4=[Sara], 6=[Hassan, Fatima]}

// Partitioning (true/false split)
Map<Boolean, List<String>> partitioned = names.stream()
    .distinct()
    .collect(Collectors.partitioningBy(n -> n.length() > 4));
// {false=[Ali, Sara], true=[Hassan, Fatima]}
```

### forEach() — Perform Action on Each Element

```java
names.stream()
    .filter(n -> n.startsWith("A"))
    .forEach(System.out::println);
// Ali
```

> **Warning:** `forEach()` is terminal — you can't chain more operations after it.

### reduce() — Combine All Elements into One

```java
List<Integer> amounts = List.of(1000, 2500, 500, 3000);

// Sum
int total = amounts.stream()
    .reduce(0, Integer::sum);
// 7000

// With identity and accumulator explained:
// reduce(identity, accumulator)
//   identity = starting value (0)
//   accumulator = how to combine two values (Integer::sum)

// Find max using reduce
int max = amounts.stream()
    .reduce(Integer.MIN_VALUE, Integer::max);
// 3000

// String concatenation
String allNames = List.of("Ali", "Sara", "Hassan").stream()
    .reduce("", (a, b) -> a + " " + b)
    .trim();
// "Ali Sara Hassan"
```

### Diagram: How reduce Works

```
  reduce(0, Integer::sum) on [1000, 2500, 500, 3000]

  Step 1:  0 + 1000 = 1000
  Step 2:  1000 + 2500 = 3500
  Step 3:  3500 + 500 = 4000
  Step 4:  4000 + 3000 = 7000  ← final result

  ┌─────┐   ┌──────┐   ┌──────┐   ┌─────┐   ┌──────┐
  │  0  │──►│ 1000 │──►│ 3500 │──►│4000 │──►│ 7000 │
  └─────┘   └──────┘   └──────┘   └─────┘   └──────┘
  identity   +1000      +2500      +500       +3000
```

### count(), min(), max()

```java
List<Integer> balances = List.of(500, 12000, 3000, 25000, 800);

long count = balances.stream().filter(b -> b > 5000).count();    // 2

int min = balances.stream().min(Integer::compareTo).orElse(0);   // 500
int max = balances.stream().max(Integer::compareTo).orElse(0);   // 25000
```

### findFirst(), findAny()

```java
Optional<String> first = names.stream()
    .filter(n -> n.startsWith("S"))
    .findFirst();

first.ifPresent(System.out::println);  // "Sara"

// findAny() — returns any match (useful in parallel streams)
Optional<String> any = names.parallelStream()
    .filter(n -> n.length() > 4)
    .findAny();
```

### anyMatch(), allMatch(), noneMatch()

```java
List<Integer> balances = List.of(500, 12000, 3000, 25000, 800);

boolean anyRich = balances.stream().anyMatch(b -> b > 20000);    // true
boolean allRich = balances.stream().allMatch(b -> b > 20000);    // false
boolean noneBroke = balances.stream().noneMatch(b -> b < 0);     // true
```

---

## Complete Banking Example

```java
import java.util.*;
import java.util.stream.Collectors;

public class StreamsBankingDemo {

    record Customer(String id, String name, String city, double balance, String accountType) {}

    public static void main(String[] args) {

        List<Customer> customers = List.of(
            new Customer("C001", "Ali Khan",     "Karachi",   25000, "Savings"),
            new Customer("C002", "Sara Ahmed",   "Lahore",    18500, "Checking"),
            new Customer("C003", "Hassan Raza",  "Islamabad", 42000, "Savings"),
            new Customer("C004", "Fatima Noor",  "Karachi",    5000, "Checking"),
            new Customer("C005", "Omar Farooq",  "Lahore",    75000, "Savings"),
            new Customer("C006", "Ayesha Malik", "Karachi",   31000, "Savings"),
            new Customer("C007", "Bilal Shah",   "Islamabad",  8000, "Checking"),
            new Customer("C008", "Zainab Ali",   "Lahore",   120000, "Savings")
        );

        // ─── 1. Filter: High-value customers (balance > 30000) ───
        System.out.println("=== High-Value Customers ===");
        customers.stream()
            .filter(c -> c.balance() > 30000)
            .forEach(c -> System.out.println("  " + c.name() + " - $" + c.balance()));

        // ─── 2. Map: Extract customer names ──────────────────────
        System.out.println("\n=== All Customer Names ===");
        List<String> names = customers.stream()
            .map(Customer::name)
            .collect(Collectors.toList());
        System.out.println("  " + names);

        // ─── 3. Sort by balance (descending) ─────────────────────
        System.out.println("\n=== Sorted by Balance (Descending) ===");
        customers.stream()
            .sorted(Comparator.comparingDouble(Customer::balance).reversed())
            .forEach(c -> System.out.printf("  %-15s $%,.2f%n", c.name(), c.balance()));

        // ─── 4. Group by city ────────────────────────────────────
        System.out.println("\n=== Customers by City ===");
        Map<String, List<Customer>> byCity = customers.stream()
            .collect(Collectors.groupingBy(Customer::city));

        byCity.forEach((city, custs) -> {
            System.out.println("  " + city + ":");
            custs.forEach(c -> System.out.println("    - " + c.name()));
        });

        // ─── 5. Group by account type, count ────────────────────
        System.out.println("\n=== Count by Account Type ===");
        Map<String, Long> countByType = customers.stream()
            .collect(Collectors.groupingBy(Customer::accountType, Collectors.counting()));
        countByType.forEach((type, count) ->
            System.out.println("  " + type + ": " + count));

        // ─── 6. Total balance ────────────────────────────────────
        double totalBalance = customers.stream()
            .mapToDouble(Customer::balance)
            .sum();
        System.out.printf("%n=== Total Balance: $%,.2f ===%n", totalBalance);

        // ─── 7. Average balance ──────────────────────────────────
        OptionalDouble avgBalance = customers.stream()
            .mapToDouble(Customer::balance)
            .average();
        avgBalance.ifPresent(avg ->
            System.out.printf("=== Average Balance: $%,.2f ===%n", avg));

        // ─── 8. Statistics ───────────────────────────────────────
        DoubleSummaryStatistics stats = customers.stream()
            .mapToDouble(Customer::balance)
            .summaryStatistics();
        System.out.println("\n=== Balance Statistics ===");
        System.out.printf("  Count:   %d%n", stats.getCount());
        System.out.printf("  Sum:     $%,.2f%n", stats.getSum());
        System.out.printf("  Min:     $%,.2f%n", stats.getMin());
        System.out.printf("  Max:     $%,.2f%n", stats.getMax());
        System.out.printf("  Average: $%,.2f%n", stats.getAverage());

        // ─── 9. Top 3 richest customers ─────────────────────────
        System.out.println("\n=== Top 3 Richest ===");
        customers.stream()
            .sorted(Comparator.comparingDouble(Customer::balance).reversed())
            .limit(3)
            .forEach(c -> System.out.printf("  %s - $%,.2f%n", c.name(), c.balance()));

        // ─── 10. Comma-separated names of Karachi customers ─────
        String karachiNames = customers.stream()
            .filter(c -> c.city().equals("Karachi"))
            .map(Customer::name)
            .collect(Collectors.joining(", "));
        System.out.println("\n=== Karachi Customers: " + karachiNames + " ===");

        // ─── 11. Partition: balance > 20000 vs <= 20000 ─────────
        Map<Boolean, List<Customer>> partitioned = customers.stream()
            .collect(Collectors.partitioningBy(c -> c.balance() > 20000));
        System.out.println("\n=== High Balance (>20000) ===");
        partitioned.get(true).forEach(c -> System.out.println("  " + c.name()));
        System.out.println("=== Low Balance (<=20000) ===");
        partitioned.get(false).forEach(c -> System.out.println("  " + c.name()));

        // ─── 12. Any customer with balance > 100000? ─────────────
        boolean hasVIP = customers.stream().anyMatch(c -> c.balance() > 100000);
        System.out.println("\n=== Has VIP (>100k): " + hasVIP + " ===");
    }
}
```

### Output

```
=== High-Value Customers ===
  Hassan Raza - $42000.0
  Omar Farooq - $75000.0
  Ayesha Malik - $31000.0
  Zainab Ali - $120000.0

=== All Customer Names ===
  [Ali Khan, Sara Ahmed, Hassan Raza, Fatima Noor, Omar Farooq, Ayesha Malik, Bilal Shah, Zainab Ali]

=== Sorted by Balance (Descending) ===
  Zainab Ali      $120,000.00
  Omar Farooq     $75,000.00
  Hassan Raza     $42,000.00
  Ayesha Malik    $31,000.00
  Ali Khan        $25,000.00
  Sara Ahmed      $18,500.00
  Bilal Shah      $8,000.00
  Fatima Noor     $5,000.00

=== Customers by City ===
  Karachi:
    - Ali Khan
    - Fatima Noor
    - Ayesha Malik
  Lahore:
    - Sara Ahmed
    - Omar Farooq
    - Zainab Ali
  Islamabad:
    - Hassan Raza
    - Bilal Shah

=== Count by Account Type ===
  Savings: 5
  Checking: 3

=== Total Balance: $324,500.00 ===
=== Average Balance: $40,562.50 ===

=== Balance Statistics ===
  Count:   8
  Sum:     $324,500.00
  Min:     $5,000.00
  Max:     $120,000.00
  Average: $40,562.50

=== Top 3 Richest ===
  Zainab Ali - $120,000.00
  Omar Farooq - $75,000.00
  Hassan Raza - $42,000.00

=== Karachi Customers: Ali Khan, Fatima Noor, Ayesha Malik ===

=== High Balance (>20000) ===
  Ali Khan
  Hassan Raza
  Omar Farooq
  Ayesha Malik
  Zainab Ali
=== Low Balance (<=20000) ===
  Sara Ahmed
  Fatima Noor
  Bilal Shah

=== Has VIP (>100k): true ===
```

---

## Parallel Streams

For large datasets, use **parallel streams** to utilize multiple CPU cores:

```java
// Sequential
long count = customers.stream()
    .filter(c -> c.balance() > 10000)
    .count();

// Parallel — same result, potentially faster on large data
long count = customers.parallelStream()
    .filter(c -> c.balance() > 10000)
    .count();
```

### Diagram

```
  Sequential Stream:
  [A, B, C, D, E, F, G, H]
   │  │  │  │  │  │  │  │
   ▼  ▼  ▼  ▼  ▼  ▼  ▼  ▼    (one thread processes all)
  [result]

  Parallel Stream:
  [A, B, C, D, E, F, G, H]
   ┌──┴──┐  ┌──┴──┐  ┌──┴──┐  ┌──┴──┐
   Thread1  Thread2  Thread3  Thread4    (split across CPU cores)
   ├──────┤  ├──────┤  ├──────┤  ├──────┤
   └──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘
      └────┬─────┘         └────┬─────┘
           └──────┬─────────────┘
              [combined result]
```

> **When to use parallel:** Large datasets (10,000+ elements), CPU-intensive operations.
> **When NOT to use parallel:** Small datasets, I/O operations, when order matters.

---

## Stream Operations — Quick Reference

```
  ┌────────────────────────────────────────────────────────────────────┐
  │                    STREAMS API CHEAT SHEET                        │
  ├────────────────────┬───────────────────────────────────────────────┤
  │ INTERMEDIATE       │ filter(Predicate)  — keep matching elements  │
  │ (returns Stream)   │ map(Function)      — transform elements      │
  │                    │ flatMap(Function)  — flatten nested streams   │
  │                    │ sorted()           — natural order sort       │
  │                    │ sorted(Comparator) — custom sort              │
  │                    │ distinct()         — remove duplicates        │
  │                    │ limit(n)           — take first n             │
  │                    │ skip(n)            — skip first n             │
  │                    │ peek(Consumer)     — debug/inspect            │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ TERMINAL           │ collect(Collector) — gather into collection   │
  │ (returns result)   │ forEach(Consumer)  — perform action           │
  │                    │ reduce(identity, BinaryOp) — combine all      │
  │                    │ count()            — count elements            │
  │                    │ min(Comparator)    — find minimum              │
  │                    │ max(Comparator)    — find maximum              │
  │                    │ findFirst()        — first element (Optional)  │
  │                    │ findAny()          — any element (Optional)    │
  │                    │ anyMatch(Pred)     — any match? (boolean)      │
  │                    │ allMatch(Pred)     — all match? (boolean)      │
  │                    │ noneMatch(Pred)    — none match? (boolean)     │
  │                    │ toArray()          — convert to array          │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ COLLECTORS         │ toList()           — to ArrayList              │
  │                    │ toSet()            — to HashSet                │
  │                    │ toMap(keyFn, valFn)— to HashMap                │
  │                    │ joining(delim)     — concatenate strings       │
  │                    │ groupingBy(fn)     — group into Map<K, List>   │
  │                    │ partitioningBy(p)  — split true/false          │
  │                    │ counting()         — count in group            │
  │                    │ summarizingDouble  — statistics                │
  └────────────────────┴───────────────────────────────────────────────┘
```
