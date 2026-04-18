# 17 — Method References

## What is a Method Reference?

A **method reference** is a shorthand notation for a lambda expression that simply **calls an existing method**. Instead of writing `x -> System.out.println(x)`, you write `System.out::println`.

### Lambda vs Method Reference

```java
// Lambda                              // Method Reference
x -> System.out.println(x)            System.out::println
x -> Math.abs(x)                      Math::abs
x -> x.toUpperCase()                  String::toUpperCase
() -> new ArrayList<>()               ArrayList::new
```

> **Rule of thumb:** If a lambda does nothing but pass its argument(s) to an existing method, replace it with a method reference.

---

## The `::` Operator

```
  ┌────────────────────────────────────────────────────────┐
  │                                                        │
  │   ClassName :: methodName                               │
  │   objectRef :: methodName                               │
  │   ClassName :: new                                      │
  │         │         │                                    │
  │     target     method                                  │
  │                                                        │
  │   The :: operator separates the target from the method │
  │   No parentheses after the method name!                │
  └────────────────────────────────────────────────────────┘
```

---

## Four Types of Method References

```
  ┌──────────────────────────────────────────────────────────────────┐
  │  TYPE                        │ SYNTAX             │ EXAMPLE      │
  ├──────────────────────────────┼────────────────────┼──────────────┤
  │ 1. Static method             │ ClassName::method  │ Math::abs    │
  │ 2. Instance method (bound)   │ object::method     │ str::equals  │
  │ 3. Instance method (unbound) │ ClassName::method  │ String::length│
  │ 4. Constructor               │ ClassName::new     │ Account::new │
  └──────────────────────────────┴────────────────────┴──────────────┘
```

---

## Type 1: Static Method Reference

References a **static method** of a class. The lambda parameter(s) become the method argument(s).

### Diagram

```
  Lambda:              x -> ClassName.staticMethod(x)
  Method Reference:    ClassName::staticMethod

  ┌───────┐          ┌─────────────────────┐
  │   x   │─────────►│ Math.abs(x)         │
  └───────┘          └─────────────────────┘

  Becomes:            Math::abs
  x is passed automatically
```

### Code Example

```java
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

public class StaticMethodRefExample {
    public static void main(String[] args) {

        // ─── Math::abs — static method ───────────────
        List<Integer> numbers = List.of(-5, 3, -8, 12, -1);

        // Lambda
        List<Integer> absLambda = numbers.stream()
            .map(n -> Math.abs(n))
            .collect(Collectors.toList());

        // Method reference — same result, cleaner
        List<Integer> absRef = numbers.stream()
            .map(Math::abs)
            .collect(Collectors.toList());

        System.out.println("Absolute values: " + absRef);
        // [5, 3, 8, 12, 1]

        // ─── Integer::parseInt — static method ───────
        List<String> amountStrings = List.of("1000", "2500", "500", "3000");

        List<Integer> amounts = amountStrings.stream()
            .map(Integer::parseInt)      // s -> Integer.parseInt(s)
            .collect(Collectors.toList());

        System.out.println("Parsed amounts: " + amounts);
        // [1000, 2500, 500, 3000]

        // ─── Custom static method reference ──────────
        List<Double> balances = List.of(25000.0, 18500.0, 42000.0, 5000.0);

        List<String> formatted = balances.stream()
            .map(StaticMethodRefExample::formatCurrency)  // custom static method
            .collect(Collectors.toList());

        System.out.println("Formatted: " + formatted);
        // [$25,000.00, $18,500.00, $42,000.00, $5,000.00]
    }

    // Custom static method used as a method reference
    static String formatCurrency(double amount) {
        return String.format("$%,.2f", amount);
    }
}
```

---

## Type 2: Bound Instance Method Reference

References a method on a **specific object instance**. The lambda parameter becomes the method argument.

### Diagram

```
  Lambda:              x -> myObject.method(x)
  Method Reference:    myObject::method

  The object is BOUND — it's a specific instance you already have.

  String prefix = "ACC-";
  Lambda:              s -> prefix.concat(s)
  Method Reference:    prefix::concat

  ┌───────┐          ┌──────────────────────┐
  │   s   │─────────►│ prefix.concat(s)     │
  └───────┘          │ ("ACC-".concat(s))   │
                     └──────────────────────┘
```

### Code Example

```java
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BoundInstanceRefExample {
    public static void main(String[] args) {

        // ─── System.out::println — bound to System.out ──
        List<String> customers = List.of("Ali Khan", "Sara Ahmed", "Hassan Raza");

        // Lambda
        customers.forEach(name -> System.out.println(name));

        // Method reference — System.out is the bound instance
        customers.forEach(System.out::println);

        // ─── Bound to a specific String ──────────────
        String searchTerm = "Ali";

        // Lambda
        Predicate<String> containsAliLambda = s -> s.contains(searchTerm);

        // Method reference — searchTerm is the bound instance
        Predicate<String> containsAliRef = searchTerm::equals;
        // Note: this checks .equals(), not .contains()

        System.out.println(containsAliRef.test("Ali"));    // true
        System.out.println(containsAliRef.test("Sara"));   // false

        // ─── Bound to a custom object ────────────────
        StringBuilder logger = new StringBuilder();

        Consumer<String> log = logger::append;  // bound to 'logger'
        log.accept("Transaction started. ");
        log.accept("Amount: $5000. ");
        log.accept("Status: SUCCESS.");

        System.out.println(logger.toString());
        // Transaction started. Amount: $5000. Status: SUCCESS.
    }
}
```

