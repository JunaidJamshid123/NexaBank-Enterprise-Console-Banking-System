# 14 — Custom Exceptions

## Why Create Custom Exceptions?

Java's built-in exceptions (`NullPointerException`, `IOException`, etc.) are generic. In real applications, you need **domain-specific** exceptions that describe **your business logic** clearly.

### Built-in vs Custom

```
  Built-in:
    throw new IllegalArgumentException("Invalid amount");
    → Generic. What kind of invalid? Which rule was broken?

  Custom:
    throw new InsufficientFundsException(account, amount, balance);
    → Specific! Carries context: which account, how much, what balance.
```

> Custom exceptions make your code **self-documenting**, easier to debug, and allow **targeted catch blocks**.

---

## How to Create Custom Exceptions

### The Two Types

```
  ┌─────────────────────────────────────────────────────────┐
  │                                                         │
  │  Checked Custom Exception:                              │
  │    class MyException extends Exception { }              │
  │    → Caller MUST handle (try/catch or throws)           │
  │    → Use for recoverable conditions                     │
  │                                                         │
  │  Unchecked Custom Exception:                            │
  │    class MyException extends RuntimeException { }       │
  │    → Caller doesn't have to handle                      │
  │    → Use for programming errors / validation failures   │
  │                                                         │
  └─────────────────────────────────────────────────────────┘
```

### Hierarchy Diagram

```
                       Throwable
                      ┌────┴────┐
                   Error     Exception
                          ┌─────┴──────────────────┐
                   RuntimeException          IOException
                          │                        │
               ┌──────────┼──────────┐             │
               │          │          │             │
     InsufficientFunds  AccountFrozen  ...    FileProcessingException
     Exception          Exception              (YOUR custom checked)
     (YOUR custom       (YOUR custom
      unchecked)         unchecked)
```

---

## Pattern 1: Basic Custom Exception

### Step-by-Step

```java
// Step 1: Extend Exception (checked) or RuntimeException (unchecked)
// Step 2: Provide constructors that call super()
// Step 3: Optionally add custom fields for extra context

public class InsufficientFundsException extends RuntimeException {

    // Constructor with message only
    public InsufficientFundsException(String message) {
        super(message);
    }

    // Constructor with message + cause (for exception chaining)
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Usage

```java
public class BasicCustomExceptionDemo {
    private double balance = 5000;

    public void withdraw(double amount) {
        if (amount > balance) {
            throw new InsufficientFundsException(
                "Cannot withdraw $" + amount + ". Balance is only $" + balance
            );
        }
        balance -= amount;
        System.out.println("Withdrawn: $" + amount);
    }

