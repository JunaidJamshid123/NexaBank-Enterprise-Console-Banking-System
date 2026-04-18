# 02 — Control Flow: if-else & switch-case (In Depth)

---

## What is Control Flow?

By default, Java executes code **line by line, top to bottom**. Control flow statements let you **change that order** — skip lines, choose between paths, or repeat blocks.

```
  DEFAULT FLOW:              WITH CONTROL FLOW:

  line 1  ──►                line 1  ──►
  line 2  ──►                    ┌─── condition? ───┐
  line 3  ──►                  YES                  NO
  line 4  ──►                line 2               line 4
  line 5  ──►                line 3                 │
                                 └──────┬───────────┘
                                      line 5

  Sequential                  Decision-based branching
```

---
---

# 1. if Statement

Executes a block **only if** the condition evaluates to `true`. If `false`, the block is skipped entirely.

```
  HOW IT WORKS:

         ┌─────────────┐
         │    START     │
         └──────┬───────┘
                │
         ┌──────▼───────┐
         │  condition   │
         │  true/false? │
         └──┬────────┬──┘
          TRUE     FALSE
            │        │
     ┌──────▼──────┐ │
     │  execute    │ │  (skipped)
     │  if-block   │ │
     └──────┬──────┘ │
            └────┬───┘
          ┌──────▼───────┐
          │   continue   │
          │   program    │
          └──────────────┘
```

```java
int temperature = 35;

if (temperature > 30) {
    System.out.println("It's hot outside!");   // ✓ prints (35 > 30 is true)
}

if (temperature > 40) {
    System.out.println("Extreme heat!");       // ✗ skipped (35 > 40 is false)
}

System.out.println("Program continues...");   // always runs
```

---

# 2. if-else

Two paths — **one MUST execute**. If condition is true → if-block runs. Otherwise → else-block runs.

```
         ┌─────────────┐
         │    START     │
         └──────┬───────┘
                │
         ┌──────▼───────┐
         │  condition?  │
         └──┬────────┬──┘
          TRUE     FALSE
            │        │
    ┌───────▼──┐  ┌──▼───────┐
    │ if-block │  │else-block│
    │(option A)│  │(option B)│
    └────┬─────┘  └────┬─────┘
         └──────┬──────┘
         ┌──────▼───────┐
         │   continue   │
         └──────────────┘

  EXACTLY ONE block always runs. Never both, never neither.
```

```java
int age = 16;

if (age >= 18) {
    System.out.println("You can vote");
} else {
    System.out.println("Too young to vote");  // ✓ prints
}
```

### Real-World Example — Login Check

```java
String inputPassword = "secret123";
String correctPassword = "secret123";

if (inputPassword.equals(correctPassword)) {
    System.out.println("Login successful! Welcome.");
} else {
    System.out.println("Incorrect password. Try again.");
}
```

---

# 3. if — else if — else (Ladder)

Check **multiple conditions** in sequence. The **first** true condition's block runs, rest are skipped. `else` is the fallback if nothing matches.

```
  ┌───────────────┐
  │ condition 1?  │──TRUE──►  Block 1 (DONE)
  └───────┬───────┘
        FALSE
  ┌───────▼───────┐
  │ condition 2?  │──TRUE──►  Block 2 (DONE)
  └───────┬───────┘
        FALSE
  ┌───────▼───────┐
  │ condition 3?  │──TRUE──►  Block 3 (DONE)
  └───────┬───────┘
        FALSE
  ┌───────▼───────┐
  │  else (default)│──────►  Default Block (DONE)
  └───────────────┘

  KEY: Only the FIRST matching block runs. Even if multiple conditions
       are true, only the first match executes.
```

```java
int marks = 82;

if (marks >= 90) {
    System.out.println("Grade: A+");
} else if (marks >= 80) {
    System.out.println("Grade: A");     // ✓ prints (first true match)
} else if (marks >= 70) {
    System.out.println("Grade: B");     // ✗ skipped (even though 82 >= 70)
} else if (marks >= 60) {
    System.out.println("Grade: C");
} else {
    System.out.println("Grade: F — Failed");
}
```

### Real-World Example — Ticket Pricing

