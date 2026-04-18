# 19 — Functional Interfaces: Predicate, Function, Consumer, Supplier

## What is a Functional Interface?

A **functional interface** is an interface with **exactly one abstract method**. It serves as the target type for **lambda expressions** and **method references**.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);           // ← the ONE abstract method
    // can have default methods
    // can have static methods
}
```

> The `@FunctionalInterface` annotation is **optional** but recommended — the compiler enforces the single-abstract-method rule.

---

## The Big Four — Core Functional Interfaces

```
  ┌────────────────────────────────────────────────────────────────────┐
  │                                                                    │
  │   Predicate<T>      T → boolean      "Does it match?"             │
  │   Function<T,R>     T → R            "Transform it"               │
  │   Consumer<T>       T → void         "Do something with it"       │
  │   Supplier<T>       () → T           "Give me something"          │
  │                                                                    │
  └────────────────────────────────────────────────────────────────────┘

  Diagram:

  ┌───────┐                ┌─────────┐
  │ Input │───Predicate───►│ boolean │    Is this account active?
  │   T   │                └─────────┘
  └───────┘

  ┌───────┐                ┌─────────┐
  │ Input │───Function────►│ Output  │    Convert Customer to DTO
  │   T   │                │   R     │
  └───────┘                └─────────┘

  ┌───────┐                ┌─────────┐
  │ Input │───Consumer────►│  void   │    Log this transaction
  │   T   │                └─────────┘
  └───────┘

  ┌───────┐                ┌─────────┐
  │ (none)│───Supplier────►│ Output  │    Generate transaction ID
  │       │                │   T     │
  └───────┘                └─────────┘
```

---

## 1. Predicate<T> — Testing / Filtering

**Signature:** `boolean test(T t)`
**Purpose:** Test a condition, return true or false.

### Core Methods

| Method           | Description                        |
|------------------|------------------------------------|
| `test(T t)`      | Evaluate the predicate             |
| `and(Predicate)` | Logical AND (both must be true)    |
| `or(Predicate)`  | Logical OR (at least one true)     |
| `negate()`       | Logical NOT (invert result)        |
| `Predicate.isEqual(target)` | Static: equals check      |
| `Predicate.not(p)` | Static: negate (Java 11+)       |

### Code Example

```java
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PredicateDeepDive {

    record Account(String id, String owner, double balance, String type, boolean active) {}

    public static void main(String[] args) {

        List<Account> accounts = List.of(
            new Account("ACC-01", "Ali Khan",     25000, "Savings",  true),
            new Account("ACC-02", "Sara Ahmed",   18500, "Checking", true),
            new Account("ACC-03", "Hassan Raza",  42000, "Savings",  false),
            new Account("ACC-04", "Fatima Noor",   5000, "Checking", true),
            new Account("ACC-05", "Omar Farooq",  75000, "Savings",  true)
        );

        // ─── Define predicates ───────────────────────
        Predicate<Account> isActive    = Account::active;
        Predicate<Account> isSavings   = acc -> acc.type().equals("Savings");
        Predicate<Account> isHighValue = acc -> acc.balance() > 20000;

        // ─── Compose predicates ──────────────────────
        Predicate<Account> isActiveHighSavings = isActive
            .and(isSavings)
            .and(isHighValue);

        Predicate<Account> isInactive = Predicate.not(isActive);   // Java 11+
        // or: isActive.negate()

        // ─── Apply with streams ──────────────────────
        System.out.println("=== Active High-Value Savings ===");
        accounts.stream()
            .filter(isActiveHighSavings)
            .forEach(a -> System.out.println("  " + a.owner() + " - $" + a.balance()));
        // Ali Khan - $25000.0
        // Omar Farooq - $75000.0

        System.out.println("\n=== Inactive Accounts ===");
        accounts.stream()
            .filter(isInactive)
            .forEach(a -> System.out.println("  " + a.owner()));
        // Hassan Raza

        // ─── Reusable filter method ──────────────────
        System.out.println("\n=== Custom Filter ===");
        filterAndPrint(accounts, isSavings.or(isHighValue));
    }

    static void filterAndPrint(List<Account> accounts, Predicate<Account> criteria) {
        accounts.stream()
            .filter(criteria)
            .forEach(a -> System.out.println("  " + a.owner()));
    }
}
```

### Diagram — Predicate Composition Chain

```
  Input: Account("Ali", 25000, "Savings", active=true)

  isActive ────────► true ──┐
                             │ AND
  isSavings ───────► true ──┤────► true ──┐
                                          │ AND
  isHighValue ─────► true ────────────────┤────► true ✓ (PASSES)
                                          │
  Final: true AND true AND true = true    │

  Input: Account("Sara", 18500, "Checking", active=true)

  isActive ────────► true ──┐
                             │ AND
  isSavings ───────► false ─┤────► false ──► REJECTED ✗
