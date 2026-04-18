package model.loan;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a bank loan with standard amortisation.
 *
 * Loans are mutable — the remaining balance decreases with each repayment.
 * The monthly payment is calculated once at construction using the standard
 * amortisation formula and never changes.
 *
 * Amortisation Formula:
 *   M = P × [r(1+r)^n] / [(1+r)^n − 1]
 *
 *   where: P = principal amount
 *          r = annualInterestRate / 12 / 100  (monthly rate as decimal)
 *          n = termMonths
 */
public class Loan {

    // ─── Fields ───────────────────────────────────────────────────────

    private final String loanId;
    private final String borrowerId;
    private final double principalAmount;
    private final double annualInterestRate;
    private final int termMonths;
    private final double monthlyPayment;          // calculated once, never changes
    private double remainingBalance;              // starts at principalAmount, decreases
    private int repaymentsMade;
    private int repaymentsRemaining;
    private LoanStatus status;
    private final LocalDate startDate;
    private LocalDate nextDueDate;
    private final String disbursedToAccountId;

    // ─── Constructor ──────────────────────────────────────────────────

    public Loan(String borrowerId,
                double principalAmount,
                double annualInterestRate,
                int termMonths,
                String disbursedToAccountId) {

        // Validate parameters — throw InvalidLoanParameterException if invalid
        if (principalAmount <= 0) {
            throw new InvalidLoanParameterException(
                    "principalAmount", principalAmount,
                    "Principal amount must be greater than 0. Provided: %.2f".formatted(principalAmount)
            );
        }
        if (annualInterestRate <= 0) {
            throw new InvalidLoanParameterException(
                    "annualInterestRate", annualInterestRate,
                    "Annual interest rate must be greater than 0. Provided: %.4f".formatted(annualInterestRate)
            );
        }
        if (termMonths <= 0) {
            throw new InvalidLoanParameterException(
                    "termMonths", termMonths,
                    "Term months must be greater than 0. Provided: %d".formatted(termMonths)
            );
        }

        this.loanId = generateLoanId();
        this.borrowerId = borrowerId;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.disbursedToAccountId = disbursedToAccountId;

        // Calculate monthly payment using standard amortisation formula:
        //   M = P × [r(1+r)^n] / [(1+r)^n − 1]
        this.monthlyPayment = calculateMonthlyPayment(principalAmount, annualInterestRate, termMonths);

        this.remainingBalance = principalAmount;
        this.repaymentsMade = 0;
        this.repaymentsRemaining = termMonths;
        this.status = LoanStatus.PENDING_APPROVAL;
        this.startDate = LocalDate.now();
        this.nextDueDate = startDate.plusMonths(1);
    }

    // ─── Amortisation Calculation ─────────────────────────────────────

    /**
     * Standard amortisation formula:
     *   M = P × [r(1+r)^n] / [(1+r)^n − 1]
     *
     * @param P    principal amount
     * @param rate annual interest rate (e.g. 12.5 for 12.5%)
     * @param n    term in months
     * @return monthly payment rounded to 2 decimal places
     */
    private static double calculateMonthlyPayment(double P, double rate, int n) {
        double r = rate / 12.0 / 100.0;           // monthly rate as decimal
        double rPow = Math.pow(1 + r, n);          // (1+r)^n
        double M = P * (r * rPow) / (rPow - 1);   // amortisation formula
        return Math.round(M * 100.0) / 100.0;      // round to 2 decimal places
    }

    // ─── Loan Lifecycle ───────────────────────────────────────────────