```java
int age = 25;
double price;

if (age < 5) {
    price = 0;                // Free for babies
    System.out.println("Free entry");
} else if (age < 12) {
    price = 50;               // Child discount
    System.out.println("Child ticket: $" + price);
} else if (age < 60) {
    price = 100;              // Adult price
    System.out.println("Adult ticket: $" + price);   // ✓ prints
} else {
    price = 60;               // Senior discount
    System.out.println("Senior ticket: $" + price);
}
```

---

# 4. Nested if

An `if` inside another `if`. The inner `if` only runs when the outer condition is true.

```
  ┌─────────────────┐
  │ outer condition? │
  └──┬───────────┬──┘
   TRUE        FALSE
     │           │
  ┌──▼────────┐  │
  │ inner     │  │ (everything skipped)
  │ condition?│  │
  └──┬─────┬──┘  │
   TRUE  FALSE   │
    │      │     │
  block  block   │
    └──────┘     │
         └───┬───┘
           continue
```

```java
boolean isLoggedIn = true;
String role = "admin";
boolean hasPermission = true;

if (isLoggedIn) {
    System.out.println("User is logged in");

    if (role.equals("admin")) {
        System.out.println("Admin panel loaded");

        if (hasPermission) {
            System.out.println("Full access granted");  // ✓ prints
        } else {
            System.out.println("Limited access");
        }

    } else {
        System.out.println("Regular user dashboard");
    }

} else {
    System.out.println("Please log in first");
}
```

**Tip:** Deeply nested ifs (3+ levels) hurt readability. Consider combining conditions:
```java
// Flattened version (cleaner):
if (isLoggedIn && role.equals("admin") && hasPermission) {
    System.out.println("Full admin access granted");
}
```

---
---

# 5. switch-case

Best when comparing **one variable** against **multiple fixed values**. Cleaner than a long if-else-if chain for equality checks.

```
  HOW IT WORKS:

  ┌──────────────────┐
  │   expression     │   (evaluate once)
  └────────┬─────────┘
           │
    ┌──────┼──────────┬──────────┬───────────┐
    │      │          │          │           │
┌───▼──┐ ┌─▼────┐ ┌───▼──┐ ┌───▼──┐ ┌──────▼──┐
│case 1│ │case 2│ │case 3│ │case 4│ │ default │
│      │ │      │ │      │ │      │ │(fallback)│
│break;│ │break;│ │break;│ │break;│ │         │
└──────┘ └──────┘ └──────┘ └──────┘ └─────────┘
   │        │        │        │          │
   └────────┴────────┴────────┴──────────┘
                     │
               continue program
```

### Rules

```
┌──────────────────────────────────────────────────────────────────┐
│                     SWITCH RULES                                 │
├──────────────────────────────────────────────────────────────────┤
│ 1. Expression types: byte, short, int, char, String, enum       │
│    ✗ NOT allowed: long, float, double, boolean                  │
│                                                                  │
│ 2. Case values must be CONSTANTS (literals or final variables)  │
│    ✗ NOT allowed: variables, method calls, ranges               │
│                                                                  │
│ 3. Each case NEEDS break; (otherwise fall-through occurs)       │
│                                                                  │
│ 4. default is OPTIONAL — runs if no case matches                │
│                                                                  │
│ 5. Duplicate case values → COMPILE ERROR                        │
└──────────────────────────────────────────────────────────────────┘
```

### Basic switch Example

```java
int day = 3;

switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");  // ✓ prints
        break;
    case 4:
        System.out.println("Thursday");
        break;
    case 5:
        System.out.println("Friday");
        break;
    case 6:
    case 7:
        System.out.println("Weekend!");   // cases 6 AND 7 share this block
        break;
    default:
        System.out.println("Invalid day");
}
```

---

### switch with String

```java
String command = "start";

switch (command) {
    case "start":
        System.out.println("Starting the engine...");  // ✓ prints
        break;
    case "stop":
        System.out.println("Stopping the engine...");
        break;
    case "pause":
        System.out.println("Engine paused.");
        break;
    default:
        System.out.println("Unknown command: " + command);
}
```

---

### Fall-Through (What Happens Without break)

When `break` is missing, execution **falls through** to the next case — regardless of whether it matches!

```
  WITH break:                    WITHOUT break:
  
  case 1: ──► code ──► break     case 1: ──► code ──┐
  case 2: ──► code ──► break     case 2: ──► code ──┤ (falls through!)
  case 3: ──► code ──► break     case 3: ──► code ──┤ (falls through!)
                                 default: ──► code ──┘ (all execute)
```

