package exception;

import java.time.LocalDateTime;

public class NexaBankException extends Exception {

    private final String errorCode;
    private final LocalDateTime timestamp;

    public NexaBankException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public NexaBankException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
