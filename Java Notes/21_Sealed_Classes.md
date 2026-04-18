# 21 — Sealed Classes (Java 17+)

## What are Sealed Classes?

A **sealed class** restricts which other classes can extend or implement it. You explicitly declare the **permitted subclasses**, and no other class can extend it.

### The Problem — Uncontrolled Inheritance

```java
// Anyone can extend this — you have NO control!
public class Account { }

// Someone on another team creates:
class CryptoAccount extends Account { }    // unexpected!
class GamblingAccount extends Account { }  // dangerous!

// You can't prevent this with normal classes.
```

### The Solution — Sealed Classes

```java
// Only these three can extend Account. Period.
public sealed class Account permits SavingsAccount, CheckingAccount, FixedDepositAccount { }
```

> **Sealed = You control the complete set of subclasses.**

---

## Why Use Sealed Classes?

```
  ┌──────────────────────────────────────────────────────────────┐
  │  BEFORE sealed classes (Java 16 and earlier):                │
  │                                                              │
  │  • open class   = anyone can extend (too open)               │
  │  • final class  = no one can extend (too closed)             │
  │  • package-private = only same package (awkward workaround)  │
  │                                                              │
  │  ┌─────┐                    ┌──────────┐                     │
  │  │ open │ <── too open      │  final   │ <── too closed      │
  │  └─────┘                    └──────────┘                     │
  │                                                              │
  │  AFTER sealed classes:                                       │
  │                                                              │
  │  ┌──────────────────────────────────────┐                    │
  │  │ sealed                               │                    │
  │  │ Exactly the subclasses YOU specify   │ <── just right!    │
  │  │ Compiler knows ALL possibilities     │                    │
  │  └──────────────────────────────────────┘                    │
  └──────────────────────────────────────────────────────────────┘
```

### Key Benefits

1. **Exhaustive switch** — compiler knows all subtypes, warns if you miss one
2. **Domain modeling** — express "exactly these types exist" in code
3. **Pattern matching** — powerful with sealed hierarchies
4. **API safety** — prevent unexpected extensions

---

## Syntax

```java
// ─── Sealed class ────────────────────────────────
public sealed class Shape permits Circle, Rectangle, Triangle { }

// ─── Subclasses MUST be one of: ──────────────────
//     final    — cannot be extended further
//     sealed   — can be extended by permitted classes only
//     non-sealed — reopens for unrestricted extension

public final class Circle extends Shape { }           // final: done
public sealed class Rectangle extends Shape           // sealed: controlled extension
       permits Square { }
public non-sealed class Triangle extends Shape { }    // non-sealed: open again
public final class Square extends Rectangle { }       // extends sealed Rectangle
```

### Diagram — Modifier Rules

```
  sealed class Shape permits Circle, Rectangle, Triangle

  ┌─────────────────────────────────────────────┐
  │                  Shape (sealed)              │
  │                                             │
  │   ┌──────────┐  ┌────────────┐  ┌────────────────┐
  │   │ Circle   │  │ Rectangle  │  │ Triangle       │
  │   │ (final)  │  │ (sealed)   │  │ (non-sealed)   │
  │   │          │  │            │  │                │
  │   │ Cannot   │  │ permits:   │  │ Anyone can     │
  │   │ extend   │  │ Square     │  │ extend this    │
  │   └──────────┘  ├────────────┤  └────────────────┘
  │                 │ Square     │        │
  │                 │ (final)    │   ┌────┴────┐
  │                 └────────────┘   │ Custom  │
  │                                  │Triangle │
  │                                  └─────────┘
  └─────────────────────────────────────────────┘

  ✓ Circle — final, no further extension
  ✓ Rectangle — sealed, only Square can extend
  ✓ Triangle — non-sealed, anyone can extend
  ✗ Pentagon extends Shape — COMPILE ERROR (not permitted!)
```

---

## Basic Example

```java
// ─── Sealed class ────────────────────────────────
public sealed class AccountType permits Savings, Checking, FixedDeposit {
    private final String label;

    public AccountType(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}

// ─── Permitted subclasses ────────────────────────
public final class Savings extends AccountType {
    private final double interestRate;

    public Savings(double interestRate) {
        super("Savings");
        this.interestRate = interestRate;
    }

    public double getInterestRate() { return interestRate; }
}

public final class Checking extends AccountType {
    private final double overdraftLimit;

    public Checking(double overdraftLimit) {
        super("Checking");
        this.overdraftLimit = overdraftLimit;
    }

    public double getOverdraftLimit() { return overdraftLimit; }
}

public final class FixedDeposit extends AccountType {
    private final int termMonths;
    private final double rate;

    public FixedDeposit(int termMonths, double rate) {
        super("Fixed Deposit");
        this.termMonths = termMonths;
        this.rate = rate;
    }

    public int getTermMonths() { return termMonths; }
    public double getRate()    { return rate; }
}
```

