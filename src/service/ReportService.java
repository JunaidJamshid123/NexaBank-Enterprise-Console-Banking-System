package service;

import model.account.Account;
import model.loan.Loan;
import model.report.ReportSnapshot;
import model.transaction.Transaction;
import model.transaction.TransactionType;
import model.user.Customer;

import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    // ─── Dependencies ────────────────────────────────────────────────
    private final AccountService accountService;
    private final LoanService loanService;
    private final CustomerService customerService;

    // ─── Constructor ──────────────────────────────────────────────────
    public ReportService(AccountService accountService, LoanService loanService,
                         CustomerService customerService) {
        this.accountService = accountService;
        this.loanService = loanService;
        this.customerService = customerService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  groupTransactionsByType
    //  Collectors.groupingBy(Transaction::type)
    // ═══════════════════════════════════════════════════════════════════
    public Map<TransactionType, List<Transaction>> groupTransactionsByType(String accountId) {
        return accountService.findAccountById(accountId)
                .map(Account::getTransactionHistory)
                .orElse(List.of())
                .stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getMonthlySpending
    //  filter by timestamp month+year, WITHDRAWAL + TRANSFER_OUT, sum
    // ═══════════════════════════════════════════════════════════════════
    public double getMonthlySpending(String accountId, int year, int month) {
        return accountService.findAccountById(accountId)
                .map(Account::getTransactionHistory)
                .orElse(List.of())
                .stream()
                .filter(t -> t.timestamp().getYear() == year && t.timestamp().getMonthValue() == month)
                .filter(t -> t.type() == TransactionType.WITHDRAWAL || t.type() == TransactionType.TRANSFER_OUT)
                .mapToDouble(Transaction::amount)
                .sum();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getHighValueTransactions
    //  flatMap all accounts' histories, filter > threshold, sorted desc
    // ═══════════════════════════════════════════════════════════════════
    public List<Transaction> getHighValueTransactions(double threshold) {
        return accountService.getAllAccounts().stream()
                .flatMap(a -> a.getTransactionHistory().stream())
                .filter(t -> t.amount() > threshold)
                .sorted(Comparator.comparingDouble(Transaction::amount).reversed())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getTopNCustomersByDebt
    //  stream customers, sorted by totalDebt desc, limit(n)
    // ═══════════════════════════════════════════════════════════════════
    public List<Customer> getTopNCustomersByDebt(int n) {
        return customerService.getAllCustomers().stream()
                .sorted(Comparator.comparingDouble(
                        (Customer c) -> loanService.calculateTotalDebt(String.valueOf(c.getId())))
                        .reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCurrencyBreakdown
    //  flatMap all accounts, groupingBy(currency, summingDouble(balance))
    // ═══════════════════════════════════════════════════════════════════
    public Map<String, Double> getCurrencyBreakdown() {
        return accountService.getAllAccounts().stream()
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        Collectors.summingDouble(Account::getBalance)
                ));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  generateFullReport — varargs sections
    //  Only compute sections named. Use Streams + Optional.ofNullable
    // ═══════════════════════════════════════════════════════════════════
    public Map<String, Object> generateFullReport(Customer customer, String... sections) {
        String customerId = String.valueOf(customer.getId());
        Set<String> requested = Arrays.stream(sections).collect(Collectors.toSet());

        Map<String, Object> report = new LinkedHashMap<>();

        // Use Streams + Optional.ofNullable to skip missing sections gracefully
        requested.stream().forEach(section -> {
            Optional.ofNullable(switch (section.toUpperCase()) {
                case "ACCOUNTS" -> accountService.getAccountsByOwnerId(customerId);
                case "BALANCE" -> accountService.getTotalBalance(customerId);
                case "LOANS" -> loanService.getLoansByBorrowerId(customerId);
                case "DEBT" -> loanService.calculateTotalDebt(customerId);
                case "CREDIT_SCORE" -> customer.getCreditScore();
                case "TRANSACTIONS" -> accountService.getAccountsByOwnerId(customerId).stream()
                        .flatMap(a -> a.getTransactionHistory().stream())
                        .sorted(Comparator.comparing(Transaction::timestamp).reversed())
                        .collect(Collectors.toList());
                case "CURRENCY_BREAKDOWN" -> accountService.getAccountsByOwnerId(customerId).stream()
                        .collect(Collectors.groupingBy(
                                Account::getCurrency,
                                Collectors.summingDouble(Account::getBalance)));
                case "PROFILE" -> Map.of(
                        "name", customer.getFullName(),
                        "email", customer.getEmail(),
                        "phone", Optional.ofNullable(customer.getPhoneNumber()).orElse("N/A"),
                        "address", Optional.ofNullable(customer.getAddress()).orElse("N/A"));
                default -> null;
            }).ifPresent(value -> report.put(section.toUpperCase(), value));
        });

        return report;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getTransactionDescriptions
    //  stream history, map(description), joining(", ")
    // ═══════════════════════════════════════════════════════════════════
    public String getTransactionDescriptions(String accountId) {
        return accountService.findAccountById(accountId)
                .map(Account::getTransactionHistory)
                .orElse(List.of())
                .stream()
                .map(Transaction::description)
                .collect(Collectors.joining(", "));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  hasAnyOverdrawnAccount
    //  stream accounts, anyMatch(balance < 0)
    // ═══════════════════════════════════════════════════════════════════
    public boolean hasAnyOverdrawnAccount(String customerId) {
        return accountService.getAccountsByOwnerId(customerId).stream()
                .anyMatch(a -> a.getBalance() < 0);
    }
}
