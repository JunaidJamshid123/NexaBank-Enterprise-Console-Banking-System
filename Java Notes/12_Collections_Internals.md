# 12 — Collections Internals: ArrayList vs LinkedList, HashMap Internals

## Why Study Internals?

Understanding **how** collections work internally helps you:
- Choose the right collection for your use case
- Write performance-optimized code
- Answer interview questions confidently
- Debug memory and performance issues

---

## 1. ArrayList Internals

### What is ArrayList?

`ArrayList` is backed by a **resizable array** (dynamic array). It stores elements in a contiguous block of memory.

### Internal Structure

```java
// Simplified internal representation
public class ArrayList<E> {
    private Object[] elementData;   // the backing array
    private int size;               // number of actual elements
}
```

### How It Works — Step by Step

#### Initial State (default capacity = 10)

```
  elementData (Object[10]):
  ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
  │null │null │null │null │null │null │null │null │null │null │
  └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
   [0]   [1]   [2]   [3]   [4]   [5]   [6]   [7]   [8]   [9]

  size = 0
```

#### After Adding Elements: add("Ali"), add("Sara"), add("Hassan")

```
  elementData (Object[10]):
  ┌───────┬────────┬─────────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
  │ "Ali" │ "Sara" │ "Hassan"│null │null │null │null │null │null │null │
  └───────┴────────┴─────────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
   [0]     [1]      [2]       [3]   [4]   [5]   [6]   [7]   [8]   [9]

  size = 3
```

#### When Array is Full — Automatic Resizing (Grow)

```
  BEFORE (capacity = 10, size = 10 — FULL!):
  ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
  │  A  │  B  │  C  │  D  │  E  │  F  │  G  │  H  │  I  │  J  │
  └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘

  add("K")  →  Array is full! Need to GROW.

  Step 1: Create new array with capacity = oldCapacity + (oldCapacity >> 1)
          New capacity = 10 + 5 = 15  (grows by 50%)

  Step 2: Copy all old elements to new array (Arrays.copyOf)

  Step 3: Add new element

  AFTER (capacity = 15, size = 11):
  ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐
  │  A  │  B  │  C  │  D  │  E  │  F  │  G  │  H  │  I  │  J  │  K  │null │null │null │null │
  └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘
```

> **Growth Formula:** `newCapacity = oldCapacity + (oldCapacity >> 1)` → **1.5x growth**

#### Inserting in the Middle: add(2, "New")

```
  BEFORE:
  ┌───────┬────────┬─────────┬─────────┬─────┐
  │ "Ali" │ "Sara" │ "Hassan"│ "Fatima"│null │
  └───────┴────────┴─────────┴─────────┴─────┘
   [0]     [1]      [2]       [3]       [4]

  add(2, "New")  →  Must SHIFT elements right

  Step 1: Shift elements [2..3] → [3..4]
  Step 2: Place "New" at index 2

  AFTER:
  ┌───────┬────────┬───────┬─────────┬─────────┐
  │ "Ali" │ "Sara" │ "New" │ "Hassan"│ "Fatima"│
  └───────┴────────┴───────┴─────────┴─────────┘
   [0]     [1]      [2]     [3]       [4]
```

> **This is why add(index, element) is O(n)** — it must shift all subsequent elements.

---

## 2. LinkedList Internals

### What is LinkedList?

`LinkedList` is a **doubly-linked list**. Each element is wrapped in a `Node` that contains pointers to the previous and next nodes.

### Internal Structure

```java
// Simplified internal representation
public class LinkedList<E> {
    private Node<E> first;   // pointer to first node
    private Node<E> last;    // pointer to last node
    private int size;

    private static class Node<E> {
        E item;              // the actual data
        Node<E> prev;        // pointer to previous node
        Node<E> next;        // pointer to next node
    }
}
```

### Memory Layout Diagram

```
  first                                                        last
    │                                                           │
    ▼                                                           ▼
  ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
  │ prev: null       │     │ prev: ──────────┐│     │ prev: ──────────┐│
  │ item: "Ali"      │◄────│ item: "Sara"    ││     │ item: "Hassan"  ││
  │ next: ──────────►│     │ next: ──────────►│◄────│ next: null      ││
  └──────────────────┘     └──────────────────┘     └──────────────────┘
       Node 0                   Node 1                   Node 2
```

