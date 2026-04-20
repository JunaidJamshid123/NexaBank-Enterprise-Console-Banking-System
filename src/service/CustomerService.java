package service;

import exception.AccountLockedException;
import exception.AuthenticationException;
import exception.CustomerNotFoundException;
import exception.DuplicateEmailException;
import model.account.Account;
import model.loan.Loan;
import model.report.ReportSnapshot;
import model.user.Customer;
import model.user.User;
import util.IdGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CustomerService {

    // ─── Dependencies ────────────────────────────────────────────────
    private final List<Customer> customers = new ArrayList<>();
    private final AccountService accountService;
    private LoanService loanService;
    private final AuditService auditService;

    // ─── Email validation predicate ──────────────────────────────────
    private static final Predicate<String> VALID_EMAIL =
            email -> email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ─── Password strength predicate (min 8 chars, 1 upper, 1 digit) ──
    private static final Predicate<String> STRONG_PASSWORD =
            pw -> pw != null && pw.length() >= 8
                    && pw.chars().anyMatch(Character::isUpperCase)
                    && pw.chars().anyMatch(Character::isDigit);

    private static final int MAX_LOGIN_FAILURES = 5;

    // ─── Constructor ──────────────────────────────────────────────────
    public CustomerService(AccountService accountService, LoanService loanService, AuditService auditService) {
        this.accountService = accountService;
        this.loanService = loanService;
        this.auditService = auditService;
    }

    public void setLoanService(LoanService loanService) {
        this.loanService = loanService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  registerCustomer
    // ═══════════════════════════════════════════════════════════════════
    public Customer registerCustomer(String fullName, String email, String password)
            throws DuplicateEmailException {

        // Validate email format with Predicate
        if (!VALID_EMAIL.test(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        // Validate password strength
        if (!STRONG_PASSWORD.test(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters with 1 uppercase letter and 1 digit.");
        }

        // Check for duplicate email
        if (customers.stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(email))) {
            throw new DuplicateEmailException(email);
        }

        // Hash password using SHA-256
        String passwordHash = hashPassword(password);

        // Create customer with initial credit score = 650
        int id = IdGenerator.nextId();
        Customer customer = new Customer(id, fullName, email, passwordHash);
        // creditScore defaults to 650 via the short constructor
        customers.add(customer);

        auditService.log("REGISTER: Customer '%s' (ID: %d) registered with email '%s'."
                .formatted(fullName, id, email));

        return customer;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  login
    // ═══════════════════════════════════════════════════════════════════
    public Optional<User> login(String email, String password)
            throws AccountLockedException, AuthenticationException {

        Optional<Customer> found = findByEmail(email);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        Customer customer = found.get();

        // Check if account is locked
        if (!customer.isActive()) {
            throw new AccountLockedException(email);
        }

        // Hash the provided password and compare
        String passwordHash = hashPassword(password);

        if (!customer.getPasswordHash().equals(passwordHash)) {
            // Increment fail count
            customer.incrementLoginFailCount();
            auditService.log("LOGIN_FAIL: Failed login attempt for '%s'. Attempt #%d."
                    .formatted(email, customer.getLoginFailCount()));

            // Lock account after 5 failures
            if (customer.getLoginFailCount() >= MAX_LOGIN_FAILURES) {
                customer.setActive(false);
                auditService.log("ACCOUNT_LOCKED: Account '%s' locked after %d failed attempts."
                        .formatted(email, MAX_LOGIN_FAILURES));
                throw new AccountLockedException(email);
            }

            throw new AuthenticationException("Invalid credentials for email: " + email);
        }

        // Successful login — reset fail count
        customer.resetLoginFailCount();
        auditService.log("LOGIN: Customer '%s' (ID: %d) logged in successfully."
                .formatted(customer.getFullName(), customer.getId()));

        return Optional.of(customer);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  findByEmail
    // ═══════════════════════════════════════════════════════════════════
    public Optional<Customer> findByEmail(String email) {
        return customers.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  updateCreditScore
    // ═══════════════════════════════════════════════════════════════════
    public void updateCreditScore(String customerId, int delta) throws CustomerNotFoundException {
        Customer customer = findById(customerId);

        int oldScore = customer.getCreditScore();
        int newScore = Math.min(850, Math.max(300, oldScore + delta));
        customer.setCreditScore(newScore);

        auditService.log("CREDIT_SCORE: Customer ID %s score changed from %d to %d (delta: %+d)."
                .formatted(customerId, oldScore, newScore, delta));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCustomersByBalance — sorted by total balance descending
    // ═══════════════════════════════════════════════════════════════════
    public List<Customer> getCustomersByBalance() {
        return customers.stream()
                .sorted(Comparator.comparingDouble(
                        (Customer c) -> accountService.getTotalBalance(String.valueOf(c.getId())))
                        .reversed())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getCustomerReport — returns ReportSnapshot record
    // ═══════════════════════════════════════════════════════════════════
    public ReportSnapshot getCustomerReport(String customerId) throws CustomerNotFoundException {
        Customer customer = findById(customerId);

        List<Account> accounts = accountService.getAccountsByOwnerId(customerId);
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();

        List<Loan> loans = loanService.getLoansByBorrowerId(customerId);
        double totalDebt = loans.stream()
                .mapToDouble(Loan::getRemainingBalance)
                .sum();

        return new ReportSnapshot(
                customerId,
                customer.getFullName(),
                customer.getEmail(),
                customer.getCreditScore(),
                accounts,
                totalBalance,
                loans,
                totalDebt
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getTotalSystemBalance — grouped by currency
    // ═══════════════════════════════════════════════════════════════════
    public Map<String, Double> getTotalSystemBalance() {
        return customers.stream()
                .flatMap(c -> accountService.getAccountsByOwnerId(String.valueOf(c.getId())).stream())
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        Collectors.summingDouble(Account::getBalance)
                ));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getAllCustomers
    // ═══════════════════════════════════════════════════════════════════
    public List<Customer> getAllCustomers() {
        return Collections.unmodifiableList(customers);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  addCustomer — for seed data / external registration
    // ═══════════════════════════════════════════════════════════════════
    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private Customer findById(String customerId) throws CustomerNotFoundException {
        return customers.stream()
                .filter(c -> String.valueOf(c.getId()).equals(customerId))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

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
