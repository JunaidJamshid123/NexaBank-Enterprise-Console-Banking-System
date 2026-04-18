# 03 — Loops: for, while, do-while, for-each (In Depth)

---

## What is a Loop?

A loop **repeats a block of code** until a condition becomes false. Without loops, you'd copy-paste the same code hundreds of times.

```
  WITHOUT LOOP:                  WITH LOOP:
  ─────────────                  ──────────
  System.out.println(1);         for (int i = 1; i <= 100; i++) {
  System.out.println(2);             System.out.println(i);
  System.out.println(3);         }
  ... (97 more lines)
  System.out.println(100);       // 3 lines instead of 100!
```

## The 4 Loop Types

```
╔═══════════════════════════════════════════════════════════════════════╗
║                         JAVA LOOPS                                   ║
╠═══════════════╦═══════════════╦════════════════╦══════════════════════╣
║     for       ║    while      ║   do-while     ║    for-each          ║
╠═══════════════╬═══════════════╬════════════════╬══════════════════════╣
║ Count is      ║ Count is      ║ Must run at    ║ Iterate arrays/     ║
║ KNOWN         ║ UNKNOWN       ║ LEAST ONCE     ║ collections         ║
╠═══════════════╬═══════════════╬════════════════╬══════════════════════╣
║ Check BEFORE  ║ Check BEFORE  ║ Check AFTER    ║ No index needed     ║
║ each run      ║ each run      ║ each run       ║                      ║
╠═══════════════╬═══════════════╬════════════════╬══════════════════════╣
║ for(init;     ║ while(cond){  ║ do {           ║ for(type x : arr){  ║
║  cond; upd){  ║   body;       ║   body;        ║   body;             ║
║   body;       ║ }             ║ } while(cond); ║ }                   ║
║ }             ║               ║                ║                      ║
╚═══════════════╩═══════════════╩════════════════╩══════════════════════╝
```

---
---

# 1. for Loop

Use when you **know exactly how many times** to repeat.

## Anatomy of a for Loop

```
  for ( init  ;  condition  ;  update )  {  body  }
        ─┬──     ────┬────     ──┬───       ──┬──
         │           │           │             │
         │           │           │             └── code that repeats
         │           │           └── runs AFTER each iteration
         │           └── checked BEFORE each iteration
         └── runs ONCE at the very beginning


  EXECUTION ORDER:
  ┌──────────────────────────────────────────────────────────┐
  │                                                          │
  │  1. init (once)                                          │
  │         │                                                │
  │  2. ┌───▼────────┐                                      │
  │     │ condition?  │──── FALSE ──► EXIT LOOP              │
  │     └───┬────────┘                                      │
  │       TRUE                                               │
  │         │                                                │
  │  3. ┌───▼────────┐                                      │
  │     │   body     │                                      │
  │     └───┬────────┘                                      │
  │         │                                                │
  │  4. ┌───▼────────┐                                      │
  │     │  update    │                                      │
  │     └───┬────────┘                                      │
  │         │                                                │
  │         └──── go back to step 2                          │
  │                                                          │
  └──────────────────────────────────────────────────────────┘
```

### Step-by-Step Trace

```java
for (int i = 1; i <= 5; i++) {
    System.out.print(i + " ");
}
// Output: 1 2 3 4 5
```

```
  Step │  i  │ i <= 5? │ Action       │ After update
  ─────┼─────┼─────────┼──────────────┼─────────────
   1   │  1  │  true   │  print "1 "  │  i = 2
   2   │  2  │  true   │  print "2 "  │  i = 3
   3   │  3  │  true   │  print "3 "  │  i = 4
   4   │  4  │  true   │  print "4 "  │  i = 5
   5   │  5  │  true   │  print "5 "  │  i = 6
   6   │  6  │  false  │  EXIT LOOP   │  —
```

### Counting Backwards

```java
for (int i = 10; i >= 1; i--) {
    System.out.print(i + " ");
}
// Output: 10 9 8 7 6 5 4 3 2 1
```

### Step by Custom Amount

```java
// Even numbers
for (int i = 0; i <= 20; i += 2) {
    System.out.print(i + " ");
}
// Output: 0 2 4 6 8 10 12 14 16 18 20
```

