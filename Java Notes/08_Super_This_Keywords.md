# 08 — Super & This Keywords (In Depth)

---

## Overview

`this` and `super` are **reference keywords** — they point to objects so you can access their members.

```
╔═══════════════════════════════════════════════════════════════╗
║        this                    vs            super            ║
╠═══════════════════════════════╦═══════════════════════════════╣
║  Points to CURRENT object    ║  Points to PARENT class part  ║
║  "Myself"                    ║  "My parent"                  ║
╠═══════════════════════════════╬═══════════════════════════════╣
║  this.field                  ║  super.field                  ║
║  this.method()               ║  super.method()               ║
║  this()    → own constructor ║  super()  → parent constructor║
╠═══════════════════════════════╬═══════════════════════════════╣
║  Used within same class      ║  Used in child class          ║
║  Resolve ambiguity           ║  Access hidden parent members ║
║  Constructor chaining        ║  Call parent constructor       ║
╚═══════════════════════════════╩═══════════════════════════════╝
```

---
---

# `this` KEYWORD — Current Object Reference

`this` refers to the **current object** — the instance on which the method/constructor was called.

## All Uses of `this`

```
┌────────────────────────────────────────────────────────────────┐
│                    5 USES OF this                               │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  1. this.field        Distinguish field from parameter         │
│  2. this.method()     Call another method of current object    │
│  3. this()            Call another constructor (chaining)      │
│  4. return this       Return current object (method chaining)  │
│  5. pass this         Pass current object as argument          │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

### Use 1: Resolve Field vs Parameter Ambiguity

When a constructor/method parameter has the **same name** as a field, `this.field` refers to the **instance variable**, and the bare name refers to the **parameter**.

```
  ┌──────────────────────────────────────────────────┐
  │   Student(String name, int age) {                │
  │                                                  │
  │     this.name  =  name;                          │
  │     ─────────     ────                           │
  │     instance      parameter                      │
  │     variable      (local)                        │
  │     (field on     (passed in                     │
  │      the object)   by caller)                    │
  │                                                  │
  │   Without 'this':                                │
  │     name = name;  ← parameter assigns to itself! │
  │     Field stays null.  BUG!                      │
  │                                                  │
  └──────────────────────────────────────────────────┘
```

```java
class Student {
    String name;       // instance field
    int age;           // instance field
    double gpa;        // instance field

    Student(String name, int age, double gpa) {
        // Without this → name = name; would be useless (self-assignment)
        this.name = name;   // this.name = field, name = parameter
        this.age = age;     // this.age = field, age = parameter
        this.gpa = gpa;           
    }

    void display() {
        System.out.println(this.name + " | Age: " + this.age + " | GPA: " + this.gpa);
        // 'this' is optional here (no ambiguity), but makes it explicit
    }
}

Student s = new Student("Alice", 20, 3.9);
s.display();  // Alice | Age: 20 | GPA: 3.9
```

---

### Use 2: Call Another Method of Current Object

```java
class Validator {
    String data;

    Validator(String data) {
        this.data = data;
    }

    boolean isNotEmpty() {
        return data != null && !data.isEmpty();
    }

    boolean isValidEmail() {
        return this.isNotEmpty() && data.contains("@") && data.contains(".");
        //     ^^^^
        //     calls own method (this is optional but adds clarity)
    }

    void validate() {
        if (this.isValidEmail()) {
            System.out.println(data + " is a valid email");
        } else {
            System.out.println(data + " is NOT a valid email");
        }
    }
}

new Validator("alice@mail.com").validate();  // valid
new Validator("hello").validate();            // NOT valid
```

---

### Use 3: Constructor Chaining with `this()`

One constructor calls another constructor in the **same class** using `this()`. Must be the **FIRST statement**.

```
  ┌─────────────────────────────────────────────────────────────┐
  │                   CONSTRUCTOR CHAINING                      │
  │                                                             │
  │   new Employee()                                           │
  │        │                                                    │
  │        ▼  this("Unknown", 0)                               │
  │   Employee() ───────────────►  Employee(String, double)    │
  │                                       │                     │
  │   new Employee("Alice")               │                     │
  │        │                              │                     │
  │        ▼  this(name, 0)               │                     │
  │   Employee(String) ─────────► Employee(String, double)     │
  │                                       │                     │
  │                                 Sets all fields             │
  │                                                             │
  │   All roads lead to ONE master constructor                 │
  └─────────────────────────────────────────────────────────────┘
```

```java
class Employee {
    String name;
    double salary;
    String department;

    // Master constructor — does the real work
    Employee(String name, double salary, String department) {
        this.name = name;
        this.salary = salary;
        this.department = department;
    }

    Employee() {
        this("Unknown", 0, "General");  // chains to master — MUST be first line
    }

    Employee(String name) {
        this(name, 0, "General");       // chains to master
    }

    Employee(String name, double salary) {
        this(name, salary, "General");  // chains to master
    }

    void display() {
        System.out.println(name + " | $" + salary + " | " + department);
    }
}

new Employee().display();                          // Unknown | $0 | General
new Employee("Alice").display();                   // Alice | $0 | General
new Employee("Bob", 5000).display();               // Bob | $5000 | General
new Employee("Charlie", 7000, "IT").display();     // Charlie | $7000 | IT
```

---

### Use 4: Return `this` (Method Chaining / Fluent API)

Returning `this` allows calling multiple methods in a single chain.

```
  WITHOUT method chaining:          WITH method chaining (return this):
  ─────────────────────            ────────────────────────────────────
  Builder b = new Builder();       new Builder()
  b.setName("Alice");                 .setName("Alice")
  b.setAge(25);                       .setAge(25)
  b.setCity("NYC");                   .setCity("NYC")
  b.build();                          .build();
```

```java
class QueryBuilder {
    String table;
    String condition;
    String orderBy;
    int limit;

    QueryBuilder select(String table) {
        this.table = table;
        return this;         // returns current object
    }

    QueryBuilder where(String condition) {
        this.condition = condition;
        return this;
    }

    QueryBuilder orderBy(String field) {
        this.orderBy = field;
        return this;
    }

    QueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    String build() {
        String query = "SELECT * FROM " + table;
        if (condition != null) query += " WHERE " + condition;
        if (orderBy != null) query += " ORDER BY " + orderBy;
        if (limit > 0) query += " LIMIT " + limit;
        return query;
    }
}

// Fluent chaining — reads like a sentence
String query = new QueryBuilder()
    .select("users")
    .where("age > 18")
    .orderBy("name")
    .limit(10)
    .build();

System.out.println(query);
// SELECT * FROM users WHERE age > 18 ORDER BY name LIMIT 10
```

---

### Use 5: Pass `this` as Argument

Pass the current object to another method or class.

```java
class Printer {
    void printStudent(Student s) {
        System.out.println("Printing: " + s.name + ", " + s.age);
    }
}

class Student {
    String name;
    int age;

    Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    void requestPrint() {
        Printer printer = new Printer();
        printer.printStudent(this);   // passes current Student object
        //                   ^^^^
        //    "Send myself to the printer"
    }
}

new Student("Alice", 20).requestPrint();  // Printing: Alice, 20
```

---
---

# `super` KEYWORD — Parent Class Reference

`super` refers to the **parent class** part of the current object. Used in subclasses to access parent members that are hidden or overridden.

## All Uses of `super`

```
┌────────────────────────────────────────────────────────────────┐
│                    3 USES OF super                              │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  1. super.field        Access parent's field (when hidden)     │
│  2. super.method()     Call parent's method (when overridden)  │
│  3. super()            Call parent's constructor               │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

### Use 1: Access Parent's Field (Field Hiding)

When child and parent have a field with the **same name**, the child's field **hides** the parent's.

```
  ┌─────────────────────┐
  │  class Animal       │
  │  String type = "A"  │   ← parent's 'type'
  └──────────┬──────────┘
             │ extends
  ┌──────────▼──────────┐
  │  class Dog          │
  │  String type = "D"  │   ← child's 'type' HIDES parent's
  │                     │
  │  type       → "D"   │   child's
  │  super.type → "A"   │   parent's
  └─────────────────────┘
```

```java
class Animal {
    String type = "Animal";
    int legs = 4;
}

class Dog extends Animal {
    String type = "Dog";      // hides Animal's 'type'

    void showTypes() {
        System.out.println("this.type  = " + this.type);   // Dog
        System.out.println("super.type = " + super.type);  // Animal
        System.out.println("legs       = " + legs);        // 4 (inherited, no hiding)
    }
}

new Dog().showTypes();
/*
this.type  = Dog
super.type = Animal
legs       = 4
*/
```

---

### Use 2: Call Parent's Method (When Overridden)

When a child overrides a parent method, `super.method()` calls the **parent's version** instead.

```
  ┌──────────────────────┐
  │  class Animal        │
  │  void sound() {      │
  │    "Generic sound"   │   ← parent version
  │  }                   │
  └──────────┬───────────┘
             │ extends
  ┌──────────▼───────────┐
  │  class Dog           │
  │  @Override           │
  │  void sound() {      │
  │    super.sound();    │   ← calls parent FIRST
  │    "Woof!"           │   ← then adds own behavior
  │  }                   │
  └──────────────────────┘
```

```java
class Animal {
    void sound() {
        System.out.println("Some generic animal sound");
    }

    void eat() {
        System.out.println("Animal is eating");
    }
}

class Dog extends Animal {
    @Override
    void sound() {
        super.sound();   // call parent's version first
        System.out.println("Woof! Woof!");
    }

    @Override
    void eat() {
        super.eat();     // reuse parent logic
        System.out.println("Dog is eating bones");  // add extra behavior
    }
}

Dog d = new Dog();
d.sound();
// Some generic animal sound   ← from super.sound()
// Woof! Woof!                 ← from Dog's own code

d.eat();
// Animal is eating            ← from super.eat()
// Dog is eating bones         ← from Dog's own code
```

---

### Use 3: Call Parent's Constructor — `super()`

Calls the parent class constructor. **Must be the FIRST line** in the child constructor.

```
  ┌─────────────────────────────────────────────────────────────┐
  │                                                             │
  │  new Dog("Rex", 3, "Labrador")                             │
  │         │                                                   │
  │         ▼                                                   │
  │  Dog constructor called                                    │
  │         │                                                   │
  │    super("Rex", 3);   ← FIRST line, calls parent           │
  │         │                                                   │
  │         ▼                                                   │
  │  Animal constructor runs                                   │
  │    this.name = "Rex"                                       │
  │    this.age = 3                                            │
  │         │                                                   │
  │         ▼                                                   │
  │  Back to Dog constructor                                   │
  │    this.breed = "Labrador"                                 │
  │         │                                                   │
  │         ▼                                                   │
  │  Object fully initialized                                  │
  │                                                             │
  └─────────────────────────────────────────────────────────────┘
```

```java
class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
        System.out.println("Person constructor: " + name + ", " + age);
    }
}

class Student extends Person {
    String major;
    double gpa;

    Student(String name, int age, String major, double gpa) {
        super(name, age);       // ← MUST be first line
        this.major = major;
        this.gpa = gpa;
        System.out.println("Student constructor: " + major + ", " + gpa);
    }

    void display() {
        System.out.println(name + " | Age: " + age + " | " + major + " | GPA: " + gpa);
    }
}

class GradStudent extends Student {
    String thesis;

    GradStudent(String name, int age, String major, double gpa, String thesis) {
        super(name, age, major, gpa);   // calls Student constructor
        this.thesis = thesis;
        System.out.println("GradStudent constructor: " + thesis);
    }
}

// Usage
GradStudent g = new GradStudent("Alice", 25, "CS", 3.9, "AI Ethics");
/*
Person constructor: Alice, 25          ← runs FIRST (grandparent)
Student constructor: CS, 3.9           ← runs SECOND (parent)
GradStudent constructor: AI Ethics     ← runs LAST (child)
*/
```

---

## Important Rules

```
┌──────────────────────────────────────────────────────────────────┐
│                     CRITICAL RULES                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. super() must be the FIRST statement in constructor          │
│  2. this() must be the FIRST statement in constructor           │
│                                                                  │
│  3. You CANNOT use both this() and super() in the same          │
│     constructor — both need to be first line → impossible!      │
│                                                                  │
│  4. If no super() is written explicitly, Java AUTOMATICALLY     │
│     inserts super() (no-arg) as the first line                  │
│     → Parent MUST have a no-arg constructor, or COMPILE ERROR   │
│                                                                  │
│  5. super and this CANNOT be used in static methods             │
│     (no object context in static)                               │
│                                                                  │
│  6. Constructor chaining always goes UP to the topmost parent   │
│     (ultimately to Object class — the root of all classes)      │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

### What Happens if You DON'T Write super()?

```java
class Parent {
    // Has no-arg constructor (default)
    Parent() {
        System.out.println("Parent constructor");
    }
}

class Child extends Parent {
    Child() {
        // Java secretly inserts: super(); here!
        System.out.println("Child constructor");
    }
}

new Child();
// Parent constructor   ← auto-called by invisible super()
// Child constructor
```

### When it FAILS:

```java
class Parent {
    Parent(String name) {   // ONLY parameterized — no no-arg!
        System.out.println("Parent: " + name);
    }
}

class Child extends Parent {
    Child() {
        // Java tries to insert super(); but Parent has NO no-arg constructor!
        // ← COMPILE ERROR: "no suitable constructor found"

        // FIX: explicitly call super with argument
        super("Default");  // ← this fixes the error
    }
}
```

---
---

# this vs super — Complete Comparison

```
╔══════════════════╦══════════════════════════╦══════════════════════════╗
║     Feature      ║        this              ║        super             ║
╠══════════════════╬══════════════════════════╬══════════════════════════╣
║ Refers to        ║ Current object           ║ Parent class part        ║
║ Field access     ║ this.field               ║ super.field              ║
║ Method call      ║ this.method()            ║ super.method()           ║
║ Constructor      ║ this() → same class      ║ super() → parent class  ║
║ Must be first?   ║ YES (in constructor)     ║ YES (in constructor)     ║
║ Can combine?     ║ Cannot use with super()  ║ Cannot use with this()   ║
║ Static context?  ║ NOT allowed              ║ NOT allowed              ║
║ Purpose          ║ Resolve ambiguity        ║ Access parent members    ║
║                  ║ Method chaining           ║ Extend parent behavior  ║
║                  ║ Constructor chaining      ║ Initialize parent       ║
╚══════════════════╩══════════════════════════╩══════════════════════════╝
```

---

# Full Example — Employee System

```java
class Person {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;       // this: resolve ambiguity
        this.age = age;
    }

    void introduce() {
        System.out.println("Hi, I'm " + name + ", age " + age);
    }
}

