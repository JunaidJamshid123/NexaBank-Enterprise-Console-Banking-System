package util;

import exception.ValidationException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class: all validators built from composed functional interfaces.
 *
 * No if-else inside this class — only Predicate composition, Function
 * chaining, Consumer combination, and method references.
 */
public class Validator {

    // ─── Private Constructor — static utility class ───────────────────
    private Validator() {
        throw new UnsupportedOperationException("Validator is a static utility class and cannot be instantiated.");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Predicates
    // ═══════════════════════════════════════════════════════════════════

    // Matches: word chars / dots / hyphens @ word chars / dots / hyphens . 2+ lowercase letters
    public static final Predicate<String> isValidEmail =
            email -> email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$");

    // At least 8 characters, 1 uppercase, 1 digit — uses method references
    public static final Predicate<String> isStrongPassword =
            pwd -> pwd.length() >= 8
                    && pwd.chars().anyMatch(Character::isUpperCase)   // method reference
                    && pwd.chars().anyMatch(Character::isDigit);      // method reference

    public static final Predicate<Double> isPositiveAmount =
            amount -> amount != null && amount > 0;

    public static final Predicate<Integer> isValidCreditScore =
            score -> score >= 300 && score <= 850;

    // ═══════════════════════════════════════════════════════════════════
    //  Composed Predicates — Predicate.and(), Predicate.or(), negate()
    // ═══════════════════════════════════════════════════════════════════

    // Composed: email must be valid AND not blank
    public static final Predicate<String> isValidEmailAndNotEmpty =
            isValidEmail.and(s -> !s.isBlank());

    // Composed: password must be strong AND at least 12 chars for admin
    public static final Predicate<String> isAdminPassword =
            isStrongPassword.and(pwd -> pwd.length() >= 12);

    // Negated: identifies weak passwords
    public static final Predicate<String> isWeakPassword =
            isStrongPassword.negate();

    // ═══════════════════════════════════════════════════════════════════
    //  Function examples — Function.andThen() composition
    // ═══════════════════════════════════════════════════════════════════

    // String::toLowerCase is a method reference; andThen chains String::trim
    public static final Function<String, String> normalizeEmail =
            ((Function<String, String>) String::toLowerCase).andThen(String::trim);

    // Formats a double amount to 2 decimal places
    public static final Function<Double, String> formatCurrency =
            amount -> String.format("%.2f", amount);

    // ═══════════════════════════════════════════════════════════════════
    //  Consumer examples — Consumer.andThen() chaining
    // ═══════════════════════════════════════════════════════════════════

    // Logs an action message to console — uses method reference System.out::println
    public static final Consumer<String> logAction = System.out::println;

    // Combined consumer: log action then print a separator
    public static final Consumer<String> logAndSeparate =
            logAction.andThen(msg -> System.out.println("─".repeat(40)));

    // ═══════════════════════════════════════════════════════════════════
    //  validate — generic method using Predicate<T>
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Validates a value against a Predicate rule. Throws ValidationException
     * if the predicate test fails.
     *
     * @param <T>          the type of value being validated
     * @param value        the value to test
     * @param rule         the Predicate that must return true for valid input
     * @param errorMessage the error message if validation fails
     * @throws ValidationException if rule.test(value) returns false
     */
    public static <T> void validate(T value, Predicate<T> rule, String errorMessage) throws ValidationException {
        // No if-else branching — Predicate.test() drives the logic
        // negate() flips the rule: if NOT valid → throw
        Predicate<T> fails = rule.negate();
        if (fails.test(value)) {
            throw new ValidationException(errorMessage, value);
        }
    }
}
