import model.account.Account;
import model.account.CheckingType;
import model.account.SavingsType;
import model.loan.Loan;
import model.notification.Notification;
import model.notification.NotificationType;
import model.transaction.Transaction;
import model.transaction.TransactionStatus;
import model.transaction.TransactionType;
import model.user.BankAdmin;
import model.user.Customer;
import model.user.Teller;
import util.IdGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // ─── Shared Data Stores (populated by seedData) ───────────────────
    private static final List<Customer> customers = new ArrayList<>();
    private static final List<BankAdmin> admins = new ArrayList<>();
    private static final List<Teller> tellers = new ArrayList<>();
    private static final List<Account> accounts = new ArrayList<>();
    private static final List<Loan> loans = new ArrayList<>();
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Notification> notifications = new ArrayList<>();

    public static void main(String[] args) {
        seedData();
        System.out.println("  [✓] System booted — seed data loaded successfully.\n");

        boolean running = true;

        while (running) {
            printBanner();
            printMainMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> customerMenu();
                case 2 -> adminMenu();
                case 3 -> tellerMenu();
                case 0 -> {
                    System.out.println("\n  Thank you for using NexaBank. Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BANNER
    // ═══════════════════════════════════════════════════════════════════
    private static void printBanner() {
        System.out.println("""
        
        ╔══════════════════════════════════════════════════╗
        ║              ★  N E X A B A N K  ★              ║
        ║          Modern Console Banking System           ║
        ╚══════════════════════════════════════════════════╝
        """);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  MAIN MENU
    // ═══════════════════════════════════════════════════════════════════
    private static void printMainMenu() {
        System.out.println("  ┌────────────────────────────────────┐");
        System.out.println("  │         SELECT YOUR ROLE           │");
        System.out.println("  ├────────────────────────────────────┤");
        System.out.println("  │  [1]  Customer                    │");
        System.out.println("  │  [2]  Bank Admin                  │");
        System.out.println("  │  [3]  Teller                      │");
        System.out.println("  │  [0]  Exit                        │");
        System.out.println("  └────────────────────────────────────┘");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOMER MENU
    // ═══════════════════════════════════════════════════════════════════
    private static void customerMenu() {
        boolean inMenu = true;
        while (inMenu) {
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
                case 1 -> customerLogin();
                case 2 -> customerRegister();
                case 0 -> inMenu = false;
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    private static void customerLogin() {
        System.out.println("\n  ── Customer Login ──────────────────");
        System.out.print("  Email    : ");
        String email = scanner.nextLine();
        System.out.print("  Password : ");
        String password = scanner.nextLine();
        // TODO: authenticate against CustomerService
        System.out.println("  [i] Login successful (simulated).\n");
        customerDashboard();
    }

    private static void customerDashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
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
                case 1  -> System.out.println("  [i] View Accounts — coming soon...");
                case 2  -> System.out.println("  [i] Open New Account — coming soon...");
                case 3  -> System.out.println("  [i] Deposit — coming soon...");
                case 4  -> System.out.println("  [i] Withdraw — coming soon...");
                case 5  -> System.out.println("  [i] Transfer — coming soon...");
                case 6  -> System.out.println("  [i] Apply for Loan — coming soon...");
                case 7  -> System.out.println("  [i] Repay Loan — coming soon...");
                case 8  -> System.out.println("  [i] View Transaction History — coming soon...");
                case 9  -> System.out.println("  [i] View Notifications — coming soon...");
                case 10 -> System.out.println("  [i] Account Statement — coming soon...");
                case 0  -> {
                    System.out.println("  [i] Logged out.\n");
                    inDashboard = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    private static void customerRegister() {
        System.out.println("\n  ── Customer Registration ───────────");
        System.out.print("  Full Name : ");
        String name = scanner.nextLine();
        System.out.print("  Email     : ");
        String email = scanner.nextLine();
        System.out.print("  Password  : ");
        String password = scanner.nextLine();
        System.out.print("  Phone     : ");
        String phone = scanner.nextLine();
        System.out.print("  Address   : ");
        String address = scanner.nextLine();
        // TODO: register via CustomerService
        System.out.println("  [i] Registration functionality coming soon...\n");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ADMIN MENU
    // ═══════════════════════════════════════════════════════════════════
    private static void adminMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""
            
            ┌────────────────────────────────────┐
            │        ADMIN PANEL                 │
            ├────────────────────────────────────┤
            │  [1]  Login                        │
            │  [0]  Back to Main Menu            │
            └────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");

            switch (choice) {
                case 1 -> adminLogin();
                case 0 -> inMenu = false;
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    private static void adminLogin() {
        System.out.println("\n  ── Admin Login ─────────────────────");
        System.out.print("  Email    : ");
        String email = scanner.nextLine();
        System.out.print("  Password : ");
        String password = scanner.nextLine();
        // TODO: authenticate against admin records
        System.out.println("  [i] Login successful (simulated).\n");
        adminDashboard();
    }

    private static void adminDashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
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
                case 1  -> System.out.println("  [i] View All Customers — coming soon...");
                case 2  -> System.out.println("  [i] Register Customer — coming soon...");
                case 3  -> System.out.println("  [i] Open Account for Customer — coming soon...");
                case 4  -> System.out.println("  [i] Freeze / Unfreeze Account — coming soon...");
                case 5  -> System.out.println("  [i] Approve Loan — coming soon...");
                case 6  -> System.out.println("  [i] Reverse Transaction — coming soon...");
                case 7  -> System.out.println("  [i] View Audit Log — coming soon...");
                case 8  -> System.out.println("  [i] Run Interest Engine — coming soon...");
                case 9  -> System.out.println("  [i] Full System Report — coming soon...");
                case 10 -> System.out.println("  [i] Check Loan Defaults — coming soon...");
                case 0  -> {
                    System.out.println("  [i] Logged out.\n");
                    inDashboard = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TELLER MENU
    // ═══════════════════════════════════════════════════════════════════
    private static void tellerMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""
            
            ┌────────────────────────────────────┐
            │        TELLER CONSOLE              │
            ├────────────────────────────────────┤
            │  [1]  Login                        │
            │  [0]  Back to Main Menu            │
            └────────────────────────────────────┘""");

            int choice = readInt("  Enter your choice: ");

            switch (choice) {
                case 1 -> tellerLogin();
                case 0 -> inMenu = false;
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    private static void tellerLogin() {
        System.out.println("\n  ── Teller Login ────────────────────");
        System.out.print("  Employee ID : ");
        String employeeId = scanner.nextLine();
        System.out.print("  Password    : ");
        String password = scanner.nextLine();
        // TODO: authenticate against teller records
        System.out.println("  [i] Login successful (simulated).\n");
        tellerDashboard();
    }

    private static void tellerDashboard() {
        boolean inDashboard = true;
        while (inDashboard) {
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
                case 1 -> System.out.println("  [i] Deposit for Customer — coming soon...");
                case 2 -> System.out.println("  [i] Withdraw for Customer — coming soon...");
                case 3 -> System.out.println("  [i] View My Stats — coming soon...");
                case 4 -> System.out.println("  [i] View Customer Account — coming soon...");
                case 0 -> {
                    System.out.println("  [i] Logged out.\n");
                    inDashboard = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SEED DATA — pre-populates the system for immediate testing
    // ═══════════════════════════════════════════════════════════════════
    private static void seedData() {
        // ─── Helper: hash passwords using SHA-256 ─────────────────────
        // Same algorithm used by CustomerService for consistency.

        // ────────────────────────────────────────────────────────────────
        //  1. CUSTOMERS
        // ────────────────────────────────────────────────────────────────
        Customer ali = new Customer(
                IdGenerator.nextId(), "Ali Hassan", "ali@nexabank.com", hashPassword("Pass@1234"),
                LocalDateTime.now(), true, "+92-300-1234567", "Karachi, Pakistan", 780);
        Customer sara = new Customer(
                IdGenerator.nextId(), "Sara Khan", "sara@nexabank.com", hashPassword("Pass@1234"),
                LocalDateTime.now(), true, "+92-321-9876543", "Lahore, Pakistan", 620);
        Customer bilal = new Customer(
                IdGenerator.nextId(), "Bilal Raza", "bilal@nexabank.com", hashPassword("Pass@1234"),
                LocalDateTime.now(), true, "+92-333-5556667", "Islamabad, Pakistan", 420);

        customers.addAll(List.of(ali, sara, bilal));

        // ────────────────────────────────────────────────────────────────
        //  2. BANK ADMIN — isSuperAdmin (accessLevel = 3)
        // ────────────────────────────────────────────────────────────────
        BankAdmin admin = new BankAdmin(
                IdGenerator.nextId(), "Admin User", "admin@nexabank.com", hashPassword("Admin@9999"),
                LocalDateTime.now(), true, 3);  // 3 = super-admin

        admins.add(admin);

        // ────────────────────────────────────────────────────────────────
        //  3. TELLER — BranchCode: BR-KHI-01, EmployeeId: EMP-001
        // ────────────────────────────────────────────────────────────────
        Teller teller = new Teller(
                IdGenerator.nextId(), "Teller One", "teller@nexabank.com", hashPassword("Teller@123"),
                LocalDateTime.now(), true, "BR-KHI-01", "EMP-001");

        tellers.add(teller);

        // ────────────────────────────────────────────────────────────────
        //  4. ACCOUNTS
        // ────────────────────────────────────────────────────────────────

        // Ali Hassan — USD Savings + USD Checking
        Account aliSavings = new Account(
                String.valueOf(ali.getId()), new SavingsType(3.5), "USD", 25_000.00);
        Account aliChecking = new Account(
                String.valueOf(ali.getId()), new CheckingType(5000.0), "USD", 12_500.00);

        // Sara Khan — EUR Savings
        Account saraSavings = new Account(
                String.valueOf(sara.getId()), new SavingsType(2.8), "EUR", 8_000.00);

        // Bilal Raza — PKR Savings
        Account bilalSavings = new Account(
                String.valueOf(bilal.getId()), new SavingsType(4.0), "PKR", 50_000.00);

        accounts.addAll(List.of(aliSavings, aliChecking, saraSavings, bilalSavings));

        // ────────────────────────────────────────────────────────────────
        //  5. LOANS
        // ────────────────────────────────────────────────────────────────

        // Loan 1 — Ali Hassan: PKR 500,000 at 12% for 36 months, ACTIVE, 3 repayments made
        Loan aliLoan = new Loan(
                String.valueOf(ali.getId()), 500_000.0, 12.0, 36, aliSavings.getAccountId());
        aliLoan.approve();
        aliLoan.makeRepayment();  // repayment 1
        aliLoan.makeRepayment();  // repayment 2
        aliLoan.makeRepayment();  // repayment 3

        // Loan 2 — Sara Khan: EUR 10,000 at 8.5% for 24 months, ACTIVE, 0 repayments (overdue)
        Loan saraLoan = new Loan(
                String.valueOf(sara.getId()), 10_000.0, 8.5, 24, saraSavings.getAccountId());
        saraLoan.approve();
        // No repayments — this loan is overdue and suitable for default detection testing

        loans.addAll(List.of(aliLoan, saraLoan));

        // ────────────────────────────────────────────────────────────────
        //  6. HISTORICAL TRANSACTIONS (10 total, mix of all types)
        // ────────────────────────────────────────────────────────────────

        // Txn 1: Ali — initial deposit to savings
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.DEPOSIT,
                10_000.00, 35_000.00, "USD", "Initial top-up deposit",
                String.valueOf(ali.getId())));

        // Txn 2: Ali — withdrawal from checking
        transactions.add(Transaction.create(
                aliChecking.getAccountId(), TransactionType.WITHDRAWAL,
                2_500.00, 10_000.00, "USD", "ATM withdrawal",
                String.valueOf(ali.getId())));

        // Txn 3: Ali — transfer out from savings
        transactions.add(Transaction.createTransfer(
                aliSavings.getAccountId(), TransactionType.TRANSFER_OUT,
                5_000.00, 30_000.00, "USD", "Transfer to checking",
                aliChecking.getAccountId(), String.valueOf(ali.getId())));

        // Txn 4: Ali — transfer in to checking
        transactions.add(Transaction.createTransfer(
                aliChecking.getAccountId(), TransactionType.TRANSFER_IN,
                5_000.00, 15_000.00, "USD", "Transfer from savings",
                aliSavings.getAccountId(), String.valueOf(ali.getId())));

        // Txn 5: Sara — deposit to EUR savings
        transactions.add(Transaction.create(
                saraSavings.getAccountId(), TransactionType.DEPOSIT,
                3_000.00, 11_000.00, "EUR", "Wire transfer deposit",
                String.valueOf(sara.getId())));

        // Txn 6: Sara — withdrawal from EUR savings
        transactions.add(Transaction.create(
                saraSavings.getAccountId(), TransactionType.WITHDRAWAL,
                500.00, 10_500.00, "EUR", "Branch withdrawal",
                String.valueOf(sara.getId())));

        // Txn 7: Bilal — deposit to PKR savings
        transactions.add(Transaction.create(
                bilalSavings.getAccountId(), TransactionType.DEPOSIT,
                25_000.00, 75_000.00, "PKR", "Salary deposit",
                String.valueOf(bilal.getId())));

        // Txn 8: Ali — loan disbursement
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.LOAN_DISBURSEMENT,
                500_000.00, 530_000.00, "USD", "Loan LOAN disbursement to savings",
                "SYSTEM"));

        // Txn 9: Ali — loan repayment
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.LOAN_REPAYMENT,
                16_607.15, 513_392.85, "USD", "Loan repayment — installment 1",
                String.valueOf(ali.getId())));

        // Txn 10: Ali — interest credit
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.INTEREST_CREDIT,
                72.92, 513_465.77, "USD", "Monthly interest credit — Savings @ 3.5%",
                "SYSTEM"));

        // ────────────────────────────────────────────────────────────────
        //  7. NOTIFICATIONS
        // ────────────────────────────────────────────────────────────────

        notifications.add(Notification.system(
                String.valueOf(ali.getId()), "Welcome to NexaBank",
                "Your accounts have been successfully created. Enjoy banking with us!"));
        notifications.add(Notification.transaction(
                String.valueOf(ali.getId()), "Loan Approved",
                "Your loan of PKR 500,000 at 12% for 36 months has been approved."));
        notifications.add(Notification.system(
                String.valueOf(sara.getId()), "Welcome to NexaBank",
                "Your EUR Savings account is now active."));
        notifications.add(Notification.alert(
                String.valueOf(sara.getId()), "Loan Payment Overdue",
                "Your loan repayment is overdue. Please make a payment to avoid default."));
        notifications.add(Notification.system(
                String.valueOf(bilal.getId()), "Welcome to NexaBank",
                "Your PKR Savings account has been created."));

        // ────────────────────────────────────────────────────────────────
        //  SUMMARY — print what was loaded
        // ────────────────────────────────────────────────────────────────
        System.out.println("""
        
          ╔══════════════════════════════════════════════════╗
          ║           SEED DATA LOADED SUCCESSFULLY         ║
          ╠══════════════════════════════════════════════════╣
          ║  Customers     : 3  (Ali, Sara, Bilal)          ║
          ║  Admin          : 1  (super-admin, Level 3)      ║
          ║  Teller         : 1  (BR-KHI-01, EMP-001)       ║
          ║  Accounts       : 4  (2 Savings, 1 Checking, 1) ║
          ║  Loans          : 2  (1 active+3 repaid, 1 due) ║
          ║  Transactions  : 10 (mixed types)               ║
          ║  Notifications : 5                               ║
          ╚══════════════════════════════════════════════════╝""");
    }

    // ─── Password Hashing (SHA-256) — mirrors CustomerService ─────────
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append("%02x".formatted(b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════════
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("  [!] Please enter a valid number.");
            scanner.nextLine();
            System.out.print(prompt);
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume leftover newline
        return value;
    }
}