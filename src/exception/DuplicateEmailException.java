package exception;

public class DuplicateEmailException extends CustomerException {
    private final String email;

    public DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "An account with email '%s' already exists.".formatted(email));
        this.email = email;
    }

    public String getEmail() { return email; }
}
