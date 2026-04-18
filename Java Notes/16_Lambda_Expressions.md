# 16 — Lambda Expressions

## What is a Lambda Expression?

A **lambda expression** is a short, anonymous (unnamed) function that you can pass around as a value. Introduced in **Java 8**, it enables **functional programming** in Java.

### Before vs After Lambdas

```java
// ─── BEFORE (Java 7): Anonymous inner class ──────
Comparator<String> comparator = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
};

// ─── AFTER (Java 8+): Lambda expression ──────────
Comparator<String> comparator = (a, b) -> a.length() - b.length();
```

> Same behavior, **much less boilerplate**.

---

## Lambda Syntax

```
  ┌──────────────────────────────────────────────────────────────┐
  │                                                              │
  │   (parameters) -> expression                                 │
  │                                                              │
  │   (parameters) -> { statements; }                            │
  │                                                              │
  └──────────────────────────────────────────────────────────────┘

  Parts:
  ┌──────────────┐    ┌───────┐    ┌────────────────────┐
  │  Parameters   │───►│ Arrow │───►│  Body              │
  │  (a, b)       │    │  ->   │    │  a + b             │
  └──────────────┘    └───────┘    └────────────────────┘
```

### All Syntax Variations

```java
// ─── No parameters ───────────────────────────────
Runnable r = () -> System.out.println("Hello");

// ─── One parameter (parentheses optional) ────────
Consumer<String> print = name -> System.out.println(name);
Consumer<String> print2 = (name) -> System.out.println(name);  // also valid

// ─── Two parameters ─────────────────────────────
Comparator<Integer> compare = (a, b) -> a - b;

// ─── With explicit types ─────────────────────────
Comparator<Integer> compare2 = (Integer a, Integer b) -> a - b;

// ─── Multi-line body (needs braces + return) ─────
Comparator<String> compare3 = (a, b) -> {
    System.out.println("Comparing: " + a + " vs " + b);
    return a.compareTo(b);
};

// ─── Returning a value (single expression) ───────
Function<Integer, Integer> square = x -> x * x;
// No 'return' keyword needed for single expressions!

// ─── Returning a value (multi-line) ──────────────
Function<Integer, Integer> factorial = n -> {
    int result = 1;
    for (int i = 1; i <= n; i++) {
        result *= i;
    }
    return result;   // 'return' required with braces
};
```

---

## Functional Interfaces

A lambda can ONLY be used where a **functional interface** is expected. A functional interface has **exactly one abstract method**.

### Diagram

```
  ┌──────────────────────────────────────────────────────┐
  │            FUNCTIONAL INTERFACE                       │
  │                                                      │
  │  @FunctionalInterface                                │
  │  public interface Predicate<T> {                     │
  │      boolean test(T t);      ← ONE abstract method   │
  │  }                                                   │
  │                                                      │
  │  Lambda:  (t) -> t > 5                               │
  │  Becomes: Predicate<Integer> p = t -> t > 5;         │
  └──────────────────────────────────────────────────────┘
```

### Built-in Functional Interfaces (java.util.function)

| Interface           | Method              | Input → Output   | Use Case                    |
|---------------------|---------------------|------------------|-----------------------------|
| `Predicate<T>`      | `test(T) → boolean` | T → boolean      | Filtering, conditions       |
| `Function<T,R>`     | `apply(T) → R`      | T → R            | Transformation, mapping     |
| `Consumer<T>`       | `accept(T) → void`  | T → void         | Performing actions          |
| `Supplier<T>`       | `get() → T`         | () → T           | Providing/generating values |
| `BiFunction<T,U,R>` | `apply(T,U) → R`    | (T, U) → R       | Two-input transformation    |
| `BiPredicate<T,U>`  | `test(T,U) → boolean`| (T, U) → boolean| Two-input condition         |
| `UnaryOperator<T>`  | `apply(T) → T`      | T → T            | Same-type transformation    |
| `BinaryOperator<T>` | `apply(T,T) → T`    | (T, T) → T       | Combine two same-type values|
| `Comparator<T>`     | `compare(T,T) → int`| (T, T) → int     | Ordering/sorting            |

