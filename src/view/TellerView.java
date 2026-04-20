package view;

import exception.*;
import model.account.Account;
import model.report.PaginatedResult;
import model.transaction.Transaction;
import model.user.Customer;
import model.user.Teller;
import model.user.User;
import service.AccountService;
import service.AuditService;
import service.CustomerService;

import java.util.List;
import java.util.Map;

import static view.ConsoleUI.*;

public class TellerView {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final AuditService auditService;
    private final Map<String, User> userRegistry;

    private Teller loggedInTeller = null;

    // Track teller session stats
    private int sessionDeposits = 0;
    private int sessionWithdrawals = 0;
    private double sessionDepositTotal = 0;
    private double sessionWithdrawalTotal = 0;

    public TellerView(CustomerService customerService, AccountService accountService,
                      AuditService auditService, Map<String, User> userRegistry) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.auditService = auditService;
        this.userRegistry = userRegistry;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TELLER PANEL (Login)
    // ═══════════════════════════════════════════════════════════════════
    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            clearScreen();
            printBanner();
            System.out.println("""
            ┌────────────────────────────────────┐
            │        TELLER CONSOLE              │
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
        System.out.println("  ── Teller Login ────────────────────");
        String employeeId = readLine("  Employee ID : ");
        String password = readLine("  Password    : ");

        Teller found = null;
        for (User u : userRegistry.values()) {
            if (u instanceof Teller t && t.getEmployeeId().equals(employeeId)) {
                found = t;
                break;
            }
        }

        if (found == null) {
            System.out.println("  [✗] No teller found with that Employee ID.");
            pause();
            return;
        }

        if (!found.getPasswordHash().equals(hashPassword(password))) {
            System.out.println("  [✗] Invalid credentials.");
            pause();
            return;
        }

        loggedInTeller = found;
        // Reset session stats on each login
        sessionDeposits = 0;
        sessionWithdrawals = 0;
        sessionDepositTotal = 0;
        sessionWithdrawalTotal = 0;

        System.out.println("  [✓] Welcome, %s (Branch: %s)".formatted(
                found.getFullName(), found.getBranch()));
        auditService.log(loggedInTeller.getEmployeeId(), "TELLER_LOGIN",
                null, "Teller %s logged in".formatted(found.getFullName()));
        pause();
        dashboard();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TELLER DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    private void dashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
            clearScreen();
            printBanner();
            System.out.println("  Teller: %s | Branch: %s | EMP: %s".formatted(
                    loggedInTeller.getFullName(), loggedInTeller.getBranch(),
                    loggedInTeller.getEmployeeId()));
            System.out.println("""
            ┌──────────────────────────────────────────┐
            │         TELLER MENU                      │
            ├──────────────────────────────────────────┤
            │  [1]  Deposit for Customer               │
            │  [2]  Withdraw for Customer              │
            │  [3]  View My Stats                      │
            │  [4]  View Customer Account              │
            │  [0]  Logout                             │
            └──────────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");
            switch (choice) {
                case 1 -> depositForCustomer();
                case 2 -> withdrawForCustomer();
                case 3 -> viewMyStats();
                case 4 -> viewCustomerAccount();
                case 0 -> {
                    auditService.log(loggedInTeller.getEmployeeId(), "TELLER_LOGOUT",
                            null, "Teller %s logged out".formatted(loggedInTeller.getFullName()));
                    clearScreen();
                    System.out.println("  [i] Teller logged out.");
                    loggedInTeller = null;
                    inDashboard = false;
                    pause();
                }
                default -> { System.out.println("  [!] Invalid choice."); pause(); }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [1] DEPOSIT FOR CUSTOMER
    // ═══════════════════════════════════════════════════════════════════
    private void depositForCustomer() {
        clearScreen();
        System.out.println("  ── Deposit for Customer ───────────────────────────");

        // Step 1: Find the customer
        Customer customer = lookupCustomer();
        if (customer == null) return;

        // Step 2: Show customer's accounts
        String ownerId = String.valueOf(customer.getId());
        List<Account> accts = accountService.getAccountsByOwnerId(ownerId);
        if (accts.isEmpty()) {
            System.out.println("  [i] This customer has no accounts.");
            pause();
            return;
        }

        printAccountTable(accts);

        // Step 3: Pick account
        String accountId = readLine("\n  Enter Account ID: ").trim();

        // Step 4: Amount
        double amount = readDouble("  Deposit amount: ");
        if (amount <= 0) {
            System.out.println("  [!] Amount must be positive.");
            pause();
            return;
        }

        // Step 5: Confirm
        System.out.println("\n  Confirm deposit of %.2f to %s for %s?".formatted(
                amount, accountId, customer.getFullName()));
        String confirm = readLine("  (y/n): ").trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("  [i] Cancelled.");
            pause();
            return;
        }

        // Step 6: Execute
        try {
            String tellerId = "TELLER:" + loggedInTeller.getEmployeeId();
            Transaction txn = accountService.deposit(accountId, amount,
                    "Teller deposit — Branch: " + loggedInTeller.getBranch(), tellerId);
            sessionDeposits++;
            sessionDepositTotal += amount;

            System.out.println("  [✓] Deposit successful!");
            System.out.println("      Transaction ID : " + txn.transactionId());
            System.out.println("      Amount          : %.2f".formatted(txn.amount()));
            System.out.println("      New Balance     : %.2f".formatted(txn.balanceAfter()));
            System.out.println("      Performed by    : " + tellerId);
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [2] WITHDRAW FOR CUSTOMER
    // ═══════════════════════════════════════════════════════════════════
    private void withdrawForCustomer() {
        clearScreen();
        System.out.println("  ── Withdraw for Customer ──────────────────────────");

        // Step 1: Find the customer
        Customer customer = lookupCustomer();
        if (customer == null) return;

        // Step 2: Show customer's accounts
        String ownerId = String.valueOf(customer.getId());
        List<Account> accts = accountService.getAccountsByOwnerId(ownerId);
        if (accts.isEmpty()) {
            System.out.println("  [i] This customer has no accounts.");
            pause();
            return;
        }

        printAccountTable(accts);

        // Step 3: Pick account
        String accountId = readLine("\n  Enter Account ID: ").trim();

        // Step 4: Amount
        double amount = readDouble("  Withdrawal amount: ");
        if (amount <= 0) {
            System.out.println("  [!] Amount must be positive.");
            pause();
            return;
        }

        // Step 5: Confirm
        System.out.println("\n  Confirm withdrawal of %.2f from %s for %s?".formatted(
                amount, accountId, customer.getFullName()));
        String confirm = readLine("  (y/n): ").trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("  [i] Cancelled.");
            pause();
            return;
        }

        // Step 6: Execute
        try {
            String tellerId = "TELLER:" + loggedInTeller.getEmployeeId();
            Transaction txn = accountService.withdraw(accountId, amount, tellerId);
            sessionWithdrawals++;
            sessionWithdrawalTotal += amount;

            System.out.println("  [✓] Withdrawal successful!");
            System.out.println("      Transaction ID : " + txn.transactionId());
            System.out.println("      Amount          : %.2f".formatted(txn.amount()));
            System.out.println("      New Balance     : %.2f".formatted(txn.balanceAfter()));
            System.out.println("      Performed by    : " + tellerId);
        } catch (InsufficientFundsException e) {
            System.out.println("  [✗] Insufficient funds: " + e.getMessage());
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [3] VIEW MY STATS
    // ═══════════════════════════════════════════════════════════════════
    private void viewMyStats() {
        clearScreen();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║            TELLER SESSION STATS                 ║");
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  Teller        : %-30s ║".formatted(loggedInTeller.getFullName()));
        System.out.println("  ║  Employee ID   : %-30s ║".formatted(loggedInTeller.getEmployeeId()));
        System.out.println("  ║  Branch        : %-30s ║".formatted(loggedInTeller.getBranch()));
        System.out.println("  ╠══════════════════════════════════════════════════╣");
        System.out.println("  ║  THIS SESSION                                  ║");
        System.out.println("  ║  Deposits      : %-5d   Total: $%,-14.2f ║".formatted(
                sessionDeposits, sessionDepositTotal));
        System.out.println("  ║  Withdrawals   : %-5d   Total: $%,-14.2f ║".formatted(
                sessionWithdrawals, sessionWithdrawalTotal));
        System.out.println("  ║  Total Txns    : %-30d ║".formatted(
                sessionDeposits + sessionWithdrawals));
        System.out.println("  ║  Net Cash Flow : $%,-29.2f ║".formatted(
                sessionDepositTotal - sessionWithdrawalTotal));
        System.out.println("  ╠══════════════════════════════════════════════════╣");

        // Show recent audit entries by this teller
        String empId = loggedInTeller.getEmployeeId();
        List<String> recentActivity = auditService.getRecentActivity();
        List<String> myActivity = recentActivity.stream()
                .filter(entry -> entry.contains(empId) || entry.contains("TELLER:" + empId))
                .toList();

        System.out.println("  ║  RECENT ACTIVITY (this teller)                 ║");
        if (myActivity.isEmpty()) {
            System.out.println("  ║    No audit entries yet.                       ║");
        } else {
            int start = Math.max(0, myActivity.size() - 5);
            for (int i = start; i < myActivity.size(); i++) {
                String entry = myActivity.get(i);
                if (entry.length() > 48) entry = entry.substring(0, 45) + "...";
                System.out.println("  ║    %-46s ║".formatted(entry));
            }
        }
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  [4] VIEW CUSTOMER ACCOUNT
    // ═══════════════════════════════════════════════════════════════════
    private void viewCustomerAccount() {
        clearScreen();
        System.out.println("  ── View Customer Account ──────────────────────────");

        // Step 1: Find the customer
        Customer customer = lookupCustomer();
        if (customer == null) return;

        // Step 2: Show all accounts
        String ownerId = String.valueOf(customer.getId());
        List<Account> accts = accountService.getAccountsByOwnerId(ownerId);
        if (accts.isEmpty()) {
            System.out.println("  [i] This customer has no accounts.");
            pause();
            return;
        }

        System.out.println("\n  Customer: %s | Email: %s".formatted(
                customer.getFullName(), customer.getEmail()));
        System.out.println("  Credit Score: %d | Total Balance: $%,.2f".formatted(
                customer.getCreditScore(), accountService.getTotalBalance(ownerId)));

        printAccountTable(accts);

        // Step 3: Optionally view transaction history for an account
        String input = readLine("\n  Enter Account ID to view transactions (or Enter to skip): ").trim();
        if (!input.isEmpty()) {
            viewAccountTransactions(input);
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Looks up a customer by email. Returns null if not found.
     */
    private Customer lookupCustomer() {
        System.out.println("  Look up customer:");
        System.out.println("    [1] By Email");
        System.out.println("    [2] Browse All Customers");

        int method = readInt("  Choice: ");

        if (method == 1) {
            String email = readLine("  Customer email: ").trim();
            var opt = customerService.findByEmail(email);
            if (opt.isEmpty()) {
                System.out.println("  [✗] No customer found with email: " + email);
                pause();
                return null;
            }
            return opt.get();

        } else if (method == 2) {
            List<Customer> all = customerService.getAllCustomers();
            if (all.isEmpty()) {
                System.out.println("  [i] No customers registered.");
                pause();
                return null;
            }

            System.out.printf("\n  %-6s %-20s %-28s%n", "ID", "Name", "Email");
            System.out.println("  " + "-".repeat(58));
            for (Customer c : all) {
                System.out.printf("  %-6d %-20s %-28s%n",
                        c.getId(), c.getFullName(), c.getEmail());
            }

            String email = readLine("\n  Enter customer email: ").trim();
            var opt = customerService.findByEmail(email);
            if (opt.isEmpty()) {
                System.out.println("  [✗] Customer not found.");
                pause();
                return null;
            }
            return opt.get();

        } else {
            System.out.println("  [!] Invalid choice.");
            pause();
            return null;
        }
    }

    private void printAccountTable(List<Account> accts) {
        System.out.printf("\n  %-25s %-12s %-8s %-12s %-8s%n",
                "Account ID", "Type", "Currency", "Balance", "Status");
        System.out.println("  " + "-".repeat(68));
        for (Account a : accts) {
            System.out.printf("  %-25s %-12s %-8s %,-12.2f %-8s%n",
                    a.getAccountId(), a.getType().typeCode(), a.getCurrency(),
                    a.getBalance(), a.getStatus());
        }
    }

    private void viewAccountTransactions(String accountId) {
        try {
            int page = 1;
            boolean browsing = true;
            while (browsing) {
                PaginatedResult<Transaction> result =
                        accountService.getTransactionHistory(accountId, page, 10);

                System.out.println("\n  ── Transaction History (Page %d of %d) ─────────────"
                        .formatted(result.currentPage(), result.totalPages()));

                if (result.items().isEmpty()) {
                    System.out.println("  [i] No transactions found.");
                    return;
                }

                System.out.printf("  %-22s %-14s %-12s %-12s %-20s%n",
                        "Txn ID", "Type", "Amount", "Balance", "Date");
                System.out.println("  " + "-".repeat(82));

                for (Transaction t : result.items()) {
                    System.out.printf("  %-22s %-14s %,-12.2f %,-12.2f %-20s%n",
                            truncate(t.transactionId(), 22), t.type(),
                            t.amount(), t.balanceAfter(),
                            t.timestamp().toLocalDate());
                }

                System.out.println("  Page %d of %d | Total: %d transactions".formatted(
                        result.currentPage(), result.totalPages(), result.totalItems()));

                if (result.totalPages() <= 1) {
                    browsing = false;
                } else {
                    String nav = readLine("  [n]ext / [p]rev / [q]uit: ").trim().toLowerCase();
                    switch (nav) {
                        case "n" -> { if (page < result.totalPages()) page++; }
                        case "p" -> { if (page > 1) page--; }
                        default -> browsing = false;
                    }
                }
            }
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
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