### Bound vs Unbound — Key Difference

```
  BOUND:     myString::equals       → calls myString.equals(x)
             ────────                  ↑ specific object already known
             The object is fixed. Lambda param becomes the argument.

  UNBOUND:   String::equals         → calls x.equals(y)
             ──────                    ↑ object comes from lambda param
             No object yet. First lambda param IS the object.
```

---

## Type 3: Unbound Instance Method Reference

References an instance method, but **no object is specified**. The first parameter of the lambda becomes the object on which the method is called.

### Diagram

```
  Lambda:              s -> s.toUpperCase()
  Method Reference:    String::toUpperCase

  ┌───────┐
  │   s   │──► s.toUpperCase()
  └───────┘
       ↑
  First param becomes the "this" object

  For two-parameter case:
  Lambda:              (s1, s2) -> s1.compareTo(s2)
  Method Reference:    String::compareTo

  ┌────┐  ┌────┐
  │ s1 │  │ s2 │──► s1.compareTo(s2)
  └────┘  └────┘
    ↑        ↑
  "this"   argument
```

### Code Example

```java
import java.util.List;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UnboundInstanceRefExample {
    public static void main(String[] args) {

        List<String> names = List.of("ali khan", "sara ahmed", "hassan raza");

        // ─── String::toUpperCase — unbound ───────────
        // Lambda:  s -> s.toUpperCase()
        // Ref:     String::toUpperCase
        List<String> upper = names.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println(upper);
        // [ALI KHAN, SARA AHMED, HASSAN RAZA]

        // ─── String::length — unbound ────────────────
        // Lambda:  s -> s.length()
        // Ref:     String::length
        List<Integer> lengths = names.stream()
            .map(String::length)
            .collect(Collectors.toList());
        System.out.println("Lengths: " + lengths);
        // [8, 10, 11]

        // ─── String::compareTo — two params, unbound ─
        // Lambda:  (a, b) -> a.compareTo(b)
        // Ref:     String::compareTo
        List<String> sorted = names.stream()
            .sorted(String::compareTo)
            .collect(Collectors.toList());
        System.out.println("Sorted: " + sorted);

        // ─── String::trim — unbound ──────────────────
        List<String> messy = List.of("  Ali  ", " Sara ", "  Hassan  ");
        List<String> clean = messy.stream()
            .map(String::trim)
            .collect(Collectors.toList());
        System.out.println("Trimmed: " + clean);
        // [Ali, Sara, Hassan]
    }
}
```

---

## Type 4: Constructor Reference

References a **constructor**. Used to create new objects.

### Diagram

```
  Lambda:              () -> new ArrayList<>()
  Method Reference:    ArrayList::new

  Lambda:              s -> new Account(s)
  Method Reference:    Account::new

  ┌───────────┐          ┌──────────────────┐
  │ parameter  │─────────►│ new ClassName()  │
  └───────────┘          └──────────────────┘
                          Constructor called!
```

### Code Example

```java
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class ConstructorRefExample {

    // Simple Account class
    static class Account {
        private String id;
        private double balance;

        // No-arg constructor
        Account() {
            this.id = "NEW";
            this.balance = 0;
        }

        // One-arg constructor
        Account(String id) {
            this.id = id;
            this.balance = 0;
        }

        @Override
        public String toString() {
            return "Account{" + id + ", $" + balance + "}";
        }
    }

    public static void main(String[] args) {

        // ─── No-arg constructor: Supplier ────────────
        // Lambda:  () -> new Account()
        // Ref:     Account::new
        Supplier<Account> factory = Account::new;
        Account newAccount = factory.get();
        System.out.println(newAccount);  // Account{NEW, $0.0}

        // ─── One-arg constructor: Function ───────────
        // Lambda:  id -> new Account(id)
        // Ref:     Account::new
        Function<String, Account> accountCreator = Account::new;
        Account acc = accountCreator.apply("ACC-1001");
        System.out.println(acc);  // Account{ACC-1001, $0.0}

        // ─── Create multiple accounts from IDs ───────
        List<String> ids = List.of("ACC-01", "ACC-02", "ACC-03", "ACC-04");

        List<Account> accounts = ids.stream()
            .map(Account::new)         // id -> new Account(id)
            .collect(Collectors.toList());

        accounts.forEach(System.out::println);
        // Account{ACC-01, $0.0}
        // Account{ACC-02, $0.0}
        // Account{ACC-03, $0.0}
        // Account{ACC-04, $0.0}

        // ─── Array constructor reference ─────────────
        // Lambda:  size -> new String[size]
        // Ref:     String[]::new
        String[] nameArray = List.of("Ali", "Sara", "Hassan").stream()
            .toArray(String[]::new);

        System.out.println("Array length: " + nameArray.length);
    }
}
```

