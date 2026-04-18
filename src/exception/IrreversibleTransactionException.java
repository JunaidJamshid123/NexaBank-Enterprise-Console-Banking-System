package exception;

public class IrreversibleTransactionException extends AccountException {
    private final String transactionId;

    public IrreversibleTransactionException(String transactionId, String reason) {
        super("IRREVERSIBLE_TXN", "Transaction '%s' cannot be reversed: %s".formatted(transactionId, reason));
        this.transactionId = transactionId;
    }

    public String getTransactionId() { return transactionId; }
}