```

---

## 2. Function<T, R> — Transformation / Mapping

**Signature:** `R apply(T t)`
**Purpose:** Transform input of type T to output of type R.

### Core Methods

| Method                         | Description                          |
|--------------------------------|--------------------------------------|
| `apply(T t)`                   | Apply the function                   |
| `andThen(Function<R, V>)`     | Chain: first this, then the other    |
| `compose(Function<V, T>)`     | Chain: first the other, then this    |
| `Function.identity()`          | Static: returns input unchanged      |

### Code Example

```java
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionDeepDive {

    record Customer(String id, String name, double balance) {}
    record CustomerDTO(String displayName, String balanceFormatted) {}

    public static void main(String[] args) {

        List<Customer> customers = List.of(
            new Customer("C001", "Ali Khan",     25000),
            new Customer("C002", "Sara Ahmed",   18500),
            new Customer("C003", "Hassan Raza",  42000)
        );

        // ─── Simple transformation ───────────────────
        Function<Customer, String> toName = Customer::name;
        Function<Customer, Double> toBalance = Customer::balance;
        Function<Double, String> formatMoney = b -> String.format("$%,.2f", b);

        // ─── Chaining with andThen ───────────────────
        Function<Customer, String> toFormattedBalance = toBalance.andThen(formatMoney);
        // Customer → Double → String

        System.out.println("=== Formatted Balances ===");
        customers.stream()
            .map(toFormattedBalance)
            .forEach(System.out::println);
        // $25,000.00
        // $18,500.00
        // $42,000.00

        // ─── Complex transformation: Customer → DTO ──
        Function<Customer, CustomerDTO> toDTO = customer -> new CustomerDTO(
            customer.name().toUpperCase(),
            formatMoney.apply(customer.balance())
        );

        System.out.println("\n=== Customer DTOs ===");
        List<CustomerDTO> dtos = customers.stream()
            .map(toDTO)
            .collect(Collectors.toList());
        dtos.forEach(dto -> System.out.println("  " + dto));

        // ─── compose vs andThen ──────────────────────
        Function<String, String> trim = String::trim;
        Function<String, String> upper = String::toUpperCase;

        // andThen: trim first, then uppercase
        Function<String, String> cleanUp = trim.andThen(upper);
        System.out.println("\n" + cleanUp.apply("  hello  "));  // HELLO

        // compose: uppercase first, then trim
        Function<String, String> cleanUp2 = trim.compose(upper);
        System.out.println(cleanUp2.apply("  hello  "));  // HELLO

        // ─── identity — returns input unchanged ──────
        Function<String, String> noChange = Function.identity();
        System.out.println(noChange.apply("Same"));  // Same
    }
}
```

### Diagram — andThen vs compose

```
  Function<A, B> f = ...
  Function<B, C> g = ...

  f.andThen(g):     A ──f──► B ──g──► C
                    Execute f first, then g

  f.compose(g):     This COMPILES only if g: X→A
                    X ──g──► A ──f──► B
                    Execute g first, then f

  Example:
  trim.andThen(upper):     "  hi  " ──trim──► "hi" ──upper──► "HI"
  upper.compose(trim):     "  hi  " ──trim──► "hi" ──upper──► "HI"
  (same result, different perspective)
