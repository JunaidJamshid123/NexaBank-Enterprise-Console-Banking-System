# 07 — Static Methods & Properties (In Depth)

---

## What Does `static` Mean?

The `static` keyword means a member belongs to the **CLASS itself**, not to any individual object. There's only **ONE copy**, shared by all instances.

**Analogy:** In a classroom, each student (object) has their own name and marks (instance variables). But the class name "CS-101" and the teacher's name are shared by ALL students — those are static.

```
  NON-STATIC (instance)                 STATIC (class-level)
  ─────────────────────                 ────────────────────
  Each object gets its OWN copy         ONE copy shared by ALL objects

  Student s1 → name = "Alice"           Student.totalCount = 3
  Student s2 → name = "Bob"             (same value for everyone)
  Student s3 → name = "Charlie"

  3 copies of 'name'                    1 copy of 'totalCount'
```

---

## Memory Model

```
  ┌────────────────────────────────────────────────────────────┐
  │                     JVM MEMORY                             │
  ├────────────────────────────────────────────────────────────┤
  │                                                            │
  │  METHOD AREA (Class Memory) — loaded ONCE when class loads │
  │  ┌──────────────────────────────────────────┐              │
  │  │  Class: Counter                          │              │
  │  │  static int count = 3                    │  ← ONE copy  │
  │  │  static void showCount() { ... }         │              │
  │  └──────────────────────────────────────────┘              │
  │                                                            │
  │  HEAP (Object Memory) — created with 'new'                │
  │  ┌────────────────┐  ┌────────────────┐                   │
  │  │ Counter obj1   │  │ Counter obj2   │                   │
  │  │ name = "A"     │  │ name = "B"     │  ← per object    │
  │  └────────────────┘  └────────────────┘                   │
  │                                                            │
  │  STACK (Method Memory) — during method execution          │
  │  ┌────────────────────┐                                   │
  │  │ main()             │                                   │
  │  │ localVar = 10      │  ← per method call                │
  │  └────────────────────┘                                   │
  │                                                            │
  └────────────────────────────────────────────────────────────┘
```

---
---

# 1. Static Variable (Class Variable)

A variable declared with `static`. Only **ONE copy** exists, shared by all objects of the class.

```
  ┌──────────────────────────────────────┐
  │      CLASS: Employee                 │
  │                                      │
  │  static int totalEmployees = 0;      │  ◄── ONE copy
  │  ┌────── shared by all ──────┐       │
  │  │                           │       │
  └──┼───────────────────────────┼───────┘
     │                           │
  ┌──▼──────────┐        ┌──────▼───────┐
  │ emp1        │        │ emp2         │
  │ name="Alice"│        │ name="Bob"   │  ← own copy
  │ salary=5000 │        │ salary=6000  │  ← own copy
  │ (reads      │        │ (reads       │
  │  totalEmp)  │        │  totalEmp)   │  ← shared
  └─────────────┘        └──────────────┘
```

### When to Use Static Variables

```
┌──────────────────────────────────────────────────────────┐
│ USE STATIC WHEN:                                         │
├──────────────────────────────────────────────────────────┤
│ • Counting total instances (counter)                    │
│ • Constants shared by all (PI, MAX_SIZE)                │
│ • Configuration values (database URL)                   │
│ • Shared resources (connection pool)                    │
│ • Values that don't belong to any single object         │
├──────────────────────────────────────────────────────────┤
│ DO NOT USE STATIC WHEN:                                  │
├──────────────────────────────────────────────────────────┤
│ • Each object needs its own value (name, salary)        │
│ • Value differs between objects                         │
└──────────────────────────────────────────────────────────┘
```

### Code Example

```java
class Employee {
    // Instance variables — each object has its own
    String name;
    double salary;

    // Static variable — one copy for all
    static int totalEmployees = 0;
    static String company = "NexaBank";

    Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
        totalEmployees++;    // shared counter
    }

    void display() {
        System.out.println(name + " | $" + salary +
                         " | Company: " + company +
                         " | Total: " + totalEmployees);
    }
}

public class Main {
    public static void main(String[] args) {
        Employee e1 = new Employee("Alice", 5000);    // totalEmployees = 1
        Employee e2 = new Employee("Bob", 6000);      // totalEmployees = 2
        Employee e3 = new Employee("Charlie", 7000);  // totalEmployees = 3

        e1.display();  // Alice | $5000 | Company: NexaBank | Total: 3
        e2.display();  // Bob   | $6000 | Company: NexaBank | Total: 3
        e3.display();  // Charlie|$7000 | Company: NexaBank | Total: 3

        // Access via class name (preferred for static)
        System.out.println("Total: " + Employee.totalEmployees);  // 3
        System.out.println("Company: " + Employee.company);       // NexaBank

        // Change company → affects ALL employees
        Employee.company = "NexaBank Global";
        e1.display();  // Alice | $5000 | Company: NexaBank Global | Total: 3
    }
}
```

