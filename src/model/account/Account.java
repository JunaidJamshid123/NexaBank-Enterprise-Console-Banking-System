package model.account;

import model.transaction.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Account {

    // ─── Fields ───────────────────────────────────────────────────────
    private final String accountId;
    private final String ownerId;                   // Customer id
    private final AccountType type;                  // Composition — sealed interface
    private double balance;                          // private, never directly settable
    private final String currency;                   // ISO 4217: USD, EUR, GBP, PKR, JPY, AED
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;

    // Transaction history — private, unmodifiable view returned externally
    private final List<Transaction> transactionHistory;

    // Daily withdrawal limit enforcement
    private double dailyWithdrawnAmount;
    private LocalDate lastWithdrawalDate;

    // ─── Constants ────────────────────────────────────────────────────
    private static final double DAILY_WITHDRAWAL_LIMIT = 50_000.0;

    // ─── Constructor ──────────────────────────────────────────────────
    public Account(String ownerId, AccountType type, String currency, double initialDeposit) {
        if (initialDeposit < type.minimumBalance()) {
            throw new IllegalArgumentException(
                    "Initial deposit %.2f is below the minimum balance %.2f for %s"
                            .formatted(initialDeposit, type.minimumBalance(), type.displayName()));
        }
        this.accountId = generateAccountId(type);
        this.ownerId = ownerId;
        this.type = type;
        this.balance = initialDeposit;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.transactionHistory = new ArrayList<>();
        this.dailyWithdrawnAmount = 0.0;
        this.lastWithdrawalDate = LocalDate.now();
    }

    // ─── Deposit ──────────────────────────────────────────────────────
    public void deposit(double amount, String description) {
        validateActiveAccount();
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        this.balance += amount;
        this.lastActivityAt = LocalDateTime.now();

        transactionHistory.add(Transaction.create(
                accountId, model.transaction.TransactionType.DEPOSIT,
                amount, balance, currency, description, null));
    }

    // ─── Withdraw ─────────────────────────────────────────────────────
    public void withdraw(double amount, String description) {
        validateActiveAccount();
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        // ── Type check on sealed AccountType ──
        double effectiveLimit;
        if (this.type instanceof CheckingType ct) {
            effectiveLimit = ct.overdraftLimit();
        } else if (this.type instanceof FixedDepositType fd) {
            throw new AccountFrozenException(
                    accountId, "Cannot withdraw from Fixed Deposit account '%s' before term of %d months ends."
                    .formatted(accountId, fd.termMonths()));
        } else {
            effectiveLimit = 0.0;
        }

        if (amount > balance + effectiveLimit) {
            throw new IllegalArgumentException(
                    "Insufficient funds. Available: %.2f (balance %.2f + overdraft %.2f)"
                            .formatted(balance + effectiveLimit, balance, effectiveLimit));
        }

        // Daily limit enforcement
        resetDailyLimitIfNewDay();
        if (dailyWithdrawnAmount + amount > DAILY_WITHDRAWAL_LIMIT) {
            throw new IllegalArgumentException(
                    "Daily withdrawal limit exceeded. Already withdrawn: %.2f, limit: %.2f"
                            .formatted(dailyWithdrawnAmount, DAILY_WITHDRAWAL_LIMIT));
        }

        this.balance -= amount;
        this.dailyWithdrawnAmount += amount;
        this.lastWithdrawalDate = LocalDate.now();
        this.lastActivityAt = LocalDateTime.now();

        transactionHistory.add(Transaction.create(
                accountId, model.transaction.TransactionType.WITHDRAWAL,
                amount, balance, currency, description, null));
    }

    // ─── Transfer helpers ─────────────────────────────────────────────
    public void transferOut(double amount, String targetAccountId) {
        validateActiveAccount();
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        double effectiveLimit;
        if (this.type instanceof CheckingType ct) {
            effectiveLimit = ct.overdraftLimit();
        } else if (this.type instanceof FixedDepositType fd) {
            throw new AccountFrozenException(
                    accountId, "Cannot transfer from Fixed Deposit account '%s'.".formatted(accountId));
        } else {
            effectiveLimit = 0.0;
        }

        if (amount > balance + effectiveLimit) {
            throw new IllegalArgumentException(
                    "Insufficient funds for transfer. Available: %.2f".formatted(balance + effectiveLimit));
        }

        this.balance -= amount;
        this.lastActivityAt = LocalDateTime.now();

        transactionHistory.add(Transaction.createTransfer(
                accountId, model.transaction.TransactionType.TRANSFER_OUT,
                amount, balance, currency, "Transfer to " + targetAccountId,
                targetAccountId, null));
    }

    public void transferIn(double amount, String sourceAccountId) {
        validateActiveAccount();
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        this.balance += amount;
        this.lastActivityAt = LocalDateTime.now();

        transactionHistory.add(Transaction.createTransfer(
                accountId, model.transaction.TransactionType.TRANSFER_IN,
                amount, balance, currency, "Transfer from " + sourceAccountId,
                sourceAccountId, null));
    }

    // ─── Interest / Profit Calculation (Pattern Matching) ─────────────
    public double calculateInterest() {
        if (this.type instanceof SavingsType st) {
            return balance * st.annualInterestRate() / 100.0;
        } else if (this.type instanceof FixedDepositType fd) {
            return balance * fd.rate() / 100.0 * fd.termMonths() / 12.0;
        } else if (this.type instanceof IslamicSavingsType is) {
            return balance * is.profitSharingRatio() / 100.0;
        } else {
            return 0.0;
        }
    }

    // ─── Account Status Management ───────────────────────────────────
    public void freeze() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot freeze a closed account.");
        }
        this.status = AccountStatus.FROZEN;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void unfreeze() {
        if (status != AccountStatus.FROZEN) {
            throw new IllegalStateException("Account is not frozen.");
        }
        this.status = AccountStatus.ACTIVE;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void close() {
        if (balance != 0) {
            throw new IllegalStateException(
                    "Cannot close account with non-zero balance: %.2f".formatted(balance));
        }
        this.status = AccountStatus.CLOSED;
        this.lastActivityAt = LocalDateTime.now();
    }

    public void markPendingReview() {
        this.status = AccountStatus.PENDING_REVIEW;
        this.lastActivityAt = LocalDateTime.now();
    }

    // ─── Account Description (Pattern Matching) ──────────────────────
    public String getAccountSummary() {
        String typeDetails;
        if (this.type instanceof SavingsType st) {
            typeDetails = "Interest Rate: %.2f%%".formatted(st.annualInterestRate());
        } else if (this.type instanceof CheckingType ct) {
            typeDetails = "Overdraft Limit: %.2f".formatted(ct.overdraftLimit());
        } else if (this.type instanceof FixedDepositType fd) {
            typeDetails = "Rate: %.2f%%, Term: %d months".formatted(fd.rate(), fd.termMonths());
        } else if (this.type instanceof IslamicSavingsType is) {
            typeDetails = "Profit Sharing: %.2f%%".formatted(is.profitSharingRatio());
        } else {
            typeDetails = "Unknown type";
        }

        return """
                ══════════════════════════════════════
                Account ID    : %s
                Owner ID      : %s
                Type          : %s (%s)
                Balance       : %s %.2f
                Status        : %s
                Details       : %s
                Created       : %s
                Last Activity : %s
                Transactions  : %d
                ══════════════════════════════════════"""
                .formatted(accountId, ownerId, type.displayName(), type.typeCode(),
                        currency, balance, status, typeDetails,
                        createdAt, lastActivityAt, transactionHistory.size());
    }

    // ─── Private Helpers ──────────────────────────────────────────────
    private void validateActiveAccount() {
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountId);
        }
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account " + accountId + " is closed.");
        }
        if (status == AccountStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Account " + accountId + " is under review.");
        }
    }

    private void resetDailyLimitIfNewDay() {
        if (!LocalDate.now().equals(lastWithdrawalDate)) {
            dailyWithdrawnAmount = 0.0;
            lastWithdrawalDate = LocalDate.now();
        }
    }

    private static String generateAccountId(AccountType type) {
        return "NXB-" + type.typeCode() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ─── Getters ──────────────────────────────────────────────────────
    public String getAccountId()        { return accountId; }
    public String getOwnerId()          { return ownerId; }
    public AccountType getType()        { return type; }
    public double getBalance()          { return balance; }
    public String getCurrency()         { return currency; }
    public AccountStatus getStatus()    { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public double getDailyWithdrawnAmount()  { return dailyWithdrawnAmount; }
    public LocalDate getLastWithdrawalDate() { return lastWithdrawalDate; }

    // Unmodifiable view of transaction history
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    @Override
    public String toString() {
        return "Account{id='%s', owner='%s', type=%s, balance=%.2f %s, status=%s}"
                .formatted(accountId, ownerId, type.typeCode(), balance, currency, status);
    }
}
