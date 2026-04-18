# 11 — Collections Framework: List, Map, Set, Queue

## What is the Collections Framework?

The **Java Collections Framework (JCF)** is a unified architecture for representing and manipulating groups of objects. It provides:

- **Interfaces** — abstract data types (List, Set, Map, Queue)
- **Implementations** — concrete classes (ArrayList, HashSet, HashMap, LinkedList)
- **Algorithms** — static methods for sorting, searching, shuffling (via `Collections` utility class)

> **Why not just use arrays?**
> Arrays are fixed-size — you must know the length at compile time. Collections are dynamic, resizable, and provide powerful operations out of the box.

---

## Collections Hierarchy Diagram

```
                          Iterable<E>
                              │
                        Collection<E>
                       ┌──────┼──────────────┐
                       │      │              │
                    List<E>  Set<E>       Queue<E>
                       │      │              │
               ┌───────┤   ┌──┴───┐    ┌─────┴──────┐
               │       │   │      │    │             │
          ArrayList  LinkedList  HashSet  TreeSet  PriorityQueue  ArrayDeque
                              │
                         LinkedHashSet


                       Map<K,V>  (separate hierarchy — NOT Collection)
                       ┌───┼────────┐
                       │   │        │
                  HashMap TreeMap LinkedHashMap
                       │
                  Hashtable (legacy)
```

---

## 1. List — Ordered, Allows Duplicates

A **List** is an ordered collection (sequence). Elements can be accessed by their integer index. Duplicates are allowed.

### Key Characteristics

| Feature           | Description                          |
|-------------------|--------------------------------------|
| Ordered           | Elements maintain insertion order    |
| Indexed           | Access by position (0-based)         |
| Duplicates        | Allowed                             |
| Null elements     | Allowed (implementation-dependent)   |

### Common Implementations

| Class        | Backed By        | Best For                        |
|-------------|------------------|---------------------------------|
| ArrayList   | Dynamic array    | Random access, iteration        |
| LinkedList  | Doubly-linked list| Frequent insertion/deletion     |
| Vector      | Dynamic array    | Thread-safe (legacy, use ArrayList)|

### Code Example

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListExample {
    public static void main(String[] args) {

        // ─── ArrayList ────────────────────────────────
        List<String> customers = new ArrayList<>();
        customers.add("Ali Khan");
        customers.add("Sara Ahmed");
        customers.add("Ali Khan");          // duplicates OK
        customers.add(1, "Hassan Raza");    // insert at index 1

        System.out.println("Customers: " + customers);
        // [Ali Khan, Hassan Raza, Sara Ahmed, Ali Khan]

        // Access by index
        String first = customers.get(0);    // "Ali Khan"
        System.out.println("First: " + first);

        // Update
        customers.set(2, "Fatima Noor");
        System.out.println("After update: " + customers);

        // Remove
        customers.remove("Ali Khan");       // removes FIRST occurrence
        customers.remove(0);                // removes by index
        System.out.println("After removal: " + customers);

        // Size
        System.out.println("Size: " + customers.size());

        // Iterate
        System.out.println("\n--- Iterating ---");
        for (String name : customers) {
            System.out.println(name);
        }

        // ─── LinkedList ──────────────────────────────
        LinkedList<String> transactions = new LinkedList<>();
        transactions.addFirst("TXN-001");
        transactions.addLast("TXN-002");
        transactions.add("TXN-003");        // adds to end

        System.out.println("\nTransactions: " + transactions);
        System.out.println("First TXN: " + transactions.getFirst());
        System.out.println("Last TXN: " + transactions.getLast());

        transactions.removeFirst();
        System.out.println("After removeFirst: " + transactions);
    }
}
```

### Output

```
Customers: [Ali Khan, Hassan Raza, Sara Ahmed, Ali Khan]
First: Ali Khan
After update: [Ali Khan, Hassan Raza, Fatima Noor, Ali Khan]
After removal: [Fatima Noor, Ali Khan]
Size: 2

--- Iterating ---
Fatima Noor
Ali Khan

Transactions: [TXN-001, TXN-002, TXN-003]
First TXN: TXN-001
Last TXN: TXN-002
After removeFirst: [TXN-002, TXN-003]
```

---

## 2. Set — No Duplicates

A **Set** is a collection that **cannot contain duplicate elements**. It models the mathematical set abstraction.

### Key Characteristics

| Feature           | Description                              |
|-------------------|------------------------------------------|
| No duplicates     | Automatically rejects duplicates         |
| HashSet           | No order guarantee                       |
| LinkedHashSet     | Maintains insertion order                |
| TreeSet           | Sorted (natural order or Comparator)     |

### Diagram — How Duplicates Are Rejected

```
  add("Ali")   add("Sara")   add("Ali")
      │             │             │
      ▼             ▼             ▼
  ┌────────────────────────────────────┐
  │           HashSet                  │
  │  ┌───────┐  ┌────────┐            │
  │  │ "Ali" │  │ "Sara" │  ✗ "Ali"   │  ← duplicate rejected
  │  └───────┘  └────────┘            │
  └────────────────────────────────────┘
