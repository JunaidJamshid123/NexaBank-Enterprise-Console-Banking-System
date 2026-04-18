package exception;

public class LoanException extends NexaBankException {
    public LoanException(String errorCode, String message) {
        super(errorCode, message);
    }

    public LoanException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
