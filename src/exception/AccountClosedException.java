package exception;

public class AccountClosedException extends AccountException {
    private final String accountId;

    public AccountClosedException(String accountId) {
        super("ACC_CLOSED", "Account '%s' is closed and cannot perform this operation.".formatted(accountId));
        this.accountId = accountId;
    }

    public String getAccountId() { return accountId; }
}
