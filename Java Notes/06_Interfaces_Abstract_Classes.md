# 06 — Interfaces & Abstract Classes (In Depth)

---

## Why Do We Need Them?

Sometimes you want to define **what** a group of classes must do, without specifying **how** they do it. This is the core of abstraction.

```
  PROBLEM:
  ────────
  You have Dog, Cat, Bird classes that all need sound() and move().
  But each animal sounds and moves DIFFERENTLY.

  SOLUTION:
  ─────────
  Define a "contract" that says:
  "Every Animal MUST have sound() and move()"
  Each subclass provides its OWN implementation.

  This contract = Abstract Class or Interface.
```

---
---

# 1. ABSTRACT CLASS — Partial Blueprint

An abstract class is a **partially complete class** that cannot be instantiated. It can contain both abstract methods (no body = contract) and concrete methods (with body = shared code).

## Why Abstract?

```
┌──────────────────────────────────────────────────────────────┐
│                  WHY ABSTRACT CLASS?                          │
├──────────────────────────────────────────────────────────────┤
│ 1. FORCE children to implement certain methods              │
│    → abstract methods MUST be overridden                    │
│                                                              │
│ 2. SHARE common code among related classes                   │
│    → concrete methods are inherited                         │
│                                                              │
│ 3. PREVENT creating incomplete objects                       │
│    → new Animal() is not meaningful                         │
│                                                              │
│ 4. DEFINE a common type for polymorphism                     │
│    → Animal a = new Dog(); works                            │
└──────────────────────────────────────────────────────────────┘
```

## Abstract Class Rules

```
┌──────────────────────────────────────────────────────────────┐
│                      RULES                                   │
├──────────────────────────────────────────────────────────────┤
│ ✓ Use 'abstract' keyword on class                           │
│ ✓ Can have abstract methods (no body — subclass MUST impl)  │
│ ✓ Can have concrete methods (with body — inherited as-is)   │
│ ✓ Can have fields (any type: private, protected, etc.)      │
│ ✓ Can have constructors (called via super())                │
│ ✓ Can have static methods                                   │
│                                                              │
│ ✗ Cannot be instantiated: new Animal() → COMPILE ERROR      │
│ ✗ A child must implement ALL abstract methods               │
│   OR be abstract itself                                     │
└──────────────────────────────────────────────────────────────┘
```

## Diagram

```
  ┌────────────────────────────────────────────────────┐
  │     abstract class Shape                            │
  │                                                    │
  │  Fields:                                           │
  │     String color;                                  │
  │                                                    │
  │  Constructor:                                      │
  │     Shape(String color)                            │
  │                                                    │
  │  Abstract (contract — no body):                    │
  │     abstract double area();          ← MUST impl  │
  │     abstract double perimeter();     ← MUST impl  │
  │                                                    │
  │  Concrete (shared — has body):                     │
  │     void display() { prints info }   ← inherited  │
  │     String getColor() { return color } ← inherited│
  │                                                    │
  │  ✖ Cannot do: new Shape()                          │
  └──────────────────┬─────────────────────────────────┘
                     │ extends
          ┌──────────┴──────────┐
     ┌────▼─────┐         ┌────▼──────┐
     │  Circle  │         │ Rectangle │
     │          │         │           │
     │ area() ✓ │ MUST    │ area()  ✓ │  MUST
     │ perim()✓ │ IMPL    │ perim() ✓ │  IMPL
     │          │         │           │
     │ display()│ inherit │ display() │  inherit
     └──────────┘         └───────────┘
```

## Code Example — Shape System

