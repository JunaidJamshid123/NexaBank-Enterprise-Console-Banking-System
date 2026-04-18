package util;

import java.util.UUID;

/**
 * Non-instantiable ID generator — all methods are static.
 *
 * Uses UUID.randomUUID() to produce unique, prefix-tagged identifiers.
 * The private constructor throws UnsupportedOperationException to prevent
 * instantiation via reflection.
 */
public class IdGenerator {

    // ─── Private Constructor — prevents instantiation ─────────────────
    private IdGenerator() {
        throw new UnsupportedOperationException("IdGenerator is a static utility class and cannot be instantiated.");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Core generator — prefix + first 8 chars of UUID (uppercase)
    // ═══════════════════════════════════════════════════════════════════
    public static String generateId(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + "-" + uuid;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Static factory methods — each delegates to generateId()
    // ═══════════════════════════════════════════════════════════════════
    public static String generateAccountId()     { return generateId("ACC"); }
    public static String generateCustomerId()    { return generateId("CUS"); }
    public static String generateLoanId()        { return generateId("LOAN"); }
    public static String generateTransactionId() { return generateId("TXN"); }
    public static String generateAuditId()       { return generateId("AUD"); }

    // ═══════════════════════════════════════════════════════════════════
    //  nextId — integer ID for User subclasses (backward compatible)
    // ═══════════════════════════════════════════════════════════════════
    private static final java.util.concurrent.atomic.AtomicInteger counter =
            new java.util.concurrent.atomic.AtomicInteger(1000);

    public static int nextId() {
        return counter.incrementAndGet();
    }
}