```

---

## 3. Consumer<T> — Performing Actions

**Signature:** `void accept(T t)`
**Purpose:** Perform an action on input, return nothing.

### Core Methods

| Method                    | Description                          |
|---------------------------|--------------------------------------|
| `accept(T t)`             | Perform the action                   |
| `andThen(Consumer<T>)`    | Chain: do this, then the other       |

### Code Example

```java
import java.util.List;
import java.util.function.Consumer;

public class ConsumerDeepDive {

    record Transaction(String id, String type, double amount, String accountId) {}

    public static void main(String[] args) {

        List<Transaction> transactions = List.of(
            new Transaction("TXN-01", "DEPOSIT",    5000, "ACC-01"),
            new Transaction("TXN-02", "WITHDRAWAL", 2000, "ACC-01"),
            new Transaction("TXN-03", "TRANSFER",   8000, "ACC-02")
        );

        // ─── Simple consumers ────────────────────────
        Consumer<Transaction> logToConsole = txn ->
            System.out.println("[LOG] " + txn.id() + ": " + txn.type() + " $" + txn.amount());

        Consumer<Transaction> validate = txn -> {
            if (txn.amount() <= 0) {
                System.out.println("[WARN] Invalid amount: " + txn.id());
            }
        };

        Consumer<Transaction> sendNotification = txn ->
            System.out.println("[SMS] Transaction " + txn.id() + " processed for " + txn.accountId());

        // ─── Chaining with andThen ───────────────────
        Consumer<Transaction> fullPipeline = validate
            .andThen(logToConsole)
            .andThen(sendNotification);

        System.out.println("=== Processing Transactions ===");
        transactions.forEach(fullPipeline);

        // ─── Passing consumer as parameter ───────────
        System.out.println("\n=== Deposits Only ===");
        processTransactions(transactions, "DEPOSIT", logToConsole);
    }

    static void processTransactions(List<Transaction> txns, String type, Consumer<Transaction> action) {
        txns.stream()
            .filter(t -> t.type().equals(type))
            .forEach(action);
    }
}
```

### Output

```
=== Processing Transactions ===
[LOG] TXN-01: DEPOSIT $5000.0
[SMS] Transaction TXN-01 processed for ACC-01
[LOG] TXN-02: WITHDRAWAL $2000.0
[SMS] Transaction TXN-02 processed for ACC-02
[LOG] TXN-03: TRANSFER $8000.0
[SMS] Transaction TXN-03 processed for ACC-02

=== Deposits Only ===
[LOG] TXN-01: DEPOSIT $5000.0
```

### Diagram — Consumer Pipeline (andThen)

```
  validate.andThen(logToConsole).andThen(sendNotification)

  Transaction ──► validate ──► logToConsole ──► sendNotification
                     │              │                 │
                  (check)      (print log)       (send SMS)
                     │              │                 │
                     ▼              ▼                 ▼
                   void           void             void

  Each consumer performs its action and passes the SAME input
  to the next consumer in the chain.
