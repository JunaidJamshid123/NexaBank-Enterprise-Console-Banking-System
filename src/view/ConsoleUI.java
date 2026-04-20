package view;

import java.util.Scanner;

/**
 * Shared console utilities: clear screen, pause, input helpers, banner.
 * All view classes delegate to this for consistent UI behaviour.
 */
public class ConsoleUI {

    private static final Scanner scanner = new Scanner(System.in);

    // ─── Screen Control ───────────────────────────────────────────────

    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.println("\n".repeat(50));
        }
    }

    public static void pause() {
        System.out.print("\n  Press Enter to continue...");
        scanner.nextLine();
    }

    // ─── Banner ───────────────────────────────────────────────────────

    public static void printBanner() {
        System.out.println("""
        
        ╔══════════════════════════════════════════════════╗
        ║              ★  N E X A B A N K  ★              ║
        ║          Modern Console Banking System           ║
        ╚══════════════════════════════════════════════════╝
        """);
    }

    // ─── Input Helpers ────────────────────────────────────────────────

    public static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("  [!] Please enter a valid number.");
            scanner.nextLine();
            System.out.print(prompt);
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static double readDouble(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return Double.parseDouble(input);
    }

    public static Scanner getScanner() {
        return scanner;
    }
}
