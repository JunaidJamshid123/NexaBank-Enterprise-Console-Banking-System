# 05 — The 4 Pillars of OOP (In Depth)

---

## Big Picture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     OBJECT-ORIENTED PROGRAMMING                        │
│                                                                         │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│   │ENCAPSULATION │  │ INHERITANCE  │  │ POLYMORPHISM │  │ABSTRACTION │ │
│   │              │  │              │  │              │  │            │ │
│   │ Wrap data +  │  │ Reuse code   │  │ One method,  │  │ Hide "how" │ │
│   │ methods into │  │ from parent  │  │ many forms   │  │ Show "what"│ │
│   │ one unit &   │  │ class in a   │  │ at compile   │  │ via abstract│ │
│   │ restrict     │  │ child class  │  │ or runtime   │  │ classes &  │ │
│   │ direct access│  │              │  │              │  │ interfaces │ │
│   └──────────────┘  └──────────────┘  └──────────────┘  └────────────┘ │
│                                                                         │
│   WHY?  → Maintainability, Reusability, Flexibility, Security          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

# PILLAR 1 — ENCAPSULATION

## What is Encapsulation?

Encapsulation means **bundling data (fields) and the methods that operate on that data into a single class**, and then **restricting direct access** to the internal state. Outside code interacts only through well-defined public methods (getters/setters).

**Analogy:** A capsule (medicine) wraps chemicals inside a shell. You swallow the capsule — you don't touch the chemicals directly.

```
  WITHOUT Encapsulation                WITH Encapsulation
  ─────────────────────                ────────────────────
  Anyone can do:                       Must go through methods:

  account.balance = -999;  ← BAD      account.withdraw(100);  ← SAFE
  account.balance = 0;     ← BAD      account.deposit(500);   ← VALIDATED
                                       account.getBalance();   ← READ-ONLY
```

---

## Why Encapsulation?

```
┌────────────────────────────────────────────────────────────┐
│                   BENEFITS                                 │
├────────────────────────┬───────────────────────────────────┤
│ 1. DATA PROTECTION     │ Prevents invalid states           │
│                        │ (e.g., negative balance)          │
├────────────────────────┼───────────────────────────────────┤
│ 2. CONTROLLED ACCESS   │ Validation logic in setters       │
├────────────────────────┼───────────────────────────────────┤
│ 3. FLEXIBILITY         │ Change internal implementation    │
│                        │ without breaking outside code     │
├────────────────────────┼───────────────────────────────────┤
│ 4. READ-ONLY / WRITE   │ Provide only getter = read-only   │
│    ONLY FIELDS         │ Provide only setter = write-only  │
└────────────────────────┴───────────────────────────────────┘
```

---

## Access Modifiers (The Lock System)

```
  VISIBILITY SCOPE
  ════════════════════════════════════════════════════════════

  private ──────► Same class ONLY
                  Most restrictive. Use for fields.

  default ──────► Same class + Same package
  (no keyword)    Classes in same folder can see it.

  protected ───► Same class + Same package + Subclasses (any package)
                  Subclass in different package CAN access.

  public ──────► Everywhere
                  Least restrictive. Use for methods/APIs.


  ┌──────────────┬───────┬─────────┬──────────┬───────────┐
  │  Modifier    │ Class │ Package │ Subclass │ World     │
  ├──────────────┼───────┼─────────┼──────────┼───────────┤
  │ private      │  ✓    │   ✗     │    ✗     │    ✗      │
  │ default      │  ✓    │   ✓     │    ✗     │    ✗      │
  │ protected    │  ✓    │   ✓     │    ✓     │    ✗      │
  │ public       │  ✓    │   ✓     │    ✓     │    ✓      │
  └──────────────┴───────┴─────────┴──────────┴───────────┘

  RULE OF THUMB: Make fields private, methods public.
```

---

## Encapsulation — Diagram

```
  ┌──────────────────────────────────────────────────────┐
  │                   CLASS: BankAccount                  │
  │  ┌────────────────────────────────────────────────┐  │
  │  │           PRIVATE ZONE (hidden)                │  │
  │  │                                                │  │
  │  │   - String accountNo                           │  │
  │  │   - double balance                             │  │
  │  │   - String pin                                 │  │
  │  │                                                │  │
  │  └────────────────────────────────────────────────┘  │
  │                                                      │
  │  ┌────────────────────────────────────────────────┐  │
  │  │           PUBLIC ZONE (exposed)                │  │
  │  │                                                │  │
  │  │   + getBalance()        → returns balance      │  │
  │  │   + deposit(amount)     → validates, then adds │  │
  │  │   + withdraw(amount)    → validates, then subs │  │
  │  │   + getAccountNo()      → returns account no   │  │
  │  │                                                │  │
  │  │   (NO setBalance, NO setPin from outside)      │  │
  │  └────────────────────────────────────────────────┘  │
  └──────────────────────────────────────────────────────┘
                         │
              ┌──────────▼──────────┐
              │   OUTSIDE WORLD     │
              │   Can ONLY call     │
              │   public methods    │
              └─────────────────────┘
```

