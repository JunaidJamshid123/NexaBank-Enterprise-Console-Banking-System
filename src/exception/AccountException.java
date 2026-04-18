package exception;

public class AccountException extends NexaBankException {
    public AccountException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AccountException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
