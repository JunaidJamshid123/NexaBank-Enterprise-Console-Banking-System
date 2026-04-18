package exception;

public class DailyLimitExceededException extends AccountException {
    private final double limit;
    private final double attempted;

    public DailyLimitExceededException(String accountId, double attempted, double limit) {
        super("DAILY_LIMIT_EXCEEDED",
                "Daily withdrawal limit exceeded for account '%s'. Attempted: %.2f, Limit: %.2f"
                        .formatted(accountId, attempted, limit));
        this.limit = limit;
        this.attempted = attempted;
    }

    public double getLimit() { return limit; }
    public double getAttempted() { return attempted; }
}