---

## Code Example 1 — Basic Encapsulation

```java
class Employee {
    // PRIVATE fields — hidden from outside
    private String name;
    private double salary;
    private int age;

    // Constructor
    public Employee(String name, double salary, int age) {
        this.name = name;
        setSalary(salary);   // uses setter for validation
        setAge(age);
    }

    // GETTER — read-only access
    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }

    public int getAge() {
        return age;
    }

    // SETTER — write access with validation
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public void setSalary(double salary) {
        if (salary >= 0) {                // validation: no negative salary
            this.salary = salary;
        } else {
            System.out.println("Invalid salary!");
        }
    }

    public void setAge(int age) {
        if (age >= 18 && age <= 65) {     // validation: working age
            this.age = age;
        } else {
            System.out.println("Age must be 18-65");
        }
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Employee e = new Employee("Alice", 50000, 30);

        System.out.println(e.getName());    // Alice
        System.out.println(e.getSalary());  // 50000.0

        e.setSalary(-500);                  // "Invalid salary!" — blocked
        System.out.println(e.getSalary());  // 50000.0 — unchanged

        e.setAge(10);                       // "Age must be 18-65" — blocked

        // e.salary = -500;  ← COMPILE ERROR (private)
        // e.name = "";      ← COMPILE ERROR (private)
    }
}
```

---

## Code Example 2 — Read-Only Class (Immutable)

```java
// No setters → object cannot be modified after creation
final class ImmutableStudent {
    private final String name;
    private final int rollNo;

    public ImmutableStudent(String name, int rollNo) {
        this.name = name;
        this.rollNo = rollNo;
    }

    public String getName()  { return name; }
    public int getRollNo()   { return rollNo; }

    // No setters! Object is frozen after creation.
}

// Usage
ImmutableStudent s = new ImmutableStudent("Bob", 101);
System.out.println(s.getName());   // Bob
// s.name = "Eve";                 // COMPILE ERROR
// No way to change the object
```

---

## Code Example 3 — Full Bank Account

```java
class BankAccount {
    private String owner;
    private String accountNo;
    private double balance;

    public BankAccount(String owner, String accountNo, double initialDeposit) {
        this.owner = owner;
        this.accountNo = accountNo;
        this.balance = Math.max(initialDeposit, 0); // no negative start
    }

    // Read-only
    public String getOwner()     { return owner; }
    public String getAccountNo() { return accountNo; }
    public double getBalance()   { return balance; }

    // Controlled operations
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited $" + amount + " | New Balance: $" + balance);
        } else {
            System.out.println("Deposit amount must be positive");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrew $" + amount + " | Remaining: $" + balance);
            return true;
        }
        System.out.println("Insufficient funds or invalid amount");
        return false;
    }

    public void displayInfo() {
        System.out.println("Owner: " + owner);
        System.out.println("A/C:   " + accountNo);
        System.out.println("Bal:   $" + balance);
    }
}

// Usage
BankAccount acc = new BankAccount("Alice", "ACC-001", 1000);
acc.deposit(500);       // Deposited $500 | New Balance: $1500
acc.withdraw(200);      // Withdrew $200 | Remaining: $1300
acc.withdraw(5000);     // Insufficient funds
acc.displayInfo();
```

---
---

# PILLAR 2 — INHERITANCE

## What is Inheritance?

Inheritance lets a **child class (subclass) acquire all the fields and methods of a parent class (superclass)** using the `extends` keyword. The child can then add its own members or override parent methods.

**Analogy:** A child inherits traits (eyes, height) from parents. The child also develops their own unique skills.

```
  REAL-WORLD MAPPING:

  Parent:  Vehicle   → has wheels, engine, drive()
  Child:   Car       → inherits wheels, engine, drive() + adds openTrunk()
  Child:   Bike      → inherits wheels, engine, drive() + adds doWheelie()

  The child DOES NOT rewrite engine/wheels code — it inherits it.
```

---

## Why Inheritance?

```
┌────────────────────────────────────────────────────────────┐
│                      BENEFITS                              │
├──────────────────────────┬─────────────────────────────────┤
│ 1. CODE REUSE            │ Write common logic ONCE in      │
│                          │ parent, all children get it     │
├──────────────────────────┼─────────────────────────────────┤
│ 2. HIERARCHICAL MODEL    │ Models real-world "IS-A"        │
│                          │ relationships (Dog IS-A Animal) │
├──────────────────────────┼─────────────────────────────────┤
│ 3. EASY MAINTENANCE      │ Fix a bug in parent →           │
│                          │ all children get the fix        │
├──────────────────────────┼─────────────────────────────────┤
│ 4. EXTENSIBILITY         │ Add new child without changing  │
│                          │ existing code                   │
├──────────────────────────┼─────────────────────────────────┤
│ 5. POLYMORPHISM SUPPORT  │ Parent reference can hold       │
│                          │ any child object                │
└──────────────────────────┴─────────────────────────────────┘
```

---

