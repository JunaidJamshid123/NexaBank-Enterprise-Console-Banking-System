# 13 — Exception Handling: try / catch / finally

## What is an Exception?

An **exception** is an event that disrupts the normal flow of a program during execution. Instead of crashing, Java provides a structured way to **catch** and **handle** these errors gracefully.

### Real-World Analogy

```
  Normal flow:
  Customer → Enter PIN → Verify → Withdraw Cash → Done ✓

  Exception:
  Customer → Enter PIN → ✗ Wrong PIN! → Show error message → Try again
                               ↑
                          Exception occurred, but program continues
```

---

## Exception Hierarchy

```
                        java.lang.Object
                              │
                       java.lang.Throwable
                       ┌──────┴──────┐
                       │             │
                    Error         Exception
                    (DON'T       ┌────┴──────────────┐
                     CATCH)      │                    │
                              RuntimeException    IOException
                  (Unchecked)   │                 FileNotFoundException
               ┌───────┬───────┤                 SQLException
               │       │       │                 ClassNotFoundException
     NullPointer  ArrayIndex  Arithmetic         (Checked Exceptions)
     Exception    OutOfBounds  Exception
                  Exception

  ┌───────────────────────────────────────────────────────┐
  │  Checked Exceptions   → MUST handle (try/catch or     │
  │                         declare with throws)          │
  │  Unchecked Exceptions → Optional to handle            │
  │                         (RuntimeException subclasses) │
  │  Errors               → NEVER catch                   │
  │                         (OutOfMemoryError, StackOverflow) │
  └───────────────────────────────────────────────────────┘
```

---

## Checked vs Unchecked Exceptions

| Feature             | Checked Exception            | Unchecked Exception           |
|---------------------|------------------------------|-------------------------------|
| **Extends**         | `Exception`                  | `RuntimeException`            |
| **Checked at**      | Compile time                 | Runtime only                  |
| **Must handle?**    | YES (try/catch or throws)    | NO (optional)                 |
| **Common examples** | IOException, SQLException    | NullPointerException, ArithmeticException |
| **Caused by**       | External conditions          | Programming bugs              |
| **Fix by**          | Handling the condition        | Fixing the code               |

---

## The try-catch Block

### Syntax

```java
try {
    // Code that might throw an exception
    // (the "risky" code)
} catch (ExceptionType e) {
    // Code to handle the exception
    // (the "recovery" code)
}
```

### Flow Diagram

```
  ┌──────────────┐
  │  try block   │
  │              │
  │  statement1  │ ← executes
  │  statement2  │ ← EXCEPTION THROWN HERE!
  │  statement3  │ ← SKIPPED (never executes)
  │              │
  └──────┬───────┘
         │ exception
         ▼
  ┌──────────────┐
  │ catch block  │
  │              │
  │ handle error │ ← executes
  │              │
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │  code after  │ ← continues normally
  │  try-catch   │
  └──────────────┘
```

### Code Example

```java
public class TryCatchBasic {
    public static void main(String[] args) {

        // ─── Example 1: ArithmeticException ──────────
        try {
            int result = 100 / 0;   // throws ArithmeticException
            System.out.println("Result: " + result);  // never executes
        } catch (ArithmeticException e) {
            System.out.println("Error: Cannot divide by zero!");
            System.out.println("Message: " + e.getMessage());
        }
        System.out.println("Program continues...\n");

        // ─── Example 2: ArrayIndexOutOfBoundsException
        try {
            int[] balances = {5000, 10000, 15000};
            System.out.println(balances[5]);   // index 5 doesn't exist!
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: Invalid array index!");
            System.out.println("Details: " + e.getMessage());
        }

        // ─── Example 3: NullPointerException ─────────
        try {
            String accountName = null;
            System.out.println(accountName.length());  // NPE!
        } catch (NullPointerException e) {
            System.out.println("\nError: Account name is null!");
        }

        // ─── Example 4: NumberFormatException ────────
        try {
            String input = "abc";
            int amount = Integer.parseInt(input);  // can't parse "abc"
        } catch (NumberFormatException e) {
            System.out.println("\nError: '" + e.getMessage() + "' is not a valid number!");
        }
    }
}
```

