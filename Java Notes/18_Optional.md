# 18 — Optional

## What is Optional?

`Optional<T>` is a **container object** that may or may not contain a non-null value. It was introduced in **Java 8** to provide a better alternative to returning `null`.

### The Problem — NullPointerException

```java
// The BILLION-DOLLAR MISTAKE (null references)
String name = getCustomerName("ACC-9999");   // returns null
int length = name.length();                   // 💥 NullPointerException!
```

### The Solution — Optional

```java
Optional<String> name = findCustomerName("ACC-9999");   // returns Optional
name.ifPresent(n -> System.out.println(n.length()));    // safe — no NPE!
```

---

## Why Use Optional?

```
  ┌──────────────────────────────────────────────────────────────┐
  │                                                              │
  │  WITHOUT Optional:                                           │
  │                                                              │
  │  String name = findCustomer(id);                             │
  │  // Does this return null? Who knows?                        │
  │  // Have to read the implementation!                         │
  │  // Forget a null check → NullPointerException               │
  │                                                              │
  ├──────────────────────────────────────────────────────────────┤
  │                                                              │
  │  WITH Optional:                                              │
  │                                                              │
  │  Optional<String> name = findCustomer(id);                   │
  │  // Method signature TELLS you it might be absent            │
  │  // Compiler forces you to handle the empty case             │
  │  // Self-documenting code                                    │
  │                                                              │
  └──────────────────────────────────────────────────────────────┘
```

---

## Creating Optional Objects

```java
import java.util.Optional;

public class CreatingOptional {
    public static void main(String[] args) {

        // 1. Optional.of(value) — value MUST be non-null
        Optional<String> name = Optional.of("Ali Khan");
        System.out.println(name);   // Optional[Ali Khan]

        // Optional.of(null);  // 💥 NullPointerException!

        // 2. Optional.ofNullable(value) — value CAN be null
        String input = null;
        Optional<String> maybeName = Optional.ofNullable(input);
        System.out.println(maybeName);   // Optional.empty

        Optional<String> maybeName2 = Optional.ofNullable("Sara");
        System.out.println(maybeName2);  // Optional[Sara]

        // 3. Optional.empty() — explicitly empty
        Optional<String> empty = Optional.empty();
        System.out.println(empty);       // Optional.empty
    }
}
```

### Diagram — Three Ways to Create

```
  ┌───────────────────────────────────────────────────────┐
  │                                                       │
  │  Optional.of("Ali")          → Optional["Ali"]  ✓    │
  │  Optional.of(null)           → 💥 NPE!          ✗    │
  │                                                       │
  │  Optional.ofNullable("Ali")  → Optional["Ali"]  ✓    │
  │  Optional.ofNullable(null)   → Optional.empty    ✓    │
  │                                                       │
  │  Optional.empty()            → Optional.empty    ✓    │
  │                                                       │
  └───────────────────────────────────────────────────────┘

  Use Optional.of()         when value is GUARANTEED non-null
  Use Optional.ofNullable() when value MIGHT be null
  Use Optional.empty()      when you know there's no value
```

---

## Checking and Retrieving Values

```java
import java.util.Optional;

public class OptionalBasicOps {
    public static void main(String[] args) {

        Optional<String> name = Optional.of("Ali Khan");
        Optional<String> empty = Optional.empty();

        // ─── isPresent() / isEmpty() ────────────────
        System.out.println(name.isPresent());    // true
        System.out.println(empty.isPresent());   // false
        System.out.println(empty.isEmpty());     // true (Java 11+)

        // ─── get() — AVOID! Throws NoSuchElementException if empty
        System.out.println(name.get());          // "Ali Khan"
        // empty.get();  // 💥 NoSuchElementException!

        // ─── ifPresent() — safe way to use the value
        name.ifPresent(n -> System.out.println("Customer: " + n));
        empty.ifPresent(n -> System.out.println("Never printed"));

        // ─── ifPresentOrElse() (Java 9+) ────────────
        name.ifPresentOrElse(
            n -> System.out.println("Found: " + n),
            () -> System.out.println("Not found!")
        );

        empty.ifPresentOrElse(
            n -> System.out.println("Found: " + n),
            () -> System.out.println("Not found!")
        );
    }
}
```

### Output

```
true
false
true
Ali Khan
Customer: Ali Khan
Found: Ali Khan
Not found!
```

---

## Default Values — orElse, orElseGet, orElseThrow

