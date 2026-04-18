# 09 — Method Overriding & Overloading (In Depth)

---

## Big Picture — Polymorphism

Overloading and Overriding are the **two mechanisms** that implement Polymorphism (one of the 4 OOP pillars).

```
╔═══════════════════════════════════════════════════════════════════════╗
║                        POLYMORPHISM                                  ║
║                   "One name, many forms"                             ║
╠══════════════════════════════════╦════════════════════════════════════╣
║   COMPILE-TIME (Early Binding)  ║   RUNTIME (Late Binding)          ║
║   Method OVERLOADING            ║   Method OVERRIDING               ║
╠══════════════════════════════════╬════════════════════════════════════╣
║                                  ║                                    ║
║  SAME class                      ║  Parent → Child classes           ║
║  SAME method name                ║  SAME method name                  ║
║  DIFFERENT parameters            ║  SAME parameters                   ║
║                                  ║                                    ║
║  Compiler decides which          ║  JVM decides which version        ║
║  method to call based on         ║  to call based on the             ║
║  the ARGUMENT LIST               ║  ACTUAL OBJECT at runtime        ║
║                                  ║                                    ║
║  Think: convenience              ║  Think: specialization            ║
║  (same action, diff inputs)      ║  (same signature, diff behavior) ║
║                                  ║                                    ║
╚══════════════════════════════════╩════════════════════════════════════╝
```

---
---

# 1. METHOD OVERLOADING (Compile-Time Polymorphism)

## What is Overloading?

Multiple methods in the **same class** with the **same name** but **different parameter lists**. The compiler picks the correct method based on the arguments you pass.

**Analogy:** The word "draw" — you can draw a circle, draw a rectangle, draw with a color. Same word, but the INPUTS vary, so the action varies.

```
  ┌──────────────────────────────────────────────────────────────┐
  │                HOW OVERLOADING WORKS                         │
  │                                                              │
  │  Your code:              Compiler resolves:                  │
  │                                                              │
  │  calc.add(2, 3)      ──► add(int, int)         → int        │
  │  calc.add(2.5, 3.5)  ──► add(double, double)   → double     │
  │  calc.add(1, 2, 3)   ──► add(int, int, int)    → int        │
  │  calc.add("Hi","!")  ──► add(String, String)    → String     │
  │                                                              │
  │  Compiler looks at:                                          │
  │    1. Number of arguments                                    │
  │    2. Types of arguments                                     │
  │    3. Order of argument types                                │
  │                                                              │
  │  ✗ Return type ALONE is NOT enough to overload               │
  └──────────────────────────────────────────────────────────────┘
```

---

## Ways to Overload

```
┌─────────────────────────────────────────────────────────────────────┐
│  VALID OVERLOADING (parameter list MUST differ):                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Different NUMBER of parameters                                  │
│     void print(int a)                                               │
│     void print(int a, int b)                                        │
│                                                                     │
│  2. Different TYPES of parameters                                   │
│     void print(int a)                                               │
│     void print(String a)                                            │
│     void print(double a)                                            │
│                                                                     │
│  3. Different ORDER of parameter types                              │
│     void print(int a, String b)                                     │
│     void print(String a, int b)                                     │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│  INVALID OVERLOADING (these DON'T count):                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ✗ Different return type ONLY                                       │
│    int  getVal()         ← COMPILE ERROR                            │
│    double getVal()       ← same params, only return differs         │
│                                                                     │
│  ✗ Different parameter NAMES only                                   │
│    void print(int x)     ← COMPILE ERROR                            │
│    void print(int y)     ← same type, only name differs             │
│                                                                     │
│  ✗ Different access modifiers only                                  │
│    public void show()    ← COMPILE ERROR                            │
│    private void show()   ← same signature                           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Code Example — Calculator

```java
class Calculator {

    // Overloaded add() — 4 versions

    // Version 1: two ints
    int add(int a, int b) {
        System.out.println("add(int, int) called");
        return a + b;
    }

    // Version 2: three ints (different NUMBER)
    int add(int a, int b, int c) {
        System.out.println("add(int, int, int) called");
        return a + b + c;
    }

    // Version 3: two doubles (different TYPE)
    double add(double a, double b) {
        System.out.println("add(double, double) called");
        return a + b;
    }

    // Version 4: two strings (different TYPE)
    String add(String a, String b) {
        System.out.println("add(String, String) called");
        return a + b;
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();

        System.out.println(calc.add(2, 3));            // add(int, int) → 5
        System.out.println(calc.add(2, 3, 4));         // add(int, int, int) → 9
        System.out.println(calc.add(2.5, 3.5));        // add(double, double) → 6.0
        System.out.println(calc.add("Hello", " World"));// add(String, String) → Hello World
    }
}
```

---

## Type Promotion in Overloading

When no exact match exists, Java **promotes** smaller types to larger ones automatically.

```
  TYPE PROMOTION PATH:
  ═══════════════════════════════════════════════════

  byte → short → int → long → float → double
                  ▲
                  │
                char

  If you call add(byte, byte) but only add(int, int) exists,
  Java promotes byte → int automatically.
