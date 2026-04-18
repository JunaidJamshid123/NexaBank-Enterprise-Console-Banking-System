package service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AuditService {

    // ─── AuditEntry Generic Record ────────────────────────────────────
    public record AuditEntry<T>(
            String auditId,
            String performedBy,
            String action,
            T entityBefore,
            T entityAfter,
            LocalDateTime timestamp
    ) {}

    // ─── Storage ──────────────────────────────────────────────────────
    private final List<AuditEntry<?>> auditLog = new ArrayList<>();

    /**
     * ArrayDeque<String> — Bounded recent activity log (max 100 entries).
     *
     * ArrayDeque is backed by a resizable circular array. It is more efficient
     * than both LinkedList and Stack for use as a queue/deque:
     *   - O(1) amortised addLast/removeFirst (no node allocations)
     *   - Better cache locality than LinkedList (contiguous memory)
     *   - No synchronization overhead unlike Stack (which extends Vector)
     *
     * We use it here as a bounded FIFO log: when the size exceeds MAX_RECENT,
     * the oldest entry is removed from the front before adding a new one.
     */
    private static final int MAX_RECENT = 100;
    private final ArrayDeque<String> recentActivity = new ArrayDeque<>();

    // Static counter for audit IDs
    private static int auditCounter = 0;

    private static String nextAuditId() {
        return "AUD-%06d".formatted(++auditCounter);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  log (generic) — records a typed audit entry with before/after
    // ═══════════════════════════════════════════════════════════════════
    public <T> void log(String performedBy, String action, T before, T after) {
        AuditEntry<T> entry = new AuditEntry<>(
                nextAuditId(),
                performedBy,
                action,
                before,
                after,
                LocalDateTime.now()
        );
        auditLog.add(entry);

        // Maintain bounded recent activity log (ArrayDeque, max 100)
        if (recentActivity.size() >= MAX_RECENT) {
            recentActivity.pollFirst();  // remove oldest
        }
        recentActivity.offerLast("[%s] %s by %s".formatted(
                entry.timestamp().toLocalTime().withNano(0), action, performedBy));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  log (convenience) — simple string-based log for backward compat
    // ═══════════════════════════════════════════════════════════════════
    public void log(String action) {
        log("SYSTEM", action, (String) null, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getLog — filtered by entity type using wildcard
    // ═══════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    public <T> List<AuditEntry<T>> getLog(Class<T> type) {
        return auditLog.stream()
                .filter(entry -> {
                    // Match if entityBefore or entityAfter is of the requested type
                    if (entry.entityBefore() != null && type.isInstance(entry.entityBefore())) return true;
                    if (entry.entityAfter() != null && type.isInstance(entry.entityAfter())) return true;
                    // For String-based logs, match if type is String
                    return type == String.class
                            && entry.entityBefore() == null
                            && entry.entityAfter() == null;
                })
                .map(entry -> (AuditEntry<T>) entry)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getFullLog — returns all entries (unmodifiable)
    // ═══════════════════════════════════════════════════════════════════
    public List<AuditEntry<?>> getFullLog() {
        return Collections.unmodifiableList(auditLog);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  exportSummary — action → count, using Streams
    // ═══════════════════════════════════════════════════════════════════
    public Map<String, Long> exportSummary() {
        return auditLog.stream()
                .collect(Collectors.groupingBy(
                        AuditEntry::action,
                        Collectors.counting()
                ));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getRecentActivity — returns the bounded ArrayDeque log as a list
    // ═══════════════════════════════════════════════════════════════════
    public List<String> getRecentActivity() {
        return List.copyOf(recentActivity);
    }
}