```

### Code Example

```java
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Set;

public class SetExample {
    public static void main(String[] args) {

        // ─── HashSet (no order guarantee) ─────────────
        Set<String> accountNumbers = new HashSet<>();
        accountNumbers.add("ACC-1001");
        accountNumbers.add("ACC-1002");
        accountNumbers.add("ACC-1003");
        accountNumbers.add("ACC-1001");     // duplicate — ignored!

        System.out.println("HashSet: " + accountNumbers);
        System.out.println("Size: " + accountNumbers.size());     // 3, not 4
        System.out.println("Contains ACC-1002? " + accountNumbers.contains("ACC-1002"));

        // ─── LinkedHashSet (insertion order) ──────────
        Set<String> orderedSet = new LinkedHashSet<>();
        orderedSet.add("Checking");
        orderedSet.add("Savings");
        orderedSet.add("Fixed Deposit");
        orderedSet.add("Savings");          // duplicate — ignored

        System.out.println("\nLinkedHashSet: " + orderedSet);
        // [Checking, Savings, Fixed Deposit]  ← insertion order preserved

        // ─── TreeSet (sorted) ─────────────────────────
        Set<Integer> balances = new TreeSet<>();
        balances.add(5000);
        balances.add(1200);
        balances.add(8900);
        balances.add(3400);

        System.out.println("\nTreeSet (sorted): " + balances);
        // [1200, 3400, 5000, 8900]  ← natural order

        // ─── Set Operations (Union, Intersection, Difference) ───
        Set<String> setA = new HashSet<>(Set.of("Ali", "Sara", "Hassan"));
        Set<String> setB = new HashSet<>(Set.of("Sara", "Fatima", "Omar"));

        // Union
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("\nUnion: " + union);

        // Intersection
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        System.out.println("Intersection: " + intersection);

        // Difference (A - B)
        Set<String> difference = new HashSet<>(setA);
        difference.removeAll(setB);
        System.out.println("Difference (A-B): " + difference);
    }
}
```

### Output

```
HashSet: [ACC-1003, ACC-1001, ACC-1002]
Size: 3
Contains ACC-1002? true

LinkedHashSet: [Checking, Savings, Fixed Deposit]

TreeSet (sorted): [1200, 3400, 5000, 8900]

Union: [Ali, Sara, Hassan, Fatima, Omar]
Intersection: [Sara]
Difference (A-B): [Ali, Hassan]
```

---

## 3. Map — Key-Value Pairs

A **Map** stores data as **key-value pairs**. Each key is unique; values can be duplicated.

> **Important:** Map does NOT extend `Collection`. It is a separate interface.

### Key Characteristics

| Feature           | Description                              |
|-------------------|------------------------------------------|
| Key-Value pairs   | Each entry is a (key, value) pair        |
| Unique keys       | Duplicate keys overwrite previous value  |
| HashMap           | No order guarantee, allows one null key  |
| LinkedHashMap     | Maintains insertion order                |
| TreeMap           | Keys sorted in natural/Comparator order  |

### Diagram — HashMap Structure

```
    Key          Value
  ┌──────────┬────────────────┐
  │ "ACC-01" │ "Ali Khan"     │
  ├──────────┼────────────────┤
  │ "ACC-02" │ "Sara Ahmed"   │
  ├──────────┼────────────────┤
  │ "ACC-03" │ "Hassan Raza"  │
  └──────────┴────────────────┘
       ↑ unique        ↑ can repeat
```

### Code Example

```java
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Map;

