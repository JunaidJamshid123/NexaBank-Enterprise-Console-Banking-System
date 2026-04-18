package exception;

public class InsufficientFundsException extends AccountException {
    private final double requested;
    private final double available;

    public InsufficientFundsException(String accountId, double requested, double available) {
        super("INSUFFICIENT_FUNDS",
                "Insufficient funds in account '%s'. Requested: %.2f, Available: %.2f, Shortfall: %.2f"
                        .formatted(accountId, requested, available, requested - available));
        this.requested = requested;
        this.available = available;
    }

    public double getRequested() { return requested; }
    public double getAvailable() { return available; }
    public double getShortfall() { return requested - available; }
}