```

---

## 4. Supplier<T> — Providing / Generating Values

**Signature:** `T get()`
**Purpose:** Supply a value without any input. Factory pattern for lazy initialization.

### Code Example

```java
import java.util.function.Supplier;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SupplierDeepDive {

    record Account(String id, String owner, double balance) {}

    public static void main(String[] args) {

        // ─── Generate unique IDs ─────────────────────
        Supplier<String> transactionIdGen = () ->
            "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        System.out.println("=== Generated IDs ===");
        System.out.println(transactionIdGen.get());   // TXN-A3F2B1C8
        System.out.println(transactionIdGen.get());   // TXN-7D9E4F2A
        System.out.println(transactionIdGen.get());   // TXN-E1B8C3D5

        // ─── Default value factory ───────────────────
        Supplier<Account> defaultAccount = () ->
            new Account("ACC-DEFAULT", "Guest", 0.0);

        Account guest = defaultAccount.get();
        System.out.println("\nDefault: " + guest);

        // ─── Lazy initialization ─────────────────────
        Supplier<List<Account>> heavyLoad = () -> {
            System.out.println("  [Loading accounts from database...]");
            return List.of(
                new Account("ACC-01", "Ali", 25000),
                new Account("ACC-02", "Sara", 18500)
            );
        };

        System.out.println("\nBefore calling get():");
        System.out.println("  No database call yet!");
        System.out.println("Calling get() now:");
        List<Account> accounts = heavyLoad.get();  // NOW it loads
        System.out.println("Loaded " + accounts.size() + " accounts");

        // ─── Generate infinite stream ────────────────
        Supplier<Double> randomBalance = () ->
            Math.round(Math.random() * 100000) / 1.0;

        System.out.println("\n=== Random Accounts ===");
        List<String> randomAccounts = Stream.generate(randomBalance)
            .limit(5)
            .map(b -> String.format("$%,.2f", b))
            .collect(Collectors.toList());
        System.out.println(randomAccounts);

        // ─── Use with orElseGet ──────────────────────
        String name = null;
        Supplier<String> fallback = () -> "Anonymous Customer";
        String result = (name != null) ? name : fallback.get();
        System.out.println("\nCustomer: " + result);   // Anonymous Customer
    }
}
```

### Diagram — Supplier as Factory

```
  Supplier<T>: Takes NOTHING, produces T

  ┌────────────┐     ┌─────────────────────────┐
  │ () ────────│────►│ "TXN-A3F2B1C8"          │
  │ (no input) │     │ (new value each call)    │
  └────────────┘     └─────────────────────────┘

  Each .get() call produces a new/different value:

  get() ──► "TXN-A3F2B1C8"
  get() ──► "TXN-7D9E4F2A"     ← different!
  get() ──► "TXN-E1B8C3D5"     ← different!
```

---

## 5. Specialized Variants

### Bi-Variants (Two Input Parameters)

| Interface              | Method                    | Description              |
|------------------------|---------------------------|--------------------------|
| `BiPredicate<T,U>`    | `test(T t, U u) → boolean`| Test with two inputs     |
| `BiFunction<T,U,R>`   | `apply(T t, U u) → R`     | Transform with two inputs|
| `BiConsumer<T,U>`      | `accept(T t, U u) → void` | Action with two inputs   |

### Operator Variants (Same Input/Output Type)

| Interface              | Method              | Description              |
|------------------------|---------------------|--------------------------|
| `UnaryOperator<T>`    | `apply(T t) → T`     | Function<T,T> shorthand  |
| `BinaryOperator<T>`   | `apply(T,T) → T`     | BiFunction<T,T,T>        |

### Primitive Variants (Avoid Boxing)

| Interface              | Method                   | Description              |
|------------------------|--------------------------|--------------------------|
| `IntPredicate`         | `test(int) → boolean`    | No boxing overhead       |
| `IntFunction<R>`       | `apply(int) → R`         | int → any type           |
| `IntConsumer`          | `accept(int) → void`     | Consume an int           |
| `IntSupplier`          | `getAsInt() → int`       | Supply an int            |
| `ToIntFunction<T>`     | `applyAsInt(T) → int`    | Any type → int           |
| `IntUnaryOperator`     | `applyAsInt(int) → int`  | int → int                |
| `IntBinaryOperator`    | `applyAsInt(int,int)→int`| (int, int) → int         |

> Similar for `Long` and `Double` variants.

### Code: Bi-Variants

```java
import java.util.function.*;

