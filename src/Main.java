import model.account.*;
import model.loan.Loan;
import model.notification.Notification;
import model.transaction.Transaction;
import model.transaction.TransactionType;
import model.user.BankAdmin;
import model.user.Customer;
import model.user.Teller;
import model.user.User;
import service.*;
import util.IdGenerator;
import view.AdminView;
import view.ConsoleUI;
import view.CustomerView;
import view.TellerView;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    // ─── Services ─────────────────────────────────────────────────────
    private static final Map<String, User> userRegistry = new HashMap<>();
    private static final AuditService auditService = new AuditService();
    private static final AccountService accountService = new AccountService(userRegistry, auditService);
    private static final InterestEngine interestEngine = new InterestEngine(auditService);
    private static final LoanService loanService;
    private static final CustomerService customerService;

    static {
        customerService = new CustomerService(accountService, null, auditService);
        loanService = new LoanService(accountService, customerService, auditService);
        customerService.setLoanService(loanService);
    }

    // ─── Views ────────────────────────────────────────────────────────
    private static final CustomerView customerView =
            new CustomerView(customerService, accountService, loanService, userRegistry);
    private static final AdminView adminView =
            new AdminView(customerService, accountService, loanService, auditService, interestEngine, userRegistry);
    private static final TellerView tellerView =
            new TellerView(customerService, accountService, auditService, userRegistry);

    // ─── Seed Data Stores ─────────────────────────────────────────────
    private static final List<Customer> customers = new ArrayList<>();
    private static final List<BankAdmin> admins = new ArrayList<>();
    private static final List<Teller> tellers = new ArrayList<>();
    private static final List<Account> accounts = new ArrayList<>();
    private static final List<Loan> loans = new ArrayList<>();
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Notification> notifications = new ArrayList<>();

    public static void main(String[] args) {
        // Enable UTF-8 output in Windows cmd.exe so box-drawing chars render correctly
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "chcp 65001 >nul").inheritIO().start().waitFor();
            }
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception ignored) {}

        seedData();
        ConsoleUI.clearScreen();
        System.out.println("  [✓] System booted — seed data loaded successfully.\n");

        boolean running = true;

        while (running) {
            ConsoleUI.clearScreen();
            ConsoleUI.printBanner();
            System.out.println("""
            ┌────────────────────────────────────┐
            │         SELECT YOUR ROLE           │
            ├────────────────────────────────────┤
            │  [1]  Customer                     │
            │  [2]  Bank Admin                   │
            │  [3]  Teller                       │
            │  [0]  Exit                         │
            └────────────────────────────────────┘""");

            int choice = ConsoleUI.readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> customerView.show();
                case 2 -> adminView.show();
                case 3 -> tellerView.show();
                case 0 -> {
                    System.out.println("\n  Thank you for using NexaBank. Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("  [!] Invalid choice. Please try again.\n");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SEED DATA — pre-populates the system for immediate testing
    // ═══════════════════════════════════════════════════════════════════
    private static void seedData() {

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

        customerService.addCustomer(ali);
        customerService.addCustomer(sara);
        customerService.addCustomer(bilal);

        userRegistry.put(String.valueOf(ali.getId()), ali);
        userRegistry.put(String.valueOf(sara.getId()), sara);
        userRegistry.put(String.valueOf(bilal.getId()), bilal);

        // ────────────────────────────────────────────────────────────────
        //  2. BANK ADMIN — isSuperAdmin (accessLevel = 3)
        // ────────────────────────────────────────────────────────────────
        BankAdmin admin = new BankAdmin(
                IdGenerator.nextId(), "Admin User", "admin@nexabank.com", hashPassword("Admin@9999"),
                LocalDateTime.now(), true, 3);

        admins.add(admin);
        userRegistry.put(String.valueOf(admin.getId()), admin);

        // ────────────────────────────────────────────────────────────────
        //  3. TELLER — BranchCode: BR-KHI-01, EmployeeId: EMP-001
        // ────────────────────────────────────────────────────────────────
        Teller teller = new Teller(
                IdGenerator.nextId(), "Teller One", "teller@nexabank.com", hashPassword("Teller@123"),
                LocalDateTime.now(), true, "BR-KHI-01", "EMP-001");

        tellers.add(teller);
        userRegistry.put(String.valueOf(teller.getId()), teller);

        // ────────────────────────────────────────────────────────────────
        //  4. ACCOUNTS
        // ────────────────────────────────────────────────────────────────
        Account aliSavings = new Account(
                String.valueOf(ali.getId()), new SavingsType(3.5), "USD", 25_000.00);
        Account aliChecking = new Account(
                String.valueOf(ali.getId()), new CheckingType(5000.0), "USD", 12_500.00);
        Account saraSavings = new Account(
                String.valueOf(sara.getId()), new SavingsType(2.8), "EUR", 8_000.00);
        Account bilalSavings = new Account(
                String.valueOf(bilal.getId()), new SavingsType(4.0), "PKR", 50_000.00);

        accounts.addAll(List.of(aliSavings, aliChecking, saraSavings, bilalSavings));

        accountService.addAccount(aliSavings);
        accountService.addAccount(aliChecking);
        accountService.addAccount(saraSavings);
        accountService.addAccount(bilalSavings);

        // ────────────────────────────────────────────────────────────────
        //  5. LOANS
        // ────────────────────────────────────────────────────────────────
        Loan aliLoan = new Loan(
                String.valueOf(ali.getId()), 500_000.0, 12.0, 36, aliSavings.getAccountId());
        aliLoan.approve();
        aliLoan.makeRepayment();
        aliLoan.makeRepayment();
        aliLoan.makeRepayment();

        Loan saraLoan = new Loan(
                String.valueOf(sara.getId()), 10_000.0, 8.5, 24, saraSavings.getAccountId());
        saraLoan.approve();

        loans.addAll(List.of(aliLoan, saraLoan));
        loanService.addLoan(aliLoan);
        loanService.addLoan(saraLoan);

        // ────────────────────────────────────────────────────────────────
        //  6. HISTORICAL TRANSACTIONS
        // ────────────────────────────────────────────────────────────────
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.DEPOSIT,
                10_000.00, 35_000.00, "USD", "Initial top-up deposit",
                String.valueOf(ali.getId())));
        transactions.add(Transaction.create(
                aliChecking.getAccountId(), TransactionType.WITHDRAWAL,
                2_500.00, 10_000.00, "USD", "ATM withdrawal",
                String.valueOf(ali.getId())));
        transactions.add(Transaction.createTransfer(
                aliSavings.getAccountId(), TransactionType.TRANSFER_OUT,
                5_000.00, 30_000.00, "USD", "Transfer to checking",
                aliChecking.getAccountId(), String.valueOf(ali.getId())));
        transactions.add(Transaction.createTransfer(
                aliChecking.getAccountId(), TransactionType.TRANSFER_IN,
                5_000.00, 15_000.00, "USD", "Transfer from savings",
                aliSavings.getAccountId(), String.valueOf(ali.getId())));
        transactions.add(Transaction.create(
                saraSavings.getAccountId(), TransactionType.DEPOSIT,
                3_000.00, 11_000.00, "EUR", "Wire transfer deposit",
                String.valueOf(sara.getId())));
        transactions.add(Transaction.create(
                saraSavings.getAccountId(), TransactionType.WITHDRAWAL,
                500.00, 10_500.00, "EUR", "Branch withdrawal",
                String.valueOf(sara.getId())));
        transactions.add(Transaction.create(
                bilalSavings.getAccountId(), TransactionType.DEPOSIT,
                25_000.00, 75_000.00, "PKR", "Salary deposit",
                String.valueOf(bilal.getId())));
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.LOAN_DISBURSEMENT,
                500_000.00, 530_000.00, "USD", "Loan disbursement to savings",
                "SYSTEM"));
        transactions.add(Transaction.create(
                aliSavings.getAccountId(), TransactionType.LOAN_REPAYMENT,
                16_607.15, 513_392.85, "USD", "Loan repayment — installment 1",
                String.valueOf(ali.getId())));
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

        for (Notification n : notifications) {
            accountService.getNotificationQueue(n.recipientId()).send(n);
        }
    }

    // ─── Password Hashing (SHA-256) ───────────────────────────────────
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
}