package com.grocery.model;

public class User {

    private String id;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String role; // "USER" or "ADMIN"

    // Default constructor
    public User() {}

    // Full constructor
    public User(String id, String username, String email,
                String password, String phone, String address, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    //Convert a comma-separated line from users.txt into a User object
    public static User fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) return null;
        return new User(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                parts[5].trim(),
                parts[6].trim()
        );
    }

    // Convert this User object into a line for users.txt
    public String toLine() {
        return id + "," + username + "," + email + "," +
                password + "," + phone + "," + address + "," + role;
    }

    // OOP: Encapsulation — all fields private, accessed via getters/setters


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username +
                "', email='" + email + "', role='" + role + "'}";
    }
}