# 22 — Pattern Matching

## What is Pattern Matching?

**Pattern matching** allows you to test a value against a pattern and, if it matches, extract data from it — all in one step. It eliminates verbose casting and conditional boilerplate.

### Before vs After

```java
// ─── BEFORE (verbose instanceof + cast) ──────────
if (obj instanceof String) {
    String s = (String) obj;        // explicit cast
    System.out.println(s.length());
}

// ─── AFTER (pattern matching for instanceof) ─────
if (obj instanceof String s) {      // test + cast + assign in ONE step
    System.out.println(s.length());
}
```

> Pattern matching has evolved across multiple Java versions:
> - **Java 16**: `instanceof` pattern matching
> - **Java 21**: Pattern matching for `switch`
> - **Java 21**: Record patterns
> - **Java 21**: Guarded patterns (`when`)

---

## Pattern Matching Timeline

```
  ┌──────────────────────────────────────────────────────────────┐
  │  Java 16   instanceof pattern matching (finalized)           │
  │  Java 17   Sealed classes (enables exhaustive switch)        │
  │  Java 21   Switch pattern matching (finalized)               │
  │  Java 21   Record patterns (finalized)                       │
  │  Java 21   Guarded patterns with "when" (finalized)          │
  └──────────────────────────────────────────────────────────────┘
```

---

## 1. Pattern Matching for instanceof (Java 16+)

### The Old Way vs The New Way

```
  OLD WAY:                              NEW WAY:
  ┌──────────────────────────┐         ┌──────────────────────────┐
  │ if (obj instanceof Str)  │         │ if (obj instanceof Str s)│
  │ {                        │         │ {                        │
  │   String s = (String)obj;│         │   // s is ready to use! │
  │   s.length();            │         │   s.length();            │
  │ }                        │         │ }                        │
  └──────────────────────────┘         └──────────────────────────┘
       3 steps                              1 step
```

### Code Example

```java
public class InstanceofPatternDemo {
    public static void main(String[] args) {

        Object[] values = { "Hello", 42, 3.14, true, null, new int[]{1, 2, 3} };

        for (Object obj : values) {
            describeOld(obj);    // old way
            describeNew(obj);    // new way
            System.out.println();
        }
    }

    // ─── Old way: instanceof + explicit cast ─────────
    static void describeOld(Object obj) {
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("Old: String of length " + s.length());
        } else if (obj instanceof Integer) {
            Integer i = (Integer) obj;
            System.out.println("Old: Integer value " + i);
        } else if (obj instanceof Double) {
            Double d = (Double) obj;
            System.out.println("Old: Double value " + d);
        }
    }

    // ─── New way: pattern matching ───────────────────
    static void describeNew(Object obj) {
        if (obj instanceof String s) {
            System.out.println("New: String of length " + s.length());
        } else if (obj instanceof Integer i) {
            System.out.println("New: Integer value " + i);
        } else if (obj instanceof Double d) {
            System.out.println("New: Double value " + d);
        } else if (obj instanceof Boolean b) {
            System.out.println("New: Boolean is " + b);
        } else if (obj instanceof int[] arr) {
            System.out.println("New: int[] of length " + arr.length);
        } else {
            System.out.println("New: null or unknown type");
        }
    }
}
```

### Scope of the Pattern Variable

```java
// The pattern variable 's' is in scope where the compiler
// can GUARANTEE the instanceof check succeeded.

if (obj instanceof String s) {
    // s is in scope here ✓
    System.out.println(s.length());
}
// s is NOT in scope here ✗

// ─── With negation (scope flows to else) ─────────
if (!(obj instanceof String s)) {
    // s is NOT in scope here
    return;
}
// s IS in scope here ✓ (because if it wasn't String, we returned)
System.out.println(s.toUpperCase());

// ─── With && (short-circuit) ─────────────────────
if (obj instanceof String s && s.length() > 5) {
    // s is in scope and guaranteed to be String with length > 5
    System.out.println("Long string: " + s);
}

// ─── With || — NOT allowed! ──────────────────────
// if (obj instanceof String s || s.length() > 5)  // COMPILE ERROR!
// (s might not be assigned if left side is true for other reasons)
```

### Diagram — Scope Rules

