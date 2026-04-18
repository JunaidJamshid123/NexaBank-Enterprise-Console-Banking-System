# 01 — Variables, Data Types & Operators (In Depth)

# F:\WORKDONE\NexaBank Banking System>java -cp "out\production\NexaBank Banking System" Main
---

## What is a Variable?

A **variable** is a named memory location that stores a value. Think of it as a **labeled box** in memory — the label is the variable name, the content inside is the value.

```
  MEMORY (RAM)
  ┌─────────────────────────────────────────────────────────┐
  │                                                         │
  │   ┌──────────┐   ┌──────────┐   ┌──────────────┐      │
  │   │ age      │   │ name     │   │ isActive     │      │
  │   │ ──────── │   │ ──────── │   │ ──────────── │      │
  │   │   25     │   │ "Alice"  │   │    true      │      │
  │   │ (int)    │   │ (String) │   │  (boolean)   │      │
  │   └──────────┘   └──────────┘   └──────────────┘      │
  │                                                         │
  │   Each box = a memory location with a name & type      │
  └─────────────────────────────────────────────────────────┘

  Syntax:   dataType  variableName = value;
  Example:  int       age          = 25;
            ↑           ↑             ↑
          type        label        content
```

---

## Three Types of Variables

```
╔══════════════════════════════════════════════════════════════════════════╗
║                        VARIABLE TYPES IN JAVA                          ║
╠═══════════════════╦══════════════════════╦═════════════════════════════╣
║  LOCAL VARIABLE   ║  INSTANCE VARIABLE   ║  STATIC (CLASS) VARIABLE   ║
╠═══════════════════╬══════════════════════╬═════════════════════════════╣
║ Declared INSIDE   ║ Declared INSIDE      ║ Declared with `static`     ║
║ a method/block    ║ class, OUTSIDE       ║ keyword inside class       ║
║                   ║ any method           ║                             ║
╠═══════════════════╬══════════════════════╬═════════════════════════════╣
║ Scope: method     ║ Scope: entire object ║ Scope: entire CLASS        ║
║ only              ║                      ║                             ║
╠═══════════════════╬══════════════════════╬═════════════════════════════╣
║ NO default value  ║ HAS default value    ║ HAS default value          ║
║ (must initialize) ║ (0, null, false)     ║ (0, null, false)           ║
╠═══════════════════╬══════════════════════╬═════════════════════════════╣
║ Lives on STACK    ║ Lives on HEAP        ║ Lives in CLASS MEMORY      ║
║                   ║ (inside object)      ║ (one copy for all objects) ║
╠═══════════════════╬══════════════════════╬═════════════════════════════╣
║ Created when      ║ Created when object  ║ Created when class is      ║
║ method is called  ║ is created (new)     ║ loaded by JVM              ║
║ Destroyed when    ║ Destroyed when       ║ Destroyed when program     ║
║ method ends       ║ object is GC'd       ║ ends                       ║
╚═══════════════════╩══════════════════════╩═════════════════════════════╝
```

### Memory Diagram

```
  CLASS MEMORY (Method Area)
  ┌──────────────────────────┐
  │  static int count = 3    │   ← ONE copy, shared by all objects
  └──────────────────────────┘

  HEAP (Object Memory)
  ┌────────────────┐  ┌────────────────┐
  │  Object #1     │  │  Object #2     │
  │  name = "Alice"│  │  name = "Bob"  │   ← each object has its own copy
  │  age = 25      │  │  age = 30      │
  └────────────────┘  └────────────────┘

  STACK (Method Call)
  ┌────────────────┐
  │  main()        │
  │  localVar = 10 │   ← only lives during method execution
  │  temp = 5      │
  └────────────────┘
```

### Code Example

```java
public class VariableTypes {
    // Instance variable — each object gets its own copy
    String name;
    int age;

    // Static variable — ONE copy shared by ALL objects
    static int totalStudents = 0;

    VariableTypes(String name, int age) {
        this.name = name;
        this.age = age;
        totalStudents++;   // shared counter increments
    }

    void display() {
        // Local variable — exists only inside this method
        String message = name + " is " + age + " years old";
        System.out.println(message);
        System.out.println("Total students: " + totalStudents);
    }

    public static void main(String[] args) {
        VariableTypes s1 = new VariableTypes("Alice", 20);
        VariableTypes s2 = new VariableTypes("Bob", 22);

        s1.display();
        // Alice is 20 years old
        // Total students: 2

        s2.display();
        // Bob is 22 years old
        // Total students: 2   ← same for both (shared)
    }
}
```

