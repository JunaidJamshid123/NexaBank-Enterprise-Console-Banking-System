# 04 — OOP: Classes, Objects & Constructors (In Depth)

---

## What is OOP?

**Object-Oriented Programming** organizes code around **objects** — self-contained units that bundle data (what they know) and behavior (what they can do).

```
  PROCEDURAL STYLE:                OOP STYLE:
  ────────────────                 ──────────
  Loose functions + data           Objects contain both

  String name = "Alice";           class Student {
  int age = 20;                        String name;
  void printInfo() { ... }             int age;
  void updateAge() { ... }             void printInfo() { ... }
                                       void updateAge() { ... }
  Data and functions are           }
  SEPARATE — easy to               → Data + behavior are
  break connections                    BUNDLED together
```

**Analogy:** In real life, a car has properties (color, speed, brand) and behaviors (accelerate, brake, honk). OOP models code the same way.

```
┌──────────────────────────────────────────────────┐
│                 4 PILLARS OF OOP                 │
├───────────────┬──────────────────────────────────┤
│ Encapsulation │ Wrap data + methods, hide access │
│ Inheritance   │ Child reuses parent code         │
│ Polymorphism  │ One name, many behaviors         │
│ Abstraction   │ Show "what", hide "how"          │
└───────────────┴──────────────────────────────────┘
```

---
---

# 1. CLASS — The Blueprint

A **class** is a template/blueprint that defines what fields (data) and methods (behavior) objects of that type will have. A class does NOT occupy memory for data — only its objects do.

```
  REAL-WORLD ANALOGY:

  ┌──────────────────────┐
  │   BLUEPRINT          │       ← CLASS
  │   (House Plan)       │
  │                      │       Defines: rooms, doors, windows
  │   Not a real house!  │       Cannot live in a blueprint
  └──────────┬───────────┘
             │ build (new)
    ┌────────┴──────────┐
    │                   │
  ┌─▼──────────┐  ┌────▼─────────┐
  │  House #1  │  │  House #2    │   ← OBJECTS
  │  Red color │  │  Blue color  │
  │  3 rooms   │  │  5 rooms     │   Real houses built from
  └────────────┘  └──────────────┘   the same blueprint
```

## Class Structure

```
┌──────────────────────────────────────────────────┐
│                  CLASS ANATOMY                   │
├──────────────────────────────────────────────────┤
│                                                  │
│  class ClassName {                               │
│                                                  │
│     ┌─── FIELDS (attributes/properties) ─────┐  │
│     │  What the object KNOWS                 │  │
│     │  String name;                          │  │
│     │  int age;                              │  │
│     │  double salary;                        │  │
│     └────────────────────────────────────────┘  │
│                                                  │
│     ┌─── CONSTRUCTORS ──────────────────────┐   │
│     │  How the object is CREATED             │  │
│     │  ClassName(params) { ... }             │  │
│     └────────────────────────────────────────┘  │
│                                                  │
│     ┌─── METHODS (behavior/actions) ────────┐   │
│     │  What the object CAN DO                │  │
│     │  void display() { ... }                │  │
│     │  double calculatePay() { ... }         │  │
│     └────────────────────────────────────────┘  │
│                                                  │
│  }                                               │
└──────────────────────────────────────────────────┘
```

### Code Example

```java
class Car {
    // ═══ FIELDS — what the car KNOWS ═══
    String brand;
    String color;
    int speed;
    boolean isRunning;

    // ═══ METHODS — what the car CAN DO ═══
    void start() {
        isRunning = true;
        System.out.println(brand + " started!");
    }

    void accelerate(int amount) {
        if (isRunning) {
            speed += amount;
            System.out.println(brand + " speed: " + speed + " km/h");
        } else {
            System.out.println("Start the car first!");
        }
    }

    void brake() {
        speed = Math.max(0, speed - 20);
        System.out.println(brand + " braking... Speed: " + speed + " km/h");
    }

    void stop() {
        speed = 0;
        isRunning = false;
        System.out.println(brand + " stopped.");
    }

    void displayInfo() {
        System.out.println("Brand: " + brand + " | Color: " + color +
                          " | Speed: " + speed + " | Running: " + isRunning);
    }
}
```

---
---

# 2. OBJECT — The Instance

An **object** is a **real instance** created from a class blueprint. Each object has its **own copy** of the fields, but shares the same method definitions.