### Usage

```java
public class SealedBasicDemo {
    public static void main(String[] args) {
        AccountType savings = new Savings(0.05);
        AccountType checking = new Checking(5000);
        AccountType fd = new FixedDeposit(12, 0.08);

        describeAccount(savings);
        describeAccount(checking);
        describeAccount(fd);
    }

    static void describeAccount(AccountType account) {
        // Exhaustive switch (Java 21+ pattern matching, or if-else here)
        if (account instanceof Savings s) {
            System.out.printf("Savings account: %.1f%% interest%n", s.getInterestRate() * 100);
        } else if (account instanceof Checking c) {
            System.out.printf("Checking account: $%,.2f overdraft limit%n", c.getOverdraftLimit());
        } else if (account instanceof FixedDeposit fd) {
            System.out.printf("Fixed Deposit: %d months at %.1f%%%n", fd.getTermMonths(), fd.getRate() * 100);
        }
        // No else needed — compiler KNOWS these are the ONLY possibilities
    }
}
```

### Output

```
Savings account: 5.0% interest
Checking account: $5,000.00 overdraft limit
Fixed Deposit: 12 months at 8.0%
```

---

## Sealed Interfaces

Interfaces can also be sealed:

```java
public sealed interface Transaction permits Deposit, Withdrawal, Transfer {
    double amount();
    String accountId();
}

public record Deposit(String accountId, double amount) implements Transaction {}
public record Withdrawal(String accountId, double amount) implements Transaction {}
public record Transfer(String fromAccount, String toAccount, double amount)
        implements Transaction {
    @Override
    public String accountId() { return fromAccount; }
}
```

### Diagram

```
  sealed interface Transaction
  permits Deposit, Withdrawal, Transfer

  ┌─────────────────────────────────────────────┐
  │          Transaction (sealed)               │
  │          amount(), accountId()              │
  │                                             │
  │   ┌──────────┐  ┌────────────┐  ┌─────────┐│
  │   │ Deposit  │  │ Withdrawal │  │Transfer ││
  │   │ (record) │  │ (record)   │  │(record) ││
  │   └──────────┘  └────────────┘  └─────────┘│
  │                                             │
  │   ✗ Refund implements Transaction           │
  │       → COMPILE ERROR (not permitted!)      │
  └─────────────────────────────────────────────┘
```

### Usage with Pattern Matching Switch (Java 21+)

```java
public class SealedInterfaceDemo {
    public static void main(String[] args) {
        Transaction t1 = new Deposit("ACC-01", 5000);
        Transaction t2 = new Withdrawal("ACC-01", 2000);
        Transaction t3 = new Transfer("ACC-01", "ACC-02", 3000);

        System.out.println(describe(t1));
        System.out.println(describe(t2));
        System.out.println(describe(t3));
    }

    static String describe(Transaction txn) {
        return switch (txn) {
            case Deposit d    -> String.format("DEPOSIT $%,.2f to %s", d.amount(), d.accountId());
            case Withdrawal w -> String.format("WITHDRAW $%,.2f from %s", w.amount(), w.accountId());
            case Transfer t   -> String.format("TRANSFER $%,.2f: %s → %s",
                                    t.amount(), t.fromAccount(), t.toAccount());
            // No default needed! Compiler knows all cases are covered.
        };
    }
}
```

### Output

```
DEPOSIT $5,000.00 to ACC-01
WITHDRAW $2,000.00 from ACC-01
TRANSFER $3,000.00: ACC-01 → ACC-02
```

---

## Sealed + Records — Power Combination

Records and sealed classes work beautifully together for **algebraic data types**.

```java
public sealed interface BankEvent permits
        AccountOpened, AccountClosed, BalanceChanged, AccountFrozen {

    String accountId();
    java.time.LocalDateTime timestamp();
}

public record AccountOpened(
    String accountId,
    String owner,
    String type,
    java.time.LocalDateTime timestamp
) implements BankEvent {}

public record AccountClosed(
    String accountId,
    String reason,
    java.time.LocalDateTime timestamp
) implements BankEvent {}

public record BalanceChanged(
    String accountId,
    double oldBalance,
    double newBalance,
    String transactionId,
    java.time.LocalDateTime timestamp
) implements BankEvent {}

public record AccountFrozen(
    String accountId,
    String reason,
    java.time.LocalDateTime timestamp
) implements BankEvent {}
```

