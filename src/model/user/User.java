package model.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base class for all users in the NexaBank system.
 *
 * OOP Constraints enforced:
 *  - Subclasses MUST call super() — fields are private, not duplicated
 *  - equals() and hashCode() based on id only
 *  - final auditString() — subclasses cannot override
 *  - Abstract getRole() and getDashboardLabel() — called polymorphically via List<User>
 */
public abstract class User {

    // ─── Fields (private — subclasses must use super() and getters) ───
    private final int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private final LocalDateTime createdAt;
    private boolean isActive;

    // ─── Constructor ──────────────────────────────────────────────────
    public User(int id, String fullName, String email, String passwordHash, LocalDateTime createdAt, boolean isActive) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.isActive = isActive;
    }

    // ─── Abstract Methods (polymorphic — called via List<User>) ──────
    /**
     * Returns the role identifier for this user (e.g. "CUSTOMER", "TELLER", "ADMIN").
     * Called polymorphically: iterate a List<User> and call getRole() without casting.
     */
    public abstract String getRole();

    /**
     * Returns a user-friendly label for the dashboard header.
     */
    public abstract String getDashboardLabel();

    // ─── Final Method — subclasses CANNOT override ────────────────────
    /**
     * Returns a standardised audit string for logging and compliance reporting.
     * Declared final so that all subclasses produce a consistent, tamper-proof format.
     */
    public final String auditString() {
        return "[AUDIT] ID: %d | Role: %s | Name: %s | Email: %s | Active: %s | Created: %s"
                .formatted(id, getRole(), fullName, email, isActive, createdAt);
    }

    // ─── Getters ──────────────────────────────────────────────────────
    public int getId()                   { return id; }
    public String getFullName()          { return fullName; }
    public String getEmail()             { return email; }
    public String getPasswordHash()      { return passwordHash; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public boolean isActive()            { return isActive; }

    // ─── Setters (mutable fields only) ────────────────────────────────
    public void setFullName(String fullName)       { this.fullName = fullName; }
    public void setEmail(String email)             { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active)          { this.isActive = active; }

    // ─── equals() — based on id ONLY ──────────────────────────────────
    /**
     * Two users are equal if and only if they share the same id.
     * This is consistent across all subclasses (Customer, Teller, BankAdmin).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return this.id == other.id;
    }

    // ─── hashCode() — consistent with equals() (id only) ─────────────
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ─── toString ─────────────────────────────────────────────────────
    @Override
    public String toString() {
        return "%s{id=%d, name='%s', email='%s', role=%s, active=%s}"
                .formatted(getClass().getSimpleName(), id, fullName, email, getRole(), isActive);
    }
}