### Output

```
Error: Cannot divide by zero!
Message: / by zero
Program continues...

Error: Invalid array index!
Details: Index 5 out of bounds for length 3

Error: Account name is null!

Error: 'For input string: "abc"' is not a valid number!
```

---

## Multiple catch Blocks

You can catch **multiple exception types** with separate catch blocks. Order matters — catch **specific exceptions first**, then general ones.

### Diagram — Match Order

```
  Exception thrown: FileNotFoundException

  catch (FileNotFoundException e)    ← MATCH! Executes this block
  catch (IOException e)              ← Skipped
  catch (Exception e)                ← Skipped

  ⚠️ WRONG ORDER:
  catch (Exception e)                ← Catches EVERYTHING (too broad first)
  catch (IOException e)              ← UNREACHABLE! Compile error!
```

### Code Example

```java
public class MultipleCatch {
    public static void main(String[] args) {
        try {
            int[] numbers = {1, 2, 3};

            // Uncomment one at a time to see different exceptions:
            // int result = numbers[0] / 0;         // ArithmeticException
            int value = numbers[10];                 // ArrayIndexOutOfBoundsException
            // String s = null; s.length();          // NullPointerException

        } catch (ArithmeticException e) {
            System.out.println("Math error: " + e.getMessage());

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Array error: " + e.getMessage());

        } catch (NullPointerException e) {
            System.out.println("Null error: " + e.getMessage());

        } catch (Exception e) {
            // Catch-all: catches any exception not caught above
            System.out.println("General error: " + e.getMessage());
        }
    }
}
```

### Multi-catch (Java 7+) — Single Block for Multiple Types

```java
try {
    // risky code
} catch (ArithmeticException | ArrayIndexOutOfBoundsException | NullPointerException e) {
    System.out.println("Error occurred: " + e.getMessage());
}
```

> The variable `e` is **implicitly final** in a multi-catch block.

---

## The finally Block

The `finally` block **ALWAYS executes** — whether an exception occurred or not. It is used for **cleanup actions** (closing files, database connections, releasing resources).

### Flow Diagram

```
  ┌──────────────────────────────────────────────────┐
  │                                                  │
  │  try {                                           │
  │      // risky code                               │
  │  }                                               │
  │  catch (Exception e) {                           │
  │      // handle error                             │
  │  }                                               │
  │  finally {                                       │
  │      // ALWAYS runs — cleanup here!              │
  │  }                                               │
  │                                                  │
  └──────────────────────────────────────────────────┘

  Scenario 1: No exception
  try ✓ → catch (skipped) → finally ✓ → continue

  Scenario 2: Exception caught
  try ✗ → catch ✓ → finally ✓ → continue

  Scenario 3: Exception NOT caught
  try ✗ → catch (no match) → finally ✓ → exception propagates up

  Scenario 4: Return in try
  try (return) → finally ✓ → then returns
  (finally runs BEFORE the return!)
```

### Code Example

```java
public class FinallyExample {
    public static void main(String[] args) {

        // ─── Scenario 1: No exception ────────────────
        System.out.println("--- Scenario 1: No Exception ---");
        try {
            System.out.println("Try: Calculating...");
            int result = 100 / 5;
            System.out.println("Try: Result = " + result);
        } catch (ArithmeticException e) {
            System.out.println("Catch: " + e.getMessage());
        } finally {
            System.out.println("Finally: Cleanup done!");
        }

        // ─── Scenario 2: Exception caught ────────────
        System.out.println("\n--- Scenario 2: Exception Caught ---");
        try {
            System.out.println("Try: Calculating...");
            int result = 100 / 0;   // exception!
            System.out.println("Try: Result = " + result);  // skipped
        } catch (ArithmeticException e) {
            System.out.println("Catch: " + e.getMessage());
        } finally {
            System.out.println("Finally: Cleanup done!");
        }

        // ─── Scenario 3: finally with return ─────────
        System.out.println("\n--- Scenario 3: Return in try ---");
        String result = testReturn();
        System.out.println("Result: " + result);
    }

    static String testReturn() {
        try {
            System.out.println("Try: About to return...");
            return "from try";
        } finally {
            System.out.println("Finally: I still run before return!");
            // ⚠️ DON'T return from finally — it overrides try's return
        }
    }
}
```

