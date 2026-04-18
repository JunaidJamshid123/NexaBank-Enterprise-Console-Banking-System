# 10 — Generics (In Depth)

---

## What Are Generics?

Generics let you write **type-safe, reusable** code. Instead of hardcoding a specific type, you use a **type parameter** (placeholder) that gets replaced with a real type at compile time.

**Without Generics (pre-Java 5):**
```java
List list = new ArrayList();
list.add("Hello");
list.add(42);               // no error — anything goes
String s = (String) list.get(1);  // RUNTIME CRASH — ClassCastException
```

**With Generics:**
```java
List<String> list = new ArrayList<>();
list.add("Hello");
// list.add(42);            // COMPILE ERROR — caught early
String s = list.get(0);     // no cast needed
```

```
╔═══════════════════════════════════════════════════════════════╗
║                  WHY GENERICS?                                ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  1. TYPE SAFETY — errors caught at compile time, not runtime  ║
║  2. NO CASTING — compiler knows the type automatically        ║
║  3. REUSABILITY — one class/method works for any type         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

## Naming Conventions

```
  T  — Type (general)
  E  — Element (collections)
  K  — Key (maps)
  V  — Value (maps)
  N  — Number
  R  — Return type
```

---

## 1. Generic Classes

```java
// A box that can hold ANY type — decided when you create it
class Box<T> {
    private T item;

    public void put(T item)  { this.item = item; }
    public T get()            { return item; }

    @Override
    public String toString() { return "Box[" + item + "]"; }
}

// Usage
Box<String> stringBox = new Box<>();
stringBox.put("Hello");
String s = stringBox.get();    // no cast

Box<Integer> intBox = new Box<>();
intBox.put(42);
int n = intBox.get();          // auto-unboxing

Box<Double> doubleBox = new Box<>();
doubleBox.put(3.14);
```

```
  WHAT HAPPENS AT COMPILE TIME:

  Box<String>             Box<Integer>
  ┌──────────┐            ┌──────────┐
  │ T=String │            │ T=Integer│
  │          │            │          │
  │ put(String)           │ put(Integer)
  │ get():String          │ get():Integer
  └──────────┘            └──────────┘

  The compiler replaces T with the actual type and
  enforces type safety. At runtime, generics are
  ERASED (type erasure) — the JVM sees raw Object.
```

### Multiple Type Parameters

```java
class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey()   { return key; }
    public V getValue() { return value; }

    @Override
    public String toString() { return key + " = " + value; }
}

// Usage
Pair<String, Integer> age = new Pair<>("Alice", 25);
Pair<Integer, Boolean> check = new Pair<>(1, true);
Pair<String, List<String>> data = new Pair<>("fruits", List.of("apple", "banana"));
```

---

## 2. Generic Methods

A method can have its **own** type parameter, independent of the class.

```java
class Util {

