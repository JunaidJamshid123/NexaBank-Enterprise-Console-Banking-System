# 20 — Java Records (Java 16+)

## What is a Record?

A **record** is a special kind of class designed to hold **immutable data**. It automatically generates:
- `private final` fields
- A canonical constructor
- `equals()`, `hashCode()`, `toString()`
- Getter methods (named after the fields, without `get` prefix)

> Records eliminate **boilerplate code** for simple data carriers.

---

## The Problem — Boilerplate Overload

```java
// TRADITIONAL CLASS — 50+ lines for a simple data holder!
public class Customer {
    private final String id;
    private final String name;
    private final double balance;

    public Customer(String id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public double getBalance()  { return balance; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer c = (Customer) o;
        return Double.compare(c.balance, balance) == 0
            && Objects.equals(id, c.id)
            && Objects.equals(name, c.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, balance);
    }

    @Override
    public String toString() {
        return "Customer[id=" + id + ", name=" + name + ", balance=" + balance + "]";
    }
}
```

### The Solution — One Line!

```java
// RECORD — does EVERYTHING above in one line
public record Customer(String id, String name, double balance) {}
```

> **Same functionality. Zero boilerplate.**

---

## Record Syntax

```
  ┌──────────────────────────────────────────────────────────────┐
  │                                                              │
  │  public record RecordName(Type field1, Type field2) {        │
  │      // optional: custom constructors, methods, etc.         │
  │  }                                                           │
  │                                                              │
  │  The fields in () are called "record components"             │
  │                                                              │
  └──────────────────────────────────────────────────────────────┘
```

### What the Compiler Generates

```
  record Customer(String id, String name, double balance) {}

  Compiler generates:
  ┌──────────────────────────────────────────────────────────────┐
  │                                                              │
  │  private final String id;          ← final fields            │
  │  private final String name;                                  │
  │  private final double balance;                               │
  │                                                              │
  │  Customer(String id, String name, double balance) {          │
  │      this.id = id;                 ← canonical constructor   │
  │      this.name = name;                                       │
  │      this.balance = balance;                                 │
  │  }                                                           │
  │                                                              │
  │  String id()       { return id; }  ← accessor methods        │
  │  String name()     { return name; }    (no "get" prefix!)   │
  │  double balance()  { return balance; }                       │
  │                                                              │
  │  boolean equals(Object o) { ... }  ← based on all fields    │
  │  int hashCode()           { ... }  ← based on all fields    │
  │  String toString()        { ... }  ← "Customer[id=.., ..]"  │
  │                                                              │
  └──────────────────────────────────────────────────────────────┘
```

---

## Basic Usage

```java
public class RecordBasicDemo {

    // ─── Define records ──────────────────────────────
    record Customer(String id, String name, double balance) {}
    record Transaction(String id, String type, double amount, String accountId) {}

    public static void main(String[] args) {

        // ─── Create instances ────────────────────────
        Customer ali = new Customer("C001", "Ali Khan", 25000);
        Customer sara = new Customer("C002", "Sara Ahmed", 18500);

        // ─── Access fields (accessor methods) ────────
        System.out.println("ID: " + ali.id());            // C001
        System.out.println("Name: " + ali.name());        // Ali Khan
        System.out.println("Balance: " + ali.balance());  // 25000.0

        // ─── toString() — auto-generated ─────────────
        System.out.println(ali);
        // Customer[id=C001, name=Ali Khan, balance=25000.0]

        // ─── equals() — based on ALL fields ──────────
        Customer ali2 = new Customer("C001", "Ali Khan", 25000);
        System.out.println("ali.equals(ali2): " + ali.equals(ali2));   // true
        System.out.println("ali.equals(sara): " + ali.equals(sara));   // false

        // ─── hashCode() — same fields = same hash ───
        System.out.println("ali.hashCode(): " + ali.hashCode());
        System.out.println("ali2.hashCode(): " + ali2.hashCode());   // same!

        // ─── Records are immutable ───────────────────
        // ali.id = "C999";    // COMPILE ERROR! fields are final
        // No setters exist!
    }
}
```

---

## Custom Constructors

### Compact Constructor (Validation)

The **compact constructor** lets you add validation without repeating field assignments.

```java
public record Account(String id, String owner, double balance, String type) {

    // Compact constructor — no parameter list, no "this.x = x"
    // Field assignments happen AUTOMATICALLY after this block
    public Account {
        // Validation
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be blank");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative: " + balance);
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner cannot be blank");
        }

        // Normalization (modify params before auto-assignment)
        owner = owner.trim().toUpperCase();
        type = (type == null) ? "SAVINGS" : type.toUpperCase();
    }
}
```

### Diagram — Compact Constructor Flow

