package exception;

public class AccountFrozenException extends AccountException {
    private final String accountId;

    public AccountFrozenException(String accountId) {
        super("ACC_FROZEN", "Account '%s' is frozen and cannot perform this operation.".formatted(accountId));
        this.accountId = accountId;
    }

    public AccountFrozenException(String accountId, String message) {
        super("ACC_FROZEN", message);
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