---
---

## Data Types

Java is **strongly typed** — every variable must declare its type before use, and the type cannot change.

```
┌──────────────────────────────────────────────────────────────────────┐
│                        JAVA DATA TYPES                               │
├──────────────────────────────────┬───────────────────────────────────┤
│        PRIMITIVE (8 types)       │        NON-PRIMITIVE (Reference)  │
│        Stored on STACK           │        Stored on HEAP             │
│        Holds actual VALUE        │        Holds REFERENCE (address)  │
├──────────────────────────────────┤───────────────────────────────────┤
│                                  │                                   │
│  INTEGER TYPES:                  │  • String                         │
│  ┌──────┬───────┬──────────────┐ │  • Arrays                         │
│  │ Type │ Size  │ Range        │ │  • Classes (objects)              │
│  ├──────┼───────┼──────────────┤ │  • Interfaces                     │
│  │ byte │ 1 byte│ -128 to 127  │ │  • Enums                          │
│  │short │ 2 byte│ -32K to 32K  │ │                                   │
│  │ int  │ 4 byte│ -2B to 2B    │ │  Stored as:                      │
│  │ long │ 8 byte│ very large   │ │  ┌──────┐    ┌────────────┐      │
│  └──────┴───────┴──────────────┘ │  │ ref ─┼──► │ Object on  │      │
│                                  │  └──────┘    │   HEAP     │      │
│  FLOATING POINT:                 │   STACK       └────────────┘      │
│  ┌───────┬───────┬─────────────┐ │                                   │
│  │ float │ 4 byte│ ~7 digits   │ │  Default Value: null              │
│  │double │ 8 byte│ ~15 digits  │ │                                   │
│  └───────┴───────┴─────────────┘ │                                   │
│                                  │                                   │
│  CHARACTER:                      │                                   │
│  ┌──────┬───────┬──────────────┐ │                                   │
│  │ char │ 2 byte│ Unicode char │ │                                   │
│  └──────┴───────┴──────────────┘ │                                   │
│                                  │                                   │
│  BOOLEAN:                        │                                   │
│  ┌────────┬──────┬─────────────┐ │                                   │
│  │boolean │1 bit │ true/false  │ │                                   │
│  └────────┴──────┴─────────────┘ │                                   │
└──────────────────────────────────┴───────────────────────────────────┘
```

### Default Values (for instance/static variables)

```
┌──────────┬──────────────┬─────────────────────────┐
│  Type    │ Default      │  Example Declaration    │
├──────────┼──────────────┼─────────────────────────┤
│  byte    │ 0            │  byte b;                │
│  short   │ 0            │  short s;               │
│  int     │ 0            │  int i;                 │
│  long    │ 0L           │  long l;                │
│  float   │ 0.0f         │  float f;               │
│  double  │ 0.0          │  double d;              │
│  char    │ '\u0000'     │  char c;    (null char) │
│  boolean │ false        │  boolean b;             │
│  String  │ null         │  String s;  (reference) │
│  Object  │ null         │  Object o;  (reference) │
└──────────┴──────────────┴─────────────────────────┘
  NOTE: Local variables have NO default — must initialize before use!
```

### Primitive vs Reference — Memory Difference

```
  PRIMITIVE:                         REFERENCE:
  Variable holds the VALUE itself.   Variable holds the ADDRESS.

  int x = 42;                       String s = "Hello";

  STACK                              STACK              HEAP
  ┌──────────┐                       ┌──────────┐      ┌───────────┐
  │  x = 42  │  (value here)         │  s = 0x5A─┼────►│  "Hello"  │
  └──────────┘                       └──────────┘      └───────────┘
```

### Code Example — All Data Types

```java
public class DataTypeDemo {
    public static void main(String[] args) {
        // Integer types
        byte   myByte   = 127;            // tiny numbers
        short  myShort  = 32000;          // small numbers
        int    myInt    = 2_000_000;      // standard (underscores for readability)
        long   myLong   = 9_999_999_999L; // large (L suffix required)

        // Floating point
        float  myFloat  = 3.14f;          // less precision (f suffix required)
        double myDouble = 3.14159265358;  // more precision (default)

        // Character & Boolean
        char    myChar    = 'A';          // single character in quotes
        char    unicode   = '\u0041';     // same as 'A' (Unicode)
        boolean myBool   = true;          // true or false only

        // Reference type
        String  myString = "Hello Java";  // points to object on heap

        System.out.println("byte:    " + myByte);
        System.out.println("short:   " + myShort);
        System.out.println("int:     " + myInt);
        System.out.println("long:    " + myLong);
        System.out.println("float:   " + myFloat);
        System.out.println("double:  " + myDouble);
        System.out.println("char:    " + myChar);
        System.out.println("unicode: " + unicode);
        System.out.println("boolean: " + myBool);
        System.out.println("String:  " + myString);
    }
}
```

