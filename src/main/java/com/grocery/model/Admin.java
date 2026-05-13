package com.grocery.model;


// OOP: Inheritance — Admin extends User, inheriting all user fields
// This demonstrates the IS-A relationship: an Admin IS-A User with extra privileges
public class Admin extends User {

    private String adminLevel;   // SUPER, MANAGER, STAFF
    private String lastLogin;
    private boolean active;

    public Admin() {
        super();
        this.setRole("ADMIN"); // always ADMIN
    }

    public Admin(String id, String username, String email, String password,
                 String phone, String address,
                 String adminLevel, String lastLogin, boolean active) {
        // Call parent constructor — reuse User's fields via super()
        super(id, username, email, password, phone, address, "ADMIN");
        this.adminLevel = adminLevel;
        this.lastLogin  = lastLogin;
        this.active     = active;
    }

    // Convert a comma-separated line from admins.txt into an Admin object
    public static Admin fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) return null;
        try {
            return new Admin(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    parts[6].trim(),
                    parts[7].trim(),
                    Boolean.parseBoolean(parts[8].trim())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert this Admin into a line for admins.txt
    @Override
    public String toLine() {
        return getId() + "," + getUsername() + "," + getEmail() + "," +
                getPassword() + "," + getPhone() + "," + getAddress() + "," +
                adminLevel + "," + lastLogin + "," + active;
    }

    // OOP: Polymorphism — overrides toString() from User
    @Override
    public String toString() {
        return "Admin{id='" + getId() + "', username='" + getUsername() +
                "', level='" + adminLevel + "', active=" + active + "}";
    }

    // OOP: Abstraction — admin-only behaviour hidden from regular users
    public boolean canManageAdmins() {
        return "SUPER".equals(adminLevel);
    }

    public boolean canManageProducts() {
        return "SUPER".equals(adminLevel) || "MANAGER".equals(adminLevel);
    }

    public boolean canViewReports() {
        return active;
    }

    // Getters and setters
    public String getAdminLevel()                    { return adminLevel; }
    public void   setAdminLevel(String adminLevel)   { this.adminLevel = adminLevel; }

    public String getLastLogin()                   { return lastLogin; }
    public void   setLastLogin(String lastLogin)   { this.lastLogin = lastLogin; }

    public boolean isActive()                { return active; }
    public void    setActive(boolean active) { this.active = active; }
}