### Diagram: How Lambda Maps to Interface

```
  Predicate<Integer> isPositive = n -> n > 0;

  This is equivalent to:

  Predicate<Integer> isPositive = new Predicate<Integer>() {
      @Override
      public boolean test(Integer n) {
          return n > 0;
      }
  };

  ┌─────────────────────┐
  │   Lambda             │
  │   n -> n > 0         │
  ├─────────────────────┤
  │   Parameter: n       │ ──► maps to test(Integer n)
  │   Body: n > 0        │ ──► maps to return n > 0;
  └─────────────────────┘
```

---

## Predicate — Filtering / Testing Conditions

```java
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PredicateExample {
    public static void main(String[] args) {

        // ─── Simple Predicate ────────────────────────
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> isEven = n -> n % 2 == 0;

        System.out.println(isPositive.test(5));    // true
        System.out.println(isPositive.test(-3));   // false
        System.out.println(isEven.test(4));        // true

        // ─── Combining Predicates ────────────────────
        Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
        Predicate<Integer> isPositiveOrEven = isPositive.or(isEven);
        Predicate<Integer> isNotPositive = isPositive.negate();

        System.out.println(isPositiveAndEven.test(4));   // true
        System.out.println(isPositiveAndEven.test(-4));  // false
        System.out.println(isNotPositive.test(-3));      // true

        // ─── Using with Streams ──────────────────────
        List<Integer> numbers = List.of(-5, 3, -2, 8, 0, -1, 7, 4);

        List<Integer> positiveEvens = numbers.stream()
            .filter(isPositiveAndEven)
            .collect(Collectors.toList());
        System.out.println("Positive & Even: " + positiveEvens);  // [8, 4]

        // ─── Banking predicate ───────────────────────
        Predicate<Double> isHighValue = balance -> balance > 50000;
        Predicate<Double> isActive = balance -> balance > 0;
        Predicate<Double> isVIP = isHighValue.and(isActive);

        System.out.println("Is VIP? " + isVIP.test(75000.0));  // true
    }
}
```

### Diagram: Predicate Composition

```
  Predicate<Integer> isPositive = n -> n > 0;
  Predicate<Integer> isEven    = n -> n % 2 == 0;

  isPositive.and(isEven):
  ┌──────────┐     ┌─────────┐
  │ n > 0 ?  │─YES─│ n%2==0? │─YES─► TRUE
  │          │     │         │
  │          │─NO──│         │─NO──► FALSE
  └──────────┘     └─────────┘

  isPositive.or(isEven):
  ┌──────────┐
  │ n > 0 ?  │─YES────────────────► TRUE
  │          │
  │          │─NO──┌─────────┐
  └──────────┘     │ n%2==0? │─YES─► TRUE
                   │         │─NO──► FALSE
                   └─────────┘

  isPositive.negate():
  ┌──────────┐
  │ n > 0 ?  │─YES─► FALSE
  │          │─NO──► TRUE
  └──────────┘
```

---

## Function — Transformation / Mapping