---

## Type Casting (Type Conversion)

```
╔═══════════════════════════════════════════════════════════════════════╗
║                        TYPE CASTING                                  ║
╠══════════════════════════════════╦════════════════════════════════════╣
║   WIDENING (Automatic)          ║   NARROWING (Manual)              ║
║   Small → Big = SAFE            ║   Big → Small = RISKY             ║
║   No data loss                  ║   Possible data loss              ║
║   Done by compiler              ║   Must use (type) cast            ║
╠══════════════════════════════════╬════════════════════════════════════╣
║                                  ║                                    ║
║   byte → short → int → long     ║   double → float → long → int     ║
║             → float → double    ║          → short → byte            ║
║                                  ║                                    ║
╚══════════════════════════════════╩════════════════════════════════════╝

  Widening path (auto):
  byte ──► short ──► int ──► long ──► float ──► double
              ▲
              │
            char
```

```java
public class CastingDemo {
    public static void main(String[] args) {
        // ═══ WIDENING (Automatic) — small to big ═══
        int myInt = 100;
        long myLong = myInt;      // int → long (automatic)
        double myDouble = myLong; // long → double (automatic)
        System.out.println("int → long → double: " + myDouble); // 100.0

        // ═══ NARROWING (Manual) — big to small ═══
        double price = 9.78;
        int rounded = (int) price;   // double → int (MUST cast)
        System.out.println("double → int: " + rounded); // 9 (truncated, NOT rounded)

        // ═══ char ↔ int ═══
        char letter = 'A';
        int ascii = letter;          // char → int (auto) = 65
        char back = (char) 66;       // int → char (manual) = 'B'
        System.out.println("'A' as int: " + ascii);   // 65
        System.out.println("66 as char: " + back);     // B

        // ═══ Overflow danger ═══
        int bigNum = 130;
        byte smallNum = (byte) bigNum;  // int → byte
        System.out.println("130 as byte: " + smallNum); // -126 (overflow!)
        // byte range is -128 to 127, so 130 wraps around
    }
}
```

---
---

## Operators

Operators perform operations on variables and values.

```
╔═══════════════════════════════════════════════════════════════════════╗
║                       JAVA OPERATORS OVERVIEW                        ║
╠═══════════════════╦══════════════════════════════════════════════════╣
║ CATEGORY          ║  OPERATORS                                      ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Arithmetic        ║  +   -   *   /   %                              ║
║                   ║  (add, subtract, multiply, divide, modulus)     ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Assignment        ║  =   +=   -=   *=   /=   %=                    ║
║                   ║  (assign, add-assign, subtract-assign, etc.)   ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Comparison        ║  ==   !=   >   <   >=   <=                      ║
║ (Relational)      ║  (returns boolean: true or false)              ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Logical           ║  &&   ||   !                                    ║
║                   ║  (AND, OR, NOT — combine booleans)             ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Unary             ║  ++   --   +   -   !   ~                       ║
║                   ║  (operate on a single operand)                 ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Bitwise           ║  &   |   ^   ~   <<   >>   >>>                 ║
║                   ║  (operate on individual bits)                  ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ Ternary           ║  condition ? valueIfTrue : valueIfFalse        ║
║                   ║  (shorthand for if-else)                       ║
╠═══════════════════╬══════════════════════════════════════════════════╣
║ instanceof        ║  obj instanceof ClassName                       ║
║                   ║  (checks if object is of a type)               ║
╚═══════════════════╩══════════════════════════════════════════════════╝
```

---

### 1. Arithmetic Operators