---

## Nested for Loop

A loop inside a loop. The inner loop completes ALL its iterations for EACH iteration of the outer loop.

```
  OUTER i=1  →  INNER j=1,2,3  (complete inner loop)
  OUTER i=2  →  INNER j=1,2,3  (complete inner loop)
  OUTER i=3  →  INNER j=1,2,3  (complete inner loop)

  Total iterations = outer × inner = 3 × 3 = 9
```

### Pattern: Right Triangle

```java
for (int i = 1; i <= 5; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print("* ");
    }
    System.out.println();  // new line after each row
}
/*
Output:
*
* *
* * *
* * * *
* * * * *
*/
```

### Pattern: Multiplication Table

```java
int n = 5;
for (int i = 1; i <= n; i++) {
    for (int j = 1; j <= 10; j++) {
        System.out.printf("%2d x %2d = %3d   ", i, j, i * j);
    }
    System.out.println();
}
```

### Pattern: Number Pyramid

```java
int rows = 5;
for (int i = 1; i <= rows; i++) {
    // Print spaces
    for (int s = 1; s <= rows - i; s++) {
        System.out.print("  ");
    }
    // Print numbers
    for (int j = 1; j <= (2 * i - 1); j++) {
        System.out.print(i + " ");
    }
    System.out.println();
}
/*
Output:
        1
      2 2 2
    3 3 3 3 3
  4 4 4 4 4 4 4
5 5 5 5 5 5 5 5 5
*/
```

---
---

# 2. while Loop

Use when you **don't know** how many times to repeat — depends on a condition that may change during execution.

```
  ┌──────────────────────────────────────────┐
  │                                          │
  │  ┌───────────────┐                      │
  │  │  condition?   │──── FALSE ──► EXIT   │
  │  └───────┬───────┘                      │
  │        TRUE                              │
  │          │                               │
  │  ┌───────▼───────┐                      │
  │  │     body      │                      │
  │  │ (must change  │                      │
  │  │  condition!)  │                      │
  │  └───────┬───────┘                      │
  │          │                               │
  │          └──── go back to condition      │
  │                                          │
  └──────────────────────────────────────────┘

  ⚠ If condition NEVER becomes false → INFINITE LOOP!
```

### Basic while

```java
int count = 1;

while (count <= 5) {
    System.out.println("Count: " + count);
    count++;       // ← MUST update, or infinite loop!
}
// Output: Count: 1, 2, 3, 4, 5
```

### Real Use Case — Sum Until Threshold

```java
int sum = 0;
int number = 1;

while (sum < 100) {
    sum += number;
    number++;
}

System.out.println("Sum reached: " + sum);       // 105
System.out.println("Numbers added: " + (number - 1)); // 14
// We didn't know in advance how many numbers to add
```

### Real Use Case — User Input Loop

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
String input = "";

while (!input.equals("quit")) {
    System.out.print("Enter command (type 'quit' to exit): ");
    input = sc.nextLine();

    if (!input.equals("quit")) {
        System.out.println("You entered: " + input);
    }
}
System.out.println("Goodbye!");
sc.close();
```

### Real Use Case — Finding a Digit

```java
int number = 987654;
int target = 5;
boolean found = false;

int temp = number;
while (temp > 0) {
    int digit = temp % 10;     // get last digit
    if (digit == target) {
        found = true;
        break;
    }
    temp = temp / 10;          // remove last digit
}

System.out.println("Number " + number + " contains " + target + "? " + found);
// true
```

---
---

# 3. do-while Loop

**Executes body FIRST**, then checks condition. Guarantees **at least one execution**, even if condition is false from the start.

```
  ┌──────────────────────────────────────────┐
  │                                          │
  │  ┌───────────────┐                      │
  │  │     body      │  ◄── runs FIRST      │
  │  └───────┬───────┘      (always!)       │
  │          │                               │
  │  ┌───────▼───────┐                      │
  │  │  condition?   │──── FALSE ──► EXIT   │
  │  └───────┬───────┘                      │
  │        TRUE                              │
  │          │                               │
  │          └──── go back to body           │
  │                                          │
  └──────────────────────────────────────────┘
