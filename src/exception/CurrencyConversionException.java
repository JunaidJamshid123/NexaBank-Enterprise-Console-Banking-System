package exception;

public class CurrencyConversionException extends CurrencyException {
    private final String fromCurrency;
    private final String toCurrency;

    public CurrencyConversionException(String fromCurrency, String toCurrency, String message) {
        super(message);
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
    }

    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
}
