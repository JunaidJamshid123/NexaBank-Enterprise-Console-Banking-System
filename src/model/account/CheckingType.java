package model.account;



public record CheckingType(double overdraftLimit) implements AccountType {
    @Override
    public String typeCode() { return "CHK"; }
    @Override
    public String displayName() { return "Checking Account"; }
    @Override
    public double minimumBalance() { return 0.0; }
}