public class BiVariantsExample {
    public static void main(String[] args) {

        // BiPredicate — two inputs, boolean output
        BiPredicate<Double, Double> canWithdraw = (amount, balance) -> amount <= balance;
        System.out.println("Can withdraw $500 from $1000? " + canWithdraw.test(500.0, 1000.0));
        System.out.println("Can withdraw $1500 from $1000? " + canWithdraw.test(1500.0, 1000.0));

        // BiFunction — two inputs, different output
        BiFunction<String, Double, String> formatAccount =
            (name, balance) -> String.format("%s: $%,.2f", name, balance);
        System.out.println(formatAccount.apply("Ali Khan", 25000.0));

        // BiConsumer — two inputs, no output
        BiConsumer<String, Double> printTransaction =
            (type, amount) -> System.out.printf("[%s] Amount: $%,.2f%n", type, amount);
        printTransaction.accept("DEPOSIT", 5000.0);

        // BinaryOperator — two same-type inputs, same-type output
        BinaryOperator<Double> add = Double::sum;
        System.out.println("Sum: " + add.apply(100.0, 250.0));
    }
}
```

---

## 6. Complete Banking Example — All Interfaces Combined

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class FunctionalInterfacesBankingDemo {

    record Account(String id, String owner, double balance, String type, boolean active) {}

    public static void main(String[] args) {

        List<Account> accounts = List.of(
            new Account("ACC-01", "Ali Khan",     25000, "Savings",  true),
            new Account("ACC-02", "Sara Ahmed",   18500, "Checking", true),
            new Account("ACC-03", "Hassan Raza",  42000, "Savings",  false),
            new Account("ACC-04", "Fatima Noor",   5000, "Checking", true),
            new Account("ACC-05", "Omar Farooq",  75000, "Savings",  true)
        );

        // ─── Predicate: Define filters ───────────────
        Predicate<Account> isActive    = Account::active;
        Predicate<Account> isSavings   = a -> a.type().equals("Savings");
        Predicate<Account> isHighValue = a -> a.balance() > 20000;

        // ─── Function: Define transformations ────────
        Function<Account, String> toSummary = a ->
            String.format("[%s] %s — $%,.2f (%s)", a.id(), a.owner(), a.balance(), a.type());

        Function<List<Account>, DoubleSummaryStatistics> toStats = list ->
            list.stream().mapToDouble(Account::balance).summaryStatistics();

        // ─── Consumer: Define actions ────────────────
        Consumer<Account> notify = a ->
            System.out.println("  📧 Notification sent to " + a.owner());

        Consumer<Account> logAccount = a ->
            System.out.println("  [LOG] Processed: " + a.id());

        // ─── Supplier: Define factories ──────────────
        Supplier<String> reportId = () ->
            "RPT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // ═══════════════════════════════════════════════
        // USING THEM ALL TOGETHER
        // ═══════════════════════════════════════════════

        System.out.println("Report ID: " + reportId.get());        // Supplier

        System.out.println("\n=== Active Savings Accounts (Predicate + Function) ===");
        accounts.stream()
            .filter(isActive.and(isSavings))                       // Predicate
            .map(toSummary)                                         // Function
            .forEach(System.out::println);                          // Consumer (method ref)

        System.out.println("\n=== Notify High-Value Customers (Predicate + Consumer) ===");
        accounts.stream()
            .filter(isActive.and(isHighValue))                     // Predicate
            .forEach(notify.andThen(logAccount));                   // Consumer chain

        System.out.println("\n=== Statistics (Function) ===");
        DoubleSummaryStatistics stats = toStats.apply(accounts);   // Function
        System.out.printf("  Count:   %d%n", stats.getCount());
        System.out.printf("  Total:   $%,.2f%n", stats.getSum());
        System.out.printf("  Average: $%,.2f%n", stats.getAverage());
        System.out.printf("  Min:     $%,.2f%n", stats.getMin());
        System.out.printf("  Max:     $%,.2f%n", stats.getMax());

        // ─── Reusable processing engine ──────────────
        System.out.println("\n=== Reusable Engine ===");
        processAccounts(
            accounts,
            isActive.and(isSavings),        // Predicate
            toSummary,                       // Function
            System.out::println             // Consumer
        );
    }

    // Generic processing engine using functional interfaces
    static <T, R> void processAccounts(
            List<T> items,
            Predicate<T> filter,
            Function<T, R> transformer,
            Consumer<R> action) {

        items.stream()
            .filter(filter)
            .map(transformer)
            .forEach(action);
    }
}
```

