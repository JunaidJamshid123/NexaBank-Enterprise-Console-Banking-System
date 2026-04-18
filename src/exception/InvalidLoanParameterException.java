package exception;

public class InvalidLoanParameterException extends LoanException {
    private final String parameterName;
    private final double providedValue;

    public InvalidLoanParameterException(String parameterName, double providedValue, String message) {
        super("INVALID_LOAN_PARAM", message);
        this.parameterName = parameterName;
        this.providedValue = providedValue;
    }

    public String getParameterName() { return parameterName; }
    public double getProvidedValue() { return providedValue; }
}
