package exception;

public class UnsupportedCurrencyException extends CurrencyException {
    private final String currency;

    public UnsupportedCurrencyException(String currency) {
        super("Unsupported currency: '%s'".formatted(currency));
        this.currency = currency;
    }

    public String getCurrency() { return currency; }
}
