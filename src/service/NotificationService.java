package service;

import model.notification.Notification;

import java.util.*;
import java.util.stream.Collectors;

public class NotificationService {

    // Each customer has a Queue<Notification> — backed by a LinkedList
    private final Map<String, Queue<Notification>> customerQueues = new HashMap<>();

    // ═══════════════════════════════════════════════════════════════════
    //  push — adds a notification to the customer's queue
    // ═══════════════════════════════════════════════════════════════════
    public void push(String customerId, Notification notification) {
        Queue<Notification> queue = customerQueues.computeIfAbsent(customerId, k -> new LinkedList<>());
        queue.offer(notification);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  peek — returns Optional<Notification> without removing
    // ═══════════════════════════════════════════════════════════════════
    public Optional<Notification> peek(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(queue.peek());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  readNext — polls the queue, marks notification as read
    // ═══════════════════════════════════════════════════════════════════
    public Optional<Notification> readNext(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }
        Notification notification = queue.poll();
        // Records are immutable — markRead() returns a new copy
        return Optional.of(notification.markRead());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getUnreadCount — stream the queue, count isRead == false
    // ═══════════════════════════════════════════════════════════════════
    public long getUnreadCount(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        if (queue == null) {
            return 0;
        }
        return queue.stream()
                .filter(n -> !n.isRead())
                .count();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getAllNotifications — returns all queued notifications for a customer
    // ═══════════════════════════════════════════════════════════════════
    public List<Notification> getAllNotifications(String customerId) {
        Queue<Notification> queue = customerQueues.get(customerId);
        if (queue == null) {
            return List.of();
        }
        return List.copyOf(queue);
    }
}