## Types of Inheritance

```
  ╔══════════════════════════════════════════════════════════════════════╗
  ║                    INHERITANCE TYPES IN JAVA                       ║
  ╠══════════════════╦══════════════════╦════════════════════════════════╣
  ║                  ║                  ║                                ║
  ║  1. SINGLE       ║  2. MULTILEVEL   ║  3. HIERARCHICAL              ║
  ║                  ║                  ║                                ║
  ║    ┌───┐         ║    ┌───┐         ║       ┌───┐                   ║
  ║    │ A │         ║    │ A │         ║       │ A │                   ║
  ║    └─┬─┘         ║    └─┬─┘         ║       └─┬─┘                   ║
  ║      │           ║      │           ║     ┌───┼───┐                 ║
  ║    ┌─▼─┐         ║    ┌─▼─┐         ║   ┌─▼─┐│ ┌─▼─┐               ║
  ║    │ B │         ║    │ B │         ║   │ B ││ │ C │               ║
  ║    └───┘         ║    └─┬─┘         ║   └───┘│ └───┘               ║
  ║                  ║    ┌─▼─┐         ║      ┌─▼─┐                   ║
  ║  A → B           ║    │ C │         ║      │ D │                   ║
  ║                  ║    └───┘         ║      └───┘                   ║
  ║                  ║  A → B → C       ║  A → B, A → C, A → D        ║
  ╠══════════════════╩══════════════════╩════════════════════════════════╣
  ║                                                                     ║
  ║  4. MULTIPLE INHERITANCE — ✖ NOT ALLOWED WITH CLASSES               ║
  ║     (Use interfaces instead)                                        ║
  ║                                                                     ║
  ║     ┌───┐  ┌───┐                                                    ║
  ║     │ A │  │ B │     Why not?  → "Diamond Problem"                  ║
  ║     └─┬─┘  └─┬─┘     If A and B both have method(),                 ║
  ║       └──┬───┘       which one does C inherit? Ambiguous!           ║
  ║        ┌─▼─┐                                                        ║
  ║        │ C │  ← COMPILE ERROR                                       ║
  ║        └───┘                                                        ║
  ║                                                                     ║
  ║  5. HYBRID — Combination of above. Possible only via interfaces.    ║
  ╚═════════════════════════════════════════════════════════════════════╝
```

---

## What is Inherited & What is NOT

```
┌─────────────────────────────┬────────────────────────────┐
│       INHERITED ✓           │      NOT INHERITED ✗       │
├─────────────────────────────┼────────────────────────────┤
│ public methods              │ Constructors               │
│ protected methods           │ private members            │
│ public fields               │ static members (shared,    │
│ protected fields            │   not truly inherited)     │
│ default members (same pkg)  │ final methods (inherited   │
│                             │   but can't be overridden) │
└─────────────────────────────┴────────────────────────────┘
```

---

## Code Example 1 — Single Inheritance

```java
class Animal {
    String name;
    int age;

    Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    void eat() {
        System.out.println(name + " is eating");
    }

    void sleep() {
        System.out.println(name + " is sleeping");
    }

    void info() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

class Dog extends Animal {
    String breed;

    Dog(String name, int age, String breed) {
        super(name, age);      // calls Animal constructor
        this.breed = breed;
    }

    void bark() {
        System.out.println(name + " says Woof!");
    }

    void fetch(String item) {
        System.out.println(name + " fetches the " + item);
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Dog d = new Dog("Rex", 3, "Labrador");
        d.info();          // Name: Rex, Age: 3       ← inherited
        d.eat();           // Rex is eating            ← inherited
        d.sleep();         // Rex is sleeping           ← inherited
        d.bark();          // Rex says Woof!            ← own method
        d.fetch("ball");   // Rex fetches the ball      ← own method
    }
}
```

---

## Code Example 2 — Multilevel Inheritance

```java
/*
   Hierarchy:  LivingBeing → Animal → Dog
*/

class LivingBeing {
    void breathe() {
        System.out.println("Breathing...");
    }
}

class Animal extends LivingBeing {   // Level 1
    void eat() {
        System.out.println("Eating...");
    }
}

class Dog extends Animal {           // Level 2
    void bark() {
        System.out.println("Woof!");
    }
}

// Usage
Dog d = new Dog();
d.breathe();  // from LivingBeing (grandparent)
d.eat();      // from Animal (parent)
d.bark();     // own method
```

---

## Code Example 3 — Hierarchical Inheritance

```java
/*
              Shape
            /       \
      Circle       Rectangle
*/

class Shape {
    String color;

    Shape(String color) {
        this.color = color;
    }

    void display() {
        System.out.println("Color: " + color);
    }
}

class Circle extends Shape {
    double radius;

    Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    double width, height;

    Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }

    double area() {
        return width * height;
    }
}

// Usage
Circle c = new Circle("Red", 5);
c.display();                                // Color: Red  (inherited)
System.out.println("Area: " + c.area());    // Area: 78.54

Rectangle r = new Rectangle("Blue", 4, 6);
r.display();                                // Color: Blue (inherited)
System.out.println("Area: " + r.area());    // Area: 24.0
```

