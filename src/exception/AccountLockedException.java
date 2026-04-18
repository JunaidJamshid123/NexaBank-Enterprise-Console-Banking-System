package exception;

public class AccountLockedException extends CustomerException {
    private final String email;

    public AccountLockedException(String email) {
        super("ACCOUNT_LOCKED", "Account for '%s' is locked after too many failed login attempts.".formatted(email));
        this.email = email;
    }

    public String getEmail() { return email; }
}
