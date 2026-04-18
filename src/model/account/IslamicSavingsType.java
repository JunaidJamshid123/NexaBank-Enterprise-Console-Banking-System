package model.account;

public record IslamicSavingsType(double profitSharingRatio) implements AccountType {
    @Override
    public String typeCode() { return "ISA"; }
    @Override
    public String displayName() { return "Islamic Savings Account"; }
    @Override
    public double minimumBalance() { return 1000.0; }
}