---

## Inheritance Memory Diagram

```
  When:  Dog d = new Dog("Rex", 3, "Labrador");

  STACK                         HEAP
  ┌─────────┐            ┌──────────────────────────┐
  │  d ──────┼──────────► │       Dog Object          │
  └─────────┘            │  ┌──────────────────────┐ │
                          │  │  Animal Part          │ │
                          │  │  name = "Rex"         │ │
                          │  │  age  = 3             │ │
                          │  │  eat(), sleep(), info()│ │
                          │  └──────────────────────┘ │
                          │  ┌──────────────────────┐ │
                          │  │  Dog Part             │ │
                          │  │  breed = "Labrador"   │ │
                          │  │  bark(), fetch()      │ │
                          │  └──────────────────────┘ │
                          └──────────────────────────┘
  
  The child object contains BOTH parent and child fields in one block.
```

---

## The IS-A Test

```
  Dog IS-A Animal?       ✓  (Dog extends Animal)
  Cat IS-A Animal?       ✓  (Cat extends Animal)
  Animal IS-A Dog?       ✗  (parent is NOT a child)
  Dog IS-A Car?          ✗  (no relationship)

  If "X IS-A Y" → use inheritance
  If "X HAS-A Y" → use composition (field inside class)
```

---
---

# PILLAR 3 — POLYMORPHISM

## What is Polymorphism?

Polymorphism means **"many forms"**. The same method name can behave differently depending on:
- **Which parameters** are passed (compile-time / overloading)
- **Which object** is calling it (runtime / overriding)

**Analogy:** The word "open" has different forms — open a door, open a file, open a bottle. Same word, different behavior depending on context.

---

## Two Types of Polymorphism

```
╔═══════════════════════════════════════════════════════════════════╗
║                        POLYMORPHISM                              ║
╠═══════════════════════════════╦═══════════════════════════════════╣
║     COMPILE-TIME              ║       RUNTIME                    ║
║     (Static Binding)          ║       (Dynamic Binding)          ║
╠═══════════════════════════════╬═══════════════════════════════════╣
║  Method OVERLOADING           ║  Method OVERRIDING               ║
║  Same class                   ║  Parent → Child class            ║
║  Same method name             ║  Same method name                ║
║  Different parameters         ║  Same parameters                 ║
║  Decided by COMPILER          ║  Decided by JVM at RUNTIME       ║
║  Based on argument types      ║  Based on actual object type     ║
╠═══════════════════════════════╬═══════════════════════════════════╣
║  add(int, int)                ║  class Animal { sound() }        ║
║  add(double, double)          ║  class Dog extends Animal {      ║
║  add(int, int, int)           ║      @Override sound()           ║
║                               ║  }                               ║
╚═══════════════════════════════╩═══════════════════════════════════╝
```

---

## Compile-Time Polymorphism (Overloading)

Multiple methods with the **same name** but **different parameter lists** in the **same class**.

```
  ┌───────────────────────────────────────────────┐
  │  Compiler looks at:                           │
  │                                               │
  │  calc.add(2, 3)       →  add(int, int)        │──► matches
  │  calc.add(2.5, 3.5)   →  add(double, double)  │──► matches
  │  calc.add(1, 2, 3)    →  add(int, int, int)   │──► matches
  │                                               │
  │  Decision made at COMPILE time                │
  └───────────────────────────────────────────────┘
```

```java
class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    double add(double a, double b) {
        return a + b;
    }

    int add(int a, int b, int c) {
        return a + b + c;
    }

    String add(String a, String b) {
        return a + b;
    }
}

// Usage
Calculator calc = new Calculator();
System.out.println(calc.add(2, 3));            // 5
System.out.println(calc.add(2.5, 3.5));        // 6.0
System.out.println(calc.add(1, 2, 3));         // 6
System.out.println(calc.add("Hello", " World"));// Hello World
```

---

## Runtime Polymorphism (Overriding)

A child class provides its own version of a method already defined in the parent. The JVM decides which version to run **at runtime**, based on the actual object.

### The Core Mechanism: Upcasting

```
  KEY CONCEPT:
  ════════════
  A parent reference can hold a child object.

          Animal a = new Dog();
          ┌──────┐   ┌───────┐
          │Parent │   │ Child │
          │ type  │   │object │
          └──────┘   └───────┘

  The variable type is Animal, but the actual object is Dog.
  Method call → JVM checks the ACTUAL object → calls Dog's version.
```

```
  COMPILE TIME                      RUNTIME
  ════════════                      ═══════

  Animal a = new Dog();             a.sound();

  Compiler checks:                  JVM checks:
  "Does Animal have                 "What is the actual
   sound()?"  → YES ✓               object? → Dog"
                                     → calls Dog.sound()
```

### Flow Diagram

