package exception;

public class LoanEligibilityException extends LoanException {
    private final int creditScore;
    private final int minimumRequired;

    public LoanEligibilityException(int creditScore, int minimumRequired, String message) {
        super("LOAN_INELIGIBLE", message);
        this.creditScore = creditScore;
        this.minimumRequired = minimumRequired;
    }

    public LoanEligibilityException(int creditScore, String message) {
        this(creditScore, 500, message);
    }

    public int getCreditScore() { return creditScore; }
    public int getMinimumRequired() { return minimumRequired; }
}
