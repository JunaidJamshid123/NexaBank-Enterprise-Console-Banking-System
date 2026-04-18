package exception;

public class AuthenticationException extends CustomerException {
    public AuthenticationException(String message) {
        super("AUTH_FAILED", message);
    }
}
