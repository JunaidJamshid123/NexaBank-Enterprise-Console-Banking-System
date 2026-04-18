package model.loan;

/**
 * Thrown when a loan is constructed with invalid parameters:
 * principal <= 0, annual interest rate <= 0, or term months <= 0.
 */
public class InvalidLoanParameterException extends RuntimeException {

    private final String parameterName;
    private final double providedValue;

    public InvalidLoanParameterException(String parameterName, double providedValue, String message) {
        super(message);
        this.parameterName = parameterName;
        this.providedValue = providedValue;
    }

    public InvalidLoanParameterException(String message) {
        super(message);
        this.parameterName = "unknown";
        this.providedValue = 0;
    }

    public String getParameterName() {
        return parameterName;
    }

    public double getProvidedValue() {
        return providedValue;
    }
}