```java
abstract class Shape {
    String color;

    // Constructor — called by children via super()
    Shape(String color) {
        this.color = color;
    }

    // ABSTRACT — children MUST implement (the contract)
    abstract double area();
    abstract double perimeter();

    // CONCRETE — shared logic (inherited as-is)
    void display() {
        System.out.println("Shape: " + getClass().getSimpleName());
        System.out.println("Color: " + color);
        System.out.printf("Area: %.2f%n", area());         // calls child's version
        System.out.printf("Perimeter: %.2f%n", perimeter());
        System.out.println("──────────────────");
    }
}

class Circle extends Shape {
    double radius;

    Circle(String color, double radius) {
        super(color);            // calls Shape constructor
        this.radius = radius;
    }

    @Override
    double area() {
        return Math.PI * radius * radius;
    }

    @Override
    double perimeter() {
        return 2 * Math.PI * radius;
    }
}

class Rectangle extends Shape {
    double width, height;

    Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }

    @Override
    double area() {
        return width * height;
    }

    @Override
    double perimeter() {
        return 2 * (width + height);
    }
}

class Triangle extends Shape {
    double a, b, c;  // sides
    double base, h;  // for area

    Triangle(String color, double a, double b, double c, double h) {
        super(color);
        this.a = a; this.b = b; this.c = c;
        this.base = a; this.h = h;
    }

    @Override
    double area() {
        return 0.5 * base * h;
    }

    @Override
    double perimeter() {
        return a + b + c;
    }
}

// Usage — Polymorphism with abstract class
public class Main {
    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle("Red", 5),
            new Rectangle("Blue", 4, 6),
            new Triangle("Green", 3, 4, 5, 4)
        };

        for (Shape s : shapes) {
            s.display();   // calls correct area()/perimeter() for each shape
        }

        // new Shape("Black");  ← COMPILE ERROR: cannot instantiate abstract
    }
}

/*
Output:
Shape: Circle
Color: Red
Area: 78.54
Perimeter: 31.42
──────────────────
Shape: Rectangle
Color: Blue
Area: 24.00
Perimeter: 20.00
──────────────────
Shape: Triangle
Color: Green
Area: 6.00
Perimeter: 12.00
──────────────────
*/
```

---
---

# 2. INTERFACE — Pure Contract

An interface defines a **contract** — a set of methods that ANY implementing class **must** provide. It focuses on **capability**, not hierarchy.

## Why Interface?

```
┌──────────────────────────────────────────────────────────────┐
│                   WHY INTERFACE?                             │
├──────────────────────────────────────────────────────────────┤
│ 1. DEFINE capabilities that unrelated classes share         │
│    → Bird, Airplane, Drone can all be "Flyable"            │
│                                                              │
│ 2. MULTIPLE inheritance (a class can implement many)        │
│    → Dog implements Swimmable, Trainable, Lovable          │
│                                                              │
│ 3. LOOSE COUPLING — depend on contract, not implementation  │
│    → method takes Printable, not specific PDFPrinter        │
│                                                              │
│ 4. ACHIEVE full abstraction — 100% contract                 │
└──────────────────────────────────────────────────────────────┘
```

## Interface Rules

```
┌──────────────────────────────────────────────────────────────┐
│                     INTERFACE RULES                          │
├──────────────────────────────────────────────────────────────┤
│ ✓ Methods: public abstract by default (no body)             │
│ ✓ Fields: public static final by default (constants ONLY)   │
│ ✓ A class uses 'implements' to adopt the interface          │
│ ✓ A class can implement MULTIPLE interfaces                 │
│ ✓ An interface can extend other interfaces                  │
│                                                              │
│ Java 8+ additions:                                          │
│ ✓ default methods — have body, inherited by implementers    │
│ ✓ static methods — called on interface, not objects         │
│                                                              │
│ Java 9+ additions:                                          │
│ ✓ private methods — helper methods inside interface         │
│                                                              │
│ ✗ No constructors                                           │
│ ✗ No instance fields (only constants)                       │
│ ✗ Cannot instantiate: new Flyable() → ERROR                 │
└──────────────────────────────────────────────────────────────┘
```

## Diagram — Multiple Interfaces

```
  ┌───────────┐   ┌───────────┐   ┌───────────┐
  │ Swimmable │   │ Trainable │   │ Feedable  │
  │ swim()    │   │ doTrick() │   │ feed()    │
  └─────┬─────┘   └─────┬─────┘   └─────┬─────┘
        │               │               │
        └───────────┬───┘───────────────┘
                    │ implements (multiple!)
              ┌─────▼─────┐
              │    Dog    │
              │ swim()  ✓ │ must implement ALL
              │ doTrick()✓│
              │ feed()  ✓ │
              │ + bark()  │ can have own methods too
              └───────────┘

  A single class can implement unlimited interfaces.
  This is Java's answer to multiple inheritance.
```

## Code Example — Multiple Interfaces