```
  CLASS (template)                    OBJECTS (real instances)
  ┌──────────────────┐
  │     Car          │
  │  brand, color    │     ┌────────────────┐  ┌────────────────┐
  │  speed, isRunning│ ──► │   car1         │  │   car2         │
  │                  │     │  brand="BMW"   │  │  brand="Audi"  │
  │  start()         │     │  color="Red"   │  │  color="Black" │
  │  accelerate()    │     │  speed=0       │  │  speed=0       │
  │  brake()         │     └────────────────┘  └────────────────┘
  │  stop()          │
  └──────────────────┘     Each object has its OWN data
                           but SAME methods from the class
```

## Creating Objects

```java
//  ClassName  varName  =  new  ClassName();
//  ────────   ──────      ───  ──────────
//  type       ref name    keyword  constructor call

Car car1 = new Car();
Car car2 = new Car();
```

```
  What happens with  new Car():

  1. JVM allocates memory on the HEAP for the new object
  2. All fields initialized to defaults (null, 0, false)
  3. Constructor body runs (if any)
  4. Reference (address) returned and stored in variable

  car1  ──────►  [brand=null, color=null, speed=0, isRunning=false]
                  (object lives on heap)
```

### Using Objects

```java
public class Main {
    public static void main(String[] args) {
        // Create objects
        Car car1 = new Car();
        car1.brand = "BMW";
        car1.color = "Red";

        Car car2 = new Car();
        car2.brand = "Audi";
        car2.color = "Black";

        // Call methods on objects
        car1.start();          // BMW started!
        car1.accelerate(50);   // BMW speed: 50 km/h
        car1.accelerate(30);   // BMW speed: 80 km/h
        car1.brake();          // BMW braking... Speed: 60 km/h

        car2.start();          // Audi started!
        car2.accelerate(100);  // Audi speed: 100 km/h

        // Each object is independent
        car1.displayInfo();    // Brand: BMW  | Speed: 60   | Running: true
        car2.displayInfo();    // Brand: Audi | Speed: 100  | Running: true
    }
}
```

---

## Memory Diagram — Stack vs Heap

```
  STACK (method variables)              HEAP (objects)
  ┌────────────────────┐           ┌─────────────────────────┐
  │                    │           │                         │
  │  main() frame:     │           │  ┌───────────────────┐  │
  │  ┌───────────────┐ │           │  │  Car Object #1    │  │
  │  │ car1 = 0x100 ─┼─┼──────────┼─►│  brand = "BMW"    │  │
  │  │               │ │           │  │  color = "Red"    │  │
  │  │ car2 = 0x200 ─┼─┼──────┐   │  │  speed = 60       │  │
  │  │               │ │      │   │  │  isRunning = true  │  │
  │  └───────────────┘ │      │   │  └───────────────────┘  │
  │                    │      │   │                         │
  └────────────────────┘      │   │  ┌───────────────────┐  │
                              └───┼─►│  Car Object #2    │  │
  car1 and car2 hold                 │  brand = "Audi"   │  │
  REFERENCES (addresses),            │  color = "Black"  │  │
  not the objects themselves          │  speed = 100      │  │
                                     │  isRunning = true  │  │
                                     └───────────────────┘  │
                                  └─────────────────────────┘
```

### Reference Assignment

```java
Car a = new Car();
a.brand = "Tesla";

Car b = a;    // b points to SAME object, NOT a copy!

b.brand = "Ford";
System.out.println(a.brand);  // "Ford" — because a and b are the SAME object!

// ┌──────┐
// │ a ───┼──┐
// └──────┘  │   ┌──────────────────┐
//           ├──►│ Car Object       │
// ┌──────┐  │   │ brand = "Ford"   │
// │ b ───┼──┘   └──────────────────┘
// └──────┘
// Both a and b point to the SAME object on heap
```

---
---

# 3. CONSTRUCTORS — Building Objects

A **constructor** is a special method that runs **automatically** when you create an object with `new`. It initializes the object's fields.

```
┌─────────────────────────────────────────────────────────────┐
│                   CONSTRUCTOR RULES                          │
├─────────────────────────────────────────────────────────────┤
│ 1. Same name as the class                                   │
│ 2. NO return type (not even void)                           │
│ 3. Called automatically by 'new'                            │
│ 4. Can be overloaded (multiple constructors)                │
│ 5. If you write NONE → Java adds a default no-arg one      │
│ 6. If you write ANY → Java does NOT add a default one      │
│ 7. Can call other constructors using this() or super()      │
└─────────────────────────────────────────────────────────────┘
```

---

## Types of Constructors