```
  ┌────────┬───────────────────┬─────────────────────────────────────┐
  │ Symbol │ Name              │ Example                             │
  ├────────┼───────────────────┼─────────────────────────────────────┤
  │   +    │ Addition          │ 10 + 3  = 13                        │
  │   -    │ Subtraction       │ 10 - 3  = 7                         │
  │   *    │ Multiplication    │ 10 * 3  = 30                        │
  │   /    │ Division          │ 10 / 3  = 3    (integer division!)  │
  │        │                   │ 10.0/3  = 3.33 (float division)     │
  │   %    │ Modulus           │ 10 % 3  = 1    (remainder)          │
  └────────┴───────────────────┴─────────────────────────────────────┘

  ⚠ Integer division truncates: 7 / 2 = 3 (not 3.5)
  ⚠ To get decimal result: 7.0 / 2 = 3.5 or (double) 7 / 2
```

```java
int a = 10, b = 3;

System.out.println("a + b = " + (a + b));  // 13
System.out.println("a - b = " + (a - b));  // 7
System.out.println("a * b = " + (a * b));  // 30
System.out.println("a / b = " + (a / b));  // 3  (integer division!)
System.out.println("a % b = " + (a % b));  // 1  (remainder)

// Float division
System.out.println("10.0 / 3 = " + (10.0 / 3));   // 3.333...
System.out.println("(double)a/b = " + ((double) a / b)); // 3.333...
```

---

### 2. Unary Operators (++, --)

```
  PRE vs POST increment — THE TRICKY PART:

  ┌──────────────────────────────────────────────────────────────┐
  │   PRE-INCREMENT (++x)          POST-INCREMENT (x++)         │
  │   ─────────────────            ────────────────────         │
  │   1. Increment FIRST          1. Use current value FIRST   │
  │   2. Then use the value       2. Then increment            │
  │                                                             │
  │   int x = 5;                  int x = 5;                   │
  │   int y = ++x;                int y = x++;                 │
  │   // x = 6, y = 6            // x = 6, y = 5              │
  │     (incremented, then used)    (used, then incremented)   │
  └──────────────────────────────────────────────────────────────┘
```

```java
int x = 5;

// Post-increment: use first, then increment
System.out.println(x++);  // prints 5 (uses 5, then x becomes 6)
System.out.println(x);    // prints 6

// Pre-increment: increment first, then use
System.out.println(++x);  // prints 7 (increments to 7, then uses 7)

// Post-decrement
int y = 10;
System.out.println(y--);  // prints 10 (uses 10, then y becomes 9)
System.out.println(y);    // prints 9
```

---

### 3. Comparison (Relational) Operators

Always return `boolean` (true/false). Used in `if`, `while`, `for` conditions.

```java
int a = 10, b = 20;

System.out.println(a == b);  // false  (equal to)
System.out.println(a != b);  // true   (not equal)
System.out.println(a > b);   // false  (greater than)
System.out.println(a < b);   // true   (less than)
System.out.println(a >= 10); // true   (greater or equal)
System.out.println(a <= 5);  // false  (less or equal)
```

**⚠ Common Mistake:** `==` vs `=`
```java
int x = 5;
// x == 5  → comparison (true/false)
// x = 5   → assignment (sets value)
```

---

### 4. Logical Operators

Combine multiple boolean conditions.

```
  ┌────────┬───────────────┬──────────────────────────────────┐
  │ Symbol │ Name          │ How it works                     │
  ├────────┼───────────────┼──────────────────────────────────┤
  │  &&    │ Logical AND   │ true ONLY if BOTH are true       │
  │  ||    │ Logical OR    │ true if AT LEAST ONE is true     │
  │  !     │ Logical NOT   │ flips: true→false, false→true   │
  └────────┴───────────────┴──────────────────────────────────┘

  TRUTH TABLE:
  ┌───────┬───────┬────────┬────────┐
  │   A   │   B   │ A && B │ A || B │
  ├───────┼───────┼────────┼────────┤
  │ true  │ true  │  true  │  true  │
  │ true  │ false │  false │  true  │
  │ false │ true  │  false │  true  │
  │ false │ false │  false │  false │
  └───────┴───────┴────────┴────────┘
```

```java
int age = 25;
boolean hasID = true;
double balance = 1500;

// AND — both must be true
if (age >= 18 && hasID) {
    System.out.println("Entry allowed");  // ✓ prints
}

// OR — at least one true
if (balance > 2000 || age > 60) {
    System.out.println("Premium customer");
} else {
    System.out.println("Regular customer"); // ✓ prints
}

// NOT — flips the boolean
System.out.println(!true);   // false
System.out.println(!false);  // true

// Short-circuit evaluation:
// && stops at first false (won't check second)
// || stops at first true (won't check second)
```