```
  new Account("ACC-01", "  ali khan  ", 25000, "savings")
        │
        ▼
  ┌──────────────────────────────────────────┐
  │  Compact Constructor {                   │
  │    1. Validate id       → OK             │
  │    2. Validate balance  → OK             │
  │    3. Validate owner    → OK             │
  │    4. owner = "ALI KHAN"  (normalized)   │
  │    5. type = "SAVINGS"    (normalized)   │
  │  }                                       │
  │                                          │
  │  AUTO-ASSIGNED (by compiler):            │
  │    this.id = id;         → "ACC-01"      │
  │    this.owner = owner;   → "ALI KHAN"    │
  │    this.balance = balance;→ 25000.0      │
  │    this.type = type;     → "SAVINGS"     │
  └──────────────────────────────────────────┘
```

### Custom Canonical Constructor

```java
public record Transaction(String id, double amount, String type) {

    // Full canonical constructor — you MUST assign all fields yourself
    public Transaction(String id, double amount, String type) {
        this.id = (id != null) ? id : "TXN-" + System.currentTimeMillis();
        this.amount = Math.abs(amount);
        this.type = (type != null) ? type.toUpperCase() : "UNKNOWN";
    }
}
```

### Additional (Overloaded) Constructors

```java
public record Customer(String id, String name, double balance) {

    // Compact constructor for validation
    public Customer {
        if (balance < 0) throw new IllegalArgumentException("Negative balance");
    }

    // Additional constructors MUST delegate to canonical
    public Customer(String id, String name) {
        this(id, name, 0.0);   // delegate with default balance
    }

    public Customer(String id) {
        this(id, "Unknown", 0.0);   // delegate with all defaults
    }
}
```

```java
// Usage
Customer c1 = new Customer("C001", "Ali Khan", 25000);  // full
Customer c2 = new Customer("C002", "Sara Ahmed");         // balance = 0
Customer c3 = new Customer("C003");                        // name=Unknown, balance=0
```

---

## Custom Methods in Records

Records can have **instance methods**, **static methods**, and even implement interfaces.

```java
import java.util.List;
import java.util.stream.Collectors;

public class RecordMethodsDemo {

    record Account(String id, String owner, double balance, String type) 
            implements Comparable<Account> {

        // ─── Instance methods ────────────────────────
        public String formattedBalance() {
            return String.format("$%,.2f", balance);
        }

        public boolean isHighValue() {
            return balance > 20000;
        }

        public Account withBalance(double newBalance) {
            // Records are immutable — return a NEW record
            return new Account(id, owner, newBalance, type);
        }

        public Account deposit(double amount) {
            return withBalance(balance + amount);
        }

        public Account withdraw(double amount) {
            if (amount > balance) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            return withBalance(balance - amount);
        }

        // ─── Static methods ─────────────────────────
        public static Account createDefault(String id) {
            return new Account(id, "New Customer", 0.0, "Savings");
        }

        // ─── Implementing interface ──────────────────
        @Override
        public int compareTo(Account other) {
            return Double.compare(this.balance, other.balance);
        }
    }

    public static void main(String[] args) {

        Account ali = new Account("ACC-01", "Ali Khan", 25000, "Savings");
        System.out.println("Balance: " + ali.formattedBalance());
        System.out.println("High value? " + ali.isHighValue());

        // Immutable update — returns new Account
        Account afterDeposit = ali.deposit(5000);
        System.out.println("After deposit: " + afterDeposit.formattedBalance());
        System.out.println("Original unchanged: " + ali.formattedBalance());

        // Static factory
        Account newAcc = Account.createDefault("ACC-99");
        System.out.println("Default: " + newAcc);

        // Sorting (uses Comparable)
        List<Account> accounts = List.of(
            new Account("A1", "Ali",    25000, "Savings"),
            new Account("A2", "Sara",    5000, "Checking"),
            new Account("A3", "Hassan", 42000, "Savings")
        );

        List<Account> sorted = accounts.stream()
            .sorted()
            .collect(Collectors.toList());
        sorted.forEach(a -> System.out.println(a.owner() + ": " + a.formattedBalance()));
    }
}
```

### Output

```
Balance: $25,000.00
High value? true
After deposit: $30,000.00
Original unchanged: $25,000.00
Default: Account[id=ACC-99, owner=New Customer, balance=0.0, type=Savings]
Sara: $5,000.00
Ali: $25,000.00
Hassan: $42,000.00
```

---

## Records with Generics