```java
import java.util.function.Function;

public class FunctionExample {
    public static void main(String[] args) {

        // ─── Basic transformation ────────────────────
        Function<String, Integer> length = String::length;
        Function<String, String> toUpper = String::toUpperCase;
        Function<Integer, Integer> doubleIt = n -> n * 2;

        System.out.println(length.apply("Hello"));       // 5
        System.out.println(toUpper.apply("hello"));       // HELLO
        System.out.println(doubleIt.apply(21));            // 42

        // ─── Chaining with andThen / compose ─────────
        Function<Integer, Integer> addTen = n -> n + 10;
        Function<Integer, Integer> multiplyThree = n -> n * 3;

        // andThen: first addTen, THEN multiplyThree
        Function<Integer, Integer> addThenMultiply = addTen.andThen(multiplyThree);
        System.out.println(addThenMultiply.apply(5));    // (5+10) * 3 = 45

        // compose: first multiplyThree, THEN addTen
        Function<Integer, Integer> multiplyThenAdd = addTen.compose(multiplyThree);
        System.out.println(multiplyThenAdd.apply(5));    // (5*3) + 10 = 25

        // ─── Banking: format account balance ─────────
        Function<Double, String> formatBalance = balance ->
            String.format("$%,.2f", balance);

        System.out.println(formatBalance.apply(1234567.89));  // $1,234,567.89
    }
}
```

### Diagram: andThen vs compose

```
  addTen.andThen(multiplyThree):
  Input: 5
  ┌──────────┐     ┌────────────────┐
  │ addTen   │────►│ multiplyThree  │────► Result
  │ 5 + 10   │     │ 15 * 3         │
  │ = 15     │     │ = 45           │
  └──────────┘     └────────────────┘

  addTen.compose(multiplyThree):
  Input: 5
  ┌────────────────┐     ┌──────────┐
  │ multiplyThree  │────►│ addTen   │────► Result
  │ 5 * 3          │     │ 15 + 10  │
  │ = 15           │     │ = 25     │
  └────────────────┘     └──────────┘
```

---

## Consumer — Performing Actions (No Return)

```java
import java.util.List;
import java.util.function.Consumer;

public class ConsumerExample {
    public static void main(String[] args) {

        // ─── Basic consumer ──────────────────────────
        Consumer<String> print = System.out::println;
        Consumer<String> yellPrint = s -> System.out.println(">>> " + s + " <<<");

        print.accept("Hello");              // Hello
        yellPrint.accept("Important!");     // >>> Important! <<<

        // ─── Chaining with andThen ───────────────────
        Consumer<String> printAndYell = print.andThen(yellPrint);
        printAndYell.accept("Alert");
        // Alert
        // >>> Alert <<<

        // ─── With forEach ────────────────────────────
        List<String> customers = List.of("Ali", "Sara", "Hassan");

        Consumer<String> greet = name ->
            System.out.println("Welcome to NexaBank, " + name + "!");

        customers.forEach(greet);
        // Welcome to NexaBank, Ali!
        // Welcome to NexaBank, Sara!
        // Welcome to NexaBank, Hassan!
    }
}
```

---

## Supplier — Providing / Generating Values

```java
import java.util.function.Supplier;
import java.util.UUID;

public class SupplierExample {
    public static void main(String[] args) {

        // ─── Generate unique IDs ─────────────────────
        Supplier<String> idGenerator = () -> "TXN-" + UUID.randomUUID().toString().substring(0, 8);

        System.out.println(idGenerator.get());  // TXN-a3f2b1c8
        System.out.println(idGenerator.get());  // TXN-7d9e4f2a

        // ─── Default value supplier ──────────────────
        Supplier<Double> defaultBalance = () -> 0.0;

        Double balance = null;
        double result = (balance != null) ? balance : defaultBalance.get();
        System.out.println("Balance: " + result);  // 0.0

        // ─── Lazy initialization ─────────────────────
        Supplier<List<String>> expensiveList = () -> {
            System.out.println("Creating expensive list...");
            return List.of("A", "B", "C");
        };

        // List is NOT created yet
        System.out.println("Before get()");
        List<String> list = expensiveList.get();  // NOW it's created
        System.out.println("After get(): " + list);
    }
}
```

---

## Method References

**Method references** are a shorthand for lambdas that call an **existing method**.

### Four Types of Method References

