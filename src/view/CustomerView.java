package view;

import exception.*;
import model.account.*;
import model.loan.Loan;
import model.loan.LoanStatus;
import model.notification.Notification;
import model.notification.NotificationQueue;
import model.report.PaginatedResult;
import model.transaction.Transaction;
import model.transaction.TransactionType;
import model.user.Customer;
import model.user.User;
import service.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static view.ConsoleUI.*;

public class CustomerView {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final Map<String, User> userRegistry;

    private Customer loggedInCustomer = null;

    public CustomerView(CustomerService customerService, AccountService accountService,
                        LoanService loanService, Map<String, User> userRegistry) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.userRegistry = userRegistry;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER PORTAL (Login / Register)
    // ═══════════════════════════════════════════════════════════════════
    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            clearScreen();
            printBanner();
            System.out.println("""
            ┌────────────────────────────────────┐
            │        CUSTOMER PORTAL             │
            ├────────────────────────────────────┤
            │  [1]  Login                        │
            │  [2]  Register                     │
            │  [0]  Back to Main Menu            │
            └────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");
            switch (choice) {
                case 1 -> login();
                case 2 -> register();
                case 0 -> inMenu = false;
                default -> System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ─── Login ────────────────────────────────────────────────────────
    private void login() {
        clearScreen();
        printBanner();
        System.out.println("  ── Customer Login ──────────────────");
        String email = readLine("  Email    : ");
        String password = readLine("  Password : ");

        try {
            Optional<User> user = customerService.login(email, password);
            if (user.isPresent() && user.get() instanceof Customer c) {
                loggedInCustomer = c;
                System.out.println("  [✓] Welcome back, %s!".formatted(c.getFullName()));
                pause();
                dashboard();
            } else {
                System.out.println("  [✗] No customer found with that email.");
                pause();
            }
        } catch (AccountLockedException e) {
            System.out.println("  [✗] Account is locked after too many failed attempts. Contact support.");
            pause();
        } catch (AuthenticationException e) {
            System.out.println("  [✗] Invalid credentials. Please try again.");
            pause();
        }
    }

    // ─── Register ─────────────────────────────────────────────────────
    private void register() {
        clearScreen();
        printBanner();
        System.out.println("  ── Customer Registration ───────────");
        String name = readLine("  Full Name : ");
        String email = readLine("  Email     : ");
        String password = readLine("  Password  : ");

        try {
            Customer customer = customerService.registerCustomer(name, email, password);
            userRegistry.put(String.valueOf(customer.getId()), customer);
            System.out.println("  [✓] Registration successful! Your Customer ID: " + customer.getId());
            System.out.println("  [i] You can now log in with your email and password.");
        } catch (DuplicateEmailException e) {
            System.out.println("  [✗] An account with that email already exists.");
        } catch (IllegalArgumentException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER DASHBOARD
    // ═══════════════════════════════════════════════════════════════════
    private void dashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
            clearScreen();
            printBanner();
            System.out.println("  Welcome, %s".formatted(loggedInCustomer.getFullName()));
            System.out.println("""
            ┌──────────────────────────────────────────┐
            │         CUSTOMER MENU                    │
            ├──────────────────────────────────────────┤
            │  [1]  View Accounts                      │
            │  [2]  Open New Account                   │
            │  [3]  Deposit                            │
            │  [4]  Withdraw                           │
            │  [5]  Transfer                           │
            │  [6]  Apply for Loan                     │
            │  [7]  Repay Loan                         │
            │  [8]  View Transaction History           │
            │  [9]  View Notifications                 │
            │  [10] Account Statement                  │
            │  [0]  Logout                             │
            └──────────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");
            switch (choice) {
                case 1  -> viewAccounts();
                case 2  -> openNewAccount();
                case 3  -> doDeposit();
                case 4  -> doWithdraw();
                case 5  -> doTransfer();
                case 6  -> applyForLoan();
                case 7  -> repayLoan();
                case 8  -> viewTransactionHistory();
                case 9  -> viewNotifications();
                case 10 -> accountStatement();
                case 0  -> {
                    clearScreen();
                    System.out.println("  [i] Logged out successfully.");
                    loggedInCustomer = null;
                    inDashboard = false;
                    pause();
                }
                default -> { System.out.println("  [!] Invalid choice."); pause(); }
            }
        }
    }

    // ─── [1] View Accounts ────────────────────────────────────────────
    private void viewAccounts() {
        clearScreen();
        String ownerId = String.valueOf(loggedInCustomer.getId());
        List<Account> accts = accountService.getAccountsByOwnerId(ownerId);

        if (accts.isEmpty()) {
            System.out.println("  [i] You have no accounts yet. Open one from the menu!");
        } else {
            System.out.println("  ── Your Accounts ──────────────────────────────────");
            for (Account a : accts) { System.out.println(a.getAccountSummary()); }
            System.out.println("  ── Total Balance (all accounts): %.2f ──"
                    .formatted(accountService.getTotalBalance(ownerId)));
        }
        pause();
    }

    // ─── [2] Open New Account ─────────────────────────────────────────
    private void openNewAccount() {
        clearScreen();
        System.out.println("  ── Open New Account ───────────────────────────────");
        System.out.println("  Account types:");
        System.out.println("    [1] Savings Account       (min 1,000)");
        System.out.println("    [2] Checking Account      (min 0)");
        System.out.println("    [3] Fixed Deposit Account  (min 50,000)");
        System.out.println("    [4] Islamic Savings Account (min 1,000)");

        int typeChoice = readInt("  Select account type: ");
        AccountType type;
        try {
            type = switch (typeChoice) {
                case 1 -> new SavingsType(readDouble("  Interest rate (e.g. 3.5): "));
                case 2 -> new CheckingType(readDouble("  Overdraft limit (e.g. 5000): "));
                case 3 -> {
                    double rate = readDouble("  Fixed deposit rate (e.g. 6.0): ");
                    int term = Integer.parseInt(readLine("  Term in months (e.g. 12): ").trim());
                    yield new FixedDepositType(rate, term);
                }
                case 4 -> new IslamicSavingsType(readDouble("  Profit sharing ratio (e.g. 4.0): "));
                default -> { System.out.println("  [!] Invalid type."); pause(); yield null; }
            };
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid input."); pause(); return;
        }
        if (type == null) return;

        String currency = readLine("  Currency (USD, EUR, PKR, GBP, JPY, AED): ").trim().toUpperCase();
        double deposit;
        try { deposit = readDouble("  Initial deposit amount: "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        try {
            Account account = accountService.openAccount(loggedInCustomer, type, deposit, currency);
            System.out.println("  [✓] Account opened successfully!");
            System.out.println(account.getAccountSummary());
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [3] Deposit ──────────────────────────────────────────────────
    private void doDeposit() {
        clearScreen();
        List<Account> accts = getMyAccounts();
        if (accts.isEmpty()) { System.out.println("  [i] No accounts."); pause(); return; }

        System.out.println("  ── Deposit ────────────────────────────────────────");
        printAccountList(accts);
        String accountId = readLine("  Enter account ID: ").trim();
        if (!ownsAccount(accts, accountId)) { System.out.println("  [✗] Not your account."); pause(); return; }

        double amount;
        try { amount = readDouble("  Deposit amount: "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        try {
            Transaction txn = accountService.deposit(accountId, amount,
                    "Customer deposit", String.valueOf(loggedInCustomer.getId()));
            System.out.println("  [✓] Deposit successful!");
            System.out.println("      Transaction ID : " + txn.transactionId());
            System.out.println("      Amount          : %.2f".formatted(txn.amount()));
            System.out.println("      New Balance     : %.2f".formatted(txn.balanceAfter()));
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [4] Withdraw ─────────────────────────────────────────────────
    private void doWithdraw() {
        clearScreen();
        List<Account> accts = getMyAccounts();
        if (accts.isEmpty()) { System.out.println("  [i] No accounts."); pause(); return; }

        System.out.println("  ── Withdraw ───────────────────────────────────────");
        printAccountList(accts);
        String accountId = readLine("  Enter account ID: ").trim();
        if (!ownsAccount(accts, accountId)) { System.out.println("  [✗] Not your account."); pause(); return; }

        double amount;
        try { amount = readDouble("  Withdrawal amount: "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        try {
            Transaction txn = accountService.withdraw(accountId, amount,
                    String.valueOf(loggedInCustomer.getId()));
            System.out.println("  [✓] Withdrawal successful!");
            System.out.println("      Transaction ID : " + txn.transactionId());
            System.out.println("      Amount          : %.2f".formatted(txn.amount()));
            System.out.println("      New Balance     : %.2f".formatted(txn.balanceAfter()));
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [5] Transfer ─────────────────────────────────────────────────
    private void doTransfer() {
        clearScreen();
        List<Account> accts = getMyAccounts();
        if (accts.isEmpty()) { System.out.println("  [i] No accounts."); pause(); return; }

        System.out.println("  ── Transfer ───────────────────────────────────────");
        printAccountList(accts);
        String fromId = readLine("  From account ID: ").trim();
        if (!ownsAccount(accts, fromId)) { System.out.println("  [✗] Not your account."); pause(); return; }

        String toId = readLine("  To account ID  : ").trim();
        double amount;
        try { amount = readDouble("  Transfer amount: "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        try {
            List<Transaction> txns = accountService.transfer(fromId, toId, amount,
                    String.valueOf(loggedInCustomer.getId()));
            System.out.println("  [✓] Transfer successful!");
            System.out.println("      Debit  Txn ID : " + txns.get(0).transactionId());
            System.out.println("      Credit Txn ID : " + txns.get(1).transactionId());
            System.out.println("      Amount         : %.2f".formatted(amount));
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [6] Apply for Loan ───────────────────────────────────────────
    private void applyForLoan() {
        clearScreen();
        System.out.println("  ── Apply for Loan ─────────────────────────────────");
        System.out.println("  Your credit score: " + loggedInCustomer.getCreditScore());
        System.out.println("  Max eligible principal: $%,.2f".formatted(loggedInCustomer.getCreditScore() * 500.0));

        double principal, rate;
        int months;
        try {
            principal = readDouble("  Loan principal amount: ");
            rate = readDouble("  Annual interest rate (e.g. 12.0): ");
            months = Integer.parseInt(readLine("  Term in months (e.g. 36): ").trim());
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid input."); pause(); return;
        }

        try {
            Loan loan = loanService.applyForLoan(loggedInCustomer, principal, rate, months);
            System.out.println("  [✓] Loan approved and disbursed!");
            System.out.println(loan.getLoanSummary());
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [7] Repay Loan ───────────────────────────────────────────────
    private void repayLoan() {
        clearScreen();
        String customerId = String.valueOf(loggedInCustomer.getId());
        List<Loan> activeLoans = loanService.getActiveLoans(customerId);

        if (activeLoans.isEmpty()) { System.out.println("  [i] No active loans."); pause(); return; }

        System.out.println("  ── Repay Loan ─────────────────────────────────────");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan l = activeLoans.get(i);
            System.out.println("    [%d] %s — Remaining: $%,.2f | Monthly: $%,.2f | Due: %s"
                    .formatted(i + 1, l.getLoanId(), l.getRemainingBalance(),
                            l.getMonthlyPayment(), l.getNextDueDate()));
        }

        String loanId = readLine("  Enter loan ID: ").trim();
        double amount;
        try { amount = readDouble("  Repayment amount (0 = monthly payment): "); }
        catch (NumberFormatException e) { System.out.println("  [!] Invalid amount."); pause(); return; }

        if (amount == 0) {
            Loan target = activeLoans.stream().filter(l -> l.getLoanId().equals(loanId)).findFirst().orElse(null);
            if (target == null) { System.out.println("  [✗] Loan not found."); pause(); return; }
            amount = target.getMonthlyPayment();
        }

        try {
            Loan loan = loanService.makeRepayment(loanId, amount);
            System.out.println("  [✓] Repayment recorded!");
            System.out.println("      Remaining: $%,.2f | Status: %s".formatted(loan.getRemainingBalance(), loan.getStatus()));
            if (loan.getStatus() == LoanStatus.PAID_OFF) System.out.println("  Congratulations! Loan fully paid off!");
        } catch (NexaBankException e) {
            System.out.println("  [✗] " + e.getMessage());
        }
        pause();
    }

    // ─── [8] Transaction History ──────────────────────────────────────
    private void viewTransactionHistory() {
        clearScreen();
        List<Account> accts = getMyAccounts();
        if (accts.isEmpty()) { System.out.println("  [i] No accounts."); pause(); return; }

        System.out.println("  ── Transaction History ────────────────────────────");
        printAccountList(accts);
        String accountId = readLine("  Enter account ID: ").trim();
        if (!ownsAccount(accts, accountId)) { System.out.println("  [✗] Not your account."); pause(); return; }

        int page = 1;
        boolean viewing = true;
        while (viewing) {
            try {
                clearScreen();
                PaginatedResult<Transaction> result = accountService.getTransactionHistory(accountId, page, 5);
                System.out.println("  ── Transactions — Page %d of %d (Total: %d) ──"
                        .formatted(result.currentPage(), result.totalPages(), result.totalItems()));

                if (result.items().isEmpty()) { System.out.println("  [i] No transactions."); pause(); return; }

                for (Transaction txn : result.items()) {
                    System.out.println("  ┌─────────────────────────────────────────");
                    System.out.println("  │ ID     : " + txn.transactionId());
                    System.out.println("  │ Type   : " + txn.type());
                    System.out.println("  │ Amount : %.2f %s".formatted(txn.amount(), txn.currency()));
                    System.out.println("  │ Balance: %.2f".formatted(txn.balanceAfter()));
                    System.out.println("  │ Desc   : " + txn.description());
                    System.out.println("  │ Date   : " + txn.timestamp());
                    System.out.println("  └─────────────────────────────────────────");
                }

                System.out.println("  [N] Next  [P] Previous  [Q] Quit");
                String nav = readLine("  Choice: ").trim().toUpperCase();
                switch (nav) {
                    case "N" -> { if (result.hasNextPage()) page++; else System.out.println("  [i] Last page."); }
                    case "P" -> { if (result.hasPreviousPage()) page--; else System.out.println("  [i] First page."); }
                    case "Q" -> viewing = false;
                    default -> System.out.println("  [!] Invalid option.");
                }
            } catch (NexaBankException e) {
                System.out.println("  [✗] " + e.getMessage()); viewing = false; pause();
            }
        }
    }

    // ─── [9] Notifications ────────────────────────────────────────────
    private void viewNotifications() {
        clearScreen();
        String customerId = String.valueOf(loggedInCustomer.getId());
        NotificationQueue queue = accountService.getNotificationQueue(customerId);

        System.out.println("  ── Notifications ──────────────────────────────────");
        System.out.println("  Total: %d | Unread: %d".formatted(queue.size(), queue.unreadCount()));

        if (queue.isEmpty()) { System.out.println("  [i] No notifications."); pause(); return; }

        for (Notification n : queue.getAllNotifications()) {
            String marker = n.isRead() ? "  " : "**";
            System.out.println("  %s [%s] %s".formatted(marker, n.type(), n.title()));
            System.out.println("       %s".formatted(n.message()));
            System.out.println("       %s".formatted(n.createdAt()));
            System.out.println("  ─────────────────────────────────────────");
        }

        if (readLine("  Mark all as read? (y/n): ").trim().equalsIgnoreCase("y")) {
            int count = queue.markAllAsRead();
            System.out.println("  [✓] Marked %d notifications as read.".formatted(count));
        }
        pause();
    }

    // ─── [10] Account Statement ───────────────────────────────────────
    private void accountStatement() {
        clearScreen();
        List<Account> accts = getMyAccounts();
        if (accts.isEmpty()) { System.out.println("  [i] No accounts."); pause(); return; }

        System.out.println("  ── Account Statement ──────────────────────────────");
        printAccountList(accts);
        String accountId = readLine("  Enter account ID: ").trim();

        Optional<Account> opt = accts.stream().filter(a -> a.getAccountId().equals(accountId)).findFirst();
        if (opt.isEmpty()) { System.out.println("  [✗] Not your account."); pause(); return; }

        Account account = opt.get();
        System.out.println("\n  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║             ACCOUNT STATEMENT                       ║");
        System.out.println("  ╠══════════════════════════════════════════════════════╣");
        System.out.println("  ║  Customer : %-38s ║".formatted(loggedInCustomer.getFullName()));
        System.out.println("  ║  Email    : %-38s ║".formatted(loggedInCustomer.getEmail()));
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println(account.getAccountSummary());

        List<Transaction> history = account.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("  [i] No transactions recorded.");
        } else {
            System.out.println("  ── Transaction Details ─────────────────────────────");
            System.out.printf("  %-8s %-14s %-12s %-12s %-20s%n", "No.", "Type", "Amount", "Balance", "Date");
            System.out.println("  " + "─".repeat(70));
            int count = 1;
            for (Transaction txn : history) {
                String sign = (txn.type() == TransactionType.WITHDRAWAL || txn.type() == TransactionType.TRANSFER_OUT) ? "-" : "+";
                System.out.printf("  %-8d %-14s %s%-11.2f %-12.2f %-20s%n",
                        count++, txn.type(), sign, txn.amount(), txn.balanceAfter(), txn.timestamp().toLocalDate());
            }
            System.out.println("  " + "─".repeat(70));
            System.out.println("  Current Balance: %.2f %s".formatted(account.getBalance(), account.getCurrency()));
        }
        pause();
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private List<Account> getMyAccounts() {
        return accountService.getAccountsByOwnerId(String.valueOf(loggedInCustomer.getId()));
    }

    private boolean ownsAccount(List<Account> accts, String accountId) {
        return accts.stream().anyMatch(a -> a.getAccountId().equals(accountId));
    }

    private void printAccountList(List<Account> accts) {
        for (Account a : accts) {
            System.out.println("    • %s | %s | %.2f %s | %s"
                    .formatted(a.getAccountId(), a.getType().displayName(),
                            a.getBalance(), a.getCurrency(), a.getStatus()));
        }
    }
}
