package view;

import exception.*;
import model.account.Account;
import model.account.AccountStatus;
import model.account.AccountType;
import model.account.SavingsType;
import model.account.CheckingType;
import model.account.FixedDepositType;
import model.account.IslamicSavingsType;
import model.loan.Loan;
import model.loan.LoanStatus;
import model.report.ReportSnapshot;
import model.transaction.Transaction;
import model.user.BankAdmin;
import model.user.Customer;
import model.user.User;
import service.*;

import java.util.List;
import java.util.Map;

import static view.ConsoleUI.*;

public class AdminView {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final AuditService auditService;
    private final InterestEngine interestEngine;
    private final Map<String, User> userRegistry;

    private BankAdmin loggedInAdmin = null;

    public AdminView(CustomerService customerService, AccountService accountService,
                     LoanService loanService, AuditService auditService,
                     InterestEngine interestEngine, Map<String, User> userRegistry) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.auditService = auditService;
        this.interestEngine = interestEngine;
        this.userRegistry = userRegistry;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADMIN PANEL (Login)
    // ═══════════════════════════════════════════════════════════════════
    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            clearScreen();
            printBanner();
            System.out.println("""
            ┌────────────────────────────────────┐
            │        ADMIN PANEL                 │
            ├────────────────────────────────────┤
            │  [1]  Login                        │
            │  [0]  Back to Main Menu            │
            └────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");
            switch (choice) {
                case 1 -> login();
                case 0 -> inMenu = false;
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void login() {
        clearScreen();
        printBanner();
        System.out.println("  ── Admin Login ─────────────────────");
        String email = readLine("  Email    : ");
        String password = readLine("  Password : ");

        // Find admin by email in userRegistry
        BankAdmin found = null;
        for (User u : userRegistry.values()) {
            if (u instanceof BankAdmin admin && u.getEmail().equalsIgnoreCase(email)) {
                found = admin;
                break;
            }
        }

        if (found == null) {
            System.out.println("  [✗] No admin account found with that email.");
            pause();
            return;
        }

        // Verify password hash
        if (!found.getPasswordHash().equals(hashPassword(password))) {
            System.out.println("  [✗] Invalid credentials.");
            pause();
            return;
        }

        loggedInAdmin = found;
        System.out.println("  [✓] Welcome, %s (Admin Level %d)".formatted(
                found.getFullName(), found.getAccessLevel()));
        pause();
        dashboard();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADMIN DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    private void dashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
            clearScreen();
            printBanner();
            System.out.println("  Admin: %s | Level: %d".formatted(
                    loggedInAdmin.getFullName(), loggedInAdmin.getAccessLevel()));
            System.out.println("""
            ┌──────────────────────────────────────────┐
            │         ADMIN DASHBOARD                  │
            ├──────────────────────────────────────────┤
            │  [1]  View All Customers                 │
            │  [2]  Register Customer                  │
            │  [3]  Open Account for Customer          │
            │  [4]  Freeze / Unfreeze Account          │
            │  [5]  Approve Loan                       │
            │  [6]  Reverse Transaction                │
            │  [7]  View Audit Log                     │
            │  [8]  Run Interest Engine                │
            │  [9]  Full System Report                 │
            │  [10] Check Loan Defaults                │
            │  [0]  Logout                             │
            └──────────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");
            switch (choice) {
                case 1  -> viewAllCustomers();
                case 2  -> registerCustomer();
                case 3  -> openAccountForCustomer();
                case 4  -> freezeUnfreezeAccount();
                case 5  -> approveLoan();
                case 6  -> reverseTransaction();
                case 7  -> viewAuditLog();
                case 8  -> runInterestEngine();
                case 9  -> fullSystemReport();
                case 10 -> checkLoanDefaults();
                case 0  -> {
                    clearScreen();
                    System.out.println("  [i] Admin logged out.");
                    loggedInAdmin = null;
                    inDashboard = false;
                    pause();
                }
                default -> { System.out.println("  [!] Invalid choice."); pause(); }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [1] VIEW ALL CUSTOMERS
    // ═══════════════════════════════════════════════════════════════════
    private void viewAllCustomers() {
        clearScreen();
        System.out.println("  ── All Registered Customers ───────────────────────");

        List<Customer> all = customerService.getAllCustomers();
        if (all.isEmpty()) {
            System.out.println("  [i] No customers registered.");
            pause();
            return;
        }

        System.out.printf("  %-6s %-20s %-28s %-8s %-8s%n", "ID", "Name", "Email", "Credit", "Active");
        System.out.println("  " + "─".repeat(75));

        for (Customer c : all) {
            System.out.printf("  %-6d %-20s %-28s %-8d %-8s%n",
                    c.getId(), c.getFullName(), c.getEmail(),
                    c.getCreditScore(), c.isActive() ? "Yes" : "No");
        }

        System.out.println("\n  Total customers: " + all.size());

        // Optionally view details of one customer
        String input = readLine("\n  Enter customer ID for details (or Enter to skip): ").trim();
        if (!input.isEmpty()) {
            try {
                ReportSnapshot report = customerService.getCustomerReport(input);
                printCustomerReport(report);
            } catch (CustomerNotFoundException e) {
                System.out.println("  [✗] Customer not found.");
            }
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [2] REGISTER CUSTOMER
    // ═══════════════════════════════════════════════════════════════════
    private void registerCustomer() {
        clearScreen();
        System.out.println("  ── Register New Customer ──────────────────────────");
        String name = readLine("  Full Name : ");
        String email = readLine("  Email     : ");
        String password = readLine("  Password  : ");

        try {
            Customer customer = customerService.registerCustomer(name, email, password);
            userRegistry.put(String.valueOf(customer.getId()), customer);
            System.out.println("  [✓] Customer registered! ID: " + customer.getId());
        } catch (DuplicateEmailException e) {
            System.out.println("  [✗] An account with that email already exists.");
        } catch (IllegalArgumentException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [3] OPEN ACCOUNT FOR CUSTOMER
    // ═══════════════════════════════════════════════════════════════════
    private void openAccountForCustomer() {
        clearScreen();
        System.out.println("  ── Open Account for Customer ──────────────────────");

        // List customers
        List<Customer> all = customerService.getAllCustomers();
        for (Customer c : all) {
            System.out.println("    • ID: %d | %s | %s".formatted(c.getId(), c.getFullName(), c.getEmail()));
        }

        String customerId = readLine("  Enter customer ID: ").trim();

        // Find customer
        Customer customer = all.stream()
                .filter(c -> String.valueOf(c.getId()).equals(customerId))
                .findFirst().orElse(null);

        if (customer == null) {
            System.out.println("  [✗] Customer not found.");
            pause();
            return;
        }

        System.out.println("  Account types:");
        System.out.println("    [1] Savings       [2] Checking       [3] Fixed Deposit       [4] Islamic Savings");

        int typeChoice = readInt("  Select type: ");
        AccountType type;
        try {
            type = switch (typeChoice) {
                case 1 -> new SavingsType(readDouble("  Interest rate: "));
                case 2 -> new CheckingType(readDouble("  Overdraft limit: "));
                case 3 -> {
                    double rate = readDouble("  FD rate: ");
                    int term = Integer.parseInt(readLine("  Term (months): ").trim());
                    yield new FixedDepositType(rate, term);
                }
                case 4 -> new IslamicSavingsType(readDouble("  Profit sharing ratio: "));
                default -> null;
            };
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid input."); pause(); return;
        }
        if (type == null) { System.out.println("  [!] Invalid type."); pause(); return; }

        String currency = readLine("  Currency: ").trim().toUpperCase();
        double deposit;
        try { deposit = readDouble("  Initial deposit: "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        try {
            Account account = accountService.openAccount(customer, type, deposit, currency);
            System.out.println("  [✓] Account opened: " + account.getAccountId());
            System.out.println(account.getAccountSummary());
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [4] FREEZE / UNFREEZE ACCOUNT
    // ═══════════════════════════════════════════════════════════════════
    private void freezeUnfreezeAccount() {
        clearScreen();
        System.out.println("  ── Freeze / Unfreeze Account ──────────────────────");

        // Show all accounts
        List<Account> allAccounts = accountService.getAllAccounts();
        System.out.printf("  %-25s %-10s %-12s %-10s%n", "Account ID", "Type", "Balance", "Status");
        System.out.println("  " + "─".repeat(60));
        for (Account a : allAccounts) {
            System.out.printf("  %-25s %-10s %-12.2f %-10s%n",
                    a.getAccountId(), a.getType().typeCode(), a.getBalance(), a.getStatus());
        }

        String accountId = readLine("\n  Enter account ID: ").trim();

        Account target = accountService.findAccountById(accountId).orElse(null);
        if (target == null) {
            System.out.println("  [✗] Account not found.");
            pause();
            return;
        }

        System.out.println("  Current status: " + target.getStatus());
        String adminId = String.valueOf(loggedInAdmin.getId());

        if (target.getStatus() == AccountStatus.FROZEN) {
            String confirm = readLine("  Unfreeze this account? (y/n): ").trim();
            if (confirm.equalsIgnoreCase("y")) {
                try {
                    accountService.unfreezeAccount(accountId, adminId);
                    System.out.println("  [✓] Account unfrozen successfully.");
                } catch (NexaBankException e) {
                    System.out.println("  [✗] " + e.getMessage());
                }
            }
        } else if (target.getStatus() == AccountStatus.ACTIVE) {
            String confirm = readLine("  Freeze this account? (y/n): ").trim();
            if (confirm.equalsIgnoreCase("y")) {
                try {
                    accountService.freezeAccount(accountId, adminId);
                    System.out.println("  [✓] Account frozen successfully.");
                } catch (NexaBankException e) {
                    System.out.println("  [✗] " + e.getMessage());
                }
            }
        } else {
            System.out.println("  [i] Account status is %s — cannot freeze/unfreeze.".formatted(target.getStatus()));
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [5] APPROVE LOAN — show pending loans for approval
    // ═══════════════════════════════════════════════════════════════════
    private void approveLoan() {
        clearScreen();
        System.out.println("  ── Loan Management ────────────────────────────────");

        List<Loan> allLoans = loanService.getAllLoans();
        if (allLoans.isEmpty()) {
            System.out.println("  [i] No loans in the system.");
            pause();
            return;
        }

        System.out.printf("  %-20s %-10s %-12s %-12s %-10s%n",
                "Loan ID", "Borrower", "Principal", "Remaining", "Status");
        System.out.println("  " + "─".repeat(70));

        for (Loan l : allLoans) {
            System.out.printf("  %-20s %-10s $%,-11.2f $%,-11.2f %-10s%n",
                    l.getLoanId(), l.getBorrowerId(), l.getPrincipalAmount(),
                    l.getRemainingBalance(), l.getStatus());
        }

        System.out.println("\n  [i] Loans are auto-approved on application via LoanService.");
        System.out.println("      Use this view to monitor loan statuses.");
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [6] REVERSE TRANSACTION
    // ═══════════════════════════════════════════════════════════════════
    private void reverseTransaction() {
        clearScreen();
        System.out.println("  ── Reverse Transaction ────────────────────────────");

        if (loggedInAdmin.getAccessLevel() < 3) {
            System.out.println("  [✗] Only super-admin (level 3+) can reverse transactions.");
            pause();
            return;
        }

        String txnId = readLine("  Enter Transaction ID to reverse: ").trim();
        String adminId = String.valueOf(loggedInAdmin.getId());

        try {
            Transaction reversal = accountService.reverseTransaction(txnId, adminId);
            System.out.println("  [✓] Transaction reversed!");
            System.out.println("      Reversal Txn ID : " + reversal.transactionId());
            System.out.println("      Type            : " + reversal.type());
            System.out.println("      Amount          : %.2f".formatted(reversal.amount()));
            System.out.println("      New Balance     : %.2f".formatted(reversal.balanceAfter()));
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [7] VIEW AUDIT LOG
    // ═══════════════════════════════════════════════════════════════════
    private void viewAuditLog() {
        clearScreen();
        System.out.println("  ── Audit Log (Recent Activity) ────────────────────");

        List<String> recentActivity = auditService.getRecentActivity();
        if (recentActivity.isEmpty()) {
            System.out.println("  [i] No audit entries yet.");
            pause();
            return;
        }

        // Show last 20 entries
        int start = Math.max(0, recentActivity.size() - 20);
        for (int i = start; i < recentActivity.size(); i++) {
            System.out.println("  " + recentActivity.get(i));
        }

        System.out.println("\n  Showing %d of %d entries.".formatted(
                recentActivity.size() - start, recentActivity.size()));

        // Summary
        Map<String, Long> summary = auditService.exportSummary();
        if (!summary.isEmpty()) {
            System.out.println("\n  ── Action Summary ─────────────────────────────────");
            summary.forEach((action, count) ->
                    System.out.println("    %-50s : %d".formatted(
                            action.length() > 50 ? action.substring(0, 47) + "..." : action, count)));
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [8] RUN INTEREST ENGINE
    // ═══════════════════════════════════════════════════════════════════
    private void runInterestEngine() {
        clearScreen();
        System.out.println("  ── Run Interest Engine ────────────────────────────");

        List<Account> allAccounts = accountService.getAllAccounts();
        System.out.println("  Accounts in system: " + allAccounts.size());
        System.out.println("  This will apply monthly interest to all eligible accounts.");

        String confirm = readLine("  Proceed? (y/n): ").trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("  [i] Cancelled.");
            pause();
            return;
        }

        // Snapshot balances before
        double totalBefore = allAccounts.stream().mapToDouble(Account::getBalance).sum();

        interestEngine.applyMonthlyInterest(allAccounts);

        double totalAfter = allAccounts.stream().mapToDouble(Account::getBalance).sum();
        double totalInterestCredited = totalAfter - totalBefore;

        System.out.println("  [✓] Interest engine completed!");
        System.out.println("      Total balance before : %.2f".formatted(totalBefore));
        System.out.println("      Total balance after  : %.2f".formatted(totalAfter));
        System.out.println("      Total interest added : %.2f".formatted(totalInterestCredited));
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [9] FULL SYSTEM REPORT
    // ═══════════════════════════════════════════════════════════════════
    private void fullSystemReport() {
        clearScreen();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║             FULL SYSTEM REPORT                      ║");
        System.out.println("  ╠══════════════════════════════════════════════════════╣");

        // Customers
        List<Customer> customers = customerService.getAllCustomers();
        System.out.println("  ║  Total Customers  : %-31d ║".formatted(customers.size()));

        // Accounts
        List<Account> accounts = accountService.getAllAccounts();
        long activeAccounts = accounts.stream().filter(a -> a.getStatus() == AccountStatus.ACTIVE).count();
        long frozenAccounts = accounts.stream().filter(a -> a.getStatus() == AccountStatus.FROZEN).count();
        System.out.println("  ║  Total Accounts   : %-31d ║".formatted(accounts.size()));
        System.out.println("  ║    Active          : %-31d ║".formatted(activeAccounts));
        System.out.println("  ║    Frozen          : %-31d ║".formatted(frozenAccounts));

        // Balance by currency
        Map<String, Double> balanceByCurrency = customerService.getTotalSystemBalance();
        System.out.println("  ╠══════════════════════════════════════════════════════╣");
        System.out.println("  ║  BALANCE BY CURRENCY                                ║");
        for (Map.Entry<String, Double> entry : balanceByCurrency.entrySet()) {
            System.out.println("  ║    %-5s : %,-40.2f ║".formatted(entry.getKey(), entry.getValue()));
        }

        // Loans
        List<Loan> allLoans = loanService.getAllLoans();
        long activeLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        long defaultedLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.DEFAULTED).count();
        long paidOffLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.PAID_OFF).count();
        double totalDebt = allLoans.stream().mapToDouble(Loan::getRemainingBalance).sum();

        System.out.println("  ╠══════════════════════════════════════════════════════╣");
        System.out.println("  ║  LOANS                                              ║");
        System.out.println("  ║    Total Loans     : %-31d ║".formatted(allLoans.size()));
        System.out.println("  ║    Active           : %-31d ║".formatted(activeLoans));
        System.out.println("  ║    Paid Off         : %-31d ║".formatted(paidOffLoans));
        System.out.println("  ║    Defaulted        : %-31d ║".formatted(defaultedLoans));
        System.out.println("  ║    Total Outstanding: $%,-30.2f ║".formatted(totalDebt));

        // Audit
        System.out.println("  ╠══════════════════════════════════════════════════════╣");
        System.out.println("  ║  Audit Log Entries : %-31d ║".formatted(
                auditService.getFullLog().size()));

        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [10] CHECK LOAN DEFAULTS
    // ═══════════════════════════════════════════════════════════════════
    private void checkLoanDefaults() {
        clearScreen();
        System.out.println("  ── Check Loan Defaults ────────────────────────────");
        System.out.println("  Scanning for overdue loans (30+ days, 0 repayments)...\n");

        List<Loan> defaulted = loanService.checkAndMarkDefaults();

        if (defaulted.isEmpty()) {
            System.out.println("  [✓] No new defaults detected.");
        } else {
            System.out.println("  [!] %d loan(s) marked as DEFAULTED:".formatted(defaulted.size()));
            for (Loan l : defaulted) {
                System.out.println("      • %s | Borrower: %s | Principal: $%,.2f | Due: %s"
                        .formatted(l.getLoanId(), l.getBorrowerId(),
                                l.getPrincipalAmount(), l.getNextDueDate()));
            }
            System.out.println("\n  Credit scores have been penalized (-50 each).");
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private void printCustomerReport(ReportSnapshot report) {
        System.out.println("\n  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║           CUSTOMER REPORT                      ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  Name         : %-30s ║".formatted(report.customerName()));
        System.out.println("  ║  Email        : %-30s ║".formatted(report.email()));
        System.out.println("  ║  Credit Score : %-30d ║".formatted(report.creditScore()));
        System.out.println("  ║  Total Balance: $%,-29.2f ║".formatted(report.totalBalance()));
        System.out.println("  ║  Total Debt   : $%,-29.2f ║".formatted(report.totalDebt()));
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  Accounts: %-35d ║".formatted(report.accounts().size()));
        for (Account a : report.accounts()) {
            System.out.println("  ║    %-42s ║".formatted(
                    "%s | %.2f %s".formatted(a.getAccountId(), a.getBalance(), a.getCurrency())));
        }
        System.out.println("  ║  Loans: %-38d ║".formatted(report.loans().size()));
        for (Loan l : report.loans()) {
            System.out.println("  ║    %-42s ║".formatted(
                    "%s | $%,.2f | %s".formatted(l.getLoanId(), l.getRemainingBalance(), l.getStatus())));
        }
        System.out.println("  ╚══════════════════════════════════════════════════╝");
    }

    private static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) { hex.append("%02x".formatted(b)); }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