```
  ┌─────────────────────────────────────────────────────────────────┐
  │  Type                     │ Lambda               │ Method Ref   │
  ├───────────────────────────┼──────────────────────┼──────────────┤
  │ Static method             │ x -> Math.abs(x)     │ Math::abs    │
  │ Instance method (bound)   │ x -> str.equals(x)   │ str::equals  │
  │ Instance method (unbound) │ s -> s.toUpperCase() │ String::toUpperCase │
  │ Constructor               │ () -> new ArrayList()│ ArrayList::new│
  └───────────────────────────┴──────────────────────┴──────────────┘
```

### Code Examples

```java
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class MethodReferenceExample {
    public static void main(String[] args) {

        List<String> names = List.of("ali", "sara", "hassan");

        // ─── 1. Static Method Reference ──────────────
        // Lambda:        s -> Integer.parseInt(s)
        // Method ref:    Integer::parseInt
        Function<String, Integer> parse = Integer::parseInt;
        System.out.println(parse.apply("42"));  // 42

        // ─── 2. Instance Method (unbound) ────────────
        // Lambda:        s -> s.toUpperCase()
        // Method ref:    String::toUpperCase
        List<String> upper = names.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println(upper);  // [ALI, SARA, HASSAN]

        // ─── 3. Instance Method (bound) ──────────────
        // Lambda:        s -> System.out.println(s)
        // Method ref:    System.out::println
        names.forEach(System.out::println);

        // ─── 4. Constructor Reference ────────────────
        // Lambda:        () -> new ArrayList<>()
        // Method ref:    ArrayList::new
        Supplier<ArrayList<String>> listFactory = ArrayList::new;
        ArrayList<String> newList = listFactory.get();
        System.out.println("New list: " + newList);
    }
}
```

### Diagram: Lambda → Method Reference

```
  ┌─────────────────────────────┐     ┌──────────────────────┐
  │ Lambda                      │     │ Method Reference      │
  ├─────────────────────────────┤     ├──────────────────────┤
  │ s -> s.toUpperCase()        │ ──► │ String::toUpperCase  │
  │ x -> Math.abs(x)           │ ──► │ Math::abs            │
  │ s -> System.out.println(s) │ ──► │ System.out::println  │
  │ () -> new Account()        │ ──► │ Account::new         │
  └─────────────────────────────┘     └──────────────────────┘

  Rule of thumb: If the lambda just PASSES the parameter
  to an existing method, use a method reference.
```

---

## Creating Custom Functional Interfaces

```java
// ─── Custom functional interface ─────────────────
@FunctionalInterface
interface TransactionValidator {
    boolean validate(double amount, double balance);
}

// ─── Another custom interface ────────────────────
@FunctionalInterface
interface AccountFormatter {
    String format(String accountId, String name, double balance);
}

public class CustomFunctionalInterfaceDemo {
    public static void main(String[] args) {

        // ─── Using TransactionValidator ──────────────
        TransactionValidator withdrawalCheck = (amount, balance) -> amount <= balance;
        TransactionValidator depositCheck = (amount, balance) -> amount > 0;
        TransactionValidator limitCheck = (amount, balance) -> amount <= 100_000;

        double amount = 5000;
        double balance = 10000;

        System.out.println("Can withdraw? " + withdrawalCheck.validate(amount, balance));
        System.out.println("Valid deposit? " + depositCheck.validate(amount, balance));
        System.out.println("Within limit? " + limitCheck.validate(amount, balance));

        // ─── Using AccountFormatter ──────────────────
        AccountFormatter simple = (id, name, bal) ->
            id + " - " + name + " ($" + bal + ")";

        AccountFormatter detailed = (id, name, bal) ->
            String.format("Account: %s%n  Owner: %s%n  Balance: $%,.2f", id, name, bal);

        System.out.println(simple.format("ACC-001", "Ali Khan", 25000));
        System.out.println(detailed.format("ACC-001", "Ali Khan", 25000));
    }
}
```

### Output

```
Can withdraw? true
Valid deposit? true
Within limit? true
ACC-001 - Ali Khan ($25000.0)
Account: ACC-001
  Owner: Ali Khan
  Balance: $25,000.00
```

