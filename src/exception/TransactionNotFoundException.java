package exception;

public class TransactionNotFoundException extends AccountException {
    private final String transactionId;

    public TransactionNotFoundException(String transactionId) {
        super("TXN_NOT_FOUND", "Transaction not found: %s".formatted(transactionId));
        this.transactionId = transactionId;
    }

    public String getTransactionId() { return transactionId; }
}