### Adding to the Front: addFirst("New")

```
  BEFORE:
  first ──► [Ali] ⇄ [Sara] ⇄ [Hassan] ◄── last

  addFirst("New"):
  1. Create new Node("New")
  2. new.next = old first ("Ali")
  3. old first.prev = new
  4. first = new

  AFTER:
  first ──► [New] ⇄ [Ali] ⇄ [Sara] ⇄ [Hassan] ◄── last

  Time: O(1) — No shifting needed!
```

### Removing from the Middle: remove("Sara")

```
  BEFORE:
  [Ali] ⇄ [Sara] ⇄ [Hassan]

  remove("Sara"):
  1. Find "Sara" node  (O(n) traversal)
  2. Ali.next = Hassan
  3. Hassan.prev = Ali
  4. Detach Sara node

  AFTER:
  [Ali] ⇄ [Hassan]

  Time: O(n) for finding, O(1) for unlinking
```

---

## 3. ArrayList vs LinkedList — Full Comparison

### Time Complexity Comparison

| Operation                | ArrayList    | LinkedList   | Winner        |
|--------------------------|-------------|-------------|---------------|
| `get(index)`             | **O(1)**    | O(n)        | ArrayList     |
| `add(element)` at end    | **O(1)***   | **O(1)**    | Tie           |
| `add(0, element)` front  | O(n)        | **O(1)**    | LinkedList    |
| `add(index, element)` mid| O(n)        | O(n)        | Tie**         |
| `remove(index)`          | O(n)        | O(n)        | Tie**         |
| `remove(0)` front        | O(n)        | **O(1)**    | LinkedList    |
| `contains(element)`      | O(n)        | O(n)        | Tie           |
| `size()`                 | **O(1)**    | **O(1)**    | Tie           |
| Iterator `next()`        | **O(1)**    | **O(1)**    | Tie           |
| Memory per element       | **Low**     | High        | ArrayList     |

> \* **Amortized O(1)** — occasionally O(n) when resizing is needed.
> \** ArrayList is often faster in practice due to CPU cache locality.

### Memory Usage Comparison

```
  ArrayList — stores only the data (+ some empty slots):
  ┌─────┬─────┬─────┬─────┬─────┬─────┐
  │ obj │ obj │ obj │null │null │null │     ~4-8 bytes per slot
  └─────┴─────┴─────┴─────┴─────┴─────┘

  LinkedList — each element wrapped in a Node with 2 extra pointers:
  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
  │ prev │ data │next│  │ prev │ data │next│  │ prev │ data │next│
  └─────────────────┘  └─────────────────┘  └─────────────────┘
        ~24 bytes per node (prev + data + next + overhead)
```

> **LinkedList uses ~3-4x more memory** per element than ArrayList.

### Code: Performance Comparison

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListPerformanceComparison {
    public static void main(String[] args) {
        int n = 100_000;

        // ─── Test 1: Add to end ──────────────────────
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) arrayList.add(i);
        long arrayEnd = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < n; i++) linkedList.add(i);
        long linkedEnd = System.nanoTime() - start;

        System.out.println("Add to end:");
        System.out.println("  ArrayList:  " + arrayEnd / 1_000_000 + " ms");
        System.out.println("  LinkedList: " + linkedEnd / 1_000_000 + " ms");

        // ─── Test 2: Add to front ────────────────────
        arrayList = new ArrayList<>();
        linkedList = new LinkedList<>();

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) arrayList.add(0, i);
        arrayEnd = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) ((LinkedList<Integer>) linkedList).addFirst(i);
        linkedEnd = System.nanoTime() - start;

        System.out.println("\nAdd to front (10k elements):");
        System.out.println("  ArrayList:  " + arrayEnd / 1_000_000 + " ms");
        System.out.println("  LinkedList: " + linkedEnd / 1_000_000 + " ms");

        // ─── Test 3: Random Access ───────────────────
        arrayList = new ArrayList<>();
        linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) arrayList.get(50_000);
        arrayEnd = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) linkedList.get(50_000);
        linkedEnd = System.nanoTime() - start;

        System.out.println("\nRandom access get(50000) x 10k:");
        System.out.println("  ArrayList:  " + arrayEnd / 1_000_000 + " ms");
        System.out.println("  LinkedList: " + linkedEnd / 1_000_000 + " ms");
    }
}
```

### Typical Output

```
Add to end:
  ArrayList:  3 ms
  LinkedList: 5 ms

