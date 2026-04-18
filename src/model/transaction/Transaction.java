package model.transaction;

import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        String transactionId,
        String accountId,
        TransactionType type,
        double amount,
        double balanceAfter,
        String currency,
        String description,
        String referenceId,       // linked transaction id for transfers
        TransactionStatus status,
        LocalDateTime timestamp,
        String performedBy        // userId of who triggered this
) {

    // Compact constructor for validation
    public Transaction {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or blank.");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be null or blank.");
        }
        if (status == null) {
            status = TransactionStatus.COMPLETED;
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ─── Factory Methods ──────────────────────────────────────────────

    /**
     * Quick factory — creates a COMPLETED transaction with auto-generated ID and current timestamp.
     */
    public static Transaction create(String accountId, TransactionType type,
                                     double amount, double balanceAfter,
                                     String currency, String description,
                                     String performedBy) {
        return new Transaction(
                generateId(), accountId, type, amount, balanceAfter,
                currency, description, null,
                TransactionStatus.COMPLETED, LocalDateTime.now(), performedBy);
    }

    /**
     * Factory for transfer transactions — includes a referenceId linking the paired transaction.
     */
    public static Transaction createTransfer(String accountId, TransactionType type,
                                             double amount, double balanceAfter,
                                             String currency, String description,
                                             String referenceId, String performedBy) {
        return new Transaction(
                generateId(), accountId, type, amount, balanceAfter,
                currency, description, referenceId,
                TransactionStatus.COMPLETED, LocalDateTime.now(), performedBy);
    }

    /**
     * Factory for a PENDING transaction (e.g. awaiting approval).
     */
    public static Transaction createPending(String accountId, TransactionType type,
                                            double amount, String currency,
                                            String description, String performedBy) {
        return new Transaction(
                generateId(), accountId, type, amount, 0.0,
                currency, description, null,
                TransactionStatus.PENDING, LocalDateTime.now(), performedBy);
    }

    /**
     * Returns a copy of this transaction with a new status.
     */
    public Transaction withStatus(TransactionStatus newStatus) {
        return new Transaction(transactionId, accountId, type, amount, balanceAfter,
                currency, description, referenceId, newStatus, timestamp, performedBy);
    }

    /**
     * Returns a REVERSAL transaction that mirrors this one.
     */
    public Transaction reverse(double currentBalance, String reversedBy) {
        TransactionType reversalType = TransactionType.REVERSAL;
        return new Transaction(
                generateId(), accountId, reversalType, amount, currentBalance,
                currency, "Reversal of " + transactionId, transactionId,
                TransactionStatus.COMPLETED, LocalDateTime.now(), reversedBy);
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    public boolean isDebit() {
        return type == TransactionType.WITHDRAWAL
                || type == TransactionType.TRANSFER_OUT
                || type == TransactionType.LOAN_REPAYMENT
                || type == TransactionType.FEE_DEBIT;
    }

    public boolean isCredit() {
        return type == TransactionType.DEPOSIT
                || type == TransactionType.TRANSFER_IN
                || type == TransactionType.LOAN_DISBURSEMENT
                || type == TransactionType.INTEREST_CREDIT;
    }

    private static String generateId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    @Override
    public String toString() {
        return "Transaction{id='%s', type=%s, amount=%.2f %s, balance=%.2f, account='%s', status=%s, ref='%s', time=%s, by='%s', desc='%s'}"
                .formatted(transactionId, type, amount, currency, balanceAfter, accountId,
                        status, referenceId, timestamp, performedBy, description);
    }
}