```java
import java.util.Optional;

public class OptionalDefaults {
    public static void main(String[] args) {

        Optional<String> name = Optional.of("Ali Khan");
        Optional<String> empty = Optional.empty();

        // ─── orElse() — provide a default value ─────
        String result1 = name.orElse("Unknown");
        String result2 = empty.orElse("Unknown");
        System.out.println(result1);   // "Ali Khan"
        System.out.println(result2);   // "Unknown"

        // ─── orElseGet() — compute default lazily ────
        // Only calls the supplier if Optional is empty
        String result3 = empty.orElseGet(() -> "Default-" + System.currentTimeMillis());
        System.out.println(result3);   // "Default-1713094800000"

        // ─── orElseThrow() — throw if empty ─────────
        try {
            String result4 = empty.orElseThrow(
                () -> new RuntimeException("Customer not found!")
            );
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // orElseThrow() with no argument (Java 10+)
        // empty.orElseThrow();  // throws NoSuchElementException
    }
}
```

### Diagram — orElse vs orElseGet

```
  ┌─────────────────────────────────────────────────────────────────┐
  │                                                                 │
  │  orElse(defaultValue):                                          │
  │    • Default is ALWAYS evaluated, even if Optional has a value  │
  │    • Use for simple/cheap defaults                              │
  │                                                                 │
  │  orElseGet(() -> computeDefault()):                             │
  │    • Default is ONLY computed if Optional is empty              │
  │    • Use for expensive operations (DB query, API call, etc.)    │
  │                                                                 │
  └─────────────────────────────────────────────────────────────────┘

  Example of the difference:

  Optional<String> name = Optional.of("Ali");

  // orElse — expensiveCall() RUNS even though value exists!
  name.orElse(expensiveCall());

  // orElseGet — expensiveCall() DOES NOT RUN (value exists)
  name.orElseGet(() -> expensiveCall());
```

---

## Transforming — map() and flatMap()

### map() — Transform the Value Inside

```java
import java.util.Optional;

public class OptionalMap {
    public static void main(String[] args) {

        Optional<String> name = Optional.of("ali khan");

        // ─── map() — transform if present ────────────
        Optional<String> upper = name.map(String::toUpperCase);
        System.out.println(upper);   // Optional[ALI KHAN]

        Optional<Integer> length = name.map(String::length);
        System.out.println(length);  // Optional[8]

        // If empty, map() returns empty
        Optional<String> empty = Optional.empty();
        Optional<String> result = empty.map(String::toUpperCase);
        System.out.println(result);  // Optional.empty

        // ─── Chaining maps ──────────────────────────
        String display = Optional.of("  ali khan  ")
            .map(String::trim)
            .map(String::toUpperCase)
            .map(s -> "Customer: " + s)
            .orElse("Unknown");

        System.out.println(display);  // Customer: ALI KHAN
    }
}
```

### Diagram — How map() Works

```
  Optional.of("ali khan").map(String::toUpperCase)

  PRESENT:
  ┌──────────────────┐     ┌────────────────┐     ┌──────────────────────┐
  │ Optional["ali"]  │────►│ toUpperCase()  │────►│ Optional["ALI KHAN"] │
  └──────────────────┘     └────────────────┘     └──────────────────────┘

  EMPTY:
  ┌──────────────────┐                            ┌──────────────────┐
  │ Optional.empty   │────────────────────────────►│ Optional.empty   │
  └──────────────────┘  (map is skipped)           └──────────────────┘
```

### flatMap() — When the Function Returns Optional

```java
import java.util.Optional;

public class OptionalFlatMap {

    static Optional<String> findAccountId(String customerId) {
        if ("C001".equals(customerId)) return Optional.of("ACC-1001");
        return Optional.empty();
    }

    static Optional<Double> findBalance(String accountId) {
        if ("ACC-1001".equals(accountId)) return Optional.of(25000.0);
        return Optional.empty();
    }

    public static void main(String[] args) {

        // ─── Problem with map() — nested Optionals ──
        Optional<String> customerId = Optional.of("C001");

        // map gives Optional<Optional<String>> — not what we want!
        Optional<Optional<String>> nested = customerId.map(id -> findAccountId(id));

        // flatMap flattens it to Optional<String>
        Optional<String> accountId = customerId.flatMap(id -> findAccountId(id));
        System.out.println(accountId);   // Optional[ACC-1001]

        // ─── Chaining flatMap ────────────────────────
        Optional<Double> balance = Optional.of("C001")
            .flatMap(OptionalFlatMap::findAccountId)    // Optional<String>
            .flatMap(OptionalFlatMap::findBalance);     // Optional<Double>

        balance.ifPresentOrElse(
            b -> System.out.printf("Balance: $%,.2f%n", b),
            () -> System.out.println("Account not found")
        );
        // Balance: $25,000.00

        // ─── Trying with unknown customer ────────────
        Optional<Double> unknown = Optional.of("C999")
            .flatMap(OptionalFlatMap::findAccountId)
            .flatMap(OptionalFlatMap::findBalance);

        unknown.ifPresentOrElse(
            b -> System.out.printf("Balance: $%,.2f%n", b),
            () -> System.out.println("Account not found")
        );
        // Account not found
    }
}
```

