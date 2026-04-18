package model.report;

import model.account.Account;
import model.loan.Loan;

import java.util.List;

public record ReportSnapshot(
        String customerId,
        String customerName,
        String email,
        int creditScore,
        List<Account> accounts,
        double totalBalance,
        List<Loan> loans,
        double totalDebt
) {}
