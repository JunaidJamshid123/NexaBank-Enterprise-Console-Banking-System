package exception;

public class LoanAlreadyPaidException extends LoanException {
    private final String loanId;

    public LoanAlreadyPaidException(String loanId) {
        super("LOAN_ALREADY_PAID", "Loan '%s' has already been fully paid off.".formatted(loanId));
        this.loanId = loanId;
    }

    public String getLoanId() { return loanId; }
}