### Processing All Event Types

```java
import java.time.LocalDateTime;
import java.util.List;

public class SealedRecordsDemo {
    public static void main(String[] args) {

        List<BankEvent> events = List.of(
            new AccountOpened("ACC-01", "Ali Khan", "Savings", LocalDateTime.now()),
            new BalanceChanged("ACC-01", 0, 25000, "TXN-001", LocalDateTime.now()),
            new BalanceChanged("ACC-01", 25000, 20000, "TXN-002", LocalDateTime.now()),
            new AccountFrozen("ACC-01", "Suspicious activity", LocalDateTime.now()),
            new AccountClosed("ACC-01", "Customer request", LocalDateTime.now())
        );

        System.out.println("=== Event Log ===");
        events.forEach(event -> System.out.println(formatEvent(event)));
    }

    static String formatEvent(BankEvent event) {
        return switch (event) {
            case AccountOpened e -> String.format(
                "[OPENED] %s by %s (%s)", e.accountId(), e.owner(), e.type());

            case AccountClosed e -> String.format(
                "[CLOSED] %s — Reason: %s", e.accountId(), e.reason());

            case BalanceChanged e -> String.format(
                "[BALANCE] %s: $%,.2f → $%,.2f (TXN: %s)",
                e.accountId(), e.oldBalance(), e.newBalance(), e.transactionId());

            case AccountFrozen e -> String.format(
                "[FROZEN] %s — Reason: %s", e.accountId(), e.reason());
        };
    }
}
```

### Output

```
=== Event Log ===
[OPENED] ACC-01 by Ali Khan (Savings)
[BALANCE] ACC-01: $0.00 → $25,000.00 (TXN: TXN-001)
[BALANCE] ACC-01: $25,000.00 → $20,000.00 (TXN: TXN-002)
[FROZEN] ACC-01 — Reason: Suspicious activity
[CLOSED] ACC-01 — Reason: Customer request
```

---

## Complete Banking Example — Sealed Hierarchy

```java
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SealedBankingDemo {

    // ─── Sealed account type hierarchy ───────────────
    sealed interface AccountType permits SavingsType, CheckingType, FixedDepositType {
        String label();
        double interestRate();
    }

    record SavingsType(double interestRate) implements AccountType {
        public String label() { return "Savings"; }
    }

    record CheckingType(double overdraftLimit) implements AccountType {
        public String label() { return "Checking"; }
        public double interestRate() { return 0.0; }
    }

    record FixedDepositType(int months, double interestRate) implements AccountType {
        public String label() { return "Fixed Deposit (" + months + " mo)"; }
    }

    // ─── Sealed transaction hierarchy ────────────────
    sealed interface BankTransaction permits DepositTxn, WithdrawalTxn, TransferTxn {
        String id();
        double amount();
        LocalDateTime time();
    }

    record DepositTxn(String id, String toAccount, double amount, LocalDateTime time)
            implements BankTransaction {}

    record WithdrawalTxn(String id, String fromAccount, double amount, LocalDateTime time)
            implements BankTransaction {}

    record TransferTxn(String id, String from, String to, double amount, LocalDateTime time)
            implements BankTransaction {}

    // ─── Processing logic ────────────────────────────
    static double calculateYearlyInterest(AccountType type, double balance) {
        return switch (type) {
            case SavingsType s      -> balance * s.interestRate();
            case CheckingType c     -> 0.0;   // no interest
            case FixedDepositType f -> balance * f.interestRate();
        };
    }

    static String describeTransaction(BankTransaction txn) {
        return switch (txn) {
            case DepositTxn d    -> String.format("💰 DEPOSIT $%,.2f → %s", d.amount(), d.toAccount());
            case WithdrawalTxn w -> String.format("💸 WITHDRAW $%,.2f ← %s", w.amount(), w.fromAccount());
            case TransferTxn t   -> String.format("🔄 TRANSFER $%,.2f: %s → %s",
                                        t.amount(), t.from(), t.to());
        };
    }

    static double calculateImpact(BankTransaction txn, String accountId) {
        return switch (txn) {
            case DepositTxn d    -> d.toAccount().equals(accountId) ? d.amount() : 0;
            case WithdrawalTxn w -> w.fromAccount().equals(accountId) ? -w.amount() : 0;
            case TransferTxn t   -> {
                double impact = 0;
                if (t.from().equals(accountId)) impact -= t.amount();
                if (t.to().equals(accountId)) impact += t.amount();
                yield impact;
            }
        };
    }

    public static void main(String[] args) {

        // ─── Account types ───────────────────────────
        AccountType savings = new SavingsType(0.05);
        AccountType checking = new CheckingType(5000);
        AccountType fd = new FixedDepositType(12, 0.08);

        double balance = 100000;
        System.out.println("=== Interest Calculation ===");
        System.out.printf("  Savings :  $%,.2f interest%n", calculateYearlyInterest(savings, balance));
        System.out.printf("  Checking:  $%,.2f interest%n", calculateYearlyInterest(checking, balance));
        System.out.printf("  FD (12mo): $%,.2f interest%n", calculateYearlyInterest(fd, balance));

        // ─── Transactions ────────────────────────────
        List<BankTransaction> transactions = List.of(
            new DepositTxn("T1", "ACC-01", 10000, LocalDateTime.now()),
            new WithdrawalTxn("T2", "ACC-01", 3000, LocalDateTime.now()),
            new TransferTxn("T3", "ACC-01", "ACC-02", 5000, LocalDateTime.now()),
            new DepositTxn("T4", "ACC-02", 8000, LocalDateTime.now())
        );

        System.out.println("\n=== Transaction Log ===");
        transactions.forEach(t -> System.out.println("  " + describeTransaction(t)));

        // ─── Net impact on ACC-01 ────────────────────
        double netImpact = transactions.stream()
            .mapToDouble(t -> calculateImpact(t, "ACC-01"))
            .sum();
        System.out.printf("%n=== Net Impact on ACC-01: $%+,.2f ===%n", netImpact);
        // +10000 - 3000 - 5000 = +2000
    }
}
```