public class MapExample {
    public static void main(String[] args) {

        // ─── HashMap ─────────────────────────────────
        Map<String, Double> accountBalances = new HashMap<>();
        accountBalances.put("ACC-1001", 25000.00);
        accountBalances.put("ACC-1002", 18500.50);
        accountBalances.put("ACC-1003", 42000.75);
        accountBalances.put("ACC-1001", 30000.00);  // overwrites previous value!

        System.out.println("Balances: " + accountBalances);
        System.out.println("ACC-1002 balance: " + accountBalances.get("ACC-1002"));
        System.out.println("Contains ACC-1003? " + accountBalances.containsKey("ACC-1003"));
        System.out.println("Contains value 18500.50? " + accountBalances.containsValue(18500.50));

        // Remove
        accountBalances.remove("ACC-1003");
        System.out.println("After removal: " + accountBalances);

        // getOrDefault — avoid NullPointerException
        double balance = accountBalances.getOrDefault("ACC-9999", 0.0);
        System.out.println("Unknown account balance: " + balance);

        // putIfAbsent — only adds if key is not already present
        accountBalances.putIfAbsent("ACC-1001", 99999.0);  // won't overwrite
        System.out.println("ACC-1001 still: " + accountBalances.get("ACC-1001"));

        // ─── Iterating a Map ─────────────────────────
        System.out.println("\n--- Iterating Map ---");

        // Method 1: entrySet
        for (Map.Entry<String, Double> entry : accountBalances.entrySet()) {
            System.out.println(entry.getKey() + " → " + entry.getValue());
        }

        // Method 2: keySet
        for (String key : accountBalances.keySet()) {
            System.out.println("Key: " + key);
        }

        // Method 3: values
        for (Double value : accountBalances.values()) {
            System.out.println("Value: " + value);
        }

        // Method 4: forEach (lambda)
        accountBalances.forEach((key, value) ->
            System.out.println(key + " = " + value)
        );

        // ─── TreeMap (sorted by key) ─────────────────
        Map<String, String> sortedCustomers = new TreeMap<>();
        sortedCustomers.put("C003", "Hassan");
        sortedCustomers.put("C001", "Ali");
        sortedCustomers.put("C002", "Sara");

        System.out.println("\nTreeMap (sorted): " + sortedCustomers);
        // {C001=Ali, C002=Sara, C003=Hassan}
    }
}
```

### Output

```
Balances: {ACC-1003=42000.75, ACC-1001=30000.00, ACC-1002=18500.50}
ACC-1002 balance: 18500.5
Contains ACC-1003? true
Contains value 18500.50? true
After removal: {ACC-1001=30000.00, ACC-1002=18500.50}
Unknown account balance: 0.0
ACC-1001 still: 30000.0

--- Iterating Map ---
ACC-1001 → 30000.0
ACC-1002 → 18500.5
Key: ACC-1001
Key: ACC-1002
Value: 30000.0
Value: 18500.5
ACC-1001 = 30000.0
ACC-1002 = 18500.5

TreeMap (sorted): {C001=Ali, C002=Sara, C003=Hassan}
```

---

## 4. Queue — FIFO (First In, First Out)

A **Queue** is a collection designed for holding elements prior to processing. It follows **FIFO** order.

### Key Characteristics

| Feature           | Description                              |
|-------------------|------------------------------------------|
| FIFO order        | First element added = first removed      |
| PriorityQueue     | Elements ordered by priority (natural/Comparator) |
| ArrayDeque        | Resizable array-based double-ended queue |
| LinkedList        | Also implements Queue                    |

### Diagram — Queue Operations

```
  enqueue (offer/add)                    dequeue (poll/remove)
       │                                        │
       ▼                                        ▼
  ┌─────────────────────────────────────────────────┐
  │  REAR ←── [D] [C] [B] [A] ──→ FRONT            │
  └─────────────────────────────────────────────────┘
       ↑ New elements enter here    ↑ Elements exit here
```

### Queue Methods

| Operation     | Throws Exception | Returns null/false |
|--------------|------------------|--------------------|
| Insert       | `add(e)`         | `offer(e)`         |
| Remove       | `remove()`       | `poll()`           |
| Examine      | `element()`      | `peek()`           |

> **Best Practice:** Use `offer()`, `poll()`, and `peek()` — they return `null`/`false` instead of throwing exceptions on empty queues.

### Code Example

```java
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Deque;

