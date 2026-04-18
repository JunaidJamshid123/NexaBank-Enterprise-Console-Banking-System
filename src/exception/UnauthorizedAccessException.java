package exception;

public class UnauthorizedAccessException extends CustomerException {
    public UnauthorizedAccessException(String userId, String action) {
        super("UNAUTHORIZED", "User '%s' is not authorized to perform: %s".formatted(userId, action));
    }
}