```
                    a.sound()
                        │
          ┌─────────────▼──────────────┐
          │  What is the ACTUAL object? │
          └─────────────┬──────────────┘
                ┌───────┼───────┐
                │       │       │
           ┌────▼──┐ ┌──▼───┐ ┌▼──────┐
           │ Dog   │ │ Cat  │ │ Bird  │
           │sound()│ │sound()│ │sound()│
           │"Woof!"│ │"Meow"│ │"Tweet"│
           └───────┘ └──────┘ └───────┘
```

### Code Example

```java
class Animal {
    String name;

    Animal(String name) {
        this.name = name;
    }

    void sound() {
        System.out.println(name + " makes a sound");
    }

    void move() {
        System.out.println(name + " moves");
    }
}

class Dog extends Animal {
    Dog(String name) { super(name); }

    @Override
    void sound() {
        System.out.println(name + " says: Woof! Woof!");
    }

    @Override
    void move() {
        System.out.println(name + " runs on 4 legs");
    }
}

class Cat extends Animal {
    Cat(String name) { super(name); }

    @Override
    void sound() {
        System.out.println(name + " says: Meow!");
    }

    @Override
    void move() {
        System.out.println(name + " walks silently");
    }
}

class Bird extends Animal {
    Bird(String name) { super(name); }

    @Override
    void sound() {
        System.out.println(name + " says: Tweet!");
    }

    @Override
    void move() {
        System.out.println(name + " flies in the sky");
    }
}

// RUNTIME POLYMORPHISM IN ACTION
public class Main {
    public static void main(String[] args) {
        // Parent reference → child objects
        Animal a1 = new Dog("Rex");
        Animal a2 = new Cat("Whiskers");
        Animal a3 = new Bird("Tweety");

        a1.sound();  // Rex says: Woof! Woof!
        a2.sound();  // Whiskers says: Meow!
        a3.sound();  // Tweety says: Tweet!

        a1.move();   // Rex runs on 4 legs
        a2.move();   // Whiskers walks silently
        a3.move();   // Tweety flies in the sky
    }
}
```

---

## Polymorphism with Arrays & Loops

The real power — treat different objects uniformly.

```java
public class Main {
    public static void main(String[] args) {
        Animal[] zoo = {
            new Dog("Rex"),
            new Cat("Whiskers"),
            new Bird("Tweety"),
            new Dog("Buddy"),
            new Cat("Luna")
        };

        System.out.println("=== Zoo Roll Call ===");
        for (Animal a : zoo) {
            a.sound();  // JVM picks correct version for each object
            a.move();
            System.out.println("---");
        }
    }
}

/*
Output:
=== Zoo Roll Call ===
Rex says: Woof! Woof!
Rex runs on 4 legs
---
Whiskers says: Meow!
Whiskers walks silently
---
Tweety says: Tweet!
Tweety flies in the sky
---
Buddy says: Woof! Woof!
Buddy runs on 4 legs
---
Luna says: Meow!
Luna walks silently
---
*/
```

---

## Polymorphism with Method Parameters

```java
class Vet {
    // Accepts ANY Animal subtype — doesn't need to know the specific type
    void checkup(Animal animal) {
        System.out.println("Checking " + animal.name);
        animal.sound();  // polymorphic call
    }
}

// Usage
Vet vet = new Vet();
vet.checkup(new Dog("Rex"));    // Checking Rex → Rex says: Woof! Woof!
vet.checkup(new Cat("Luna"));   // Checking Luna → Luna says: Meow!
vet.checkup(new Bird("Tweety"));// Checking Tweety → Tweety says: Tweet!
```

---

## instanceof & Downcasting

```java
Animal a = new Dog("Rex");

// Check actual type
if (a instanceof Dog) {
    Dog d = (Dog) a;        // downcast: Animal → Dog
    d.bark();               // can now call Dog-specific methods
}

// Java 16+ pattern matching
if (a instanceof Dog d) {
    d.bark();               // shorter syntax
}
```

---

## Compile-Time vs Runtime Summary

```
┌─────────┬───────────────────────────┬────────────────────────────┐
│         │  COMPILE-TIME             │  RUNTIME                   │
│         │  (Overloading)            │  (Overriding)              │
├─────────┼───────────────────────────┼────────────────────────────┤
│ WHERE   │  Same class              │  Parent ↔ Child            │
│ WHAT    │  Same name, diff params  │  Same name, same params    │
│ WHEN    │  Compiler decides        │  JVM decides at runtime    │
│ BASED ON│  Argument list           │  Actual object type        │
│ KEYWORD │  (none needed)           │  @Override                 │
│ BINDING │  Early binding           │  Late binding              │
│ SPEED   │  Faster (resolved early) │  Slightly slower           │
│ EXAMPLE │  print(int) print(Str)   │  dog.sound() vs cat.sound()│
└─────────┴───────────────────────────┴────────────────────────────┘
```

---
---

# PILLAR 4 — ABSTRACTION

## What is Abstraction?

Abstraction means **hiding the complex implementation details and showing only the essential features** to the user. The user knows **WHAT** something does, but not **HOW** it does it.

