package com.grocery.service;

import com.grocery.model.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UserService {

    // Path to users.txt — adjust if needed
    private static final String FILE_PATH = "src/main/resources/data/users.txt";


    // CREATE — Register a new user

    public boolean register(User user) {
        // Check if username or email already exists
        if (findByUsername(user.getUsername()) != null) return false;
        if (findByEmail(user.getEmail()) != null) return false;

        user.setId(generateId());
        user.setRole("USER"); // default role

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) { // append = true
            writer.write(user.toLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    // READ — Get all users

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return users;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    User u = User.fromLine(line);
                    if (u != null) users.add(u);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    // READ — Find user by ID
    public User findById(String id) {
        return getAllUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Find user by username (for login)
    public User findByUsername(String username) {
        return getAllUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // READ — Find user by email
    public User findByEmail(String email) {
        return getAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }


    // LOGIN — Verify credentials

    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null; // credentials don't match
    }


    // UPDATE — Modify an existing user

    public boolean update(User updatedUser) {
        List<User> users = getAllUsers();
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) return false;
        return writeAllUsers(users);
    }


    // DELETE — Remove a user by ID

    public boolean delete(String id) {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (!removed) return false;
        return writeAllUsers(users);
    }


    // HELPER — Rewrite the entire file

    private boolean writeAllUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (User u : users) {
                writer.write(u.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // HELPER — Generate a simple unique ID
    private String generateId() {
        return "U" + System.currentTimeMillis();
    }
}