### Output

```
Report ID: RPT-A3F2B1

=== Active Savings Accounts (Predicate + Function) ===
[ACC-01] Ali Khan — $25,000.00 (Savings)
[ACC-05] Omar Farooq — $75,000.00 (Savings)

=== Notify High-Value Customers (Predicate + Consumer) ===
  📧 Notification sent to Ali Khan
  [LOG] Processed: ACC-01
  📧 Notification sent to Omar Farooq
  [LOG] Processed: ACC-05

=== Statistics (Function) ===
  Count:   5
  Total:   $165,500.00
  Average: $33,100.00
  Min:     $5,000.00
  Max:     $75,000.00

=== Reusable Engine ===
[ACC-01] Ali Khan — $25,000.00 (Savings)
[ACC-05] Omar Farooq — $75,000.00 (Savings)
```

---

## 7. Creating Your Own Functional Interfaces

```java
@FunctionalInterface
interface TransactionProcessor<T> {
    boolean process(T transaction, double amount);
    // can have default methods:
    default void log(String msg) { System.out.println("[LOG] " + msg); }
}

@FunctionalInterface
interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}

public class CustomFunctionalDemo {
    public static void main(String[] args) {

        // Custom two-param interface
        TransactionProcessor<String> withdraw = (accountId, amount) -> {
            System.out.printf("Withdrawing $%.2f from %s%n", amount, accountId);
            return amount <= 50000;  // within limit
        };

        boolean success = withdraw.process("ACC-001", 5000);
        System.out.println("Success: " + success);

        // Custom three-param function
        TriFunction<String, String, Double, String> createReceipt =
            (from, to, amount) -> String.format("Transfer $%,.2f from %s to %s", amount, from, to);

        System.out.println(createReceipt.apply("ACC-01", "ACC-02", 5000.0));
    }
}
```

---

## Summary — Complete Reference

```
  ┌────────────────────────────────────────────────────────────────────┐
  │              FUNCTIONAL INTERFACES CHEAT SHEET                    │
  ├──────────────────────┬─────────────────────────────────────────────┤
  │ CORE FOUR            │                                             │
  │  Predicate<T>        │  T → boolean     filter, validate, test     │
  │  Function<T,R>       │  T → R           transform, map, convert    │
  │  Consumer<T>         │  T → void        forEach, log, print, save  │
  │  Supplier<T>         │  () → T          factory, generate, lazy    │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ BI-VARIANTS          │                                             │
  │  BiPredicate<T,U>    │  (T,U) → boolean                           │
  │  BiFunction<T,U,R>   │  (T,U) → R                                 │
  │  BiConsumer<T,U>     │  (T,U) → void                              │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ OPERATORS            │                                             │
  │  UnaryOperator<T>    │  T → T           (extends Function<T,T>)   │
  │  BinaryOperator<T>   │  (T,T) → T       (extends BiFunction<T,T,T>)│
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ COMPOSITION          │                                             │
  │  Predicate:          │  .and()  .or()  .negate()  Predicate.not() │
  │  Function:           │  .andThen()  .compose()  Function.identity()│
  │  Consumer:           │  .andThen()                                 │
  └──────────────────────┴─────────────────────────────────────────────┘
```
