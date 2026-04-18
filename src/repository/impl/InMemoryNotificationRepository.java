package repository.impl;

import model.notification.Notification;
import repository.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * In-memory notification repository backed by HashMap.
 *
 * Each customer's notifications are also stored in a LinkedList-backed Queue
 * for efficient FIFO polling.
 *
 * ══════════════════════════════════════════════════════════════════════════
 * WHY LinkedList IS USED AS THE QUEUE BACKING:
 * ══════════════════════════════════════════════════════════════════════════
 *
 * LinkedList implements both List and Queue (Deque) interfaces.
 *
 * O(1) offer() and poll():
 *   - offer() appends to the tail — LinkedList maintains a direct reference
 *     to the tail node, so no traversal is needed.
 *   - poll() removes from the head — again O(1) since LinkedList maintains
 *     a direct head reference. Re-linking the next node is constant time.
 *   - By contrast, ArrayList's remove(0) is O(n) because it must shift all
 *     remaining elements left by one position (System.arraycopy).
 *
 * TRADE-OFF — CACHE LOCALITY:
 *   - LinkedList nodes are scattered across the heap (each node is a separate
 *     object with prev/next pointers). This leads to poor CPU cache locality
 *     — iterating a LinkedList causes many cache misses.
 *   - ArrayList stores elements in a contiguous array, giving excellent cache
 *     locality for sequential reads/iterations.
 *   - For a notification queue where the primary operations are offer/poll
 *     (not random access or bulk iteration), LinkedList's O(1) head removal
 *     outweighs ArrayList's cache advantages.
 * ══════════════════════════════════════════════════════════════════════════
 */
public class InMemoryNotificationRepository implements Repository<Notification, String> {

    private final HashMap<String, Notification> store = new HashMap<>();

    // Per-customer notification queues — LinkedList as Queue backing
    private final HashMap<String, Queue<Notification>> customerQueues = new HashMap<>();

    @Override
    public void save(Notification entity) {
        store.put(entity.notificationId(), entity);
        // Also enqueue for the customer
        customerQueues
                .computeIfAbsent(entity.recipientId(), k -> new LinkedList<>())
                .offer(entity);
    }

    @Override
    public Optional<Notification> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Notification> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Notification> findAll(Predicate<Notification> filter) {
        return store.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        Notification removed = store.remove(id);
        if (removed != null) {
            Queue<Notification> queue = customerQueues.get(removed.recipientId());
            if (queue != null) {
                queue.remove(removed);
            }
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

    // ─── Customer Queue Operations ────────────────────────────────────

    public Queue<Notification> getCustomerQueue(String customerId) {
        return customerQueues.computeIfAbsent(customerId, k -> new LinkedList<>());
    }

    public Optional<Notification> peekNext(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        return (queue == null || queue.isEmpty()) ? Optional.empty() : Optional.of(queue.peek());
    }

    public Optional<Notification> pollNext(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        return (queue == null || queue.isEmpty()) ? Optional.empty() : Optional.of(queue.poll());
    }

    public long getUnreadCount(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        if (queue == null) return 0;
        return queue.stream().filter(n -> !n.isRead()).count();
    }

    public List<Notification> findByCustomerId(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        return (queue == null) ? List.of() : List.copyOf(queue);
    }
}
