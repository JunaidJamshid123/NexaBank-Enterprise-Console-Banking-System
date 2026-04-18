package repository.impl;

import model.loan.Loan;
import model.loan.LoanStatus;
import repository.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * In-memory loan repository backed by HashMap.
 *
 * Also maintains a PriorityQueue<Loan> sorted by nextDueDate ascending
 * (earliest due first) for efficient repayment scheduling.
 */
public class InMemoryLoanRepository implements Repository<Loan, String> {

    private final HashMap<String, Loan> store = new HashMap<>();

    /**
     * PriorityQueue<Loan> — Loan repayment priority queue.
     *
     * Sorted by nextDueDate ascending (earliest due date first).
     * PriorityQueue is a min-heap: peek()/poll() always returns the loan
     * with the closest upcoming due date. This enables efficient scheduling
     * of repayment reminders and default checking.
     *
     * O(log n) for offer/poll, O(1) for peek.
     */
    private final PriorityQueue<Loan> repaymentQueue =
            new PriorityQueue<>(Comparator.comparing(Loan::getNextDueDate));

    @Override
    public void save(Loan entity) {
        store.put(entity.getLoanId(), entity);
        // Only active loans go into the priority queue
        if (entity.getStatus() == LoanStatus.ACTIVE) {
            repaymentQueue.offer(entity);
        }
    }

    @Override
    public Optional<Loan> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Loan> findAll(Predicate<Loan> filter) {
        return store.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        Loan removed = store.remove(id);
        if (removed != null) {
            repaymentQueue.remove(removed);
        }
    }

    @Override
    public boolean exists(String id) {
        return store.containsKey(id);
    }

    @Override
    public long count() {
        return store.size();
    }

    // ─── PriorityQueue Access ─────────────────────────────────────────

    public Optional<Loan> peekNextDue() {
        return Optional.ofNullable(repaymentQueue.peek());
    }

    public Optional<Loan> pollNextDue() {
        return Optional.ofNullable(repaymentQueue.poll());
    }

    public List<Loan> getRepaymentSchedule() {
        return new ArrayList<>(repaymentQueue);
    }

    // ─── Query Helpers ────────────────────────────────────────────────

    public List<Loan> findByBorrowerId(String borrowerId) {
        return store.values().stream()
                .filter(l -> l.getBorrowerId().equals(borrowerId))
                .collect(Collectors.toList());
    }

    public List<Loan> findActive() {
        return findAll(l -> l.getStatus() == LoanStatus.ACTIVE);
    }
}
