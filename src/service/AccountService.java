package service;

import exception.*;
import model.account.*;
import model.notification.NotificationQueue;
import model.notification.NotificationType;
import model.report.PaginatedResult;
import model.transaction.Transaction;
import model.transaction.TransactionStatus;
import model.transaction.TransactionType;
import model.user.BankAdmin;
import model.user.Teller;
import model.user.User;
import util.CurrencyConverter;

import java.util.*;
import java.util.stream.Collectors;

public class AccountService {

    // ─── Storage ──────────────────────────────────────────────────────
    private final List<Account> accounts = new ArrayList<>();
    private final Map<String, NotificationQueue> notificationQueues = new HashMap<>();
    private final Map<String, User> userRegistry;   // userId → User (for role checks)
    private final AuditService auditService;

    // ─── Constructor ──────────────────────────────────────────────────
    public AccountService(Map<String, User> userRegistry, AuditService auditService) {
        this.userRegistry = userRegistry;
        this.auditService = auditService;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  openAccount
    // ═══════════════════════════════════════════════════════════════════
    public Account openAccount(model.user.Customer customer, AccountType type,
                               double initialDeposit, String currency) throws NexaBankException {

        // Validate minimum balance for the type
        if (initialDeposit < type.minimumBalance()) {
            throw new NexaBankException("ACC_MIN_BALANCE",
                    "Initial deposit %.2f is below the minimum %.2f for %s."
                            .formatted(initialDeposit, type.minimumBalance(), type.displayName()));
        }

        // FixedDeposit extra check — minimum 1000
        if (type instanceof FixedDepositType && initialDeposit < 1000) {
            throw new NexaBankException("ACC_MIN_BALANCE", "Fixed Deposit requires a minimum deposit of 1000.");
        }

        // Create account — constructor sets balance = initialDeposit & generates accountId
        String ownerId = String.valueOf(customer.getId());
        Account account = new Account(ownerId, type, currency, initialDeposit);
        accounts.add(account);

        // Push notification to customer
        pushNotification(ownerId,
                "Account Opened",
                "Your new %s (%s) has been opened with %s %.2f."
                        .formatted(type.displayName(), account.getAccountId(), currency, initialDeposit),
                NotificationType.TRANSACTION);

        auditService.log("ACCOUNT_OPENED: %s for customer %s (ID: %s). Initial deposit: %s %.2f."
                .formatted(account.getAccountId(), customer.getFullName(), ownerId, currency, initialDeposit));

        return account;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  deposit (overload 1) — delegates to overload 2
    // ═══════════════════════════════════════════════════════════════════
    public Transaction deposit(String accountId, double amount) throws NexaBankException {
        return deposit(accountId, amount, "Standard deposit", null);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  deposit (overload 2) — full version
    // ═══════════════════════════════════════════════════════════════════
    public Transaction deposit(String accountId, double amount,
                               String description, String performedBy) throws NexaBankException {

        Account account = findAccountOrThrow(accountId);

        // Account.deposit() validates ACTIVE status and amount > 0 internally
        try {
            account.deposit(amount, description);
        } catch (IllegalArgumentException | model.account.AccountFrozenException e) {
            throw new NexaBankException("DEPOSIT_FAILED", e.getMessage());
        }

        // Get the last recorded transaction (the one just created by account.deposit())
        List<Transaction> history = account.getTransactionHistory();
        Transaction txn = history.get(history.size() - 1);

        pushNotification(account.getOwnerId(),
                "Deposit Received",
                "%.2f %s deposited to %s. New balance: %.2f %s."
                        .formatted(amount, account.getCurrency(), accountId,
                                account.getBalance(), account.getCurrency()),
                NotificationType.TRANSACTION);

        auditService.log("DEPOSIT: %.2f %s to %s by %s. Balance: %.2f."
                .formatted(amount, account.getCurrency(), accountId,
                        performedBy != null ? performedBy : "SELF", account.getBalance()));

        return txn;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  withdraw
    // ═══════════════════════════════════════════════════════════════════
    public Transaction withdraw(String accountId, double amount, String performedBy)
            throws NexaBankException {

        Account account = findAccountOrThrow(accountId);

        // FixedDepositType — throw AccountTypeViolationException
        if (account.getType() instanceof FixedDepositType fd) {
            throw new AccountTypeViolationException(accountId, "FIXED_DEPOSIT",
                    "Cannot withdraw from Fixed Deposit account '%s' before term of %d months ends."
                            .formatted(accountId, fd.termMonths()));
        }

        // Validate ACTIVE
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new exception.AccountFrozenException(accountId);
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException(accountId);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new NexaBankException("ACC_INACTIVE", "Account %s is not active. Status: %s"
                    .formatted(accountId, account.getStatus()));
        }

        if (amount <= 0) {
            throw new NexaBankException("INVALID_AMOUNT", "Withdrawal amount must be positive.");
        }

        // Check balance + overdraft atomically — check and deduct in same block
        double overdraftLimit = 0.0;
        if (account.getType() instanceof CheckingType ct) {
            overdraftLimit = ct.overdraftLimit();
        }
        double available = account.getBalance() + overdraftLimit;
        if (amount > available) {
            throw new InsufficientFundsException(accountId, amount, available);
        }

        // Daily withdrawal cap: 50,000 — Account.withdraw() enforces this
        try {
            account.withdraw(amount, "Withdrawal by " + (performedBy != null ? performedBy : "SELF"));
        } catch (IllegalArgumentException e) {
            throw new DailyLimitExceededException(accountId, account.getDailyWithdrawnAmount(), 50_000.0);
        }

        List<Transaction> history = account.getTransactionHistory();
        Transaction txn = history.get(history.size() - 1);

        pushNotification(account.getOwnerId(),
                "Withdrawal Processed",
                "%.2f %s withdrawn from %s. New balance: %.2f %s."
                        .formatted(amount, account.getCurrency(), accountId,
                                account.getBalance(), account.getCurrency()),
                NotificationType.TRANSACTION);

        auditService.log("WITHDRAWAL: %.2f %s from %s by %s. Balance: %.2f."
                .formatted(amount, account.getCurrency(), accountId,
                        performedBy != null ? performedBy : "SELF", account.getBalance()));

        return txn;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  transfer — atomic (rollback on failure)
    // ═══════════════════════════════════════════════════════════════════
    public List<Transaction> transfer(String fromId, String toId,
                                      double amount, String performedBy) throws NexaBankException {

        if (fromId.equals(toId)) {
            throw new NexaBankException("TRANSFER_SAME_ACC", "Cannot transfer to the same account.");
        }

        Account fromAccount = findAccountOrThrow(fromId);
        Account toAccount = findAccountOrThrow(toId);

        // Both must be ACTIVE
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new NexaBankException("ACC_INACTIVE", "Source account %s is not active.".formatted(fromId));
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new NexaBankException("ACC_INACTIVE", "Destination account %s is not active.".formatted(toId));
        }

        // Currency conversion if currencies differ
        double creditAmount = amount;
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            creditAmount = CurrencyConverter.convert(amount,
                    fromAccount.getCurrency(), toAccount.getCurrency())
                    .orElseThrow(() -> new NexaBankException("CURRENCY_ERR",
                            "Unsupported currency pair: %s → %s".formatted(
                                    fromAccount.getCurrency(), toAccount.getCurrency())));
        }

        // Simulate atomicity — try/catch rollback
        double originalFromBalance = fromAccount.getBalance();

        try {
            fromAccount.transferOut(amount, toId);
            toAccount.transferIn(creditAmount, fromId);
        } catch (Exception e) {
            // Rollback: if transferOut succeeded but transferIn failed
            if (fromAccount.getBalance() < originalFromBalance) {
                fromAccount.deposit(amount, "Rollback: failed transfer to " + toId);
            }
            throw new NexaBankException("TRANSFER_FAILED", "Transfer failed: " + e.getMessage());
        }

        // Collect the two transactions (last from each account)
        List<Transaction> fromHistory = fromAccount.getTransactionHistory();
        List<Transaction> toHistory = toAccount.getTransactionHistory();

        Transaction txnOut = fromHistory.get(fromHistory.size() - 1);
        Transaction txnIn = toHistory.get(toHistory.size() - 1);

        // Notifications
        pushNotification(fromAccount.getOwnerId(),
                "Transfer Sent",
                "%.2f %s transferred from %s to %s."
                        .formatted(amount, fromAccount.getCurrency(), fromId, toId),
                NotificationType.TRANSACTION);

        pushNotification(toAccount.getOwnerId(),
                "Transfer Received",
                "%.2f %s received in %s from %s."
                        .formatted(creditAmount, toAccount.getCurrency(), toId, fromId),
                NotificationType.TRANSACTION);

        auditService.log("TRANSFER: %.2f %s from %s → %s (credited %.2f %s) by %s."
                .formatted(amount, fromAccount.getCurrency(), fromId, toId,
                        creditAmount, toAccount.getCurrency(),
                        performedBy != null ? performedBy : "SELF"));

        return List.of(txnOut, txnIn);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  freezeAccount — only ADMIN or TELLER can freeze
    // ═══════════════════════════════════════════════════════════════════
    public void freezeAccount(String accountId, String adminId) throws NexaBankException {

        // Role check
        User user = userRegistry.get(adminId);
        if (user == null) {
            throw new UnauthorizedAccessException(adminId, "freeze account");
        }
        if (!(user instanceof BankAdmin) && !(user instanceof Teller)) {
            throw new UnauthorizedAccessException(adminId,
                    "freeze account — only ADMIN or TELLER roles are permitted");
        }

        Account account = findAccountOrThrow(accountId);
        account.freeze();

        // Record ALERT notification
        pushNotification(account.getOwnerId(),
                "Account Frozen",
                "Your account %s has been frozen by bank staff. Contact support for details."
                        .formatted(accountId),
                NotificationType.ALERT);

        auditService.log("FREEZE: Account %s frozen by %s (ID: %s)."
                .formatted(accountId, user.getRole(), adminId));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  reverseTransaction — ADMIN with superAdmin (level 3) only
    // ═══════════════════════════════════════════════════════════════════
    public Transaction reverseTransaction(String txnId, String adminId) throws NexaBankException {

        // Role check — must be ADMIN with accessLevel == 3 (super-admin)
        User user = userRegistry.get(adminId);
        if (user == null || !(user instanceof BankAdmin admin)) {
            throw new UnauthorizedAccessException(adminId, "reverse transaction");
        }
        if (admin.getAccessLevel() < 3) {
            throw new UnauthorizedAccessException(adminId,
                    "reverse transaction — requires super-admin (level 3+)");
        }

        // Find the transaction across all accounts
        Transaction original = null;
        Account targetAccount = null;

        for (Account account : accounts) {
            for (Transaction txn : account.getTransactionHistory()) {
                if (txn.transactionId().equals(txnId)) {
                    original = txn;
                    targetAccount = account;
                    break;
                }
            }
            if (original != null) break;
        }

        if (original == null) {
            throw new TransactionNotFoundException(txnId);
        }

        // Only DEPOSIT and WITHDRAWAL are reversible
        if (original.type() != TransactionType.DEPOSIT && original.type() != TransactionType.WITHDRAWAL) {
            throw new IrreversibleTransactionException(txnId,
                    "Only DEPOSIT and WITHDRAWAL can be reversed. This is: " + original.type());
        }

        // Already reversed?
        if (original.status() == TransactionStatus.REVERSED) {
            throw new IrreversibleTransactionException(txnId, "Transaction has already been reversed.");
        }

        // Create inverse transaction
        try {
            if (original.type() == TransactionType.DEPOSIT) {
                targetAccount.withdraw(original.amount(), "Reversal of deposit " + txnId);
            } else {
                targetAccount.deposit(original.amount(), "Reversal of withdrawal " + txnId);
            }
        } catch (Exception e) {
            throw new NexaBankException("REVERSAL_FAILED", "Reversal failed: " + e.getMessage());
        }

        // Get the reversal transaction just recorded
        List<Transaction> history = targetAccount.getTransactionHistory();
        Transaction reversalTxn = history.get(history.size() - 1);

        pushNotification(targetAccount.getOwnerId(),
                "Transaction Reversed",
                "Transaction %s has been reversed by admin. Amount: %.2f %s."
                        .formatted(txnId, original.amount(), targetAccount.getCurrency()),
                NotificationType.ALERT);

        auditService.log("REVERSAL: Transaction %s reversed by admin %s. Type: %s, Amount: %.2f."
                .formatted(txnId, adminId, original.type(), original.amount()));

        return reversalTxn;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getTransactionHistory — paginated, sorted by timestamp desc
    // ═══════════════════════════════════════════════════════════════════
    public PaginatedResult<Transaction> getTransactionHistory(String accountId, int page, int pageSize)
            throws NexaBankException {

        Account account = findAccountOrThrow(accountId);

        // Sort by timestamp descending
        List<Transaction> sorted = account.getTransactionHistory().stream()
                .sorted(Comparator.comparing(Transaction::timestamp).reversed())
                .collect(Collectors.toList());

        int totalItems = sorted.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));

        // Clamp page
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<Transaction> pageItems = (fromIndex < totalItems)
                ? sorted.subList(fromIndex, toIndex)
                : List.of();

        return new PaginatedResult<>(
                Collections.unmodifiableList(new ArrayList<>(pageItems)),
                page, pageSize, totalItems, totalPages);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  QUERY HELPERS
    // ═══════════════════════════════════════════════════════════════════

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public List<Account> getAccountsByOwnerId(String ownerId) {
        return accounts.stream()
                .filter(a -> a.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public double getTotalBalance(String ownerId) {
        return accounts.stream()
                .filter(a -> a.getOwnerId().equals(ownerId))
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public List<Account> getAllAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    public Optional<Account> findAccountById(String accountId) {
        return accounts.stream()
                .filter(a -> a.getAccountId().equals(accountId))
                .findFirst();
    }

    public NotificationQueue getNotificationQueue(String customerId) {
        return notificationQueues.computeIfAbsent(customerId, NotificationQueue::new);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private Account findAccountOrThrow(String accountId) throws AccountNotFoundException {
        return accounts.stream()
                .filter(a -> a.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void pushNotification(String customerId, String title, String message, NotificationType type) {
        NotificationQueue queue = notificationQueues.computeIfAbsent(customerId, NotificationQueue::new);
        queue.send(title, message, type);
    }
}
