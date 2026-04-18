package service;

import model.account.*;
import model.transaction.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.function.*;

public class InterestEngine {

    private final AuditService auditService;

    // ─── Functional Interfaces ────────────────────────────────────────

    // Function to extract balance from an account
    private final Function<Account, Double> getBalance = Account::getBalance;

    // Function factory: given a monthly rate, returns a Function that computes interest on a balance
    private Function<Double, Double> interestCalculator(double monthlyRate) {
        return balance -> Math.round(balance * monthlyRate * 100.0) / 100.0;
    }

    // Predicate: is a FixedDeposit account mature?
    // Mature = created date + termMonths <= today
    private final Predicate<Account> isMature = account -> {
        if (account.getType() instanceof FixedDepositType fd) {
            LocalDate maturityDate = account.getCreatedAt().toLocalDate().plusMonths(fd.termMonths());
            return !LocalDate.now().isBefore(maturityDate);
        }
        return false;
    };

    // ─── Constructor ──────────────────────────────────────────────────
    public InterestEngine(AuditService auditService) {
        this.auditService = auditService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  applyMonthlyInterest — uses Consumer<Account> internally
    // ═══════════════════════════════════════════════════════════════════
    public void applyMonthlyInterest(List<Account> accounts) {

        // Consumer for SavingsType accounts: credit monthly interest
        Consumer<Account> applySavingsInterest = account -> {
            if (account.getType() instanceof SavingsType st && account.getStatus() == AccountStatus.ACTIVE) {
                // interest = balance * (annualRate / 12 / 100)
                double monthlyRate = st.annualInterestRate() / 12.0 / 100.0;

                // Compose: extract balance → apply rate
                Function<Account, Double> computeInterest = getBalance.andThen(interestCalculator(monthlyRate));
                double interest = computeInterest.apply(account);

                if (interest > 0) {
                    account.deposit(interest, "Monthly interest credit (%.2f%% p.a.)".formatted(st.annualInterestRate()));
                    auditService.log("INTEREST_CREDIT: %.2f to %s (Savings @ %.2f%% p.a.)"
                            .formatted(interest, account.getAccountId(), st.annualInterestRate()));
                }
            }
        };

        // Consumer for FixedDepositType accounts: only credit on maturity
        Consumer<Account> applyFixedDepositInterest = account -> {
            if (account.getType() instanceof FixedDepositType fd && account.getStatus() == AccountStatus.ACTIVE) {
                // Only credit if the FD has matured
                if (isMature.test(account)) {
                    // FD interest = balance * rate / 100 * termMonths / 12
                    double totalRate = fd.rate() / 100.0 * fd.termMonths() / 12.0;

                    Function<Account, Double> computeInterest = getBalance.andThen(balance -> Math.round(balance * totalRate * 100.0) / 100.0);
                    double interest = computeInterest.apply(account);

                    if (interest > 0) {
                        account.deposit(interest, "Fixed Deposit maturity interest (%.2f%% for %d months)"
                                .formatted(fd.rate(), fd.termMonths()));
                        auditService.log("INTEREST_CREDIT: %.2f to %s (FD matured @ %.2f%% for %d months)"
                                .formatted(interest, account.getAccountId(), fd.rate(), fd.termMonths()));
                    }
                }
            }
        };

        // Consumer for IslamicSavingsType: monthly profit share
        Consumer<Account> applyIslamicInterest = account -> {
            if (account.getType() instanceof IslamicSavingsType ist && account.getStatus() == AccountStatus.ACTIVE) {
                double monthlyRate = ist.profitSharingRatio() / 12.0 / 100.0;

                Function<Account, Double> computeProfit = getBalance.andThen(interestCalculator(monthlyRate));
                double profit = computeProfit.apply(account);

                if (profit > 0) {
                    account.deposit(profit, "Monthly profit share (%.2f%% p.a.)".formatted(ist.profitSharingRatio()));
                    auditService.log("INTEREST_CREDIT: %.2f to %s (Islamic Savings @ %.2f%% profit share)"
                            .formatted(profit, account.getAccountId(), ist.profitSharingRatio()));
                }
            }
        };

        // Combine all consumers and apply to each account using forEach
        Consumer<Account> applyAll = applySavingsInterest
                .andThen(applyFixedDepositInterest)
                .andThen(applyIslamicInterest);

        accounts.forEach(applyAll);

        auditService.log("INTEREST_ENGINE: Monthly interest applied to %d accounts.".formatted(accounts.size()));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getInterestProjection — returns a Supplier<Double> (lazy computation)
    //  The caller invokes .get() — not us.
    // ═══════════════════════════════════════════════════════════════════
    public Supplier<Double> getInterestProjection(Account account, int months) {
        // Lazy: computation only happens when the caller calls .get()
        return () -> {
            double balance = getBalance.apply(account);
            double monthlyRate = getMonthlyRate(account);

            // Compound interest projection: balance * (1 + monthlyRate) ^ months
            double projected = balance * Math.pow(1 + monthlyRate, months);
            return Math.round(projected * 100.0) / 100.0;
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private double getMonthlyRate(Account account) {
        if (account.getType() instanceof SavingsType st) {
            return st.annualInterestRate() / 12.0 / 100.0;
        } else if (account.getType() instanceof FixedDepositType fd) {
            return fd.rate() / 12.0 / 100.0;
        } else if (account.getType() instanceof IslamicSavingsType ist) {
            return ist.profitSharingRatio() / 12.0 / 100.0;
        }
        return 0.0;
    }
}