```
  if (obj instanceof String s) {
      ┌─────────────────────┐
      │ s is in scope ✓     │
      │ s is type String    │
      └─────────────────────┘
  } else {
      ┌─────────────────────┐
      │ s is NOT in scope ✗ │
      └─────────────────────┘
  }

  if (!(obj instanceof String s)) {
      return;  // exit early
  }
  ┌─────────────────────────────┐
  │ s is in scope ✓             │
  │ Guaranteed: obj IS a String │
  └─────────────────────────────┘
```

---

## 2. Pattern Matching for switch (Java 21+)

### Basic Syntax

```java
static String describe(Object obj) {
    return switch (obj) {
        case Integer i  -> "Integer: " + i;
        case String s   -> "String: " + s;
        case Double d   -> "Double: " + d;
        case null       -> "null value";
        default         -> "Unknown: " + obj.getClass().getSimpleName();
    };
}
```

### Diagram — How Switch Pattern Matching Works

```
  Object obj = "Hello";

  switch (obj) {
      case Integer i  →  Does obj match Integer? NO → skip
      case String s   →  Does obj match String?  YES → execute, s = "Hello"
      case Double d   →  (not reached)
      ...
  }

  ┌──────────┐
  │   obj    │
  │ "Hello"  │
  └────┬─────┘
       │
       ▼
  ┌─────────────┐   NO    ┌─────────────┐   YES   ┌──────────────┐
  │ Integer i?  │────────►│ String s?   │────────►│ Execute body │
  └─────────────┘         └─────────────┘         │ s = "Hello"  │
                                                   └──────────────┘
```

### Code Example

```java
public class SwitchPatternDemo {

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    public static void main(String[] args) {

        Shape[] shapes = {
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 8)
        };

        for (Shape shape : shapes) {
            System.out.printf("%-25s Area: %.2f%n", shape, calculateArea(shape));
        }
    }

    static double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle c    -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t  -> 0.5 * t.base() * t.height();
            // No default needed — sealed interface, all cases covered!
        };
    }
}
```

### Output

```
Circle[radius=5.0]        Area: 78.54
Rectangle[width=4.0, height=6.0] Area: 24.00
Triangle[base=3.0, height=8.0] Area: 12.00
```

---

## 3. Guarded Patterns — `when` Clause (Java 21+)

Add conditions **after** the type match using `when`:

```java
static String classifyBalance(Object obj) {
    return switch (obj) {
        case Double d when d > 100000  -> "VIP Customer ($" + d + ")";
        case Double d when d > 10000   -> "Premium Customer ($" + d + ")";
        case Double d when d > 0       -> "Regular Customer ($" + d + ")";
        case Double d when d == 0      -> "Empty Account";
        case Double d                  -> "Overdrawn! ($" + d + ")";
        case Integer i when i > 0     -> "Integer balance: " + i;
        case null                      -> "No balance data";
        default                        -> "Unknown type";
    };
}
```

### Diagram — Guarded Pattern Evaluation

```
  switch (balance) {    // balance = 25000.0

  case Double d when d > 100000  → d=25000, 25000 > 100000? NO → skip
  case Double d when d > 10000   → d=25000, 25000 > 10000?  YES → MATCH!
  case Double d when d > 0       → (not reached)
  ...

  ┌──────────┐
  │ 25000.0  │
  └────┬─────┘
       │
       ▼
  ┌────────────────────────┐   NO
  │ Double d && d > 100000 │─────────┐
  └────────────────────────┘         │
                                     ▼
                          ┌────────────────────────┐   YES
                          │ Double d && d > 10000  │─────────► "Premium Customer"
                          └────────────────────────┘

  ORDER MATTERS! Specific guards before general ones.
```

### Complete Guarded Example

