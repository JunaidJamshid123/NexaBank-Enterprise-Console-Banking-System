package model.account;

public class AccountFrozenException extends RuntimeException {

    private final String accountId;

    public AccountFrozenException(String accountId, String message) {
        super(message);
        this.accountId = accountId;
    }

    public AccountFrozenException(String accountId) {
        this(accountId, "Account " + accountId + " is frozen and cannot perform this operation.");
    }

    public String getAccountId() {
        return accountId;
    }
}