### Output

```
--- Scenario 1: No Exception ---
Try: Calculating...
Try: Result = 20
Finally: Cleanup done!

--- Scenario 2: Exception Caught ---
Try: Calculating...
Catch: / by zero
Finally: Cleanup done!

--- Scenario 3: Return in try ---
Try: About to return...
Finally: I still run before return!
Result: from try
```

---

## try-with-resources (Java 7+)

For objects that implement `AutoCloseable` (files, streams, database connections), `try-with-resources` **automatically closes** them.

### Diagram

```
  TRADITIONAL:                          TRY-WITH-RESOURCES:
  ┌──────────────────┐                 ┌──────────────────────────┐
  │ FileReader fr;   │                 │ try (FileReader fr =     │
  │ try {            │                 │      new FileReader(..)) │
  │   fr = new ...   │                 │ {                        │
  │   // use fr      │                 │   // use fr              │
  │ } catch (...) {  │                 │ } catch (...) {          │
  │   ...            │                 │   ...                    │
  │ } finally {      │                 │ }                        │
  │   if (fr != null)│                 │ // fr is AUTO-CLOSED!    │
  │     fr.close();  │                 └──────────────────────────┘
  │ }                │
  └──────────────────┘
```

### Code Example

```java
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class TryWithResources {
    public static void main(String[] args) {

        // ─── Old way (requires manual close) ─────────
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("accounts.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } finally {
            try {
                if (br != null) br.close();   // messy!
            } catch (IOException e) {
                System.out.println("Error closing: " + e.getMessage());
            }
        }

        // ─── New way — try-with-resources ────────────
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // reader is automatically closed — no finally needed!

        // ─── Multiple resources ──────────────────────
        try (
            FileReader fr = new FileReader("input.txt");
            BufferedReader buffered = new BufferedReader(fr)
        ) {
            // use both — both will be auto-closed
            System.out.println(buffered.readLine());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

---

## throw and throws Keywords

### throw — Manually Throw an Exception

```java
public class ThrowExample {
    public static void withdraw(double amount, double balance) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException(
                "Insufficient funds. Balance: " + balance + ", Requested: " + amount
            );
        }
        System.out.println("Withdrawn: " + amount);
    }

    public static void main(String[] args) {
        try {
            withdraw(5000, 3000);   // throws exception
        } catch (IllegalArgumentException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
}
```

### throws — Declare That a Method May Throw

```java
import java.io.FileReader;
import java.io.IOException;

public class ThrowsExample {

    // Method declares it MAY throw IOException
    // Caller MUST handle it (try/catch or re-declare throws)
    public static String readAccountFile(String filename) throws IOException {
        FileReader fr = new FileReader(filename);     // may throw
        // ... read file
        return "account data";
    }

    public static void main(String[] args) {
        // Option 1: Handle with try-catch
        try {
            String data = readAccountFile("accounts.txt");
            System.out.println(data);
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }
}
```

### Diagram: throw vs throws

```
  ┌──────────────────────────────────────────────────────────┐
  │                                                          │
  │  throw                          throws                   │
  │  ─────                          ──────                   │
  │  Used INSIDE a method           Used in method SIGNATURE │
  │  Creates and throws an          Declares possible        │
  │  exception object               exceptions               │
  │                                                          │
  │  throw new Exception("msg");    void m() throws IOEx {}  │
  │                                                          │
  │  Followed by an object          Followed by class names  │
  │  Can throw ONE at a time        Can declare MULTIPLE     │
  └──────────────────────────────────────────────────────────┘
```

---

## Exception Methods

Every exception object provides these useful methods:

```java
try {
    int[] arr = new int[3];
    arr[10] = 5;
} catch (ArrayIndexOutOfBoundsException e) {

    // getMessage() — Short description
    System.out.println(e.getMessage());
    // "Index 10 out of bounds for length 3"

    // toString() — Class name + message
    System.out.println(e.toString());
    // "java.lang.ArrayIndexOutOfBoundsException: Index 10 out of bounds..."

    // printStackTrace() — Full stack trace (most useful for debugging)
    e.printStackTrace();
    // java.lang.ArrayIndexOutOfBoundsException: Index 10 out of bounds...
    //     at TryCatchBasic.main(TryCatchBasic.java:5)

    // getClass().getName() — Exception class name
    System.out.println(e.getClass().getName());
    // "java.lang.ArrayIndexOutOfBoundsException"
}
```

---

## Banking Example — Complete Exception Handling

```java
public class BankingExceptionDemo {
    private double balance;
    private boolean isFrozen;

    public BankingExceptionDemo(double initialBalance) {
        this.balance = initialBalance;
        this.isFrozen = false;
    }

    public void deposit(double amount) {
        try {
            validateAmount(amount);
            checkAccountStatus();
            balance += amount;
            System.out.println("Deposited: $" + amount + " | New Balance: $" + balance);
        } catch (IllegalArgumentException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Account issue: " + e.getMessage());
        } finally {
            System.out.println("Transaction logged.\n");
        }
    }

    public void withdraw(double amount) {
        try {
            validateAmount(amount);
            checkAccountStatus();
            if (amount > balance) {
                throw new IllegalArgumentException(
                    "Insufficient funds. Balance: $" + balance
                );
            }
            balance -= amount;
            System.out.println("Withdrawn: $" + amount + " | New Balance: $" + balance);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        } finally {
            System.out.println("Transaction logged.\n");
        }
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive. Got: $" + amount);
        }
        if (amount > 1_000_000) {
            throw new IllegalArgumentException("Amount exceeds single transaction limit.");
        }
    }

    private void checkAccountStatus() {
        if (isFrozen) {
            throw new IllegalStateException("Account is frozen. Contact support.");
        }
    }

    public void freezeAccount() { this.isFrozen = true; }

    public static void main(String[] args) {
        BankingExceptionDemo account = new BankingExceptionDemo(5000.00);

        account.deposit(2000);       // ✓ Success
        account.withdraw(10000);     // ✗ Insufficient funds
        account.deposit(-500);       // ✗ Negative amount

        account.freezeAccount();
        account.withdraw(1000);      // ✗ Account frozen
    }
}
```

### Output

```
Deposited: $2000.0 | New Balance: $7000.0
Transaction logged.

Withdrawal failed: Insufficient funds. Balance: $7000.0
Transaction logged.

Deposit failed: Amount must be positive. Got: $-500.0
Transaction logged.

Withdrawal failed: Account is frozen. Contact support.
Transaction logged.
```

---

## Summary

```
  ┌──────────────────────────────────────────────────────────────┐
  │                   EXCEPTION HANDLING                         │
  ├──────────────────────────────────────────────────────────────┤
  │                                                              │
  │  try        → Wrap risky code                                │
  │  catch      → Handle specific exceptions                     │
  │  finally    → Always runs (cleanup)                          │
  │  throw      → Manually throw an exception                    │
  │  throws     → Declare that method may throw                  │
  │                                                              │
  │  try-with-resources → Auto-close resources                   │
  │  Multi-catch        → catch (TypeA | TypeB e)                │
  │                                                              │
  ├──────────────────────────────────────────────────────────────┤
  │  BEST PRACTICES:                                             │
  │  • Catch specific exceptions, not just Exception             │
  │  • Don't swallow exceptions (empty catch blocks)             │
  │  • Use try-with-resources for closeable objects               │
  │  • Don't use exceptions for flow control                     │
  │  • Log exceptions, don't just print them                     │
  │  • Throw early, catch late                                   │
  └──────────────────────────────────────────────────────────────┘
```