```
╔═══════════════════════════════════════════════════════════════════════╗
║                     CONSTRUCTOR TYPES                                ║
╠═══════════════════╦══════════════════════╦════════════════════════════╣
║  DEFAULT          ║  PARAMETERIZED       ║  COPY                     ║
║  (No-Arg)         ║  (With Args)         ║  (Clone)                  ║
╠═══════════════════╬══════════════════════╬════════════════════════════╣
║  No parameters    ║  Takes parameters    ║  Takes another object     ║
║  Sets defaults    ║  Custom init values  ║  Copies its field values  ║
║  Auto-provided    ║  Must write manually ║  Must write manually      ║
║  if none written  ║                      ║                            ║
╚═══════════════════╩══════════════════════╩════════════════════════════╝
```

### Object Creation Flow

```
  new Student("Alice", 20)
         │
         ▼
  ┌────────────────────────────────────────┐
  │ 1. JVM allocates memory on HEAP       │
  │ 2. Fields set to defaults:            │
  │    name = null, age = 0               │
  │ 3. Constructor body runs:             │
  │    name = "Alice", age = 20           │
  │ 4. Reference returned to variable     │
  └────────────────────────────────────────┘
         │
         ▼
  Student s ──► [name="Alice", age=20]
```

---

### Default Constructor (No-Arg)

Java auto-provides this if you write no constructors. Sets fields to default values.

```java
class Student {
    String name;
    int age;

    // Default constructor — no parameters
    Student() {
        name = "Unknown";
        age = 0;
        System.out.println("Default constructor called");
    }

    void display() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

// Usage
Student s = new Student();   // Default constructor called
s.display();                 // Name: Unknown, Age: 0
```

---

### Parameterized Constructor

Accepts arguments to initialize fields with specific values.

```java
class Student {
    String name;
    int age;
    String major;

    // Parameterized constructor
    Student(String name, int age, String major) {
        this.name = name;       // this.name = field, name = parameter
        this.age = age;
        this.major = major;
    }

    void display() {
        System.out.println(name + " | Age: " + age + " | Major: " + major);
    }
}

// Usage
Student s1 = new Student("Alice", 20, "CS");
Student s2 = new Student("Bob", 22, "Math");

s1.display();  // Alice | Age: 20 | Major: CS
s2.display();  // Bob | Age: 22 | Major: Math
```

---

### Constructor Overloading

Multiple constructors with **different parameter lists** in the same class. Gives flexibility in how objects are created.

```
  ┌───────────────────────────────────────────────────┐
  │                                                   │
  │  new BankAccount()              → calls 0-arg    │
  │  new BankAccount("Alice")       → calls 1-arg    │
  │  new BankAccount("Alice", 5000) → calls 2-arg    │
  │                                                   │
  │  Java picks the matching constructor              │
  │  based on the number/type of arguments            │
  └───────────────────────────────────────────────────┘
```

```java
class BankAccount {
    String owner;
    double balance;
    String accountType;

    // 0-arg constructor
    BankAccount() {
        this.owner = "Default";
        this.balance = 0;
        this.accountType = "Savings";
    }

    // 1-arg constructor
    BankAccount(String owner) {
        this.owner = owner;
        this.balance = 0;
        this.accountType = "Savings";
    }

    // 2-arg constructor
    BankAccount(String owner, double balance) {
        this.owner = owner;
        this.balance = balance;
        this.accountType = "Savings";
    }

    // 3-arg constructor
    BankAccount(String owner, double balance, String accountType) {
        this.owner = owner;
        this.balance = balance;
        this.accountType = accountType;
    }

    void display() {
        System.out.println(owner + " | $" + balance + " | " + accountType);
    }
}

// Usage — different ways to create
BankAccount a1 = new BankAccount();                           // Default | $0 | Savings
BankAccount a2 = new BankAccount("Alice");                    // Alice | $0 | Savings
BankAccount a3 = new BankAccount("Bob", 5000);                // Bob | $5000 | Savings
BankAccount a4 = new BankAccount("Charlie", 10000, "Current");// Charlie | $10000 | Current
```

---

### Copy Constructor

Creates a **new object** with the same values as an existing object.

```
  Original Object                 Copied Object
  ┌────────────────┐             ┌────────────────┐
  │ name = "Alice" │  ──copy──►  │ name = "Alice" │
  │ age  = 20      │             │ age  = 20      │
  └────────────────┘             └────────────────┘
  0x100 (address)                0x200 (different address!)

  They have the SAME VALUES but are DIFFERENT objects.
  Changing one does NOT affect the other.
```

