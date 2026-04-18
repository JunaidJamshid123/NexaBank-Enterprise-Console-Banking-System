package exception;

/**
 * Thrown when a value fails a Predicate-based validation rule.
 *
 * Extends NexaBankException (checked). Error code prefix: VAL.
 */
public class ValidationException extends NexaBankException {

    private final Object invalidValue;

    public ValidationException(String message, Object invalidValue) {
        super("VAL-001", message);
        this.invalidValue = invalidValue;
    }

    public Object getInvalidValue() { return invalidValue; }
}