```java
public class GuardedPatternDemo {

    sealed interface Transaction permits Deposit, Withdrawal, Transfer {}
    record Deposit(String account, double amount) implements Transaction {}
    record Withdrawal(String account, double amount) implements Transaction {}
    record Transfer(String from, String to, double amount) implements Transaction {}

    public static void main(String[] args) {

        Transaction[] transactions = {
            new Deposit("ACC-01", 500),
            new Deposit("ACC-01", 150000),
            new Withdrawal("ACC-01", 200),
            new Withdrawal("ACC-01", 60000),
            new Transfer("ACC-01", "ACC-02", 5000),
            new Transfer("ACC-01", "ACC-02", 250000)
        };

        for (Transaction txn : transactions) {
            System.out.println(processTransaction(txn));
        }
    }

    static String processTransaction(Transaction txn) {
        return switch (txn) {
            // Large deposit — requires compliance review
            case Deposit d when d.amount() > 100000 ->
                "⚠️ LARGE DEPOSIT $%,.2f to %s — Compliance review required!"
                    .formatted(d.amount(), d.account());

            // Normal deposit
            case Deposit d ->
                "✅ Deposit $%,.2f to %s"
                    .formatted(d.amount(), d.account());

            // Large withdrawal — requires manager approval
            case Withdrawal w when w.amount() > 50000 ->
                "⚠️ LARGE WITHDRAWAL $%,.2f from %s — Manager approval needed!"
                    .formatted(w.amount(), w.account());

            // Normal withdrawal
            case Withdrawal w ->
                "✅ Withdrawal $%,.2f from %s"
                    .formatted(w.amount(), w.account());

            // Large transfer — extra verification
            case Transfer t when t.amount() > 200000 ->
                "🔴 LARGE TRANSFER $%,.2f: %s → %s — Requires 2FA + manager"
                    .formatted(t.amount(), t.from(), t.to());

            // Normal transfer
            case Transfer t ->
                "✅ Transfer $%,.2f: %s → %s"
                    .formatted(t.amount(), t.from(), t.to());
        };
    }
}
```

### Output

```
✅ Deposit $500.00 to ACC-01
⚠️ LARGE DEPOSIT $150,000.00 to ACC-01 — Compliance review required!
✅ Withdrawal $200.00 from ACC-01
⚠️ LARGE WITHDRAWAL $60,000.00 from ACC-01 — Manager approval needed!
✅ Transfer $5,000.00: ACC-01 → ACC-02
🔴 LARGE TRANSFER $250,000.00: ACC-01 → ACC-02 — Requires 2FA + manager
```

---

## 4. Record Patterns — Destructuring (Java 21+)

Record patterns let you **extract fields directly** in the pattern:

```java
// Instead of:
case Circle c -> Math.PI * c.radius() * c.radius();

// You can destructure:
case Circle(double r) -> Math.PI * r * r;
//           ↑ extract radius directly!
```

### Code Example

```java
public class RecordPatternDemo {

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    // Nested records
    record Point(double x, double y) {}
    record Line(Point start, Point end) {}

    public static void main(String[] args) {

        // ─── Basic record pattern ────────────────────
        Shape shape = new Circle(5.0);

        if (shape instanceof Circle(double r)) {
            System.out.println("Circle radius: " + r);  // 5.0
        }

        // ─── In switch ──────────────────────────────
        Shape[] shapes = { new Circle(5), new Rectangle(4, 6), new Triangle(3, 8) };

        for (Shape s : shapes) {
            String desc = switch (s) {
                case Circle(double r)            -> "Circle with radius " + r;
                case Rectangle(double w, double h) -> "Rectangle " + w + " x " + h;
                case Triangle(double b, double h)  -> "Triangle base=" + b + " height=" + h;
            };
            System.out.println(desc);
        }

        // ─── Nested record patterns ─────────────────
        Line line = new Line(new Point(0, 0), new Point(3, 4));

        if (line instanceof Line(Point(double x1, double y1), Point(double x2, double y2))) {
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.printf("Line length: %.2f%n", length);  // 5.00
        }
    }
}
```

### Diagram — Record Pattern Destructuring

```
  Record:  Circle(double radius)
  Value:   new Circle(5.0)

  Pattern: case Circle(double r)

  ┌───────────────────┐
  │ Circle(5.0)       │
  │                   │
  │ radius ─── 5.0    │──── extracted as 'r'
  │                   │
  └───────────────────┘

  Nested Pattern: Line(Point(double x1, double y1), Point(double x2, double y2))

  ┌─────────────────────────────────────────┐
  │ Line                                    │
  │  start ── Point                         │
  │            x ── 0.0  ──── extracted x1  │
  │            y ── 0.0  ──── extracted y1  │
  │  end ──── Point                         │
  │            x ── 3.0  ──── extracted x2  │
  │            y ── 4.0  ──── extracted y2  │
  └─────────────────────────────────────────┘
```

