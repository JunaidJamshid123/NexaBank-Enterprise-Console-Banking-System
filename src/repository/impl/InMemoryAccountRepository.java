package repository.impl;

import model.account.Account;
import repository.Repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * In-memory account repository backed by LinkedHashMap.
 *
 * LinkedHashMap<String, Account> — preserves insertion order for display,
 * while still providing O(1) average get/put like HashMap.
 *
 * ══════════════════════════════════════════════════════════════════════════
 * COLLECTIONS INTERNALS — HashMap / LinkedHashMap / TreeMap
 * ══════════════════════════════════════════════════════════════════════════
 *
 * WHY HashMap IS O(1) AVERAGE FOR get/put:
 * ──────────────────────────────────────────
 * HashMap stores entries in an array of "buckets" (Node[]).
 * The bucket index is determined by: index = hash(key) & (capacity - 1).
 * This bitwise AND operation is O(1). Once the bucket is found, the entry
 * is directly accessible — hence O(1) average time for get() and put().
 *
 * HASH COLLISIONS — CHAINING IN JAVA 8+:
 * ──────────────────────────────────────────
 * When two keys produce the same bucket index (hash collision), HashMap
 * chains entries in a linked list within that bucket. In Java 8+, when
 * the number of entries in a single bucket reaches a TREEIFY_THRESHOLD
 * of 8, the linked list is converted to a balanced Red-Black Tree
 * (TreeNode). This degrades worst-case lookup from O(n) to O(log n).
 * When the count drops below UNTREEIFY_THRESHOLD (6), it converts back
 * to a linked list.
 *
 * WHY String IS AN EXCELLENT KEY TYPE:
 * ──────────────────────────────────────────
 * 1. IMMUTABLE — String's hashCode is computed once and cached internally.
 *    Since String cannot change after creation, its hashCode remains stable
 *    across the lifetime of the HashMap, preventing "lost" entries.
 * 2. WELL-DISTRIBUTED — String.hashCode() uses a polynomial rolling hash
 *    (s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]) which produces a
 *    well-distributed spread across buckets, minimising collisions.
 * 3. EQUALS/HASHCODE CONTRACT — String properly implements both equals()
 *    and hashCode() consistently, satisfying the HashMap contract.
 *
 * WHEN TO PREFER LinkedHashMap OR TreeMap:
 * ──────────────────────────────────────────
 * - LinkedHashMap: When you need insertion-order iteration (e.g. displaying
 *   accounts in the order they were created). It maintains a doubly-linked
 *   list through all entries. Slightly more memory than HashMap.
 * - TreeMap: When you need keys sorted in natural order or by a custom
 *   Comparator (e.g. currency codes sorted alphabetically). Operations
 *   are O(log n) instead of O(1), backed by a Red-Black Tree.
 * ══════════════════════════════════════════════════════════════════════════
 */
public class InMemoryAccountRepository implements Repository<Account, String> {

    private final LinkedHashMap<String, Account> store = new LinkedHashMap<>();

    // HashSet<String> — Set of frozen account IDs for O(1) status check
    private final HashSet<String> frozenAccountIds = new HashSet<>();

    @Override
    public void save(Account entity) {
        store.put(entity.getAccountId(), entity);
    }

    @Override
    public Optional<Account> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Account> findAll(Predicate<Account> filter) {
        return store.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
        frozenAccountIds.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return store.containsKey(id);
    }

    @Override
    public long count() {
        return store.size();
    }

    // ─── Frozen Account Tracking (HashSet — O(1) contains check) ─────

    public void markFrozen(String accountId) {
        frozenAccountIds.add(accountId);
    }

    public void markUnfrozen(String accountId) {
        frozenAccountIds.remove(accountId);
    }

    public boolean isFrozen(String accountId) {
        return frozenAccountIds.contains(accountId);
    }

    public Set<String> getFrozenAccountIds() {
        return Collections.unmodifiableSet(frozenAccountIds);
    }

    // ─── Query by Owner ───────────────────────────────────────────────

    public List<Account> findByOwnerId(String ownerId) {
        return store.values().stream()
                .filter(a -> a.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }
}
