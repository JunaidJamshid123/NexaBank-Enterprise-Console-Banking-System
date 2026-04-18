package exception;

public class AccountTypeViolationException extends AccountException {
    private final String accountId;
    private final String accountType;

    public AccountTypeViolationException(String accountId, String accountType, String message) {
        super("ACC_TYPE_VIOLATION", message);
        this.accountId = accountId;
        this.accountType = accountType;
    }

    public String getAccountId() { return accountId; }
    public String getAccountType() { return accountType; }
}
