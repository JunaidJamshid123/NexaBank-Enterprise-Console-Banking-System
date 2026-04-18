package service;

import exception.LoanEligibilityException;
import exception.LoanNotFoundException;
import exception.NexaBankException;
import model.account.Account;
import model.account.AccountType;
import model.account.SavingsType;
import model.loan.Loan;
import model.loan.LoanStatus;
import model.notification.NotificationType;
import model.user.Customer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoanService {

    // ─── Storage & Dependencies ───────────────────────────────────────
    private final List<Loan> loans = new ArrayList<>();
    private final AccountService accountService;
    private final CustomerService customerService;
    private final AuditService auditService;

    // ─── Constructor ──────────────────────────────────────────────────
    public LoanService(AccountService accountService, CustomerService customerService,
                       AuditService auditService) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.auditService = auditService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  applyForLoan
    // ═══════════════════════════════════════════════════════════════════
    public Loan applyForLoan(Customer customer, double principal, double rate, int months)
            throws NexaBankException {

        int creditScore = customer.getCreditScore();
        String customerId = String.valueOf(customer.getId());

        // Credit score < 500: reject outright
        if (creditScore < 500) {
            throw new LoanEligibilityException(creditScore,
                    "Credit score %d is below the minimum 500 required for a loan.".formatted(creditScore));
        }

        // Credit score 500-650: apply a 2% surcharge on the interest rate
        // This compensates for higher default risk in the mid-tier credit range.
        double effectiveRate = rate;
        if (creditScore >= 500 && creditScore <= 650) {
            effectiveRate = rate + 2.0;  // +2% surcharge for credit scores 500-650
        }

        // Max principal = creditScore * 500
        double maxPrincipal = creditScore * 500.0;
        if (principal > maxPrincipal) {
            throw new LoanEligibilityException(creditScore,
                    "Requested principal %.2f exceeds maximum allowed %.2f (creditScore %d × 500)."
                            .formatted(principal, maxPrincipal, creditScore));
        }

        // Find customer's primary SAVINGS account for disbursement
        List<Account> customerAccounts = accountService.getAccountsByOwnerId(customerId);
        Account savingsAccount = customerAccounts.stream()
                .filter(a -> a.getType() instanceof SavingsType)
                .findFirst()
                .orElseThrow(() -> new NexaBankException("NO_SAVINGS_ACC",
                        "Customer %s has no SAVINGS account for loan disbursement.".formatted(customerId)));

        // Create the loan
        Loan loan = new Loan(customerId, principal, effectiveRate, months, savingsAccount.getAccountId());

        // Approve and activate the loan
        loan.approve();
        loans.add(loan);

        // Disburse amount to the customer's savings account via AccountService.deposit()
        accountService.deposit(savingsAccount.getAccountId(), principal,
                "Loan disbursement — Loan ID: " + loan.getLoanId(), "SYSTEM");

        // Push LOAN_DISBURSEMENT notification
        accountService.getNotificationQueue(customerId)
                .send("Loan Approved & Disbursed",
                        "Your loan %s for $%,.2f has been approved at %.2f%% rate for %d months. "
                                .formatted(loan.getLoanId(), principal, effectiveRate, months)
                                + "Funds disbursed to %s.".formatted(savingsAccount.getAccountId()),
                        NotificationType.TRANSACTION);

        auditService.log("LOAN_APPROVED: %s for customer %s. Principal: $%,.2f, Rate: %.2f%%, Term: %d months."
                .formatted(loan.getLoanId(), customerId, principal, effectiveRate, months));

        return loan;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  makeRepayment — partial repayment allowed
    // ═══════════════════════════════════════════════════════════════════
    public Loan makeRepayment(String loanId, double amount) throws NexaBankException {

        Loan loan = findLoanOrThrow(loanId);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new NexaBankException("LOAN_INACTIVE", "Loan %s is not active. Status: %s"
                    .formatted(loanId, loan.getStatus()));
        }

        if (amount <= 0) {
            throw new NexaBankException("INVALID_AMOUNT", "Repayment amount must be positive.");
        }

        // Perform partial repayment (Loan handles capping at remainingBalance)
        loan.makePartialRepayment(amount);

        String customerId = loan.getBorrowerId();

        // If fully paid off, boost credit score +15
        if (loan.getStatus() == LoanStatus.PAID_OFF) {
            try {
                customerService.updateCreditScore(customerId, 15);
            } catch (Exception e) {
                // Log but don't fail the repayment
                auditService.log("WARNING: Could not update credit score for %s: %s"
                        .formatted(customerId, e.getMessage()));
            }

            accountService.getNotificationQueue(customerId)
                    .send("Loan Paid Off",
                            "Congratulations! Your loan %s has been fully paid off. Credit score +15."
                                    .formatted(loanId),
                            NotificationType.TRANSACTION);

            auditService.log("LOAN_PAID_OFF: %s by customer %s. Credit score boosted +15."
                    .formatted(loanId, customerId));
        } else {
            accountService.getNotificationQueue(customerId)
                    .send("Repayment Received",
                            "Repayment of $%,.2f received for loan %s. Remaining: $%,.2f."
                                    .formatted(amount, loanId, loan.getRemainingBalance()),
                            NotificationType.TRANSACTION);

            auditService.log("LOAN_REPAYMENT: $%,.2f for %s. Remaining: $%,.2f."
                    .formatted(amount, loanId, loan.getRemainingBalance()));
        }

        return loan;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  checkAndMarkDefaults — scheduled-style method
    // ═══════════════════════════════════════════════════════════════════
    public List<Loan> checkAndMarkDefaults() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        List<Loan> newlyDefaulted = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .filter(l -> l.getNextDueDate().isBefore(thirtyDaysAgo))
                .filter(l -> l.getRepaymentsMade() == 0)
                .collect(Collectors.toList());

        for (Loan loan : newlyDefaulted) {
            loan.markDefaulted();
            String customerId = loan.getBorrowerId();

            // Credit score penalty: -50
            try {
                customerService.updateCreditScore(customerId, -50);
            } catch (Exception e) {
                auditService.log("WARNING: Could not update credit score for %s: %s"
                        .formatted(customerId, e.getMessage()));
            }

            accountService.getNotificationQueue(customerId)
                    .send("Loan Defaulted",
                            "Your loan %s has been marked as DEFAULTED due to non-payment. "
                                    .formatted(loan.getLoanId())
                                    + "Credit score reduced by 50. Please contact the bank immediately.",
                            NotificationType.ALERT);

            auditService.log("LOAN_DEFAULTED: %s for customer %s. Credit score -50."
                    .formatted(loan.getLoanId(), customerId));
        }

        return newlyDefaulted;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getActiveLoans — filter by ACTIVE status
    // ═══════════════════════════════════════════════════════════════════
    public List<Loan> getActiveLoans(String customerId) {
        return loans.stream()
                .filter(l -> l.getBorrowerId().equals(customerId))
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  calculateTotalDebt — sum remaining balances
    // ═══════════════════════════════════════════════════════════════════
    public double calculateTotalDebt(String customerId) {
        return loans.stream()
                .filter(l -> l.getBorrowerId().equals(customerId))
                .mapToDouble(Loan::getRemainingBalance)
                .sum();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  QUERY HELPERS
    // ═══════════════════════════════════════════════════════════════════

    public void addLoan(Loan loan) {
        loans.add(loan);
    }

    public List<Loan> getLoansByBorrowerId(String borrowerId) {
        return loans.stream()
                .filter(l -> l.getBorrowerId().equals(borrowerId))
                .collect(Collectors.toList());
    }

    public List<Loan> getAllLoans() {
        return Collections.unmodifiableList(loans);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private Loan findLoanOrThrow(String loanId) throws LoanNotFoundException {
        return loans.stream()
                .filter(l -> l.getLoanId().equals(loanId))
                .findFirst()
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }
}