---

## 5. Null Handling in Pattern Switch

```java
static String processInput(Object obj) {
    return switch (obj) {
        case null           -> "Received null";          // explicit null case
        case String s       -> "String: " + s;
        case Integer i      -> "Integer: " + i;
        default             -> "Other: " + obj;
    };
}

// Can also combine null with another case:
static String process(String s) {
    return switch (s) {
        case null, default   -> "null or unrecognized";
        case "DEPOSIT"       -> "Processing deposit";
        case "WITHDRAWAL"    -> "Processing withdrawal";
    };
}
```

---

## 6. Complete Banking Example — All Patterns Combined

```java
import java.time.LocalDateTime;
import java.util.*;

public class PatternMatchingBankingDemo {

    // ─── Sealed hierarchy ────────────────────────────
    sealed interface BankRequest permits
            OpenAccount, CloseAccount, TransactionRequest, BalanceInquiry {}

    record OpenAccount(String customerId, String type, double initialDeposit)
            implements BankRequest {}

    record CloseAccount(String accountId, String reason)
            implements BankRequest {}

    sealed interface TransactionRequest extends BankRequest
            permits DepositReq, WithdrawalReq, TransferReq {}

    record DepositReq(String accountId, double amount)
            implements TransactionRequest {}

    record WithdrawalReq(String accountId, double amount)
            implements TransactionRequest {}

    record TransferReq(String fromAccount, String toAccount, double amount)
            implements TransactionRequest {}

    record BalanceInquiry(String accountId)
            implements BankRequest {}

    // ─── Response types ──────────────────────────────
    sealed interface BankResponse permits Success, Failure {}
    record Success(String message, Object data) implements BankResponse {}
    record Failure(String errorCode, String message) implements BankResponse {}

    // ─── Process requests using pattern matching ─────
    static BankResponse processRequest(BankRequest request) {
        return switch (request) {
            case null ->
                new Failure("NULL_REQUEST", "Request cannot be null");

            // ─── Account Operations ──────────────────
            case OpenAccount(String cid, String type, double deposit) when deposit < 0 ->
                new Failure("INVALID_DEPOSIT", "Initial deposit cannot be negative");

            case OpenAccount(String cid, String type, double deposit) when deposit < 1000 ->
                new Failure("MIN_DEPOSIT", "Minimum initial deposit is $1,000");

            case OpenAccount(String cid, String type, double deposit) ->
                new Success("Account opened for customer " + cid,
                    Map.of("customerId", cid, "type", type, "balance", deposit));

            case CloseAccount(String accId, String reason) when reason == null || reason.isBlank() ->
                new Failure("MISSING_REASON", "Closure reason is required");

            case CloseAccount(String accId, String reason) ->
                new Success("Account " + accId + " closed", Map.of("reason", reason));

            // ─── Transaction Operations ──────────────
            case DepositReq(String accId, double amount) when amount <= 0 ->
                new Failure("INVALID_AMOUNT", "Deposit must be positive");

            case DepositReq(String accId, double amount) when amount > 100000 ->
                new Failure("LIMIT_EXCEEDED", "Single deposit limit: $100,000. Amount: $" + amount);

            case DepositReq(String accId, double amount) ->
                new Success("Deposited $" + amount + " to " + accId, amount);

            case WithdrawalReq(String accId, double amount) when amount <= 0 ->
                new Failure("INVALID_AMOUNT", "Withdrawal must be positive");

            case WithdrawalReq(String accId, double amount) ->
                new Success("Withdrawn $" + amount + " from " + accId, amount);

            case TransferReq(String from, String to, double amount) when from.equals(to) ->
                new Failure("SAME_ACCOUNT", "Cannot transfer to same account");

            case TransferReq(String from, String to, double amount) when amount <= 0 ->
                new Failure("INVALID_AMOUNT", "Transfer amount must be positive");

            case TransferReq(String from, String to, double amount) ->
                new Success("Transferred $" + amount + ": " + from + " → " + to, amount);

            // ─── Inquiry ─────────────────────────────
            case BalanceInquiry(String accId) ->
                new Success("Balance for " + accId, 25000.0);  // simulated
        };
    }

    // ─── Format response using pattern matching ──────
    static String formatResponse(BankResponse response) {
        return switch (response) {
            case Success(String msg, Object data) ->
                "✅ " + msg + (data != null ? " [Data: " + data + "]" : "");
            case Failure(String code, String msg) ->
                "❌ [" + code + "] " + msg;
        };
    }

    public static void main(String[] args) {

        List<BankRequest> requests = List.of(
            new OpenAccount("C001", "Savings", 5000),
            new OpenAccount("C002", "Checking", 500),      // below minimum
            new DepositReq("ACC-01", 25000),
            new DepositReq("ACC-01", 150000),               // over limit
            new WithdrawalReq("ACC-01", 3000),
            new TransferReq("ACC-01", "ACC-02", 5000),
            new TransferReq("ACC-01", "ACC-01", 1000),      // same account!
            new BalanceInquiry("ACC-01"),
            new CloseAccount("ACC-03", "Customer request"),
            new CloseAccount("ACC-04", "")                   // blank reason
        );

        System.out.println("=== Processing Bank Requests ===\n");
        for (BankRequest req : requests) {
            BankResponse response = processRequest(req);
            System.out.println(formatResponse(response));
        }
    }
}
```

