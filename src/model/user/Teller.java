package model.user;

import java.time.LocalDateTime;

/**
 * Represents a bank teller / employee.
 */
public class Teller extends User {

    // ─── Teller-Specific Fields ───────────────────────────────────────
    private String branch;
    private String employeeId;

    // ─── Constructor ──────────────────────────────────────────────────
    public Teller(int id, String fullName, String email, String passwordHash,
                  LocalDateTime createdAt, boolean isActive, String branch, String employeeId) {
        super(id, fullName, email, passwordHash, createdAt, isActive);
        this.branch = branch;
        this.employeeId = employeeId;
    }

    // ─── Abstract Method Implementations ──────────────────────────────
    @Override
    public String getRole() {
        return "TELLER";
    }

    @Override
    public String getDashboardLabel() {
        return "Teller Console — %s [%s]".formatted(getFullName(), branch);
    }

    // ─── Getters & Setters ────────────────────────────────────────────
    public String getBranch()                  { return branch; }
    public void setBranch(String branch)       { this.branch = branch; }
    public String getEmployeeId()              { return employeeId; }
    public void setEmployeeId(String empId)    { this.employeeId = empId; }
}
