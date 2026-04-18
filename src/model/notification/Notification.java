package model.notification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable notification record.
 *
 * Notifications are modelled as records because they are pure data carriers —
 * once created, their content never changes. The only "state" a notification
 * has (isRead) is set at creation time; marking as read produces a NEW record
 * via {@link #markRead()}.
 *
 * @param notificationId unique identifier (auto-generated)
 * @param recipientId    customer ID this notification belongs to
 * @param title          short headline
 * @param message        detailed body text
 * @param type           category of the notification
 * @param isRead         whether the customer has seen this notification
 * @param createdAt      timestamp of creation
 */
public record Notification(
        String notificationId,
        String recipientId,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        LocalDateTime createdAt
) {

    // ─── Compact Constructor — Validation ─────────────────────────────

    public Notification {
        if (notificationId == null || notificationId.isBlank()) {
            throw new IllegalArgumentException("Notification ID cannot be null or blank");
        }
        if (recipientId == null || recipientId.isBlank()) {
            throw new IllegalArgumentException("Recipient ID cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Notification title cannot be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ─── Factory Methods ──────────────────────────────────────────────

    /**
     * Creates a new unread notification with an auto-generated ID and current timestamp.
     */
    public static Notification create(String recipientId,
                                      String title,
                                      String message,
                                      NotificationType type) {
        return new Notification(
                generateId(),
                recipientId,
                title,
                message,
                type,
                false,
                LocalDateTime.now()
        );
    }

    /**
     * Convenience factory for TRANSACTION notifications.
     */
    public static Notification transaction(String recipientId, String title, String message) {
        return create(recipientId, title, message, NotificationType.TRANSACTION);
    }

    /**
     * Convenience factory for ALERT notifications.
     */
    public static Notification alert(String recipientId, String title, String message) {
        return create(recipientId, title, message, NotificationType.ALERT);
    }

    /**
     * Convenience factory for SYSTEM notifications.
     */
    public static Notification system(String recipientId, String title, String message) {
        return create(recipientId, title, message, NotificationType.SYSTEM);
    }

    /**
     * Convenience factory for PROMOTION notifications.
     */
    public static Notification promotion(String recipientId, String title, String message) {
        return create(recipientId, title, message, NotificationType.PROMOTION);
    }

    // ─── Immutable State Transitions ──────────────────────────────────

    /**
     * Returns a new Notification record identical to this one but marked as read.
     * Records are immutable — we return a copy instead of mutating.
     */
    public Notification markRead() {
        return new Notification(notificationId, recipientId, title, message, type, true, createdAt);
    }

    // ─── ID Generation ────────────────────────────────────────────────

    private static String generateId() {
        return "NTF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    // ─── Display ──────────────────────────────────────────────────────

    public String toDisplayString() {
        String readMarker = isRead ? "  " : "● ";
        return "%s[%s] %s — %s  (%s)".formatted(readMarker, type, title, message, createdAt);
    }
}