```

### Key Difference from while

```
  while:                            do-while:
  ─────                             ────────
  Check FIRST, then run.            Run FIRST, then check.
  Might run 0 times.                Always runs at least 1 time.

  int x = 100;                     int x = 100;
  while (x < 5) {                  do {
    print(x);   // NEVER runs       print(x);   // Runs ONCE (prints 100)
  }                                 } while (x < 5);
```

```java
// While — doesn't run (10 < 5 is false)
int x = 10;
while (x < 5) {
    System.out.println("While: " + x);   // never prints
}

// Do-while — runs once (body first, then 10 < 5 is false → exits)
int y = 10;
do {
    System.out.println("Do-While: " + y);  // prints "Do-While: 10"
} while (y < 5);
```

### Real Use Case — Menu System

```java
import java.util.Scanner;

Scanner sc = new Scanner(System.in);
int choice;

do {
    System.out.println("\n══════ MENU ══════");
    System.out.println("1. View Balance");
    System.out.println("2. Deposit");
    System.out.println("3. Withdraw");
    System.out.println("4. Exit");
    System.out.print("Choose option: ");
    choice = sc.nextInt();

    switch (choice) {
        case 1: System.out.println("Balance: $1000"); break;
        case 2: System.out.println("Deposit successful"); break;
        case 3: System.out.println("Withdrawal successful"); break;
        case 4: System.out.println("Goodbye!"); break;
        default: System.out.println("Invalid option!");
    }

} while (choice != 4);
// Menu shows at least once, keeps looping until user picks 4

sc.close();
```

### Real Use Case — Input Validation

```java
Scanner sc = new Scanner(System.in);
int age;

do {
    System.out.print("Enter your age (1-120): ");
    age = sc.nextInt();

    if (age < 1 || age > 120) {
        System.out.println("Invalid! Try again.");
    }

} while (age < 1 || age > 120);  // keep asking until valid

System.out.println("Your age: " + age);
sc.close();
```

---
---

# 4. for-each Loop (Enhanced for)

Simplifies iterating over **arrays** and **collections**. No index variable, no risk of `ArrayIndexOutOfBoundsException`.

```
  REGULAR for:                        FOR-EACH:
  ────────────                        ─────────
  for(int i=0; i<arr.length; i++){    for(int x : arr) {
      System.out.println(arr[i]);         System.out.println(x);
  }                                   }

  ┌──────────────────────────────────────────────────┐
  │  for (dataType  variable  :  array/collection)   │
  │      ────────   ────────     ─────────────────   │
  │      type of    name for     the thing you're    │
  │      each item  current item iterating over      │
  └──────────────────────────────────────────────────┘
```

```
  How it works internally:

  int[] nums = {10, 20, 30, 40};

  Iteration 1:  n = 10  (first element)
  Iteration 2:  n = 20  (second element)
  Iteration 3:  n = 30  (third element)
  Iteration 4:  n = 40  (fourth element)
  → DONE (no more elements)
```

### Basic Examples

```java
// Array of integers
int[] scores = {85, 92, 78, 95, 88};

for (int score : scores) {
    System.out.println("Score: " + score);
}

// Array of strings
String[] fruits = {"Apple", "Banana", "Cherry", "Mango"};

for (String fruit : fruits) {
    System.out.println("Fruit: " + fruit);
}

// Calculate sum
int total = 0;
for (int s : scores) {
    total += s;
}
System.out.println("Average: " + (total / scores.length));  // 87
```

### Limitation — No Index Access

```java
// for-each does NOT give you the index
// If you need index, use regular for loop

String[] names = {"Alice", "Bob", "Charlie"};

// for-each — no index
for (String name : names) {
    System.out.println(name);
}

// regular for — with index
for (int i = 0; i < names.length; i++) {
    System.out.println("Index " + i + ": " + names[i]);
}
```

### Limitation — Cannot Modify Array

```java
int[] nums = {1, 2, 3, 4, 5};

// This does NOT modify the original array!
for (int n : nums) {
    n = n * 2;   // only modifies the local copy 'n'
}
// nums is still {1, 2, 3, 4, 5}