    /**
     * Approves the loan and sets it to ACTIVE.
     * Only PENDING_APPROVAL loans can be approved.
     */
    public void approve() {
        if (status != LoanStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Loan %s cannot be approved — current status: %s".formatted(loanId, status)
            );
        }
        this.status = LoanStatus.ACTIVE;
    }

    /**
     * Rejects the loan.
     * Only PENDING_APPROVAL loans can be rejected.
     */
    public void reject() {
        if (status != LoanStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Loan %s cannot be rejected — current status: %s".formatted(loanId, status)
            );
        }
        this.status = LoanStatus.REJECTED;
    }

    /**
     * Records a single repayment of the fixed monthly amount.
     * Decreases the remaining balance, increments repayments made,
     * and advances the next due date by one month.
     *
     * If this is the final repayment, the loan is automatically marked PAID_OFF
     * and any remaining rounding residual is zeroed out.
     */
    public void makeRepayment() {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Loan %s is not active — current status: %s. Cannot accept repayment."
                            .formatted(loanId, status)
            );
        }
        if (repaymentsRemaining <= 0) {
            throw new IllegalStateException(
                    "Loan %s has no remaining repayments.".formatted(loanId)
            );
        }

        repaymentsMade++;
        repaymentsRemaining--;
        remainingBalance -= monthlyPayment;

        // Guard against floating-point drift — final payment zeroes out the balance
        if (repaymentsRemaining == 0) {
            remainingBalance = 0.0;
            status = LoanStatus.PAID_OFF;
        } else {
            // Round remaining balance to 2 decimal places to prevent drift
            remainingBalance = Math.round(remainingBalance * 100.0) / 100.0;
            nextDueDate = nextDueDate.plusMonths(1);
        }
    }

    /**
     * Marks the loan as defaulted (borrower failed to meet obligations).
     */
    public void markDefaulted() {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Loan %s cannot be defaulted — current status: %s".formatted(loanId, status)
            );
        }
        this.status = LoanStatus.DEFAULTED;
    }

    /**
     * Records a partial repayment of any positive amount up to the remaining balance.
     * Decreases remainingBalance, increments repaymentsMade, recalculates repaymentsRemaining.
     * If remainingBalance drops to 0, marks the loan as PAID_OFF.
     *
     * @param amount the repayment amount (must be > 0 and <= remainingBalance)
     */
    public void makePartialRepayment(double amount) {
        if (status != LoanStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Loan %s is not active — current status: %s. Cannot accept repayment."
                            .formatted(loanId, status)
            );
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Repayment amount must be positive.");
        }
        if (amount > remainingBalance) {
            amount = remainingBalance; // cap at remaining
        }

        remainingBalance -= amount;
        remainingBalance = Math.round(remainingBalance * 100.0) / 100.0;
        repaymentsMade++;

        if (remainingBalance <= 0) {
            remainingBalance = 0.0;
            repaymentsRemaining = 0;
            status = LoanStatus.PAID_OFF;
        } else {
            // Recalculate remaining repayments based on current balance and monthly payment
            repaymentsRemaining = (int) Math.ceil(remainingBalance / monthlyPayment);
            nextDueDate = nextDueDate.plusMonths(1);
        }
    }

    // ─── Computed Properties ──────────────────────────────────────────

    /**
     * Total amount the borrower will pay over the life of the loan.
     */
    public double totalPayableAmount() {
        return Math.round(monthlyPayment * termMonths * 100.0) / 100.0;
    }

    /**
     * Total interest paid over the life of the loan.
     */
    public double totalInterest() {
        return Math.round((totalPayableAmount() - principalAmount) * 100.0) / 100.0;
    }

    /**
     * Progress as a percentage (0.0 to 100.0).
     */
    public double repaymentProgress() {
        if (termMonths == 0) return 100.0;
        return Math.round((double) repaymentsMade / termMonths * 10000.0) / 100.0;
    }

    // ─── Summary ──────────────────────────────────────────────────────

    public String getLoanSummary() {
        return """
                ╔══════════════════════════════════════════════════╗
                ║              LOAN SUMMARY                       ║
                ╠══════════════════════════════════════════════════╣
                ║  Loan ID          : %s
                ║  Borrower ID      : %s
                ║  Status           : %s
                ║  Principal        : $%,.2f
                ║  Interest Rate    : %.2f%% per annum
                ║  Term             : %d months
                ║  Monthly Payment  : $%,.2f
                ║  Remaining Balance: $%,.2f
                ║  Repayments Made  : %d / %d
                ║  Progress         : %.1f%%
                ║  Total Payable    : $%,.2f
                ║  Total Interest   : $%,.2f
                ║  Start Date       : %s
                ║  Next Due Date    : %s
                ║  Disbursed To     : %s
                ╚══════════════════════════════════════════════════╝
                """.formatted(
                loanId, borrowerId, status,
                principalAmount, annualInterestRate, termMonths,
                monthlyPayment, remainingBalance,
                repaymentsMade, termMonths, repaymentProgress(),
                totalPayableAmount(), totalInterest(),
                startDate, nextDueDate, disbursedToAccountId
        );
    }

    // ─── ID Generation ────────────────────────────────────────────────

    private static String generateLoanId() {
        return "LOAN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    // ─── Getters ──────────────────────────────────────────────────────

    public String getLoanId()              { return loanId; }
    public String getBorrowerId()          { return borrowerId; }
    public double getPrincipalAmount()     { return principalAmount; }
    public double getAnnualInterestRate()  { return annualInterestRate; }
    public int getTermMonths()             { return termMonths; }
    public double getMonthlyPayment()      { return monthlyPayment; }
    public double getRemainingBalance()    { return remainingBalance; }
    public int getRepaymentsMade()         { return repaymentsMade; }
    public int getRepaymentsRemaining()    { return repaymentsRemaining; }
    public LoanStatus getStatus()          { return status; }
    public LocalDate getStartDate()        { return startDate; }
    public LocalDate getNextDueDate()      { return nextDueDate; }
    public String getDisbursedToAccountId() { return disbursedToAccountId; }

    @Override
    public String toString() {
        return "Loan[id=%s, borrower=%s, principal=$%,.2f, rate=%.2f%%, term=%dmo, monthly=$%,.2f, remaining=$%,.2f, status=%s]"
                .formatted(loanId, borrowerId, principalAmount, annualInterestRate,
                        termMonths, monthlyPayment, remainingBalance, status);
    }
}