---

### Static Constants (`static final`)

Values that **never change** and are **shared**. Convention: `ALL_CAPS`.

```java
class MathConstants {
    static final double PI = 3.14159265358979;
    static final double E  = 2.71828182845904;
    static final int    MAX_ARRAY_SIZE = 1000;
}

// Usage — no object needed
double circumference = 2 * MathConstants.PI * 10;
System.out.println("Circumference: " + circumference);
```

---
---

# 2. Static Method (Class Method)

A method declared with `static`. Called on the **class**, not on objects. Cannot use `this` or access instance members.

```
  ┌────────────────────────────────────────────────────────┐
  │              STATIC METHOD ACCESS RULES                │
  ├────────────────────────────────────────────────────────┤
  │                                                        │
  │  Static method CAN access:                             │
  │    ✓ Static variables                                  │
  │    ✓ Static methods                                    │
  │    ✓ Local variables (within itself)                   │
  │    ✓ Parameters passed to it                           │
  │                                                        │
  │  Static method CANNOT access:                          │
  │    ✗ Instance variables (non-static fields)           │
  │    ✗ Instance methods (non-static methods)            │
  │    ✗ 'this' keyword                                   │
  │    ✗ 'super' keyword                                  │
  │                                                        │
  │  WHY? Because static exists without any object.        │
  │  No object = no instance data = can't use 'this'.      │
  └────────────────────────────────────────────────────────┘
```

### Code Example — Utility Methods

```java
class MathUtils {
    // All static — no state, pure calculations
    static int add(int a, int b) {
        return a + b;
    }

    static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    static int factorial(int n) {
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }

    static double average(int[] numbers) {
        int sum = 0;
        for (int n : numbers) sum += n;
        return (double) sum / numbers.length;
    }
}

// Usage — NO object creation needed!
public class Main {
    public static void main(String[] args) {
        System.out.println(MathUtils.add(10, 20));      // 30
        System.out.println(MathUtils.max(15, 8));        // 15
        System.out.println(MathUtils.factorial(5));      // 120
        System.out.println(MathUtils.isPrime(17));       // true

        int[] scores = {85, 92, 78, 95};
        System.out.println(MathUtils.average(scores));   // 87.5
    }
}
```

---

### Why Can't Static Methods Access Instance Members?

```java
class Demo {
    int instanceVar = 10;        // belongs to objects
    static int staticVar = 20;   // belongs to class

    void instanceMethod() {
        // CAN access BOTH
        System.out.println(instanceVar);  // ✓ (has an object)
        System.out.println(staticVar);    // ✓ (always available)
    }

    static void staticMethod() {
        // System.out.println(instanceVar);   // ✗ COMPILE ERROR!
        // instanceMethod();                   // ✗ COMPILE ERROR!
        // System.out.println(this.staticVar); // ✗ COMPILE ERROR!

        System.out.println(staticVar);         // ✓ (static → static OK)

        // WORKAROUND: create an object inside static method
        Demo obj = new Demo();
        System.out.println(obj.instanceVar);   // ✓ (now we have an object)
    }
}
```

```
  WHY?

  static method can exist WITHOUT any object.
  Instance variables only exist INSIDE objects.

  If no object exists → no instance data → cannot access.

  main() is static → that's why you need to create objects
  inside main to use instance methods.
```

---
---

# 3. Static Block (Static Initializer)

Runs **ONCE** when the class is first loaded — before `main()`, before any constructor, before any object creation.

```
  CLASS LOADING ORDER:
  ═══════════════════════════════════════════

  1. Class is loaded by JVM
           │
  2. Static variables initialized (in order)
           │
  3. Static block(s) execute (in order)
           │
  4. main() runs (if present)
           │
  5. Objects created → constructors run
```