**Analogy:**
- You press the **brake pedal** in a car → the car stops.
- You don't know the hydraulic mechanism, ABS system, disc calipers inside.
- You interact with a **simple interface** (pedal), the complexity is **hidden**.

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  USER SEES:                    HIDDEN INSIDE:                   │
│  ──────────                    ──────────────                   │
│                                                                 │
│  ┌─────────────┐              ┌──────────────────────────────┐ │
│  │ ATM Machine │              │ • Connect to bank server     │ │
│  │             │              │ • Authenticate card + PIN    │ │
│  │ [Withdraw]  │ ──────────►  │ • Check balance              │ │
│  │ [Balance ]  │              │ • Debit account              │ │
│  │ [Transfer]  │              │ • Dispense cash              │ │
│  └─────────────┘              │ • Print receipt              │ │
│                               │ • Log transaction            │ │
│  Simple buttons               └──────────────────────────────┘ │
│  (abstraction)                Complex logic (hidden)           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## How to Achieve Abstraction in Java

```
┌───────────────────────────────────────────────────────────┐
│                 ABSTRACTION IN JAVA                       │
├─────────────────────────┬─────────────────────────────────┤
│   1. Abstract Classes   │   2. Interfaces                 │
│   (partial abstraction) │   (full abstraction)            │
├─────────────────────────┼─────────────────────────────────┤
│   Can have:             │   Methods: abstract by default  │
│   • abstract methods    │   Fields: static final only     │
│   • concrete methods    │   No constructors               │
│   • fields              │   Multiple inheritance OK       │
│   • constructors        │   Java 8+: default, static      │
│   Single inheritance    │   Java 9+: private methods      │
│                         │                                 │
│   0-100% abstraction    │   100% abstraction (pure)       │
└─────────────────────────┴─────────────────────────────────┘
```

---

## Abstract Class — In Detail

An abstract class is a class declared with `abstract`. It **cannot be instantiated** and may contain abstract methods (no body) that child classes **must** implement.

```
  ┌────────────────────────────────────────────────┐
  │        abstract class Payment                   │
  │                                                │
  │  Fields:  double amount                        │
  │                                                │
  │  Abstract:  processPayment()    ← no body      │
  │             getReceipt()        ← no body      │
  │                                                │
  │  Concrete:  validate()          ← has body     │
  │             logTransaction()    ← has body     │
  │                                                │
  │  ✖ Cannot do:  new Payment()                   │
  └──────────────────┬─────────────────────────────┘
                     │ extends
          ┌──────────┴──────────┐
     ┌────▼─────┐         ┌────▼──────┐
     │CreditCard│         │  PayPal   │
     │Payment   │         │  Payment  │
     ├──────────┤         ├───────────┤
     │process() │ ← MUST  │ process() │ ← MUST
     │receipt() │   IMPL  │ receipt() │   IMPL
     └──────────┘         └───────────┘
```

### Code Example — Abstract Payment System

```java
abstract class Payment {
    double amount;
    String payer;

    Payment(String payer, double amount) {
        this.payer = payer;
        this.amount = amount;
    }

    // ABSTRACT — child MUST implement (the WHAT)
    abstract void processPayment();
    abstract String getReceipt();

    // CONCRETE — shared logic (reusable)
    void validate() {
        if (amount <= 0) {
            System.out.println("Invalid amount!");
        } else {
            System.out.println("Amount $" + amount + " validated.");
        }
    }

    void logTransaction() {
        System.out.println("LOG: " + payer + " paid $" + amount);
    }
}

class CreditCardPayment extends Payment {
    String cardNumber;

    CreditCardPayment(String payer, double amount, String cardNumber) {
        super(payer, amount);
        this.cardNumber = cardNumber;
    }

    @Override
    void processPayment() {
        validate();
        System.out.println("Processing credit card: " + cardNumber);
        System.out.println("Charged $" + amount + " to card.");
        logTransaction();
    }

    @Override
    String getReceipt() {
        return "CC Receipt | " + payer + " | $" + amount + " | Card: ****" +
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
    void processPayment() {
        validate();
        System.out.println("Processing PayPal: " + email);
        System.out.println("Transferred $" + amount + " via PayPal.");
        logTransaction();
    }

    @Override
    String getReceipt() {
        return "PayPal Receipt | " + payer + " | $" + amount + " | " + email;
    }
}

// Usage — ABSTRACTION + POLYMORPHISM combined
public class Main {
    public static void main(String[] args) {
        Payment p1 = new CreditCardPayment("Alice", 150.00, "1234567890123456");
        Payment p2 = new PayPalPayment("Bob", 75.50, "bob@email.com");

        p1.processPayment();
        System.out.println(p1.getReceipt());
        System.out.println();
        p2.processPayment();
        System.out.println(p2.getReceipt());
    }
}

/*
Output:
Amount $150.0 validated.
Processing credit card: 1234567890123456
Charged $150.0 to card.
LOG: Alice paid $150.0
CC Receipt | Alice | $150.0 | Card: ****3456

Amount $75.5 validated.
Processing PayPal: bob@email.com
Transferred $75.5 via PayPal.
LOG: Bob paid $75.5
PayPal Receipt | Bob | $75.5 | bob@email.com
*/
```