```java
interface Swimmable {
    void swim();
    int getSwimSpeed();     // abstract by default
}

interface Trainable {
    void doTrick(String trick);
    boolean isTrainable();
}

interface Feedable {
    void feed(String food);
}

class Dog implements Swimmable, Trainable, Feedable {
    String name;

    Dog(String name) {
        this.name = name;
    }

    // Must implement ALL methods from ALL interfaces

    @Override
    public void swim() {
        System.out.println(name + " is dog-paddling!");
    }

    @Override
    public int getSwimSpeed() {
        return 5;   // km/h
    }

    @Override
    public void doTrick(String trick) {
        System.out.println(name + " does: " + trick + "!");
    }

    @Override
    public boolean isTrainable() {
        return true;
    }

    @Override
    public void feed(String food) {
        System.out.println(name + " happily eats " + food);
    }
}

class Fish implements Swimmable, Feedable {
    String species;

    Fish(String species) {
        this.species = species;
    }

    @Override
    public void swim() {
        System.out.println(species + " swims gracefully");
    }

    @Override
    public int getSwimSpeed() {
        return 20;
    }

    @Override
    public void feed(String food) {
        System.out.println(species + " nibbles " + food);
    }

    // Fish does NOT implement Trainable — it's not trainable!
}

// Usage
public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Rex");
        Fish fish = new Fish("Goldfish");

        // Polymorphism via interfaces
        Swimmable[] swimmers = {dog, fish};
        for (Swimmable s : swimmers) {
            s.swim();
            System.out.println("Speed: " + s.getSwimSpeed() + " km/h");
        }

        // Only dog is trainable
        dog.doTrick("Roll over");
        dog.feed("bone");
        fish.feed("fish flakes");
    }
}

/*
Rex is dog-paddling!
Speed: 5 km/h
Goldfish swims gracefully
Speed: 20 km/h
Rex does: Roll over!
Rex happily eats bone
Goldfish nibbles fish flakes
*/
```

---

## Default & Static Methods (Java 8+)

```
┌───────────────────────────────────────────────────────────────┐
│                    JAVA 8+ INTERFACE METHODS                  │
├──────────────────┬────────────────────────────────────────────┤
│ abstract         │ No body. Must be implemented.             │
│ (traditional)    │ void draw();                              │
├──────────────────┼────────────────────────────────────────────┤
│ default          │ HAS body. Inherited by implementers.      │
│ (Java 8+)        │ Can be overridden.                        │
│                  │ default void log() { ... }                │
├──────────────────┼────────────────────────────────────────────┤
│ static           │ HAS body. Called on INTERFACE, not object.│
│ (Java 8+)        │ Cannot be overridden.                     │
│                  │ static void helper() { ... }              │
├──────────────────┼────────────────────────────────────────────┤
│ private          │ HAS body. Helper inside interface only.   │
│ (Java 9+)        │ Not visible to implementers.              │
│                  │ private void validate() { ... }           │
└──────────────────┴────────────────────────────────────────────┘
```

```java
interface Logger {
    // Abstract — must implement
    void log(String message);

    // Default — has body, can be overridden
    default void warn(String message) {
        System.out.println("[WARN] " + message);
    }

    default void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    // Static — called on Logger, not on objects
    static void configure(String level) {
        System.out.println("Logger configured at: " + level);
    }
}

class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }

    // warn() and error() are inherited from interface — no need to write
    // Can override if needed:
    @Override
    public void warn(String message) {
        System.out.println("[⚠ WARNING] " + message);   // custom version
    }
}

// Usage
ConsoleLogger cl = new ConsoleLogger();
cl.log("App started");          // [LOG] App started
cl.warn("Low memory");          // [⚠ WARNING] Low memory  (overridden)
cl.error("Crash!");             // [ERROR] Crash!           (default inherited)
Logger.configure("DEBUG");      // Logger configured at: DEBUG  (static)
```

---

## Interface Extending Interface

Interfaces can extend other interfaces using `extends`. The implementing class must implement ALL methods from the chain.

```
  ┌───────────┐
  │ Readable  │  read()
  └─────┬─────┘
        │ extends
  ┌─────▼─────┐
  │ Writable  │  read() + write()
  └─────┬─────┘
        │ extends
  ┌─────▼──────┐
  │ ReadWrite  │  read() + write() + delete()
  └─────┬──────┘
        │ implements
  ┌─────▼──────┐
  │  FileStore │  must implement ALL THREE
  └────────────┘
```