---

## Lambdas and Effectively Final Variables

Lambdas can access **local variables** from the enclosing scope, but those variables must be **effectively final** (never reassigned after initialization).

```java
public class EffectivelyFinalDemo {
    public static void main(String[] args) {

        String bankName = "NexaBank";   // effectively final — never reassigned

        // ✓ This works — bankName is effectively final
        Consumer<String> greet = name ->
            System.out.println("Welcome to " + bankName + ", " + name);
        greet.accept("Ali");

        // ✗ This would NOT compile:
        // int counter = 0;
        // Runnable r = () -> counter++;  // ERROR: counter is not effectively final

        // ─── Workaround: use an array or AtomicInteger ──
        int[] counter = {0};   // array reference is final, contents can change
        Runnable increment = () -> counter[0]++;
        increment.run();
        increment.run();
        System.out.println("Counter: " + counter[0]);  // 2
    }
}
```

### Diagram

```
  ┌──────────────────────────────────────────────────┐
  │  Enclosing scope:                                │
  │    String bankName = "NexaBank";  // final ✓     │
  │    int count = 0;                 // mutable ✗   │
  │                                                  │
  │    Lambda: name -> {                             │
  │      bankName   ← can read ✓ (effectively final) │
  │      count++    ← COMPILE ERROR ✗ (not final)    │
  │    }                                             │
  └──────────────────────────────────────────────────┘
```

---

## Complete Banking Example — Lambdas Everywhere

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class LambdaBankingDemo {

    record Account(String id, String owner, double balance, String type) {}

    public static void main(String[] args) {

        List<Account> accounts = List.of(
            new Account("ACC-01", "Ali Khan",     25000, "Savings"),
            new Account("ACC-02", "Sara Ahmed",   18500, "Checking"),
            new Account("ACC-03", "Hassan Raza",  42000, "Savings"),
            new Account("ACC-04", "Fatima Noor",   5000, "Checking"),
            new Account("ACC-05", "Omar Farooq",  75000, "Savings")
        );

        // ─── Predicate: Filter conditions ────────────
        Predicate<Account> isHighValue  = acc -> acc.balance() > 20000;
        Predicate<Account> isSavings    = acc -> acc.type().equals("Savings");
        Predicate<Account> isVIP        = isHighValue.and(isSavings);

        System.out.println("=== VIP Accounts (Savings + >20000) ===");
        accounts.stream()
            .filter(isVIP)
            .forEach(acc -> System.out.println("  " + acc.owner() + " - $" + acc.balance()));

        // ─── Function: Transform data ────────────────
        Function<Account, String> toDisplayString = acc ->
            String.format("[%s] %s: $%,.2f (%s)", acc.id(), acc.owner(), acc.balance(), acc.type());

        System.out.println("\n=== All Accounts (Formatted) ===");
        accounts.stream()
            .map(toDisplayString)
            .forEach(System.out::println);

        // ─── Consumer: Process each account ──────────
        Consumer<Account> sendNotification = acc ->
            System.out.println("  SMS sent to " + acc.owner() + ": Your balance is $" + acc.balance());

        System.out.println("\n=== Sending Notifications to VIPs ===");
        accounts.stream()
            .filter(isVIP)
            .forEach(sendNotification);

        // ─── Comparator: Custom sorting ──────────────
        Comparator<Account> byBalance = Comparator.comparingDouble(Account::balance);
        Comparator<Account> byBalanceDesc = byBalance.reversed();
        Comparator<Account> byTypeTheBalance = Comparator.comparing(Account::type)
            .thenComparing(byBalanceDesc);

        System.out.println("\n=== Sorted by Type, then Balance (Desc) ===");
        accounts.stream()
            .sorted(byTypeTheBalance)
            .map(toDisplayString)
            .forEach(System.out::println);

        // ─── Supplier: Generate transaction IDs ──────
        Supplier<String> txnIdGenerator = () -> "TXN-" + UUID.randomUUID().toString().substring(0, 8);

        System.out.println("\n=== Generated Transaction IDs ===");
        for (int i = 0; i < 3; i++) {
            System.out.println("  " + txnIdGenerator.get());
        }

        // ─── BinaryOperator: Combine/reduce ─────────
        BinaryOperator<Double> sum = Double::sum;
        double totalBalance = accounts.stream()
            .map(Account::balance)
            .reduce(0.0, sum);
        System.out.printf("%n=== Total Balance: $%,.2f ===%n", totalBalance);

        // ─── Passing lambdas as method parameters ────
        System.out.println("\n=== Custom Processing ===");
        processAccounts(accounts, isSavings, toDisplayString, System.out::println);
    }

    // Method that accepts functional interfaces as parameters
    static void processAccounts(
            List<Account> accounts,
            Predicate<Account> filter,
            Function<Account, String> formatter,
            Consumer<String> action) {

        accounts.stream()
            .filter(filter)
            .map(formatter)
            .forEach(action);
    }
}
```

### Output

```
=== VIP Accounts (Savings + >20000) ===
  Ali Khan - $25000.0
  Hassan Raza - $42000.0
  Omar Farooq - $75000.0