### Output

```
=== Interest Calculation ===
  Savings :  $5,000.00 interest
  Checking:  $0.00 interest
  FD (12mo): $8,000.00 interest

=== Transaction Log ===
  💰 DEPOSIT $10,000.00 → ACC-01
  💸 WITHDRAW $3,000.00 ← ACC-01
  🔄 TRANSFER $5,000.00: ACC-01 → ACC-02
  💰 DEPOSIT $8,000.00 → ACC-02

=== Net Impact on ACC-01: $+2,000.00 ===
```

---

## Rules and Constraints

```
  ┌──────────────────────────────────────────────────────────────────┐
  │               SEALED CLASS RULES                                │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  1. Use "sealed" keyword + "permits" clause                      │
  │     sealed class X permits A, B, C { }                           │
  │                                                                  │
  │  2. Permitted subclasses MUST:                                   │
  │     • Be in the same module (or same package if no modules)      │
  │     • Directly extend the sealed class                           │
  │     • Be declared as: final, sealed, or non-sealed               │
  │                                                                  │
  │  3. The "permits" clause can be omitted if subclasses are        │
  │     in the same file:                                            │
  │     sealed class X { }  // permits inferred from same file       │
  │     final class A extends X { }                                  │
  │     final class B extends X { }                                  │
  │                                                                  │
  │  4. Subclass modifiers:                                          │
  │     • final     → no further extension                           │
  │     • sealed    → controlled further extension                   │
  │     • non-sealed → reopens for free extension                    │
  │                                                                  │
  │  5. Records are implicitly final → perfect for sealed permits    │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```

---

## Summary

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                    SEALED CLASSES                                │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  sealed class/interface Name permits A, B, C { }                 │
  │                                                                  │
  │  SUBCLASS MUST BE:                                               │
  │    final       → stops inheritance                               │
  │    sealed      → continues controlled inheritance                │
  │    non-sealed  → reopens for unrestricted inheritance            │
  │                                                                  │
  │  BENEFITS:                                                       │
  │    • Exhaustive switch — compiler checks all cases               │
  │    • Domain modeling — "exactly these types exist"               │
  │    • API safety — prevent unexpected extensions                  │
  │    • Pattern matching — powerful with sealed hierarchies         │
  │                                                                  │
  │  COMMON PATTERNS:                                                │
  │    sealed interface + record implementations                     │
  │    sealed class + final subclasses                               │
  │    Event systems, command patterns, state machines               │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```