```java
interface Readable {
    String read();
}

interface Writable extends Readable {
    void write(String data);
}

interface ReadWriteDelete extends Writable {
    void delete();
}

class FileStore implements ReadWriteDelete {
    String content = "";

    @Override
    public String read() {
        return content;
    }

    @Override
    public void write(String data) {
        content += data;
    }

    @Override
    public void delete() {
        content = "";
    }
}
```

---
---

# 3. Abstract Class vs Interface — Full Comparison

```
╔══════════════════════╦═══════════════════════╦═══════════════════════╗
║      Feature         ║   Abstract Class      ║    Interface          ║
╠══════════════════════╬═══════════════════════╬═══════════════════════╣
║ Keyword              ║ abstract class        ║ interface             ║
║ Implemented via      ║ extends               ║ implements            ║
║ Multiple allowed?    ║ NO (single only)      ║ YES (many interfaces) ║
║ Methods              ║ Abstract + Concrete   ║ Abstract (+default)   ║
║ Fields               ║ Any type              ║ static final ONLY     ║
║ Constructors         ║ YES                   ║ NO                    ║
║ Access modifiers     ║ Any (private, etc.)   ║ public only           ║
║ Can have main()?     ║ YES                   ║ YES (static)          ║
║ Speed                ║ Fast                  ║ Slightly slower       ║
║ Abstraction level    ║ 0-100% (partial)      ║ 100% (full)           ║
╠══════════════════════╬═══════════════════════╬═══════════════════════╣
║ RELATIONSHIP         ║ IS-A (identity)       ║ CAN-DO (capability)   ║
║ Example              ║ Dog IS-A Animal       ║ Dog CAN Swim          ║
║ Think of it as       ║ A "kind of..."        ║ A "capable of..."     ║
╚══════════════════════╩═══════════════════════╩═══════════════════════╝
```

### Decision Guide

```
  Need shared code + fields?           → Abstract Class
  Need constructors?                   → Abstract Class
  Modeling "is-a" (same family)?       → Abstract Class
  Multiple inheritance?                → Interface
  Unrelated classes share behavior?    → Interface
  Defining a pure contract?            → Interface
  Want both?                           → Use both! (common pattern)

  class Dog extends Animal implements Swimmable, Trainable
            ↑ abstract class              ↑ interfaces
            IS-A Animal                   CAN swim, CAN be trained
```

---

# Full Example — Combining Both

```java
// INTERFACE — capabilities
interface Printable {
    void print();
}

interface Exportable {
    String exportAsText();
}

// ABSTRACT CLASS — base with shared code
abstract class Document {
    String title;
    String author;

    Document(String title, String author) {
        this.title = title;
        this.author = author;
    }

    abstract int getPageCount();

    void showInfo() {
        System.out.println("Title:  " + title);
        System.out.println("Author: " + author);
        System.out.println("Pages:  " + getPageCount());
    }
}

// CONCRETE — extends abstract + implements interfaces
class Report extends Document implements Printable, Exportable {
    String[] sections;

    Report(String title, String author, String[] sections) {
        super(title, author);
        this.sections = sections;
    }

    @Override
    int getPageCount() {
        return sections.length * 2;  // estimate 2 pages per section
    }

    @Override
    public void print() {
        System.out.println("=== PRINTING REPORT ===");
        showInfo();
        for (String s : sections) {
            System.out.println("  Section: " + s);
        }
    }

    @Override
    public String exportAsText() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" by ").append(author).append("\n");
        for (String s : sections) {
            sb.append("- ").append(s).append("\n");
        }
        return sb.toString();
    }
}

class Invoice extends Document implements Printable {
    double amount;

    Invoice(String title, String author, double amount) {
        super(title, author);
        this.amount = amount;
    }

    @Override
    int getPageCount() {
        return 1;
    }

    @Override
    public void print() {
        System.out.println("=== PRINTING INVOICE ===");
        showInfo();
        System.out.println("Amount: $" + amount);
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Report r = new Report("Q4 Report", "Alice",
                             new String[]{"Sales", "Marketing", "Finance"});

        Invoice inv = new Invoice("INV-001", "Bob", 2500.00);

        // Polymorphism via abstract class
        Document[] docs = {r, inv};
        for (Document d : docs) {
            d.showInfo();
            System.out.println();
        }

        // Polymorphism via interface
        Printable[] printables = {r, inv};
        for (Printable p : printables) {
            p.print();
            System.out.println();
        }

        // Only Report is Exportable
        System.out.println(r.exportAsText());
    }
}
```

---