=== All Accounts (Formatted) ===
[ACC-01] Ali Khan: $25,000.00 (Savings)
[ACC-02] Sara Ahmed: $18,500.00 (Checking)
[ACC-03] Hassan Raza: $42,000.00 (Savings)
[ACC-04] Fatima Noor: $5,000.00 (Checking)
[ACC-05] Omar Farooq: $75,000.00 (Savings)

=== Sending Notifications to VIPs ===
  SMS sent to Ali Khan: Your balance is $25000.0
  SMS sent to Hassan Raza: Your balance is $42000.0
  SMS sent to Omar Farooq: Your balance is $75000.0

=== Sorted by Type, then Balance (Desc) ===
[ACC-02] Sara Ahmed: $18,500.00 (Checking)
[ACC-04] Fatima Noor: $5,000.00 (Checking)
[ACC-05] Omar Farooq: $75,000.00 (Savings)
[ACC-03] Hassan Raza: $42,000.00 (Savings)
[ACC-01] Ali Khan: $25,000.00 (Savings)

=== Generated Transaction IDs ===
  TXN-a3f2b1c8
  TXN-7d9e4f2a
  TXN-e1b8c3d5

=== Total Balance: $165,500.00 ===

=== Custom Processing ===
[ACC-01] Ali Khan: $25,000.00 (Savings)
[ACC-03] Hassan Raza: $42,000.00 (Savings)
[ACC-05] Omar Farooq: $75,000.00 (Savings)
```

---

## Summary — Quick Reference

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                  LAMBDA EXPRESSIONS                             │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  SYNTAX:                                                         │
  │    () -> expression              // no params                    │
  │    x -> expression               // one param                    │
  │    (x, y) -> expression          // multiple params              │
  │    (x, y) -> { statements; }     // multi-line body              │
  │                                                                  │
  │  KEY INTERFACES:                                                 │
  │    Predicate<T>     T → boolean     filter, conditions           │
  │    Function<T,R>    T → R           transform, map               │
  │    Consumer<T>      T → void        forEach, actions             │
  │    Supplier<T>      () → T          generate, factory            │
  │    Comparator<T>    (T,T) → int     sorting                      │
  │                                                                  │
  │  METHOD REFERENCES:                                              │
  │    String::toUpperCase    (unbound instance)                     │
  │    System.out::println    (bound instance)                       │
  │    Math::abs              (static)                               │
  │    ArrayList::new         (constructor)                          │
  │                                                                  │
  │  RULES:                                                          │
  │    • Only works with functional interfaces (1 abstract method)   │
  │    • Captured variables must be effectively final                 │
  │    • Use method references when lambda just delegates            │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```
