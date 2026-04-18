package model.user;

import java.time.LocalDateTime;

/**
 * Represents a bank administrator with system-wide privileges.
 */
public class BankAdmin extends User {

    // ─── Admin-Specific Fields ────────────────────────────────────────
    private int accessLevel;  // 1 = basic, 2 = senior, 3 = super-admin

    // ─── Constructor ──────────────────────────────────────────────────
    public BankAdmin(int id, String fullName, String email, String passwordHash,
                     LocalDateTime createdAt, boolean isActive, int accessLevel) {
        super(id, fullName, email, passwordHash, createdAt, isActive);
        this.accessLevel = accessLevel;
    }

    // ─── Abstract Method Implementations ──────────────────────────────
    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardLabel() {
        return "Admin Panel — %s (Level %d)".formatted(getFullName(), accessLevel);
    }

    // ─── Getters & Setters ────────────────────────────────────────────
    public int getAccessLevel()                  { return accessLevel; }
    public void setAccessLevel(int accessLevel)  { this.accessLevel = accessLevel; }
}
