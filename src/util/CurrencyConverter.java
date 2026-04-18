package util;

import exception.CurrencyConversionException;

import java.util.*;
import java.util.function.Predicate;

public class CurrencyConverter {

    /**
     * TreeMap<String, Double> — stores exchange rates sorted by ISO currency code.
     *
     * TreeMap is backed by a Red-Black Tree (self-balancing BST), giving O(log n)
     * for get/put but guaranteeing keys are always sorted in natural order.
     * This means iterating RATES_TO_USD prints currencies alphabetically:
     *   AED → EUR → GBP → JPY → PKR → SAR → USD
     *
     * Here, sorted order is preferred over HashMap's O(1) lookup because:
     *   1. The map is small (7 entries) — O(log 7) ≈ O(1) in practice.
     *   2. Sorted output is useful when displaying supported currencies to users.
     *   3. getSupportedCurrencies() returns a naturally sorted Set for free.
     */
    private static final TreeMap<String, Double> RATES_TO_USD = new TreeMap<>();

    static {
        RATES_TO_USD.put("USD", 1.0);
        RATES_TO_USD.put("EUR", 0.92);
        RATES_TO_USD.put("GBP", 0.79);
        RATES_TO_USD.put("PKR", 278.5);
        RATES_TO_USD.put("JPY", 149.3);
        RATES_TO_USD.put("AED", 3.67);
        RATES_TO_USD.put("SAR", 3.75);
    }

    // Predicate used to validate currency codes before adding
    private static final Predicate<String> IS_VALID_CURRENCY_CODE =
            code -> code != null && code.matches("^[A-Z]{3}$");

    // ═══════════════════════════════════════════════════════════════════
    //  convert — returns Optional<Double>. Empty for unknown currencies.
    // ═══════════════════════════════════════════════════════════════════
    public static Optional<Double> convert(double amount, String from, String to) {
        if (from.equals(to)) return Optional.of(amount);

        if (!RATES_TO_USD.containsKey(from) || !RATES_TO_USD.containsKey(to)) {
            return Optional.empty();
        }

        try {
            double fromRate = RATES_TO_USD.get(from);
            double toRate = RATES_TO_USD.get(to);

            // Convert: from → USD → to
            double amountInUsd = amount / fromRate;
            double converted = amountInUsd * toRate;

            return Optional.of(Math.round(converted * 100.0) / 100.0);
        } catch (ArithmeticException e) {
            throw new CurrencyConversionException(from, to,
                    "Failed to convert %.2f from %s to %s: %s"
                            .formatted(amount, from, to, e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getSupportedCurrencies — unmodifiable sorted set from TreeMap
    // ═══════════════════════════════════════════════════════════════════
    public static Set<String> getSupportedCurrencies() {
        return Collections.unmodifiableSet(RATES_TO_USD.keySet());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  addRate — validates currency code with Predicate before adding
    // ═══════════════════════════════════════════════════════════════════
    public static void addRate(String currencyCode, double rate) {
        if (!IS_VALID_CURRENCY_CODE.test(currencyCode)) {
            throw new IllegalArgumentException(
                    "Invalid currency code '%s'. Must be exactly 3 uppercase letters (e.g. 'USD').".formatted(currencyCode));
        }
        if (rate <= 0) {
            throw new IllegalArgumentException(
                    "Exchange rate must be positive. Provided: %.4f".formatted(rate));
        }
        RATES_TO_USD.put(currencyCode, rate);
    }

    public static boolean isSupported(String currency) {
        return RATES_TO_USD.containsKey(currency);
    }
}
