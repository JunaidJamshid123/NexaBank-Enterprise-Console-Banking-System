package model.account;

public sealed interface AccountType
        permits SavingsType, CheckingType, FixedDepositType, IslamicSavingsType {
    String typeCode();
    String displayName();
    double minimumBalance();
}
