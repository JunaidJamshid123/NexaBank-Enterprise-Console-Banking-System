package model.report;

import java.util.List;

public record PaginatedResult<T>(
        List<T> items,
        int currentPage,
        int pageSize,
        int totalItems,
        int totalPages
) {
    public PaginatedResult {
        if (currentPage < 1) throw new IllegalArgumentException("Page must be >= 1");
        if (pageSize < 1) throw new IllegalArgumentException("Page size must be >= 1");
    }

    public boolean hasNextPage() { return currentPage < totalPages; }
    public boolean hasPreviousPage() { return currentPage > 1; }
}