---

## Complete Banking Example — All Four Types Together

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class MethodReferencesBankingDemo {

    static class Customer {
        String id, name;
        double balance;

        Customer(String id) {
            this.id = id;
            this.name = "Unknown";
            this.balance = 0;
        }

        Customer(String id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }

        String getName()    { return name; }
        double getBalance() { return balance; }

        static String formatBalance(double b) {
            return String.format("$%,.2f", b);
        }

        static int compareByBalance(Customer a, Customer b) {
            return Double.compare(a.balance, b.balance);
        }

        boolean isHighValue() { return balance > 20000; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", id, name, formatBalance(balance));
        }
    }

    public static void main(String[] args) {
        List<Customer> customers = List.of(
            new Customer("C01", "Ali Khan",    25000),
            new Customer("C02", "Sara Ahmed",  18500),
            new Customer("C03", "Hassan Raza", 42000),
            new Customer("C04", "Fatima Noor",  5000),
            new Customer("C05", "Omar Farooq", 75000)
        );

        // ─── Type 1: Static method reference ─────────
        System.out.println("=== Sorted by Balance (static method ref) ===");
        customers.stream()
            .sorted(Customer::compareByBalance)      // static method
            .forEach(System.out::println);

        // ─── Type 2: Bound instance method reference ─
        System.out.println("\n=== Print all (bound: System.out::println) ===");
        customers.forEach(System.out::println);      // bound to System.out

        // ─── Type 3: Unbound instance method reference
        System.out.println("\n=== Customer Names (unbound: Customer::getName) ===");
        List<String> names = customers.stream()
            .map(Customer::getName)                  // unbound instance
            .collect(Collectors.toList());
        System.out.println(names);

        // ─── Type 4: Constructor reference ───────────
        System.out.println("\n=== New Accounts from IDs (constructor ref) ===");
        List<String> newIds = List.of("C10", "C11", "C12");
        List<Customer> newCustomers = newIds.stream()
            .map(Customer::new)                      // constructor
            .collect(Collectors.toList());
        newCustomers.forEach(System.out::println);

        // ─── Combined example ────────────────────────
        System.out.println("\n=== High-Value Formatted Balances ===");
        customers.stream()
            .filter(Customer::isHighValue)                // unbound instance
            .map(Customer::getBalance)                    // unbound instance
            .map(Customer::formatBalance)                 // static
            .forEach(System.out::println);                // bound instance
    }
}
```

### Output

```
=== Sorted by Balance (static method ref) ===
[C04] Fatima Noor: $5,000.00
[C02] Sara Ahmed: $18,500.00
[C01] Ali Khan: $25,000.00
[C03] Hassan Raza: $42,000.00
[C05] Omar Farooq: $75,000.00

=== Print all (bound: System.out::println) ===
[C01] Ali Khan: $25,000.00
[C02] Sara Ahmed: $18,500.00
[C03] Hassan Raza: $42,000.00
[C04] Fatima Noor: $5,000.00
[C05] Omar Farooq: $75,000.00

=== Customer Names (unbound: Customer::getName) ===
[Ali Khan, Sara Ahmed, Hassan Raza, Fatima Noor, Omar Farooq]

=== New Accounts from IDs (constructor ref) ===
[C10] Unknown: $0.00
[C11] Unknown: $0.00
[C12] Unknown: $0.00

=== High-Value Formatted Balances ===
$25,000.00
$42,000.00
$75,000.00
```

---

## Decision Flowchart — Lambda or Method Reference?

```
  ┌──────────────────────────────────────────────────┐
  │  Does the lambda ONLY call one existing method?  │
  │                                                  │
  │  x -> obj.method(x)                              │
  │  x -> Class.method(x)                            │
  │  x -> x.method()                                 │
  │  () -> new Class()                               │
  └──────────────────────┬───────────────────────────┘
                         │
              ┌──────────┴──────────┐
              │ YES                 │ NO
              ▼                     ▼
  ┌──────────────────┐   ┌────────────────────────┐
  │ Use Method Ref   │   │ Keep the Lambda        │
  │ Class::method    │   │ (x, y) -> x + y * 2   │
  │ obj::method      │   │ Can't simplify further │
  │ Class::new       │   └────────────────────────┘
  └──────────────────┘
```

---

## Summary

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                  METHOD REFERENCES                              │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  TYPE 1 — Static:       Math::abs         (x -> Math.abs(x))    │
  │  TYPE 2 — Bound:        obj::method       (x -> obj.method(x))  │
  │  TYPE 3 — Unbound:      String::length    (s -> s.length())     │
  │  TYPE 4 — Constructor:  Account::new      (s -> new Account(s)) │
  │                                                                  │
  │  WHEN TO USE:                                                    │
  │  • Lambda just passes params to one existing method              │
  │  • Improves readability (shorter, less noise)                    │
  │  • Works with Streams, forEach, Comparators, etc.                │
  │                                                                  │
  │  WHEN NOT TO USE:                                                │
  │  • Lambda has extra logic beyond a single method call            │
  │  • Method reference would be less readable than lambda           │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```