### Output

```
=== Processing Bank Requests ===

✅ Account opened for customer C001 [Data: {customerId=C001, type=Savings, balance=5000.0}]
❌ [MIN_DEPOSIT] Minimum initial deposit is $1,000
✅ Deposited $25000.0 to ACC-01 [Data: 25000.0]
❌ [LIMIT_EXCEEDED] Single deposit limit: $100,000. Amount: $150000.0
✅ Withdrawn $3000.0 from ACC-01 [Data: 3000.0]
✅ Transferred $5000.0: ACC-01 → ACC-02 [Data: 5000.0]
❌ [SAME_ACCOUNT] Cannot transfer to same account
✅ Balance for ACC-01 [Data: 25000.0]
✅ Account ACC-03 closed [Data: {reason=Customer request}]
❌ [MISSING_REASON] Closure reason is required
```

---

## Pattern Types Summary

```
  ┌────────────────────────────────────────────────────────────────────┐
  │                  PATTERN TYPES IN JAVA                            │
  ├──────────────────────┬─────────────────────────────────────────────┤
  │ Type Pattern         │ case Integer i -> ...                       │
  │ (Java 16+)           │ obj instanceof String s                    │
  │                      │ Tests type + binds variable                │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ Guarded Pattern      │ case String s when s.length() > 5 -> ...   │
  │ (Java 21+)           │ Type match + additional condition           │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ Record Pattern       │ case Circle(double r) -> ...               │
  │ (Java 21+)           │ Destructures record into components        │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ Nested Pattern       │ case Line(Point(var x, var y), _) -> ...   │
  │ (Java 21+)           │ Destructures nested records                │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ Null Pattern         │ case null -> ...                            │
  │ (Java 21+)           │ Explicitly handles null in switch           │
  ├──────────────────────┼─────────────────────────────────────────────┤
  │ Default Pattern      │ default -> ...                              │
  │                      │ Catches everything else                     │
  └──────────────────────┴─────────────────────────────────────────────┘
```

---

## Summary

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                    PATTERN MATCHING                              │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  instanceof PATTERN (Java 16+):                                  │
  │    if (obj instanceof Type var) { /* use var */ }                │
  │                                                                  │
  │  switch PATTERN (Java 21+):                                      │
  │    switch (obj) {                                                │
  │        case Type var -> ...                                      │
  │        case Type var when condition -> ...  (guarded)            │
  │        case Record(Type f1, Type f2) -> ... (destructure)       │
  │        case null -> ...                                          │
  │        default -> ...                                            │
  │    }                                                             │
  │                                                                  │
  │  KEY BENEFITS:                                                   │
  │    • Eliminates explicit casts                                   │
  │    • Exhaustive checking with sealed types                       │
  │    • Cleaner, more readable conditional logic                    │
  │    • Record destructuring extracts fields inline                 │
  │    • Guarded patterns combine type + condition checks            │
  │                                                                  │
  │  WORKS BEST WITH:                                                │
  │    • Sealed classes/interfaces                                   │
  │    • Records                                                     │
  │    • Complex domain models (banking, events, commands)           │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```