---

## Interface — In Detail

An interface is a **pure contract**. It defines **what** a class must do, not how. All methods are `public abstract` by default.

```
  ┌──────────────────────┐
  │  interface Printable  │     ← contract
  │    void print();      │
  │    void preview();    │
  └──────────┬───────────┘
             │ implements
   ┌─────────┴─────────┐
   │                   │
┌──▼──────┐       ┌───▼──────┐
│ Invoice │       │  Report  │
│ print() │       │  print() │
│ preview()       │  preview()
└─────────┘       └──────────┘

  ANY class that implements Printable
  GUARANTEES it has print() and preview().
```

### Code Example — Interface with Multiple Implementation

```java
interface Notifiable {
    void sendNotification(String message);
    String getChannel();
}

class EmailNotifier implements Notifiable {
    String email;

    EmailNotifier(String email) {
        this.email = email;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("EMAIL to " + email + ": " + message);
    }

    @Override
    public String getChannel() {
        return "Email";
    }
}

class SMSNotifier implements Notifiable {
    String phone;

    SMSNotifier(String phone) {
        this.phone = phone;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("SMS to " + phone + ": " + message);
    }

    @Override
    public String getChannel() {
        return "SMS";
    }
}

class PushNotifier implements Notifiable {
    String deviceId;

    PushNotifier(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void sendNotification(String message) {
        System.out.println("PUSH to " + deviceId + ": " + message);
    }

    @Override
    public String getChannel() {
        return "Push Notification";
    }
}

// Usage — treat all uniformly via interface
public class Main {
    public static void main(String[] args) {
        Notifiable[] channels = {
            new EmailNotifier("alice@mail.com"),
            new SMSNotifier("+1234567890"),
            new PushNotifier("device-abc-123")
        };

        for (Notifiable n : channels) {
            System.out.print("[" + n.getChannel() + "] ");
            n.sendNotification("Your order has shipped!");
        }
    }
}

/*
Output:
[Email] EMAIL to alice@mail.com: Your order has shipped!
[SMS] SMS to +1234567890: Your order has shipped!
[Push Notification] PUSH to device-abc-123: Your order has shipped!
*/
```

---

## Abstraction Levels Diagram

```
  ABSTRACTION SPECTRUM
  ═══════════════════════════════════════════════════════

  0% Abstract              50% Abstract            100% Abstract
  (Concrete Class)         (Abstract Class)         (Interface)
  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
  │ All methods │         │ Some abstract│         │ All abstract │
  │ have body   │         │ Some concrete│         │ (no body)    │
  │             │         │ Has fields   │         │ Constants    │
  │ Can create  │         │ Cannot create│         │ Cannot create│
  │ objects     │         │ objects      │         │ objects      │
  └─────────────┘         └─────────────┘         └─────────────┘
        │                       │                       │
     Dog dog =            ✖ new Animal()          ✖ new Drawable()
     new Dog();           Animal a = new Dog();   Drawable d = new Circle();
```

---

## When to Use Abstract Class vs Interface

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│   USE ABSTRACT CLASS when:          USE INTERFACE when:          │
│   ─────────────────────             ────────────────────         │
│                                                                  │
│   • Related classes share           • Unrelated classes share    │
│     common code (IS-A)                a capability (CAN-DO)     │
│                                                                  │
│   • You need constructors           • You need multiple          │
│     or instance fields                inheritance               │
│                                                                  │
│   • You want to provide             • You want to define a      │
│     partial implementation            pure contract             │
│                                                                  │
│   EXAMPLE:                          EXAMPLE:                     │
│   abstract class Vehicle {          interface Flyable {          │
│       String brand;                     void fly();             │
│       void honk() { ... }           }                           │
│       abstract void drive();        Bird implements Flyable     │
│   }                                 Airplane implements Flyable │
│   Car extends Vehicle               Drone implements Flyable    │
│   Truck extends Vehicle                                         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---
---

# ALL 4 PILLARS — Combined Example

A real-world-style example using all four pillars together.

