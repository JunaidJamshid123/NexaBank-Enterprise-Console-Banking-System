package exception;

public class CustomerException extends NexaBankException {
    public CustomerException(String errorCode, String message) {
        super(errorCode, message);
    }

    public CustomerException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
