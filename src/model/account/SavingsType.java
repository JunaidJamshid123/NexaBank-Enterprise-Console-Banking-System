package model.account;

public record SavingsType(double annualInterestRate) implements AccountType {
    @Override
    public String typeCode() { return "SAV"; }
    @Override
    public String displayName() { return "Savings Account"; }
    @Override
    public double minimumBalance() { return 1000.0; }
}
