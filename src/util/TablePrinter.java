package util;

import model.report.PaginatedResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Console formatting utility — prints ASCII tables and paginated results.
 *
 * Uses String.format() with %-Ns for left-aligned column padding.
 */
public class TablePrinter {

    // ─── Private Constructor — static utility class ───────────────────
    private TablePrinter() {
        throw new UnsupportedOperationException("TablePrinter is a static utility class and cannot be instantiated.");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  printTable — calculates column widths, draws bordered ASCII table
    // ═══════════════════════════════════════════════════════════════════
    public static void printTable(List<String> headers, List<List<String>> rows) {
        int cols = headers.size();

        // Calculate max width for each column (header vs data)
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < cols && i < row.size(); i++) {
                String cell = row.get(i) != null ? row.get(i) : "";
                widths[i] = Math.max(widths[i], cell.length());
            }
        }

        // Build format string: "| %-Ws | %-Ws | ... |"
        StringBuilder formatBuilder = new StringBuilder("|");
        for (int w : widths) {
            formatBuilder.append(" %-").append(w).append("s |");
        }
        String format = formatBuilder.toString();

        // Build horizontal border: "+---+---+...+"
        String border = buildBorder(widths);

        // Print table
        System.out.println(border);
        System.out.printf((format) + "%n", headers.toArray());
        System.out.println(border);

        for (List<String> row : rows) {
            // Pad row if fewer cells than headers
            Object[] cells = new Object[cols];
            for (int i = 0; i < cols; i++) {
                cells[i] = (i < row.size() && row.get(i) != null) ? row.get(i) : "";
            }
            System.out.printf((format) + "%n", cells);
        }

        System.out.println(border);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  printPaginated — generic, works with any entity via rowMapper
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Prints a paginated result as an ASCII table.
     *
     * @param <T>       the entity type
     * @param result    the PaginatedResult to display
     * @param headers   column headers
     * @param rowMapper Function that converts an entity T to a List<String> row
     */
    public static <T> void printPaginated(PaginatedResult<T> result,
                                          List<String> headers,
                                          Function<T, List<String>> rowMapper) {
        List<List<String>> rows = result.items().stream()
                .map(rowMapper)
                .toList();

        printTable(headers, rows);

        // Print pagination info
        System.out.printf("  Page %d of %d  |  Total items: %d  |  Page size: %d%n",
                result.currentPage(), result.totalPages(),
                result.totalItems(), result.pageSize());

        if (result.hasPreviousPage()) System.out.print("  ← Previous");
        if (result.hasPreviousPage() && result.hasNextPage()) System.out.print("  |  ");
        if (result.hasNextPage()) System.out.print("  Next →");
        System.out.println();
    }

    // ─── Helper: build border line ────────────────────────────────────
    private static String buildBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }
}