    public static void main(String[] args) {
        BasicCustomExceptionDemo account = new BasicCustomExceptionDemo();
        try {
            account.withdraw(8000);
        } catch (InsufficientFundsException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
}
```

**Output:**
```
Transaction failed: Cannot withdraw $8000.0. Balance is only $5000.0
```

---

## Pattern 2: Custom Exception with Extra Fields

Adding **custom fields** makes exceptions carry rich context — very useful for logging and debugging.

```java
public class InsufficientFundsException extends RuntimeException {
    private final String accountId;
    private final double requestedAmount;
    private final double currentBalance;
    private final double deficit;

    public InsufficientFundsException(String accountId, double requestedAmount, double currentBalance) {
        super(String.format(
            "Account %s: Cannot withdraw $%.2f. Balance: $%.2f. Short by: $%.2f",
            accountId, requestedAmount, currentBalance, requestedAmount - currentBalance
        ));
        this.accountId = accountId;
        this.requestedAmount = requestedAmount;
        this.currentBalance = currentBalance;
        this.deficit = requestedAmount - currentBalance;
    }

    // Getters — allow catch blocks to access individual fields
    public String getAccountId()       { return accountId; }
    public double getRequestedAmount() { return requestedAmount; }
    public double getCurrentBalance()  { return currentBalance; }
    public double getDeficit()         { return deficit; }
}
```

### Usage

```java
public class RichExceptionDemo {
    public static void main(String[] args) {
        try {
            processWithdrawal("ACC-1001", 8000, 5000);
        } catch (InsufficientFundsException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("Account: " + e.getAccountId());
            System.out.println("Short by: $" + e.getDeficit());
        }
    }

    static void processWithdrawal(String accountId, double amount, double balance) {
        if (amount > balance) {
            throw new InsufficientFundsException(accountId, amount, balance);
        }
    }
}
```

**Output:**
```
ERROR: Account ACC-1001: Cannot withdraw $8000.00. Balance: $5000.00. Short by: $3000.00
Account: ACC-1001
Short by: $3000.0
```

---

## Pattern 3: Checked Custom Exception

Use when the caller **must** handle the exception — typically for conditions outside the programmer's control.

```java
// Checked exception — forces caller to handle it
public class AccountNotFoundException extends Exception {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
```

### Usage with throws Declaration

```java
import java.util.HashMap;
import java.util.Map;

public class CheckedExceptionDemo {
    private static final Map<String, Double> accounts = new HashMap<>();

    static {
        accounts.put("ACC-1001", 25000.0);
        accounts.put("ACC-1002", 18500.0);
    }

    // Must declare "throws" because AccountNotFoundException is checked
    public static double getBalance(String accountId) throws AccountNotFoundException {
        if (!accounts.containsKey(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return accounts.get(accountId);
    }

    public static void main(String[] args) {
        // Caller MUST handle it — won't compile without try/catch or throws
        try {
            double balance = getBalance("ACC-9999");
            System.out.println("Balance: " + balance);
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
            System.out.println("Searched for: " + e.getAccountId());
        }
    }
}
```

**Output:**
```
Account not found: ACC-9999
Searched for: ACC-9999
```

---

## Pattern 4: Exception Chaining (Wrapping)

When one exception **causes** another, chain them. This preserves the original stack trace.

```java
public class TransactionFailedException extends Exception {
    private final String transactionId;

    public TransactionFailedException(String transactionId, String message, Throwable cause) {
        super(message, cause);   // pass the original cause
        this.transactionId = transactionId;
    }

    public String getTransactionId() { return transactionId; }
}
```

### Usage: Wrapping Low-Level Exceptions

```java
public class ExceptionChainingDemo {

    public static void transferFunds(String txnId, String from, String to, double amount)
            throws TransactionFailedException {
        try {
            // Simulate a database error
            connectToDatabase();
        } catch (Exception e) {
            // Wrap the low-level exception in a domain-specific one
            throw new TransactionFailedException(
                txnId,
                "Transfer failed for TXN: " + txnId,
                e   // original cause preserved!
            );
        }
    }

    private static void connectToDatabase() {
        // Simulates a database connection failure
        throw new RuntimeException("Connection refused: DB server unreachable");
    }

    public static void main(String[] args) {
        try {
            transferFunds("TXN-5001", "ACC-01", "ACC-02", 1000);
        } catch (TransactionFailedException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("TXN ID: " + e.getTransactionId());
            System.out.println("Root cause: " + e.getCause().getMessage());
        }
    }
}
```

**Output:**
```
ERROR: Transfer failed for TXN: TXN-5001
TXN ID: TXN-5001
Root cause: Connection refused: DB server unreachable
```

### Diagram — Exception Chain

```
  ┌─────────────────────────────────────┐
  │ TransactionFailedException          │
  │   message: "Transfer failed..."     │
  │   cause: ──────────────────────┐    │
  └────────────────────────────────┼────┘
                                   │
                                   ▼
  ┌─────────────────────────────────────┐
  │ RuntimeException                    │
  │   message: "Connection refused..."  │
  │   cause: null                       │
  └─────────────────────────────────────┘

  e.getMessage()         → "Transfer failed..."
  e.getCause()           → RuntimeException
  e.getCause().getMessage() → "Connection refused..."
```

---

## Pattern 5: Exception Hierarchy for a Domain

For a banking system, create a **hierarchy** of related exceptions:

```
                  BankingException (base)
                  ┌──────┼──────────────┐
                  │      │              │
        AccountException │     TransactionException
        ┌─────┼──────┐  │     ┌────────┼────────┐
        │     │      │  │     │        │        │
  AccountNot AccountFrozen  InsufficientFunds TransactionLimit
  Found     Exception       Exception        ExceededException
  Exception
```

### Code: Exception Hierarchy

```java
// ─── Base Exception ──────────────────────────────
public class BankingException extends RuntimeException {
    private final String errorCode;

    public BankingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}

// ─── Account Exceptions ─────────────────────────
public class AccountException extends BankingException {
    private final String accountId;

    public AccountException(String errorCode, String accountId, String message) {
        super(errorCode, message);
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String accountId) {
        super("ACC_NOT_FOUND", accountId, "Account not found: " + accountId);
    }
}

public class AccountFrozenException extends AccountException {
    public AccountFrozenException(String accountId) {
        super("ACC_FROZEN", accountId,
            "Account " + accountId + " is frozen. Contact support.");
    }
}

// ─── Transaction Exceptions ─────────────────────
public class TransactionException extends BankingException {
    private final String transactionId;

    public TransactionException(String errorCode, String transactionId, String message) {
        super(errorCode, message);
        this.transactionId = transactionId;
    }

    public String getTransactionId() { return transactionId; }
}

public class InsufficientFundsException extends TransactionException {
    private final double deficit;

    public InsufficientFundsException(String txnId, double requested, double balance) {
        super("INSUFFICIENT_FUNDS", txnId,
            String.format("Need $%.2f more for transaction %s", requested - balance, txnId));
        this.deficit = requested - balance;
    }

    public double getDeficit() { return deficit; }
}

public class TransactionLimitExceededException extends TransactionException {
    public TransactionLimitExceededException(String txnId, double amount, double limit) {
        super("TXN_LIMIT_EXCEEDED", txnId,
            String.format("Amount $%.2f exceeds limit $%.2f for TXN %s", amount, limit, txnId));
    }
}
```

### Using the Hierarchy

```java
public class BankingExceptionHierarchyDemo {
    public static void main(String[] args) {
        try {
            processTransaction("TXN-001", "ACC-1001", 50000, 10000);

        } catch (InsufficientFundsException e) {
            System.out.println("[" + e.getErrorCode() + "] " + e.getMessage());
            System.out.println("Deficit: $" + e.getDeficit());

        } catch (TransactionLimitExceededException e) {
            System.out.println("[" + e.getErrorCode() + "] " + e.getMessage());

        } catch (AccountException e) {
            // Catches AccountNotFoundException AND AccountFrozenException
            System.out.println("[" + e.getErrorCode() + "] " + e.getMessage());

        } catch (BankingException e) {
            // Catch-all for any banking exception
            System.out.println("[" + e.getErrorCode() + "] " + e.getMessage());
        }
    }

    static void processTransaction(String txnId, String accId, double amount, double balance) {
        if (amount > balance) {
            throw new InsufficientFundsException(txnId, amount, balance);
        }
        if (amount > 100_000) {
            throw new TransactionLimitExceededException(txnId, amount, 100_000);
        }
    }
}
```

**Output:**
```
[INSUFFICIENT_FUNDS] Need $40000.00 more for transaction TXN-001
Deficit: $40000.0
```

---

## Best Practices

```
  ┌──────────────────────────────────────────────────────────────────┐
  │              CUSTOM EXCEPTION BEST PRACTICES                    │
  ├──────────────────────────────────────────────────────────────────┤
  │                                                                  │
  │  1. Name ends with "Exception"                                   │
  │     ✓ InsufficientFundsException                                 │
  │     ✗ InsufficientFunds                                          │
  │                                                                  │
  │  2. Provide at least these constructors:                         │
  │     • (String message)                                           │
  │     • (String message, Throwable cause)                          │
  │                                                                  │
  │  3. Make fields final — exceptions should be immutable           │
  │                                                                  │
  │  4. Use checked for recoverable conditions                       │
  │     Use unchecked for programming errors                         │
  │                                                                  │
  │  5. Don't extend Throwable or Error directly                     │
  │                                                                  │
  │  6. Include context (accountId, amount, etc.)                    │
  │     Don't just pass a string message                             │
  │                                                                  │
  │  7. Create a hierarchy for related exceptions                    │
  │     Allows catching at different granularity levels              │
  │                                                                  │
  │  8. Override toString() only if you need custom format           │
  │     getMessage() from super is usually enough                    │
  │                                                                  │
  │  9. Always preserve the cause when wrapping exceptions           │
  │     throw new CustomEx("msg", originalException);                │
  │                                                                  │
  │ 10. Document exceptions with @throws Javadoc                     │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```

---

## Quick Reference

```java
// ─── Simplest custom exception ───────────────────
public class MyException extends RuntimeException {
    public MyException(String msg) { super(msg); }
    public MyException(String msg, Throwable cause) { super(msg, cause); }
}

// ─── With error code + extra fields ──────────────
public class MyException extends RuntimeException {
    private final String code;
    private final Object context;

    public MyException(String code, String msg, Object context) {
        super(msg);
        this.code = code;
        this.context = context;
    }

    public String getCode() { return code; }
    public Object getContext() { return context; }
}

// ─── Checked (forces try/catch) ──────────────────
public class MyCheckedException extends Exception {
    public MyCheckedException(String msg) { super(msg); }
}

// ─── Throwing ────────────────────────────────────
throw new MyException("ERR_001", "Something failed", someData);

// ─── Catching ────────────────────────────────────
try { ... }
catch (MyException e) {
    log.error("[{}] {}", e.getCode(), e.getMessage());
}
```