### Diagram — map vs flatMap

```
  map() when function returns Optional:  (PROBLEM)
  ┌──────────┐    ┌──────────────┐    ┌─────────────────────────────┐
  │ Opt["C1"]│───►│findAccountId │───►│ Opt[ Opt["ACC-1001"] ]      │
  └──────────┘    └──────────────┘    └─────────────────────────────┘
                                        ↑ Nested Optionals! Bad!

  flatMap() when function returns Optional:  (SOLUTION)
  ┌──────────┐    ┌──────────────┐    ┌──────────────────────┐
  │ Opt["C1"]│───►│findAccountId │───►│ Opt["ACC-1001"]      │
  └──────────┘    └──────────────┘    └──────────────────────┘
                                        ↑ Flattened! Good!
```

---

## Filtering — filter()

```java
import java.util.Optional;

public class OptionalFilter {
    public static void main(String[] args) {

        Optional<Double> balance = Optional.of(25000.0);

        // Keep the value only if it matches the predicate
        Optional<Double> highBalance = balance.filter(b -> b > 10000);
        System.out.println(highBalance);   // Optional[25000.0]

        Optional<Double> veryHigh = balance.filter(b -> b > 50000);
        System.out.println(veryHigh);      // Optional.empty

        // ─── Practical: Validate and use ─────────────
        Optional.of(25000.0)
            .filter(b -> b > 0)                     // must be positive
            .filter(b -> b <= 1_000_000)             // within limit
            .ifPresentOrElse(
                b -> System.out.println("Valid balance: $" + b),
                () -> System.out.println("Invalid balance")
            );
    }
}
```

---

## Optional with Streams

```java
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptionalStreams {
    public static void main(String[] args) {

        List<String> customers = List.of("Ali Khan", "Sara Ahmed", "Hassan Raza");

        // ─── stream().findFirst() returns Optional ───
        Optional<String> first = customers.stream()
            .filter(n -> n.startsWith("S"))
            .findFirst();

        first.ifPresent(System.out::println);  // Sara Ahmed

        // ─── Optional.stream() (Java 9+) ────────────
        // Converts Optional to a 0-or-1 element stream
        List<Optional<String>> optionals = List.of(
            Optional.of("Ali"),
            Optional.empty(),
            Optional.of("Sara"),
            Optional.empty(),
            Optional.of("Hassan")
        );

        List<String> present = optionals.stream()
            .flatMap(Optional::stream)     // remove empties, extract values
            .collect(Collectors.toList());

        System.out.println(present);  // [Ali, Sara, Hassan]
    }
}
```

---

## Complete Banking Example

```java
import java.util.*;
import java.util.stream.Collectors;

public class OptionalBankingDemo {

    record Customer(String id, String name, String email) {}
    record Account(String accountId, String customerId, double balance, String type) {}

    // Simulated database
    private static final List<Customer> CUSTOMERS = List.of(
        new Customer("C001", "Ali Khan", "ali@email.com"),
        new Customer("C002", "Sara Ahmed", null),             // no email!
        new Customer("C003", "Hassan Raza", "hassan@email.com")
    );

    private static final List<Account> ACCOUNTS = List.of(
        new Account("ACC-01", "C001", 25000, "Savings"),
        new Account("ACC-02", "C001", 18000, "Checking"),
        new Account("ACC-03", "C002", 42000, "Savings"),
        new Account("ACC-04", "C003",  5000, "Checking")
    );

    // ─── Repository methods returning Optional ───────
    static Optional<Customer> findCustomer(String id) {
        return CUSTOMERS.stream()
            .filter(c -> c.id().equals(id))
            .findFirst();
    }

    static Optional<Account> findPrimaryAccount(String customerId) {
        return ACCOUNTS.stream()
            .filter(a -> a.customerId().equals(customerId))
            .filter(a -> a.type().equals("Savings"))
            .findFirst();
    }

    static Optional<String> getEmail(Customer customer) {
        return Optional.ofNullable(customer.email());
    }

    public static void main(String[] args) {

        // ─── 1. Simple lookup ────────────────────────
        System.out.println("=== Customer Lookup ===");
        findCustomer("C001").ifPresentOrElse(
            c -> System.out.println("Found: " + c.name()),
            () -> System.out.println("Not found")
        );

        findCustomer("C999").ifPresentOrElse(
            c -> System.out.println("Found: " + c.name()),
            () -> System.out.println("Not found")
        );

        // ─── 2. Chained lookups with flatMap ─────────
        System.out.println("\n=== Account Balance Lookup ===");
        Optional<Double> balance = findCustomer("C001")
            .flatMap(c -> findPrimaryAccount(c.id()))
            .map(Account::balance);

        balance.ifPresentOrElse(
            b -> System.out.printf("Primary balance: $%,.2f%n", b),
            () -> System.out.println("No savings account found")
        );

        // ─── 3. Optional email with default ──────────
        System.out.println("\n=== Email Lookup ===");
        String email1 = findCustomer("C001")
            .flatMap(OptionalBankingDemo::getEmail)
            .orElse("no-email@default.com");
        System.out.println("C001 email: " + email1);   // ali@email.com

        String email2 = findCustomer("C002")
            .flatMap(OptionalBankingDemo::getEmail)
            .orElse("no-email@default.com");
        System.out.println("C002 email: " + email2);   // no-email@default.com

        // ─── 4. Transform + filter ──────────────────
        System.out.println("\n=== High-Value Account Check ===");
        findCustomer("C001")
            .flatMap(c -> findPrimaryAccount(c.id()))
            .filter(a -> a.balance() > 20000)
            .map(a -> a.customerId() + " is a high-value customer!")
            .ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Not a high-value customer")
            );

        // ─── 5. or() — try alternative (Java 9+) ────
        System.out.println("\n=== Fallback with or() ===");
        Optional<Account> account = findPrimaryAccount("C999")
            .or(() -> findPrimaryAccount("C001"));     // fallback

        account.ifPresent(a ->
            System.out.println("Found account: " + a.accountId()));

        // ─── 6. Collecting non-empty Optionals ──────
        System.out.println("\n=== All Customer Emails ===");
        List<String> emails = CUSTOMERS.stream()
            .map(OptionalBankingDemo::getEmail)
            .flatMap(Optional::stream)              // filter out empties
            .collect(Collectors.toList());
        System.out.println(emails);  // [ali@email.com, hassan@email.com]
    }
}
```

