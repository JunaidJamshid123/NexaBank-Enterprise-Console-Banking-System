package exception;

public class CustomerNotFoundException extends CustomerException {
    public CustomerNotFoundException(String identifier) {
        super("CUST_NOT_FOUND", "Customer not found: %s".formatted(identifier));
    }
}