Add to front (10k elements):
  ArrayList:  15 ms
  LinkedList: 1 ms

Random access get(50000) x 10k:
  ArrayList:  0 ms
  LinkedList: 320 ms       ← MASSIVE difference!
```

---

## 4. HashMap Internals — Deep Dive

### How HashMap Works

HashMap uses an **array of buckets** (called `table`). Each bucket can hold a **linked list** (or a **red-black tree** when too many collisions occur).

### Internal Structure

```java
// Simplified
public class HashMap<K, V> {
    Node<K,V>[] table;         // array of buckets
    int size;                  // total key-value pairs
    float loadFactor;          // default 0.75
    int threshold;             // capacity * loadFactor → when to resize

    static class Node<K,V> {
        final int hash;        // cached hash
        final K key;
        V value;
        Node<K,V> next;        // linked list pointer (for collisions)
    }
}
```

### Step-by-Step: How put(key, value) Works

```
  put("Ali", 25000)

  Step 1: Calculate hash
          hash = "Ali".hashCode()           → 65921
          index = hash & (capacity - 1)     → 65921 & 15 = 1  (for capacity 16)

  Step 2: Go to bucket[1]

  Step 3: If bucket is empty → create new Node, place it there

  Step 4: If bucket has nodes → compare keys:
          - Same key? → Update value
          - Different key? → Append to linked list (collision)
```

### Visual: HashMap with Buckets and Collisions

```
  table (Node[] of capacity 16):

  Index │ Bucket Content
  ──────┼─────────────────────────────────────────────
   [0]  │ null
   [1]  │ ┌─────────────┐     ┌─────────────┐
        │ │ "Ali"=25000 │────►│ "Zaid"=5000 │────► null
        │ └─────────────┘     └─────────────┘
        │     (collision! Same bucket index)
   [2]  │ null
   [3]  │ ┌──────────────┐
        │ │ "Sara"=18500 │────► null
        │ └──────────────┘
   [4]  │ null
   ...  │ null
   [9]  │ ┌────────────────┐
        │ │ "Hassan"=42000 │────► null
        │ └────────────────┘
   ...  │ null
  [15]  │ null
```

### How get(key) Works

```
  get("Ali")

  Step 1: hash = "Ali".hashCode()  →  65921
  Step 2: index = 65921 & 15  →  1
  Step 3: Go to bucket[1]
  Step 4: Compare: bucket[1] key is "Ali"? YES → return 25000

  get("Zaid")

  Step 1: hash = "Zaid".hashCode()  →  (some value)
  Step 2: index = hash & 15  →  1  (same bucket as Ali — collision!)
  Step 3: Go to bucket[1]
  Step 4: Compare: first node key is "Ali" ≠ "Zaid" → go to next
  Step 5: Compare: next node key is "Zaid"? YES → return 5000
```

### Resizing (Rehashing)

When `size > capacity * loadFactor`, HashMap **doubles** the capacity and **rehashes** all entries.

```
  Default: capacity = 16, loadFactor = 0.75
  Threshold = 16 * 0.75 = 12

  When 13th element is added → RESIZE!

  Step 1: Create new table with capacity = 32
  Step 2: Recalculate index for EVERY entry (hash & 31 instead of hash & 15)
  Step 3: Move entries to new buckets

  ┌──────────────────┐          ┌───────────────────────────────────┐
  │ Old table (16)   │  ─────►  │ New table (32)                    │
  │ 12 entries       │  rehash  │ Same 12 entries, new positions    │
  └──────────────────┘          └───────────────────────────────────┘
```

### Treeification (Java 8+)

When a single bucket has **more than 8 nodes**, the linked list is converted to a **Red-Black Tree** for O(log n) lookups instead of O(n).

```
  Bucket with many collisions:

  BEFORE (linked list, >8 nodes):
  [A] → [B] → [C] → [D] → [E] → [F] → [G] → [H] → [I]
                    Lookup: O(n) — must traverse

  AFTER treeification (Red-Black Tree):
            [E]
           /   \
         [C]   [G]
        / \    / \
      [A] [D] [F] [H]
       \           \
       [B]         [I]
                    Lookup: O(log n) — balanced tree