### Output

```
=== Customer Lookup ===
Found: Ali Khan
Not found

=== Account Balance Lookup ===
Primary balance: $25,000.00

=== Email Lookup ===
C001 email: ali@email.com
C002 email: no-email@default.com

=== High-Value Account Check ===
C001 is a high-value customer!

=== Fallback with or() ===
Found account: ACC-01

=== All Customer Emails ===
[ali@email.com, hassan@email.com]
```

---

## Anti-Patterns — What NOT to Do

```java
// ❌ DON'T: Use Optional.get() without checking
optional.get();  // may throw NoSuchElementException

// ❌ DON'T: Use Optional as method parameter
void process(Optional<String> name) { }   // bad practice

// ❌ DON'T: Use Optional for fields
class Customer {
    Optional<String> email;  // use null instead, Optional for returns only
}

// ❌ DON'T: Use Optional with collections
Optional<List<String>> names;  // return empty list instead!

// ❌ DON'T: Check isPresent() then get() — just use orElse/ifPresent
if (opt.isPresent()) {
    return opt.get();          // verbose, defeats the purpose
}

// ✅ DO: Use it for return types that might be absent
Optional<Customer> findById(String id) { ... }

// ✅ DO: Chain operations
findById(id).map(Customer::name).orElse("Unknown");

// ✅ DO: Use orElseThrow for required values
Customer c = findById(id).orElseThrow(() -> new NotFoundException(id));
```

---

## Optional Methods — Complete Reference

```
  ┌────────────────────────────────────────────────────────────────────┐
  │                    OPTIONAL<T> CHEAT SHEET                        │
  ├────────────────────┬───────────────────────────────────────────────┤
  │ CREATION           │ Optional.of(value)     — non-null required   │
  │                    │ Optional.ofNullable(v)  — null-safe          │
  │                    │ Optional.empty()        — empty optional      │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ CHECKING           │ isPresent()             — true if has value   │
  │                    │ isEmpty()               — true if empty (11+) │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ RETRIEVING         │ get()                   — ⚠️ throws if empty  │
  │                    │ orElse(default)          — value or default    │
  │                    │ orElseGet(supplier)      — lazy default        │
  │                    │ orElseThrow(supplier)    — throw if empty      │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ CONDITIONAL        │ ifPresent(consumer)      — run if present     │
  │                    │ ifPresentOrElse(c, r)    — present or else(9+)│
  ├────────────────────┼───────────────────────────────────────────────┤
  │ TRANSFORMING       │ map(function)            — transform value    │
  │                    │ flatMap(function)         — flatten Optional   │
  │                    │ filter(predicate)         — keep if matches    │
  ├────────────────────┼───────────────────────────────────────────────┤
  │ ALTERNATIVES       │ or(supplier)             — fallback Opt (9+)  │
  │                    │ stream()                 — 0-or-1 stream (9+) │
  └────────────────────┴───────────────────────────────────────────────┘
```