```

```java
class Demo {
    void show(int a)    { System.out.println("int: " + a); }
    void show(double a) { System.out.println("double: " + a); }
    void show(String a) { System.out.println("String: " + a); }
}

Demo d = new Demo();
d.show(42);       // int: 42        ← exact match
d.show(3.14);     // double: 3.14   ← exact match
d.show("Hello");  // String: Hello  ← exact match
d.show('A');      // int: 65        ← char promoted to int (no char version)
d.show((byte) 5); // int: 5         ← byte promoted to int (no byte version)
```

### Ambiguity Trap

```java
class Trap {
    void test(int a, double b)    { System.out.println("int, double"); }
    void test(double a, int b)    { System.out.println("double, int"); }
}

Trap t = new Trap();
t.test(10, 20.5);   // int, double    ← exact match, no ambiguity
t.test(10.5, 20);   // double, int    ← exact match, no ambiguity
// t.test(10, 20);  // ← COMPILE ERROR! Ambiguous — both versions could match
```

---

## Common Overloading: Constructors

```java
class User {
    String name;
    String email;
    int age;

    User(String name) {
        this.name = name;
        this.email = "none";
        this.age = 0;
    }

    User(String name, String email) {
        this.name = name;
        this.email = email;
        this.age = 0;
    }

    User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    void display() {
        System.out.println(name + " | " + email + " | " + age);
    }
}

// Multiple ways to create a User
new User("Alice").display();                        // Alice | none | 0
new User("Bob", "bob@mail.com").display();          // Bob | bob@mail.com | 0
new User("Charlie", "c@mail.com", 25).display();    // Charlie | c@mail.com | 25
```

---
---

# 2. METHOD OVERRIDING (Runtime Polymorphism)

## What is Overriding?

A child class provides its **own implementation** of a method already defined in the parent class. At runtime, the JVM calls the **child's version** based on the actual object type.

**Analogy:** Both humans and robots can "greet()". The method name is the same, but the behavior is completely different. The version that runs depends on whether you're talking to a human or a robot.

```
  ┌──────────────────────────────────────────────────────────────┐
  │               HOW OVERRIDING WORKS                           │
  │                                                              │
  │  Animal a = new Dog();                                       │
  │                                                              │
  │  COMPILE TIME:                    RUNTIME:                   │
  │  Compiler checks:                JVM checks:                 │
  │  "Does Animal have sound()?"     "What is the ACTUAL object?"│
  │  → YES ✓ (compiles OK)          → Dog                       │
  │                                  → calls Dog.sound()         │
  │                                                              │
  │  The VARIABLE TYPE (Animal) decides what methods are         │
  │  CALLABLE. The OBJECT TYPE (Dog) decides which               │
  │  VERSION runs.                                               │
  └──────────────────────────────────────────────────────────────┘
```

---

## Overriding Rules

```
┌──────────────────────────────────────────────────────────────────┐
│                   OVERRIDING RULES                                │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ✓ MUST have same method name                                   │
│  ✓ MUST have same parameter list (number, types, order)         │
│  ✓ MUST have same return type (or covariant — subtype)          │
│  ✓ MUST be in a subclass (inheritance required)                 │
│  ✓ Access modifier must be SAME or MORE visible                 │
│    (parent protected → child protected or public, NOT private)  │
│  ✓ Use @Override annotation (optional but recommended)          │
│                                                                  │
│  ✗ CANNOT override static methods (those are "hidden", not      │
│    overridden — called method hiding)                            │
│  ✗ CANNOT override final methods                                │
│  ✗ CANNOT override private methods (not visible to child)       │
│  ✗ CANNOT make access MORE restrictive                          │
│    (parent public → child private is NOT allowed)               │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Diagram — Override Resolution Flow

```
         a.sound()    (Animal a = new Dog())
              │
     ┌────────▼────────────┐
     │  COMPILE TIME       │
     │  Does 'Animal'      │
     │  have sound()?      │──── NO ──► COMPILE ERROR
     └────────┬────────────┘
            YES
              │
     ┌────────▼────────────┐
     │  RUNTIME            │
     │  What is the actual │
     │  object type?       │
     └────────┬────────────┘
              │
    ┌─────────┼──────────┐
    │         │          │
┌───▼───┐ ┌──▼────┐ ┌───▼───┐
│  Dog  │ │  Cat  │ │ Bird  │
│sound()│ │sound()│ │sound()│
│"Woof" │ │"Meow" │ │"Tweet"│
└───────┘ └───────┘ └───────┘

  JVM calls the version from the ACTUAL object → Dog.sound()
```

