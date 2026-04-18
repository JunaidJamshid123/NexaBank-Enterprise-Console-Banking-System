package model.notification;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Manages a per-customer notification queue.
 *
 * WHY LinkedList as the backing Queue implementation:
 * ──────────────────────────────────────────────────────────────────────
 * LinkedList implements both List and Queue (Deque) interfaces.
 * We choose it over ArrayDeque here because:
 *
 *  1. Queue semantics — Notifications are processed in FIFO order
 *     (oldest notification is read/dismissed first). LinkedList's
 *     offer()/poll()/peek() operations are all O(1).
 *
 *  2. No capacity constraints — Unlike ArrayDeque, LinkedList never
 *     needs to resize an internal array. Notification queues grow and
 *     shrink unpredictably per customer, so we avoid resize overhead.
 *
 *  3. Null-safety is not a concern — We validate that notifications
 *     are never null before enqueuing, so ArrayDeque's null-rejection
 *     offers no advantage here.
 *
 *  4. Traversal — We occasionally need to iterate over all notifications
 *     (e.g., show unread count, list all). LinkedList supports both
 *     Queue polling AND full iteration, whereas a pure Queue only
 *     exposes the head.
 * ──────────────────────────────────────────────────────────────────────
 */
public class NotificationQueue {

    // ─── Fields ───────────────────────────────────────────────────────

    private final String customerId;
    private final Queue<Notification> notifications;

    // ─── Constructor ──────────────────────────────────────────────────

    public NotificationQueue(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        this.customerId = customerId;

        // LinkedList as Queue — see class-level Javadoc for rationale
        this.notifications = new LinkedList<>();
    }

    // ─── Enqueue ──────────────────────────────────────────────────────

    /**
     * Adds a notification to the end of the queue (FIFO order).
     */
    public void send(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        if (!notification.recipientId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "Notification recipient '%s' does not match queue owner '%s'"
                            .formatted(notification.recipientId(), customerId)
            );
        }
        notifications.offer(notification);
    }

    /**
     * Creates and enqueues a notification in one step.
     */
    public Notification send(String title, String message, NotificationType type) {
        Notification notification = Notification.create(customerId, title, message, type);
        notifications.offer(notification);
        return notification;
    }

    // ─── Dequeue / Read ───────────────────────────────────────────────

    /**
     * Retrieves and removes the oldest notification (head of queue).
     * Returns null if the queue is empty.
     */
    public Notification pollNext() {
        return notifications.poll();
    }

    /**
     * Peeks at the oldest notification without removing it.
     * Returns null if the queue is empty.
     */
    public Notification peekNext() {
        return notifications.peek();
    }

    /**
     * Marks the oldest unread notification as read.
     * Replaces it in the queue with a read copy (records are immutable).
     * Returns the marked notification, or null if no unread exist.
     */
    public Notification markNextAsRead() {
        // We need to iterate to find the first unread — use LinkedList's List capabilities
        LinkedList<Notification> list = (LinkedList<Notification>) notifications;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isRead()) {
                Notification read = list.get(i).markRead();
                list.set(i, read);
                return read;
            }
        }
        return null;
    }

    /**
     * Marks ALL notifications as read. Returns the count of newly-read notifications.
     */
    public int markAllAsRead() {
        LinkedList<Notification> list = (LinkedList<Notification>) notifications;
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isRead()) {
                list.set(i, list.get(i).markRead());
                count++;
            }
        }
        return count;
    }

    // ─── Query Methods ────────────────────────────────────────────────

    /**
     * Number of unread notifications.
     */
    public long unreadCount() {
        return notifications.stream()
                .filter(n -> !n.isRead())
                .count();
    }

    /**
     * Total notifications in the queue.
     */
    public int size() {
        return notifications.size();
    }

    public boolean isEmpty() {
        return notifications.isEmpty();
    }

    /**
     * Returns all notifications as an unmodifiable list (does not remove them).
     */
    public List<Notification> getAllNotifications() {
        return List.copyOf(notifications);
    }

    /**
     * Returns only unread notifications.
     */
    public List<Notification> getUnreadNotifications() {
        return notifications.stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    /**
     * Returns notifications filtered by type.
     */
    public List<Notification> getByType(NotificationType type) {
        return notifications.stream()
                .filter(n -> n.type() == type)
                .collect(Collectors.toList());
    }

    /**
     * Removes all read notifications from the queue. Returns the count removed.
     */
    public int clearReadNotifications() {
        int before = notifications.size();
        notifications.removeIf(Notification::isRead);
        return before - notifications.size();
    }

    /**
     * Clears the entire notification queue. Returns the count removed.
     */
    public int clearAll() {
        int count = notifications.size();
        notifications.clear();
        return count;
    }

    // ─── Summary ──────────────────────────────────────────────────────

    public String getSummary() {
        long unread = unreadCount();
        return """
                ╔══════════════════════════════════════════════════╗
                ║           NOTIFICATION INBOX                    ║
                ╠══════════════════════════════════════════════════╣
                ║  Customer ID   : %s
                ║  Total         : %d notification(s)
                ║  Unread        : %d
                ║  Read          : %d
                ╚══════════════════════════════════════════════════╝
                """.formatted(customerId, notifications.size(), unread, notifications.size() - unread);
    }

    // ─── Getters ──────────────────────────────────────────────────────

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public String toString() {
        return "NotificationQueue[customer=%s, total=%d, unread=%d]"
                .formatted(customerId, notifications.size(), unreadCount());
    }
}