public class QueueExample {
    public static void main(String[] args) {

        // ─── Queue (LinkedList implementation) ───────
        Queue<String> customerQueue = new LinkedList<>();
        customerQueue.offer("Ali Khan");        // join queue
        customerQueue.offer("Sara Ahmed");
        customerQueue.offer("Hassan Raza");
        customerQueue.offer("Fatima Noor");

        System.out.println("Queue: " + customerQueue);
        System.out.println("Front (peek): " + customerQueue.peek());   // doesn't remove
        System.out.println("Served (poll): " + customerQueue.poll());  // removes front

        System.out.println("After serving: " + customerQueue);
        System.out.println("Queue size: " + customerQueue.size());

        // ─── PriorityQueue (min-heap by default) ─────
        System.out.println("\n--- PriorityQueue ---");
        Queue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(50);
        priorityQueue.offer(10);
        priorityQueue.offer(30);
        priorityQueue.offer(20);

        System.out.println("PriorityQueue: " + priorityQueue);
        // Note: toString() may not show sorted order

        // But poll() always returns the smallest
        while (!priorityQueue.isEmpty()) {
            System.out.println("Poll: " + priorityQueue.poll());
        }
        // Poll: 10, 20, 30, 50  ← sorted!

        // ─── Deque (Double-Ended Queue) ──────────────
        System.out.println("\n--- ArrayDeque as Stack (LIFO) ---");
        Deque<String> stack = new ArrayDeque<>();
        stack.push("Page 1");       // push = addFirst
        stack.push("Page 2");
        stack.push("Page 3");

        System.out.println("Stack: " + stack);
        System.out.println("Pop: " + stack.pop());     // removes top
        System.out.println("Peek: " + stack.peek());   // examines top
        System.out.println("Stack after: " + stack);
    }
}
```

### Output

```
Queue: [Ali Khan, Sara Ahmed, Hassan Raza, Fatima Noor]
Front (peek): Ali Khan
Served (poll): Ali Khan
After serving: [Sara Ahmed, Hassan Raza, Fatima Noor]
Queue size: 3

--- PriorityQueue ---
PriorityQueue: [10, 20, 30, 50]
Poll: 10
Poll: 20
Poll: 30
Poll: 50

--- ArrayDeque as Stack (LIFO) ---
Stack: [Page 3, Page 2, Page 1]
Pop: Page 3
Peek: Page 2
Stack after: [Page 2, Page 1]
```

---

## Quick Comparison — When to Use What?

| Collection    | Ordered? | Duplicates? | Null? | Thread-Safe? | Use When...                         |
|--------------|----------|-------------|-------|-------------|--------------------------------------|
| ArrayList    | ✅ Yes   | ✅ Yes      | ✅    | ❌          | Random access, most common list      |
| LinkedList   | ✅ Yes   | ✅ Yes      | ✅    | ❌          | Frequent add/remove at ends          |
| HashSet      | ❌ No    | ❌ No       | ✅(1) | ❌          | Fast lookup, no duplicates           |
| LinkedHashSet| ✅ Yes   | ❌ No       | ✅(1) | ❌          | Insertion order + no duplicates      |
| TreeSet      | ✅ Sorted| ❌ No       | ❌    | ❌          | Sorted unique elements               |
| HashMap      | ❌ No    | Keys ❌     | ✅(1) | ❌          | Fast key-value lookup                |
| LinkedHashMap| ✅ Yes   | Keys ❌     | ✅(1) | ❌          | Ordered key-value pairs              |
| TreeMap      | ✅ Sorted| Keys ❌     | ❌    | ❌          | Sorted key-value pairs               |
| PriorityQueue| Priority | ✅ Yes      | ❌    | ❌          | Processing by priority               |
| ArrayDeque   | ✅ Yes   | ✅ Yes      | ❌    | ❌          | Stack or Queue operations            |

---

## Collections Utility Class — Useful Methods

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionsUtilExample {
    public static void main(String[] args) {
        List<Integer> numbers = new ArrayList<>(List.of(5, 2, 8, 1, 9, 3));

        Collections.sort(numbers);               // [1, 2, 3, 5, 8, 9]
        Collections.reverse(numbers);             // [9, 8, 5, 3, 2, 1]
        Collections.shuffle(numbers);             // random order
        int max = Collections.max(numbers);       // 9
        int min = Collections.min(numbers);       // 1
        int freq = Collections.frequency(numbers, 5); // count of 5

        // Unmodifiable (immutable) list
        List<String> immutable = Collections.unmodifiableList(
            List.of("A", "B", "C")
        );
        // immutable.add("D");  // throws UnsupportedOperationException

        // Synchronized (thread-safe) list
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
    }
}
```

---

## Summary

```
┌──────────────────────────────────────────────────────────┐
│                  COLLECTIONS FRAMEWORK                   │
├────────────┬─────────────────────────────────────────────┤
│  List      │  Ordered + Indexed + Duplicates allowed     │
│  Set       │  No duplicates + Fast lookup                │
│  Map       │  Key-Value pairs + Unique keys              │
│  Queue     │  FIFO processing + Priority ordering        │
├────────────┴─────────────────────────────────────────────┤
│  Use ArrayList for most cases                            │
│  Use HashSet when you need uniqueness                    │
│  Use HashMap for key→value lookups                       │
│  Use PriorityQueue when order matters by priority        │
└──────────────────────────────────────────────────────────┘
```