class Employee extends Person {
    String company;
    double salary;
    static int totalEmployees = 0;

    // Constructor chaining with this() and super()
    Employee(String name, int age) {
        this(name, age, "Unknown", 0);  // this() → chains to full constructor
    }

    Employee(String name, int age, String company, double salary) {
        super(name, age);               // super() → initializes Person part
        this.company = company;         // this: resolve ambiguity
        this.salary = salary;
        totalEmployees++;
    }

    @Override
    void introduce() {
        super.introduce();              // super.method() → call parent's version
        System.out.println("I work at " + company + " earning $" + salary);
    }

    // Method chaining with return this
    Employee promote(double raise) {
        this.salary += raise;
        System.out.println(this.name + " promoted! New salary: $" + this.salary);
        return this;                    // return this → enables chaining
    }

    Employee changeDepartment(String company) {
        this.company = company;
        return this;
    }

    void display() {
        System.out.println("── " + name + " | " + age + " | " + company +
                         " | $" + salary + " ──");
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Employee e1 = new Employee("Alice", 28, "Google", 8000);
        Employee e2 = new Employee("Bob", 30);

        e1.introduce();
        // Hi, I'm Alice, age 28        ← super.introduce()
        // I work at Google earning $8000 ← Employee's own

        System.out.println();

        // Method chaining with 'return this'
        e1.promote(2000)
          .changeDepartment("Google Cloud")
          .promote(1000);

        // Alice promoted! New salary: $10000
        // Alice promoted! New salary: $11000

        e1.display();  // ── Alice | 28 | Google Cloud | $11000 ──
        e2.display();  // ── Bob | 30 | Unknown | $0 ──

        System.out.println("Total employees: " + Employee.totalEmployees);  // 2
    }
}
```

---