---

## Code Example — Payment System

```java
class Payment {
    String payer;
    double amount;

    Payment(String payer, double amount) {
        this.payer = payer;
        this.amount = amount;
    }

    // Base method — default behavior
    void process() {
        System.out.println("Processing generic payment of $" + amount);
    }

    String receipt() {
        return "Payment: $" + amount + " by " + payer;
    }
}

class CreditCardPayment extends Payment {
    String cardNumber;

    CreditCardPayment(String payer, double amount, String cardNumber) {
        super(payer, amount);
        this.cardNumber = cardNumber;
    }

    @Override
    void process() {
        System.out.println("Charging $" + amount + " to credit card ****" +
                          cardNumber.substring(cardNumber.length() - 4));
    }

    @Override
    String receipt() {
        return "CC Payment: $" + amount + " | Card: ****" +
               cardNumber.substring(cardNumber.length() - 4);
    }
}

class PayPalPayment extends Payment {
    String email;

    PayPalPayment(String payer, double amount, String email) {
        super(payer, amount);
        this.email = email;
    }

    @Override
    void process() {
        System.out.println("Transferring $" + amount + " via PayPal to " + email);
    }

    @Override
    String receipt() {
        return "PayPal: $" + amount + " | " + email;
    }
}

class CryptoPayment extends Payment {
    String wallet;

    CryptoPayment(String payer, double amount, String wallet) {
        super(payer, amount);
        this.wallet = wallet;
    }

    @Override
    void process() {
        System.out.println("Sending $" + amount + " in crypto to wallet " + wallet);
    }

    @Override
    String receipt() {
        return "Crypto: $" + amount + " | Wallet: " + wallet;
    }
}

// Runtime Polymorphism in action
public class Main {
    public static void main(String[] args) {
        Payment[] payments = {
            new CreditCardPayment("Alice", 150.00, "1234567890123456"),
            new PayPalPayment("Bob", 75.50, "bob@email.com"),
            new CryptoPayment("Charlie", 200.00, "0xABC123"),
        };

        System.out.println("═══════ PROCESSING PAYMENTS ═══════");
        for (Payment p : payments) {
            p.process();    // JVM calls the correct overridden version
            System.out.println("Receipt: " + p.receipt());
            System.out.println("─────────────────────────────────");
        }
    }
}

/*
═══════ PROCESSING PAYMENTS ═══════
Charging $150.0 to credit card ****3456
Receipt: CC Payment: $150.0 | Card: ****3456
─────────────────────────────────────
Transferring $75.5 via PayPal to bob@email.com
Receipt: PayPal: $75.5 | bob@email.com
─────────────────────────────────────
Sending $200.0 in crypto to wallet 0xABC123
Receipt: Crypto: $200.0 | Wallet: 0xABC123
─────────────────────────────────────
*/
```

---

## Using super in Override — Extend Parent Behavior

Instead of completely replacing the parent method, you can **call it first** then add extra logic.

```java
class Logger {
    void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

class TimestampLogger extends Logger {
    @Override
    void log(String message) {
        System.out.print("[" + java.time.LocalTime.now() + "] ");
        super.log(message);  // reuses parent's logic, adds timestamp
    }
}

class FileLogger extends TimestampLogger {
    @Override
    void log(String message) {
        super.log(message);  // reuses TimestampLogger's logic
        System.out.println("  ↳ (also saved to file)");
    }
}

// Multilevel override chain
Logger l = new FileLogger();
l.log("Server started");
// [14:30:05] [LOG] Server started
//   ↳ (also saved to file)
```

---

## Covariant Return Type

When overriding, the return type can be a **subtype** of the parent's return type.

```java
class Animal {
    Animal create() {
        System.out.println("Creating Animal");
        return new Animal();
    }
}

class Dog extends Animal {
    @Override
    Dog create() {       // Return type is Dog (subtype of Animal) — VALID
        System.out.println("Creating Dog");
        return new Dog();
    }
}

// Dog is a subtype of Animal, so returning Dog instead of Animal is OK
```

---

## What CANNOT Be Overridden