    // Generic method — <T> before return type
    public static <T> void printArray(T[] array) {
        for (T item : array) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    // Generic method returning a value
    public static <T> T getFirst(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    // Multiple type parameters
    public static <K, V> String formatEntry(K key, V value) {
        return key + " → " + value;
    }
}

// Usage — type is INFERRED from arguments
Util.printArray(new String[]{"A", "B", "C"});   // A B C
Util.printArray(new Integer[]{1, 2, 3});         // 1 2 3

String first = Util.getFirst(List.of("x", "y", "z"));  // "x"
String entry = Util.formatEntry("name", "Alice");       // name → Alice
```

---

## 3. Generic Interfaces

```java
interface Repository<T> {
    void save(T entity);
    T findById(int id);
    List<T> findAll();
    void delete(int id);
}

// Concrete implementation fixes T to a specific type
class UserRepository implements Repository<User> {
    private List<User> users = new ArrayList<>();

    @Override
    public void save(User entity) { users.add(entity); }

    @Override
    public User findById(int id) {
        return users.stream()
                .filter(u -> u.getId() == id)
                .findFirst().orElse(null);
    }

    @Override
    public List<User> findAll() { return List.copyOf(users); }

    @Override
    public void delete(int id) { users.removeIf(u -> u.getId() == id); }
}
```

---

## 4. Bounded Type Parameters

Restrict what types can be used.

### Upper Bound — `extends`

```java
// T must be Number or a subclass of Number
class MathBox<T extends Number> {
    private T value;

    public MathBox(T value) { this.value = value; }

    public double doubleValue() {
        return value.doubleValue();  // safe — T is guaranteed to be a Number
    }

    public boolean isGreaterThan(MathBox<? extends Number> other) {
        return this.doubleValue() > other.doubleValue();
    }
}

MathBox<Integer> a = new MathBox<>(10);    // OK
MathBox<Double> b = new MathBox<>(3.14);   // OK
// MathBox<String> c = new MathBox<>("hi"); // COMPILE ERROR — String is not a Number
```

### Multiple Bounds

```java
// T must implement BOTH Comparable AND Serializable
class SortableBox<T extends Comparable<T> & Serializable> {
    private T value;

    public SortableBox(T value) { this.value = value; }

    public int compareTo(SortableBox<T> other) {
        return this.value.compareTo(other.value);
    }
}
```

```
  BOUNDS RULES:
  ┌────────────────────────────────────────────────────┐
  │  <T extends ClassName>        — one class bound     │
  │  <T extends Interface>        — one interface bound  │
  │  <T extends Class & Interface>— class FIRST, then & │
  │  <T extends A & B & C>        — multiple interfaces  │
  │                                                     │
  │  NOTE: "extends" is used for both classes AND        │
  │  interfaces in generic bounds (not "implements")     │
  └────────────────────────────────────────────────────┘
```

---

## 5. Wildcards — `?`

Used when you **don't know** or **don't care** about the exact type.

```
╔════════════════════════╦═════════════════════════════════════════════╗
║  Wildcard              ║  Meaning                                    ║
╠════════════════════════╬═════════════════════════════════════════════╣
║  <?>                   ║  Unknown type — can read as Object          ║
║  <? extends Number>    ║  Number or any SUBTYPE — read only (safe)   ║
║  <? super Integer>     ║  Integer or any SUPERTYPE — write safe      ║
╚════════════════════════╩═════════════════════════════════════════════╝
```

### PECS Rule — Producer Extends, Consumer Super

```
  ┌──────────────────────────────────────────────────────────────┐
  │                     P E C S                                   │
  │                                                              │
  │  If you only READ from a collection  → ? extends T           │
  │  If you only WRITE to a collection   → ? super T             │
  │  If you READ and WRITE              → use exact type T       │
  │                                                              │
  │  "Producer Extends, Consumer Super"                          │
  └──────────────────────────────────────────────────────────────┘
```

```java
// ? extends — READING (producer)
public static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number n : numbers) {
        total += n.doubleValue();
    }
    return total;
}

sum(List.of(1, 2, 3));           // List<Integer> — OK
sum(List.of(1.5, 2.5));          // List<Double> — OK
sum(List.of(1L, 2L));            // List<Long> — OK

// ? super — WRITING (consumer)
public static void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
}

List<Number> numList = new ArrayList<>();
addNumbers(numList);              // OK — Number is a super of Integer
List<Object> objList = new ArrayList<>();
addNumbers(objList);              // OK — Object is a super of Integer
```

---

## 6. Type Erasure

At runtime, generic type info is **erased**. The JVM sees raw types.

```
  COMPILE TIME                     RUNTIME (after erasure)
  ─────────────                    ────────────────────────
  Box<String>                      Box
  List<Integer>                    List
  Pair<String, Double>             Pair
  T                                Object
  T extends Number                 Number

  This is why you CANNOT do:
    new T()               ← cannot instantiate type parameter
    new T[10]              ← cannot create generic array
    instanceof List<String>← cannot check generic type at runtime
    static T field         ← static fields cannot use class type param
```

---

## Complete Example — Generic Stack

```java
class GenericStack<T> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    @SuppressWarnings("unchecked")
    public GenericStack() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void push(T item) {
        if (size == elements.length) grow();
        elements[size++] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        T item = (T) elements[--size];
        elements[size] = null;  // help GC
        return item;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new RuntimeException("Stack is empty");
        return (T) elements[size - 1];
    }

    public boolean isEmpty() { return size == 0; }
    public int size()        { return size; }

    private void grow() {
        elements = java.util.Arrays.copyOf(elements, elements.length * 2);
    }
}

// Usage
GenericStack<String> names = new GenericStack<>();
names.push("Alice");
names.push("Bob");
System.out.println(names.pop());   // Bob
System.out.println(names.peek());  // Alice

GenericStack<Integer> nums = new GenericStack<>();
nums.push(10);
nums.push(20);
System.out.println(nums.pop());    // 20
```

---