// To modify, use regular for:
for (int i = 0; i < nums.length; i++) {
    nums[i] = nums[i] * 2;   // modifies actual array
}
// nums is now {2, 4, 6, 8, 10}
```

---
---

# 5. Loop Control Statements

### break — Exit Immediately

Stops the loop completely and jumps to the code after the loop.

```
  ┌────────────┐
  │   loop     │
  │   ...      │
  │  if (cond) │
  │   BREAK ───┼──► EXIT loop immediately
  │   ...      │
  └────────────┘
```

```java
// Find first negative number
int[] data = {5, 12, 8, -3, 7, -1};

for (int num : data) {
    if (num < 0) {
        System.out.println("First negative: " + num);  // -3
        break;  // stop searching
    }
}
```

### continue — Skip Current Iteration

Skips the rest of the current iteration and jumps to the next one.

```
  ┌─────────────────────────┐
  │   loop  iteration 1     │  ──► runs normally
  │   loop  iteration 2     │  ──► CONTINUE (skip rest) ──┐
  │   loop  iteration 3     │  ──► runs normally           │
  │   loop  iteration 4     │  ◄──────────────────────────┘
  └─────────────────────────┘      (goes to next iteration)
```

```java
// Print only even numbers
for (int i = 1; i <= 10; i++) {
    if (i % 2 != 0) {
        continue;  // skip odd numbers
    }
    System.out.print(i + " ");
}
// Output: 2 4 6 8 10
```

### Labeled break — Exit Outer Loop from Inner Loop

```java
// Without label — break only exits the INNER loop
// With label — break exits the LABELED loop

outer:                              // label
for (int i = 1; i <= 5; i++) {
    for (int j = 1; j <= 5; j++) {
        if (i * j > 10) {
            System.out.println("Breaking at i=" + i + " j=" + j);
            break outer;            // exits BOTH loops
        }
        System.out.print(i * j + "\t");
    }
    System.out.println();
}
/*
Output:
1	2	3	4	5
2	4	6	8	10
Breaking at i=3 j=4
*/
```

---

# 6. Infinite Loops

Loops that never end — useful for servers, game loops, or event listeners. Always pair with a `break` condition!

```java
// Infinite for
for (;;) {
    // runs forever
    break;  // must have exit condition
}

// Infinite while
while (true) {
    // runs forever
    break;  // must have exit condition
}
```

---

# 7. Loop Comparison Table

```
┌─────────────────┬──────────────┬──────────────┬───────────────┬──────────────┐
│    Feature      │     for      │    while     │   do-while    │  for-each    │
├─────────────────┼──────────────┼──────────────┼───────────────┼──────────────┤
│ Iterations known│     YES      │      NO      │      NO       │ YES (size)   │
│ Min executions  │      0       │       0      │    **1**      │      0       │
│ Condition check │   BEFORE     │    BEFORE    │    AFTER      │  Automatic   │
│ Index access    │     YES      │      YES     │     YES       │     NO       │
│ Can modify arr  │     YES      │      YES     │     YES       │     NO       │
│ Use case        │  Counting    │  Unknown end │  Menu/Retry   │ Read arrays  │
│ Syntax          │ Compact      │  Flexible    │  Guaranteed 1 │ Cleanest     │
│ Infinite risk   │  Low (clear) │  Medium      │   Medium      │  None        │
└─────────────────┴──────────────┴──────────────┴───────────────┴──────────────┘
```

---

# Full Example — Number Guessing Game

```java
import java.util.Scanner;
import java.util.Random;

public class GuessingGame {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        int secret = rand.nextInt(100) + 1;  // 1-100
        int guess;
        int attempts = 0;

        System.out.println("I'm thinking of a number between 1 and 100!");

        do {
            System.out.print("Your guess: ");
            guess = sc.nextInt();
            attempts++;

            if (guess < secret) {
                System.out.println("Too LOW! Try higher.");
            } else if (guess > secret) {
                System.out.println("Too HIGH! Try lower.");
            } else {
                System.out.println("CORRECT! You got it in " + attempts + " attempts!");
            }

        } while (guess != secret);

        // Rating loop
        String rating;
        if (attempts <= 3) {
            rating = "Genius!";
        } else if (attempts <= 7) {
            rating = "Great job!";
        } else {
            rating = "Keep practicing!";
        }
        System.out.println("Rating: " + rating);

        sc.close();
    }
}
```

---