### Code Example

```java
class AppConfig {
    static String appName;
    static String version;
    static int maxUsers;

    // Static block — runs once when class loads
    static {
        System.out.println("=== Static block executing ===");
        appName = "NexaBank";
        version = "2.0";
        maxUsers = 1000;
        System.out.println("Config loaded: " + appName + " v" + version);
    }

    // Another static block (multiple allowed, run in order)
    static {
        System.out.println("=== Second static block ===");
        if (maxUsers > 500) {
            System.out.println("Enterprise mode enabled");
        }
    }

    AppConfig() {
        System.out.println("Constructor called");
    }

    public static void main(String[] args) {
        System.out.println("=== main() started ===");

        AppConfig c1 = new AppConfig();
        AppConfig c2 = new AppConfig();
    }
}

/*
Output:
=== Static block executing ===
Config loaded: NexaBank v2.0
=== Second static block ===
Enterprise mode enabled
=== main() started ===
Constructor called
Constructor called

Note: Static blocks run BEFORE main() and constructors!
      And only ONCE, even though 2 objects are created.
*/
```

---
---

# 4. Static vs Instance — Complete Comparison

```
╔══════════════════════╦════════════════════════╦═══════════════════════╗
║     Feature          ║      Static            ║     Instance          ║
╠══════════════════════╬════════════════════════╬═══════════════════════╣
║ Belongs to           ║ CLASS                  ║ OBJECT                ║
║ Memory copies        ║ ONE copy               ║ One per object        ║
║ Loaded when          ║ Class loads             ║ Object created (new)  ║
║ Access syntax        ║ ClassName.member       ║ object.member         ║
║ Can use this?        ║ NO                     ║ YES                   ║
║ Can use super?       ║ NO                     ║ YES                   ║
║ Can access static?   ║ YES                    ║ YES                   ║
║ Can access instance? ║ NO (directly)          ║ YES                   ║
║ Shared?              ║ YES (all objects)      ║ NO (per object)       ║
║ Use for              ║ Counters, utils,       ║ Object-specific       ║
║                      ║ constants, helpers     ║ data & behavior       ║
╚══════════════════════╩════════════════════════╩═══════════════════════╝
```

---

# 5. Common `static` Patterns

### Pattern 1: Singleton (Only ONE instance allowed)

```java
class Database {
    private static Database instance;  // static holds the single instance

    private Database() { }            // private constructor — no outside creation

    static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;               // always returns the SAME object
    }

    void connect() {
        System.out.println("Connected to DB");
    }
}

// Usage
Database db1 = Database.getInstance();
Database db2 = Database.getInstance();
System.out.println(db1 == db2);  // true — SAME object!
```

### Pattern 2: Factory Method

```java
class Notification {
    String type;
    String message;

    private Notification(String type, String message) {
        this.type = type;
        this.message = message;
    }

    // Static factory methods — clearer than constructors
    static Notification email(String msg) {
        return new Notification("EMAIL", msg);
    }

    static Notification sms(String msg) {
        return new Notification("SMS", msg);
    }

    static Notification push(String msg) {
        return new Notification("PUSH", msg);
    }

    void send() {
        System.out.println("[" + type + "] " + message);
    }
}

// Usage — reads like English
Notification.email("Your order shipped").send();
Notification.sms("OTP: 4521").send();
Notification.push("New message!").send();
```

### Pattern 3: Static Counter

```java
class Order {
    private static int nextId = 1000;

    int orderId;
    String item;

    Order(String item) {
        this.orderId = nextId++;   // auto-incrementing ID
        this.item = item;
    }

    void display() {
        System.out.println("Order #" + orderId + ": " + item);
    }
}

// Usage
new Order("Laptop").display();     // Order #1000: Laptop
new Order("Phone").display();      // Order #1001: Phone
new Order("Tablet").display();     // Order #1002: Tablet
```

---

# Why is `main()` static?

```
  public static void main(String[] args)
         ──────
         │
         └── static because JVM needs to call main()
             WITHOUT creating any object first.

  If main() were NOT static:
    JVM would need to do: new MainClass().main(args);
    But how would it know which constructor to call?
    What arguments to pass? What if the constructor fails?

  Static main() solves all this — JVM just calls: MainClass.main(args)
  No object needed. Simple and reliable.
```

---