```java
// ═══════════════════ ABSTRACTION (Interface) ═══════════════════
interface Taxable {
    double calculateTax();
    String getTaxCategory();
}

// ═══════════════════ ABSTRACTION (Abstract Class) ═══════════════════
abstract class Employee {
    // ═══════════════════ ENCAPSULATION ═══════════════════
    private String name;
    private int id;
    private double baseSalary;

    Employee(String name, int id, double baseSalary) {
        this.name = name;
        this.id = id;
        this.baseSalary = baseSalary;
    }

    // Getters (controlled access)
    public String getName()      { return name; }
    public int getId()           { return id; }
    public double getBaseSalary(){ return baseSalary; }

    // Setter with validation
    public void setBaseSalary(double salary) {
        if (salary >= 0) this.baseSalary = salary;
    }

    // Abstract method — each employee type calculates pay differently
    abstract double calculatePay();

    void displayInfo() {
        System.out.println("ID: " + id + " | Name: " + name +
                          " | Base: $" + baseSalary);
    }
}

// ═══════════════════ INHERITANCE + IMPLEMENTATION ═══════════════════
class FullTimeEmployee extends Employee implements Taxable {
    private double bonus;

    FullTimeEmployee(String name, int id, double baseSalary, double bonus) {
        super(name, id, baseSalary);
        this.bonus = bonus;
    }

    // POLYMORPHISM — own version of calculatePay
    @Override
    double calculatePay() {
        return getBaseSalary() + bonus;
    }

    @Override
    public double calculateTax() {
        return calculatePay() * 0.20;   // 20% tax
    }

    @Override
    public String getTaxCategory() {
        return "Full-Time (20%)";
    }
}

class ContractEmployee extends Employee implements Taxable {
    private int hoursWorked;
    private double hourlyRate;

    ContractEmployee(String name, int id, double hourlyRate, int hoursWorked) {
        super(name, id, 0);
        this.hourlyRate = hourlyRate;
        this.hoursWorked = hoursWorked;
    }

    // POLYMORPHISM — different calculation
    @Override
    double calculatePay() {
        return hourlyRate * hoursWorked;
    }

    @Override
    public double calculateTax() {
        return calculatePay() * 0.10;   // 10% tax
    }

    @Override
    public String getTaxCategory() {
        return "Contract (10%)";
    }
}

// ═══════════════════ USAGE — POLYMORPHISM IN ACTION ═══════════════════
public class Main {
    public static void main(String[] args) {
        Employee[] staff = {
            new FullTimeEmployee("Alice", 101, 5000, 1000),
            new ContractEmployee("Bob", 102, 50, 160),
            new FullTimeEmployee("Charlie", 103, 6000, 1500),
            new ContractEmployee("Diana", 104, 75, 120)
        };

        System.out.println("══════════ PAYROLL REPORT ══════════");
        double totalPayroll = 0;

        for (Employee e : staff) {
            e.displayInfo();
            double pay = e.calculatePay();     // polymorphic
            System.out.println("  Pay: $" + pay);

            if (e instanceof Taxable t) {      // check interface
                System.out.println("  Tax: $" + t.calculateTax() +
                                 " [" + t.getTaxCategory() + "]");
            }

            totalPayroll += pay;
            System.out.println("────────────────────────────────");
        }

        System.out.println("TOTAL PAYROLL: $" + totalPayroll);
    }
}

/*
Output:
══════════ PAYROLL REPORT ══════════
ID: 101 | Name: Alice | Base: $5000.0
  Pay: $6000.0
  Tax: $1200.0 [Full-Time (20%)]
────────────────────────────────────
ID: 102 | Name: Bob | Base: $0.0
  Pay: $8000.0
  Tax: $800.0 [Contract (10%)]
────────────────────────────────────
ID: 103 | Name: Charlie | Base: $6000.0
  Pay: $7500.0
  Tax: $1500.0 [Full-Time (20%)]
────────────────────────────────────
ID: 104 | Name: Diana | Base: $0.0
  Pay: $9000.0
  Tax: $900.0 [Contract (10%)]
────────────────────────────────────
TOTAL PAYROLL: $30500.0
*/
```

---

## How Each Pillar Plays a Role Above

```
┌────────────────┬────────────────────────────────────────────────┐
│ PILLAR         │ WHERE IT'S USED                               │
├────────────────┼────────────────────────────────────────────────┤
│ Encapsulation  │ private fields + getters/setters in Employee  │
│                │ Salary cannot be set to negative              │
├────────────────┼────────────────────────────────────────────────┤
│ Inheritance    │ FullTimeEmployee extends Employee             │
│                │ ContractEmployee extends Employee             │
│                │ Both reuse displayInfo(), getName(), etc.     │
├────────────────┼────────────────────────────────────────────────┤
│ Polymorphism   │ Employee[] holds different types              │
│                │ calculatePay() returns different results      │
│                │ based on actual object at runtime             │
├────────────────┼────────────────────────────────────────────────┤
│ Abstraction    │ abstract class Employee — can't instantiate   │
│                │ abstract calculatePay() — forces children     │
│                │ interface Taxable — defines tax contract      │
│                │ Main only calls methods, not internal logic   │
└────────────────┴────────────────────────────────────────────────┘
```

---

## Final Cheat Sheet

```
┌───────────────┬──────────────────────┬──────────────────────────────┐
│   PILLAR      │   ONE-LINE SUMMARY   │   KEY MECHANISM              │
├───────────────┼──────────────────────┼──────────────────────────────┤
│ Encapsulation │ Hide data, expose API│ private fields + getters     │
│ Inheritance   │ Reuse parent code    │ extends keyword              │
│ Polymorphism  │ Many forms, one call │ overloading + overriding     │
│ Abstraction   │ Show what, hide how  │ abstract class + interface   │
└───────────────┴──────────────────────┴──────────────────────────────┘
```

---