```java
public class GenericRecordDemo {

    // Generic record — works with any type
    record Pair<A, B>(A first, B second) {
        public <C> Pair<A, C> mapSecond(java.util.function.Function<B, C> fn) {
            return new Pair<>(first, fn.apply(second));
        }
    }

    record ApiResponse<T>(boolean success, T data, String message) {
        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, data, "Success");
        }
        public static <T> ApiResponse<T> error(String msg) {
            return new ApiResponse<>(false, null, msg);
        }
    }

    public static void main(String[] args) {

        Pair<String, Double> accountBalance = new Pair<>("ACC-01", 25000.0);
        System.out.println(accountBalance);
        // Pair[first=ACC-01, second=25000.0]

        Pair<String, String> formatted = accountBalance.mapSecond(
            b -> String.format("$%,.2f", b)
        );
        System.out.println(formatted);
        // Pair[first=ACC-01, second=$25,000.00]

        ApiResponse<String> success = ApiResponse.ok("Account created");
        ApiResponse<String> failure = ApiResponse.error("Invalid account");
        System.out.println(success);
        System.out.println(failure);
    }
}
```

---

## Records with Streams — Perfect Match

```java
import java.util.*;
import java.util.stream.Collectors;

public class RecordStreamsDemo {

    record Transaction(String id, String type, double amount, String accountId) {}

    record Summary(String accountId, long count, double total, double average) {}

    public static void main(String[] args) {

        List<Transaction> transactions = List.of(
            new Transaction("T1", "DEPOSIT",    5000, "ACC-01"),
            new Transaction("T2", "DEPOSIT",    3000, "ACC-01"),
            new Transaction("T3", "WITHDRAWAL", 2000, "ACC-01"),
            new Transaction("T4", "DEPOSIT",    8000, "ACC-02"),
            new Transaction("T5", "DEPOSIT",   12000, "ACC-02"),
            new Transaction("T6", "WITHDRAWAL", 5000, "ACC-02")
        );

        // ─── Group and summarize ─────────────────────
        Map<String, Summary> summaries = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::accountId,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    txns -> {
                        String accId = txns.get(0).accountId();
                        long count = txns.size();
                        double total = txns.stream().mapToDouble(Transaction::amount).sum();
                        double avg = total / count;
                        return new Summary(accId, count, total, avg);
                    }
                )
            ));

        summaries.values().forEach(s ->
            System.out.printf("%s: %d txns, total=$%,.2f, avg=$%,.2f%n",
                s.accountId(), s.count(), s.total(), s.average())
        );
    }
}
```

---

## What Records CANNOT Do

```
  ┌──────────────────────────────────────────────────────────────┐
  │  Records CANNOT:                                             │
  │                                                              │
  │  ✗ Extend another class (implicitly extend java.lang.Record) │
  │  ✗ Be abstract                                               │
  │  ✗ Have mutable instance fields                              │
  │  ✗ Declare additional instance fields (only components)      │
  │  ✗ Have setter methods (no setXxx())                         │
  │                                                              │
  │  Records CAN:                                                │
  │                                                              │
  │  ✓ Implement interfaces                                      │
  │  ✓ Have instance methods                                     │
  │  ✓ Have static fields and methods                            │
  │  ✓ Have custom constructors                                  │
  │  ✓ Override toString(), equals(), hashCode()                 │
  │  ✓ Be generic                                                │
  │  ✓ Be nested / local                                         │
  │  ✓ Use annotations                                           │
  └──────────────────────────────────────────────────────────────┘
```

---

## Record vs Class — When to Use Which

```
  ┌────────────────────────────────────────────────────────────┐
  │  Use RECORD when:              Use CLASS when:              │
  │                                                            │
  │  • Data is immutable           • Need mutable state         │
  │  • Pure data carrier           • Complex behavior/logic     │
  │  • DTOs, value objects         • Need inheritance           │
  │  • API responses               • Need private constructors  │
  │  • Config/settings objects     • Need builder pattern       │
  │  • Map keys, set elements      • Entity with identity       │
  │  • Stream intermediate data    • Database entity with ORM   │
  └────────────────────────────────────────────────────────────┘
```

---

## Summary

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                      JAVA RECORDS                               │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  record Name(Type f1, Type f2) {}                                │
  │                                                                  │
  │  AUTO-GENERATES:                                                 │
  │    • final fields + constructor                                  │
  │    • Accessor methods: f1(), f2()  (no "get" prefix)            │
  │    • equals(), hashCode(), toString()                            │
  │                                                                  │
  │  COMPACT CONSTRUCTOR:                                            │
  │    public Name { /* validate/normalize */ }                      │
  │    Fields auto-assigned AFTER block                              │
  │                                                                  │
  │  IMMUTABLE UPDATES:                                              │
  │    Account withBalance(double b) { return new Account(..., b); } │
  │                                                                  │
  │  IDEAL FOR: DTOs, value objects, stream data, API responses      │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```
