package model.user;

import java.time.LocalDateTime;

/**
 * Represents a bank customer.
 *
 * OOP Constraint: this() constructor chaining — the shorter constructor
 * delegates to the full one via this(...), which in turn calls super(...).
 *
 * Constructor chain:  Customer(id, name, email, pw)
 *                         ↓ this(...)
 *                     Customer(id, name, email, pw, createdAt, isActive, phone, address)
 *                         ↓ super(...)
 *                     User(id, name, email, pw, createdAt, isActive)
 */
public class Customer extends User {

    // ─── Customer-Specific Fields ─────────────────────────────────────
    private String phoneNumber;
    private String address;
    private int creditScore;
    private int loginFailCount;

    // ─── Full Constructor ─────────────────────────────────────────────
    public Customer(int id, String fullName, String email, String passwordHash,
                    LocalDateTime createdAt, boolean isActive, String phoneNumber, String address,
                    int creditScore) {
        super(id, fullName, email, passwordHash, createdAt, isActive);  // super() — never copy fields
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.creditScore = creditScore;
        this.loginFailCount = 0;
    }

    // ─── Shorter Constructor — delegates via this() ───────────────────
    /**
     * Convenience constructor: defaults to active, current timestamp, no phone/address, credit score 650.
     * Uses this() chaining to delegate to the full constructor.
     */
    public Customer(int id, String fullName, String email, String passwordHash) {
        this(id, fullName, email, passwordHash, LocalDateTime.now(), true, null, null, 650);
    }

    // ─── Abstract Method Implementations ──────────────────────────────
    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getDashboardLabel() {
        return "Welcome, %s — Customer Portal".formatted(getFullName());
    }

    // ─── Getters & Setters ────────────────────────────────────────────
    public String getPhoneNumber()          { return phoneNumber; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }
    public String getAddress()              { return address; }
    public void setAddress(String address)  { this.address = address; }
    public int getCreditScore()             { return creditScore; }
    public void setCreditScore(int score)   { this.creditScore = score; }
    public int getLoginFailCount()          { return loginFailCount; }
    public void setLoginFailCount(int count){ this.loginFailCount = count; }
    public void incrementLoginFailCount()   { this.loginFailCount++; }
    public void resetLoginFailCount()       { this.loginFailCount = 0; }
}