---

### 5. Assignment Operators

```
  ┌──────────┬──────────────────┬──────────────────────┐
  │ Operator │ Equivalent       │ Example              │
  ├──────────┼──────────────────┼──────────────────────┤
  │  x = 10  │  x = 10          │ assign               │
  │  x += 5  │  x = x + 5       │ add and assign       │
  │  x -= 3  │  x = x - 3       │ subtract and assign  │
  │  x *= 2  │  x = x * 2       │ multiply and assign  │
  │  x /= 4  │  x = x / 4       │ divide and assign    │
  │  x %= 3  │  x = x % 3       │ modulus and assign   │
  └──────────┴──────────────────┴──────────────────────┘
```

```java
int score = 100;
score += 10;  // 110
score -= 20;  // 90
score *= 2;   // 180
score /= 3;   // 60
score %= 7;   // 4  (60 % 7 = 4)
System.out.println(score); // 4
```

---

### 6. Ternary Operator

A one-line `if-else`. Returns a value.

```
  Syntax:   result = (condition) ? valueIfTrue : valueIfFalse;

  ┌────────────┐
  │ condition? │
  └──┬─────┬───┘
   YES     NO
    │       │
  value1  value2
    │       │
    └───┬───┘
     result
```

```java
int age = 20;

// Instead of:
// String status;
// if (age >= 18) { status = "Adult"; }
// else { status = "Minor"; }

// Use ternary:
String status = (age >= 18) ? "Adult" : "Minor";
System.out.println(status);  // Adult

// Nested ternary (use sparingly)
int score = 75;
String grade = (score >= 90) ? "A" :
               (score >= 80) ? "B" :
               (score >= 70) ? "C" : "F";
System.out.println(grade);  // C
```

---

### Operator Precedence (High → Low)

```
  ┌──────┬──────────────────────────────────────┐
  │ Rank │ Operators                            │
  ├──────┼──────────────────────────────────────┤
  │  1   │ ()  []  .              (grouping)    │
  │  2   │ ++  --  !  ~  (unary) (right→left)  │
  │  3   │ *   /   %              (multiply)    │
  │  4   │ +   -                  (add)         │
  │  5   │ <<  >>  >>>            (shift)       │
  │  6   │ <  <=  >  >=  instanceof             │
  │  7   │ ==  !=                 (equality)    │
  │  8   │ &                      (bitwise AND) │
  │  9   │ ^                      (bitwise XOR) │
  │ 10   │ |                      (bitwise OR)  │
  │ 11   │ &&                     (logical AND) │
  │ 12   │ ||                     (logical OR)  │
  │ 13   │ ?:                     (ternary)     │
  │ 14   │ =  +=  -=  *=  /=     (assignment)  │
  └──────┴──────────────────────────────────────┘

  TIP: When in doubt, use parentheses () to make order explicit!

  int result = 2 + 3 * 4;     // 14 (not 20) — * before +
  int result = (2 + 3) * 4;   // 20 — () forces + first
```

---

### Complete Code Example — All Operators

```java
public class OperatorMaster {
    public static void main(String[] args) {
        // ═══ ARITHMETIC ═══
        int x = 15, y = 4;
        System.out.println("15 / 4  = " + (x / y));     // 3 (int division)
        System.out.println("15 % 4  = " + (x % y));     // 3 (remainder)
        System.out.println("15.0/4  = " + (15.0 / y));  // 3.75

        // ═══ UNARY ═══
        int a = 10;
        System.out.println("a++  = " + (a++));  // 10 (post)
        System.out.println("++a  = " + (++a));  // 12 (pre)
        System.out.println("--a  = " + (--a));  // 11 (pre)

        // ═══ COMPARISON ═══
        System.out.println("10 == 10 : " + (10 == 10));  // true
        System.out.println("10 != 5  : " + (10 != 5));   // true

        // ═══ LOGICAL ═══
        boolean hot = true, raining = false;
        System.out.println("hot && !raining : " + (hot && !raining)); // true
        System.out.println("hot || raining  : " + (hot || raining));  // true

        // ═══ ASSIGNMENT ═══
        int score = 50;
        score += 30;  // 80
        score -= 10;  // 70
        score *= 2;   // 140
        System.out.println("Final score: " + score);

        // ═══ TERNARY ═══
        String result = (score > 100) ? "High" : "Low";
        System.out.println("Score is: " + result);  // High
    }
}
```

---
