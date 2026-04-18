package model.account;

public record FixedDepositType(double rate, int termMonths) implements AccountType {
    @Override
    public String typeCode() { return "FD"; }
    @Override
    public String displayName() { return "Fixed Deposit Account"; }
    @Override
    public double minimumBalance() { return 50000.0; }
}