```java
int level = 1;

switch (level) {
    case 1:
        System.out.println("Beginner");    // prints
    case 2:
        System.out.println("Intermediate");// prints (fall-through!)
    case 3:
        System.out.println("Advanced");    // prints (fall-through!)
        break;
    default:
        System.out.println("Unknown");
}
// Output: Beginner, Intermediate, Advanced
// Because there's no break after case 1 and case 2
```

### Intentional Fall-Through (Grouped Cases)

```java
char grade = 'B';

switch (grade) {
    case 'A':
    case 'B':
    case 'C':
        System.out.println("Passed!");   // A, B, or C → same result
        break;
    case 'D':
    case 'F':
        System.out.println("Failed!");   // D or F → same result
        break;
    default:
        System.out.println("Invalid grade");
}
```

---

### Enhanced switch (Java 14+)

Arrow syntax — no `break` needed, no fall-through risk, can return values.

```java
int day = 3;

String name = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";       // ✓ matched
    case 4 -> "Thursday";
    case 5 -> "Friday";
    case 6, 7 -> "Weekend";     // multiple values in one case
    default -> "Invalid";
};

System.out.println(name);  // Wednesday
```

```java
// Multi-line block with yield
String type = switch (day) {
    case 1, 2, 3, 4, 5 -> {
        System.out.println("It's a weekday");
        yield "Weekday";     // yield returns value from block
    }
    case 6, 7 -> {
        System.out.println("It's the weekend!");
        yield "Weekend";
    }
    default -> "Invalid";
};
```

---
---

# 6. if-else vs switch — When to Use What

```
┌─────────────────────┬──────────────────────┬───────────────────────┐
│     Feature         │       if-else        │       switch          │
├─────────────────────┼──────────────────────┼───────────────────────┤
│ Check type          │ Any expression       │ Equality only         │
│ Range checks        │ ✓ (x > 5 && x < 10) │ ✗ not possible        │
│ Data types          │ Any type             │ int, char, String,    │
│                     │                      │ enum only             │
│ Multiple values     │ Use ||               │ Group cases           │
│ Complex conditions  │ ✓ (&&, ||, !)        │ ✗ simple match only   │
│ Readability         │ Good for few cases   │ Better for many cases │
│ Performance         │ Checks sequentially  │ Jump table (faster)   │
│ Fall-through        │ Not possible         │ Possible (no break)   │
│ null handling       │ Can check for null   │ Throws NPE on null    │
└─────────────────────┴──────────────────────┴───────────────────────┘
```

### Use if-else when:
```java
// Ranges, complex conditions, boolean expressions
if (score >= 90 && attendance > 75) { ... }
if (name != null && !name.isEmpty()) { ... }
if (age > 18 || hasParentConsent) { ... }
```

### Use switch when:
```java
// Checking ONE variable against MANY fixed values
switch (menuChoice) {
    case 1: ... break;
    case 2: ... break;
    case 3: ... break;
}
```

---

# Full Example — Grade Calculator

```java
import java.util.Scanner;

public class GradeCalculator {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter marks (0-100): ");
        int marks = sc.nextInt();

        // Using if-else-if ladder
        String grade;
        String remark;

        if (marks < 0 || marks > 100) {
            grade = "Invalid";
            remark = "Marks must be 0-100";
        } else if (marks >= 90) {
            grade = "A+";
            remark = "Outstanding!";
        } else if (marks >= 80) {
            grade = "A";
            remark = "Excellent!";
        } else if (marks >= 70) {
            grade = "B";
            remark = "Good";
        } else if (marks >= 60) {
            grade = "C";
            remark = "Average";
        } else if (marks >= 50) {
            grade = "D";
            remark = "Below average";
        } else {
            grade = "F";
            remark = "Failed";
        }

        System.out.println("Marks:  " + marks);
        System.out.println("Grade:  " + grade);
        System.out.println("Remark: " + remark);

        // Using switch on the grade
        switch (grade) {
            case "A+":
            case "A":
            case "B":
                System.out.println("Result: PASS (with distinction)");
                break;
            case "C":
            case "D":
                System.out.println("Result: PASS");
                break;
            case "F":
                System.out.println("Result: FAIL");
                break;
            default:
                System.out.println("Result: N/A");
        }

        sc.close();
    }
}
```

---