```

### Code: Demonstrating HashMap Behavior

```java
import java.util.HashMap;
import java.util.Map;

public class HashMapInternalsDemo {
    public static void main(String[] args) {

        // ─── Basic Operations ────────────────────────
        Map<String, Double> accounts = new HashMap<>(16, 0.75f);

        accounts.put("ACC-1001", 25000.0);
        accounts.put("ACC-1002", 18500.0);
        accounts.put("ACC-1003", 42000.0);

        // Observe hashCode and bucket index
        for (String key : accounts.keySet()) {
            int hash = key.hashCode();
            int bucketIndex = hash & 15;   // & (capacity - 1) for capacity 16
            System.out.println("Key: " + key
                + " | hashCode: " + hash
                + " | bucket: " + bucketIndex);
        }

        // ─── Collision Demo: Bad Hash ────────────────
        System.out.println("\n--- Collision Demo ---");
        Map<BadHashKey, String> collisionMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            collisionMap.put(new BadHashKey(i), "Value-" + i);
        }
        System.out.println("All 10 entries in the SAME bucket due to bad hashCode!");
        System.out.println("Size: " + collisionMap.size());
    }
}

// A class with a terrible hashCode — all objects go to the same bucket
class BadHashKey {
    int id;

    BadHashKey(int id) { this.id = id; }

    @Override
    public int hashCode() {
        return 1;   // ALWAYS returns 1 → every entry goes to bucket 1
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return this.id == ((BadHashKey) obj).id;
    }
}
```

---

## 5. hashCode() and equals() Contract

For HashMap to work correctly, you **must** follow this contract:

```
  ┌──────────────────────────────────────────────────────────────┐
  │  RULE 1: If a.equals(b) is true → a.hashCode() == b.hashCode()  │
  │                                                              │
  │  RULE 2: If a.hashCode() == b.hashCode()                    │
  │          → a.equals(b) may or may not be true (collision OK) │
  │                                                              │
  │  RULE 3: If a.equals(b) is false                             │
  │          → hashCodes can be same or different                │
  └──────────────────────────────────────────────────────────────┘
```

### Example: Correct Implementation

```java
public class AccountKey {
    private String accountNumber;
    private String branchCode;

    public AccountKey(String accountNumber, String branchCode) {
        this.accountNumber = accountNumber;
        this.branchCode = branchCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountKey that = (AccountKey) o;
        return accountNumber.equals(that.accountNumber)
            && branchCode.equals(that.branchCode);
    }

    @Override
    public int hashCode() {
        // Use the same fields used in equals()
        int result = accountNumber.hashCode();
        result = 31 * result + branchCode.hashCode();
        return result;
    }
}
```

---

## Summary — Decision Guide

```
  ┌────────────────────────────────────────────────────────────┐
  │           WHEN TO USE WHAT — DECISION FLOWCHART            │
  ├────────────────────────────────────────────────────────────┤
  │                                                            │
  │  Need key-value pairs?                                     │
  │    YES → HashMap (or TreeMap if sorted keys needed)        │
  │    NO  ↓                                                   │
  │                                                            │
  │  Need unique elements only?                                │
  │    YES → HashSet (or TreeSet if sorted needed)             │
  │    NO  ↓                                                   │
  │                                                            │
  │  Need ordered list with random access?                     │
  │    YES → ArrayList  ← DEFAULT CHOICE for most cases        │
  │    NO  ↓                                                   │
  │                                                            │
  │  Need frequent add/remove at head/tail?                    │
  │    YES → LinkedList (or ArrayDeque)                        │
  │    NO  ↓                                                   │
  │                                                            │
  │  Need FIFO processing?                                     │
  │    YES → ArrayDeque (or LinkedList)                        │
  │                                                            │
  │  Need priority-based processing?                           │
  │    YES → PriorityQueue                                     │
  └────────────────────────────────────────────────────────────┘

  GOLDEN RULE: When in doubt, use ArrayList and HashMap.
               They are the best choice 90% of the time.
```
