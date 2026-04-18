package exception;

public class AccountNotFoundException extends AccountException {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super("ACC_NOT_FOUND", "Account not found: %s".formatted(accountId));
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