```java
class Employee {
    String name;
    double salary;
    String department;

    // Regular constructor
    Employee(String name, double salary, String department) {
        this.name = name;
        this.salary = salary;
        this.department = department;
    }

    // Copy constructor — takes another Employee
    Employee(Employee other) {
        this.name = other.name;
        this.salary = other.salary;
        this.department = other.department;
    }

    void display() {
        System.out.println(name + " | $" + salary + " | " + department);
    }
}

// Usage
Employee original = new Employee("Alice", 75000, "Engineering");
Employee copy = new Employee(original);   // copy constructor

copy.salary = 80000;  // change copy only

original.display();  // Alice | $75000 | Engineering  ← unchanged
copy.display();      // Alice | $80000 | Engineering  ← modified independently
```

---

### Constructor Chaining with this()

One constructor calls another constructor in the **same class** using `this()`. Reduces code duplication.

```
  ┌────────────────────────────────────────────────────────┐
  │                                                        │
  │  new Product()                                        │
  │       │                                                │
  │       ▼  calls this("Unknown", 0, 0)                  │
  │  Product()  ────────────────────────►  Product(3-arg)  │
  │                                              │         │
  │  new Product("Phone")                        │         │
  │       │                                      │         │
  │       ▼  calls this(name, 0, 0)              │         │
  │  Product(1-arg)  ───────────────────► Product(3-arg)  │
  │                                              │         │
  │                                      sets all fields  │
  └────────────────────────────────────────────────────────┘

  All constructors eventually lead to ONE that does the actual work.
```

```java
class Product {
    String name;
    double price;
    int quantity;

    // Master constructor — does real work
    Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Chains to master
    Product() {
        this("Unknown", 0.0, 0);   // calls 3-arg
    }

    // Chains to master
    Product(String name) {
        this(name, 0.0, 0);        // calls 3-arg
    }

    // Chains to master
    Product(String name, double price) {
        this(name, price, 0);      // calls 3-arg
    }

    void display() {
        System.out.println(name + " | $" + price + " | Qty: " + quantity);
    }
}

// All valid:
new Product();                      // Unknown | $0.0 | Qty: 0
new Product("Mouse");               // Mouse | $0.0 | Qty: 0
new Product("Keyboard", 49.99);     // Keyboard | $49.99 | Qty: 0
new Product("Monitor", 299.99, 5);  // Monitor | $299.99 | Qty: 5
```

---

## Class vs Object Summary

```
┌──────────────────────┬───────────────────────────────────┐
│      CLASS           │          OBJECT                   │
├──────────────────────┼───────────────────────────────────┤
│ Blueprint/template   │ Real instance                     │
│ Defines structure    │ Holds actual data                 │
│ Created once         │ Created many times (new)          │
│ No memory for data   │ Gets memory on heap               │
│ Logical              │ Physical (in memory)              │
│ Example: Car (plan)  │ Example: my red BMW               │
│ class Car { }        │ Car myCar = new Car();            │
└──────────────────────┴───────────────────────────────────┘
```

---

# Full Example — Student Management

```java
class Student {
    String name;
    int rollNo;
    double[] marks;

    // Parameterized constructor
    Student(String name, int rollNo, double[] marks) {
        this.name = name;
        this.rollNo = rollNo;
        this.marks = marks;
    }

    // Calculate average
    double getAverage() {
        double sum = 0;
        for (double m : marks) {
            sum += m;
        }
        return sum / marks.length;
    }

    // Get grade based on average
    String getGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A+";
        if (avg >= 80) return "A";
        if (avg >= 70) return "B";
        if (avg >= 60) return "C";
        return "F";
    }

    // Display all info
    void displayReport() {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("  Name:    " + name);
        System.out.println("  Roll No: " + rollNo);
        System.out.print("  Marks:   ");
        for (double m : marks) {
            System.out.print(m + " ");
        }
        System.out.println();
        System.out.printf("  Average: %.2f%n", getAverage());
        System.out.println("  Grade:   " + getGrade());
        System.out.println("╚══════════════════════════════╝");
    }
}

public class Main {
    public static void main(String[] args) {
        Student s1 = new Student("Alice", 101, new double[]{85, 92, 78, 95});
        Student s2 = new Student("Bob", 102, new double[]{62, 55, 70, 68});

        s1.displayReport();
        s2.displayReport();
    }
}

/*
╔══════════════════════════════╗
  Name:    Alice
  Roll No: 101
  Marks:   85.0 92.0 78.0 95.0
  Average: 87.50
  Grade:   A
╚══════════════════════════════╝
╔══════════════════════════════╗
  Name:    Bob
  Roll No: 102
  Marks:   62.0 55.0 70.0 68.0
  Average: 63.75
  Grade:   C
╚══════════════════════════════╝
*/
```

---