```java
class Parent {
    // 1. FINAL — cannot override
    final void locked() {
        System.out.println("This is locked forever");
    }

    // 2. STATIC — method hiding (NOT overriding)
    static void greet() {
        System.out.println("Parent greet");
    }

    // 3. PRIVATE — invisible to child
    private void secret() {
        System.out.println("Parent secret");
    }
}

class Child extends Parent {
    // final void locked() { }     // ✗ COMPILE ERROR

    static void greet() {          // This is METHOD HIDING, not overriding
        System.out.println("Child greet");   // no @Override possible
    }

    void secret() {                // This is a NEW method, NOT an override
        System.out.println("Child secret");  // Parent's secret was private
    }
}

// Method hiding vs overriding — important difference:
Parent p = new Child();
p.greet();     // "Parent greet" ← static: resolved by reference type, not object type
// p.secret(); // ✗ COMPILE ERROR — private
p.locked();    // "This is locked forever"
```

```
  OVERRIDING vs HIDING:

  Override (instance methods):
    Parent ref = new Child();
    ref.method();  → calls Child's version (object decides)

  Hiding (static methods):
    Parent ref = new Child();
    ref.staticMethod();  → calls Parent's version (reference decides)
```

---
---

# 3. OVERLOADING vs OVERRIDING — Complete Comparison

```
╔══════════════════════╦═══════════════════════════╦═══════════════════════════╗
║    Feature           ║   OVERLOADING             ║   OVERRIDING              ║
╠══════════════════════╬═══════════════════════════╬═══════════════════════════╣
║ Also called          ║ Static polymorphism       ║ Dynamic polymorphism      ║
║ Where               ║ Same class                ║ Parent → Child class      ║
║ Method name          ║ Must be SAME              ║ Must be SAME              ║
║ Parameters           ║ Must be DIFFERENT         ║ Must be SAME              ║
║ Return type          ║ Can differ                ║ Must be same/covariant    ║
║ Access modifier      ║ Can differ                ║ Same or wider (more open) ║
║ Binding              ║ Compile-time              ║ Runtime                   ║
║ Resolved by          ║ Argument list             ║ Actual object type        ║
║ Inheritance needed?  ║ No                        ║ Yes                       ║
║ @Override?           ║ No                        ║ Yes (recommended)         ║
║ static methods?      ║ Can be overloaded         ║ Cannot be overridden      ║
║ final methods?       ║ Can be overloaded         ║ Cannot be overridden      ║
║ private methods?     ║ Can be overloaded         ║ Cannot be overridden      ║
║ Purpose              ║ Convenience (diff inputs) ║ Specialization (diff     ║
║                      ║                           ║ behavior in subclass)     ║
║ Performance          ║ Slightly faster           ║ Slightly slower (virtual  ║
║                      ║ (resolved at compile)     ║ method table lookup)      ║
╚══════════════════════╩═══════════════════════════╩═══════════════════════════╝
```

---

# Full Example — Both Together

```java
class Printer {

    // ═══ OVERLOADED: same name, different params (same class) ═══
    void print(String text) {
        System.out.println("Text: " + text);
    }

    void print(int number) {
        System.out.println("Number: " + number);
    }

    void print(String text, int copies) {
        for (int i = 0; i < copies; i++) {
            System.out.println(text);
        }
    }

    void print(String[] items) {
        for (String item : items) {
            System.out.println("- " + item);
        }
    }
}

class ColorPrinter extends Printer {

    // ═══ OVERRIDDEN: same signature, child class (specialization) ═══
    @Override
    void print(String text) {
        System.out.println("[COLOR] " + text);
    }

    @Override
    void print(int number) {
        System.out.println("[COLOR] Number: " + number);
    }

    // print(String, int) and print(String[]) are INHERITED as-is
}

class LaserPrinter extends Printer {

    @Override
    void print(String text) {
        System.out.println("[LASER] " + text);
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Printer p = new ColorPrinter();  // parent ref → child object

        // OVERRIDING decides: ColorPrinter's versions called
        p.print("Hello");               // [COLOR] Hello
        p.print(42);                     // [COLOR] Number: 42

        // OVERLOADING decides: which method signature matches
        p.print("Copy this", 3);         // Copy this ×3 (inherited)
        p.print(new String[]{"A", "B"}); // - A  - B     (inherited)

        System.out.println();

        // Different object → different override
        Printer p2 = new LaserPrinter();
        p2.print("Hello");              // [LASER] Hello
        p2.print(42);                    // Number: 42 (not overridden, uses Printer's)
    }
}

/*
[COLOR] Hello
[COLOR] Number: 42
Copy this
Copy this
Copy this
- A
- B

[LASER] Hello
Number: 42
*/
```

---

## Quick Decision Guide

```
  "I want the SAME method name with DIFFERENT inputs"
    → OVERLOADING (same class, diff params)

  "I want a CHILD class to behave DIFFERENTLY from the parent"
    → OVERRIDING (same signature, child class)

  "I want to ADD extra behavior on top of what parent does"
    → OVERRIDING + super.method()

  "I want to PREVENT a method from being changed by children"
    → Make it final

  "I want to FORCE children to provide their own version"
    → Make it abstract
```

---
