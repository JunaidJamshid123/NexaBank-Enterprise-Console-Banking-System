package exception;

public class LoanNotFoundException extends LoanException {
    public LoanNotFoundException(String loanId) {
        super("LOAN_NOT_FOUND", "Loan not found: %s".formatted(loanId));
    }
}
