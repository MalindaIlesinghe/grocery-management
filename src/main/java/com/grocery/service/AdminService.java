package com.grocery.service;

import com.grocery.model.Admin;
import com.grocery.model.Order;
import com.grocery.model.Product;
import com.grocery.model.User;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminService {

    private static final String FILE_PATH = "src/main/resources/data/admins.txt";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Services for dashboard stats — OOP composition
    private final UserService    userService    = new UserService();
    private final ProductService productService = new ProductService();
    private final OrderService   orderService   = new OrderService();

    // ──────────────────────────────────────────
    // CREATE — Register a new admin
    // ──────────────────────────────────────────
    public boolean register(Admin admin) {
        if (findByUsername(admin.getUsername()) != null) return false;
        if (findByEmail(admin.getEmail())       != null) return false;

        admin.setId(generateId());
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setLastLogin("Never");

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            writer.write(admin.toLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────
    // READ — Get all admins
    // ──────────────────────────────────────────
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return admins;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Admin a = Admin.fromLine(line);
                    if (a != null) admins.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return admins;
    }

    // READ — Find admin by ID
    public Admin findById(String id) {
        return getAllAdmins().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Find admin by username (for login)
    public Admin findByUsername(String username) {
        return getAllAdmins().stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // READ — Find admin by email
    public Admin findByEmail(String email) {
        return getAllAdmins().stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // ──────────────────────────────────────────
    // LOGIN — Verify admin credentials
    // ──────────────────────────────────────────
    public Admin login(String username, String password) {
        Admin admin = findByUsername(username);
        if (admin == null)               return null;
        if (!admin.isActive())           return null; // blocked admin cannot log in
        if (!admin.getPassword().equals(password)) return null;

        // Update lastLogin timestamp
        admin.setLastLogin(LocalDateTime.now().format(FORMATTER));
        update(admin);

        return admin;
    }

    // ──────────────────────────────────────────
    // UPDATE — Edit admin details or toggle active
    // ──────────────────────────────────────────
    public boolean update(Admin updatedAdmin) {
        List<Admin> admins = getAllAdmins();
        boolean found = false;

        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(updatedAdmin.getId())) {
                admins.set(i, updatedAdmin);
                found = true;
                break;
            }
        }

        if (!found) return false;
        return writeAllAdmins(admins);
    }

    // UPDATE — Toggle admin active/inactive (instead of deleting)
    public boolean toggleActive(String adminId) {
        Admin admin = findById(adminId);
        if (admin == null) return false;
        admin.setActive(!admin.isActive());
        return update(admin);
    }

    // UPDATE — Change admin level
    public boolean changeLevel(String adminId, String newLevel) {
        Admin admin = findById(adminId);
        if (admin == null) return false;
        admin.setAdminLevel(newLevel);
        return update(admin);
    }

    // ──────────────────────────────────────────
    // DELETE — Remove an admin account
    // ──────────────────────────────────────────
    public boolean delete(String id) {
        List<Admin> admins = getAllAdmins();
        boolean removed = admins.removeIf(a -> a.getId().equals(id));
        if (!removed) return false;
        return writeAllAdmins(admins);
    }

    // ──────────────────────────────────────────
    // DASHBOARD — Aggregate stats from all services
    // ──────────────────────────────────────────
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<User>    users    = userService.getAllUsers();
        List<Product> products = productService.getAllProducts();
        List<Order>   orders   = orderService.getAllOrders();

        stats.put("totalUsers",     users.size());
        stats.put("totalProducts",  products.size());
        stats.put("totalOrders",    orders.size());
        stats.put("totalAdmins",    getAllAdmins().size());

        // Order counts by status
        stats.put("pendingOrders",
                orders.stream().filter(Order::isPending).count());
        stats.put("confirmedOrders",
                orders.stream().filter(Order::isConfirmed).count());
        stats.put("deliveredOrders",
                orders.stream().filter(Order::isDelivered).count());
        stats.put("cancelledOrders",
                orders.stream().filter(Order::isCancelled).count());

        // Total revenue from delivered orders only
        double revenue = orders.stream()
                .filter(Order::isDelivered)
                .mapToDouble(Order::getTotalPrice)
                .sum();
        stats.put("totalRevenue", revenue);

        // Low stock products (5 units or fewer)
        List<Product> lowStock = products.stream()
                .filter(p -> p.getStock() <= 5)
                .collect(Collectors.toList());
        stats.put("lowStockProducts", lowStock);

        // 5 most recent orders for the activity feed
        List<Order> recentOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(5)
                .collect(Collectors.toList());
        stats.put("recentOrders", recentOrders);

        return stats;
    }

    // ──────────────────────────────────────────
    // HELPER — Rewrite the entire admins file
    // ──────────────────────────────────────────
    private boolean writeAllAdmins(List<Admin> admins) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, false))) {
            for (Admin a : admins) {
                writer.write(a.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateId() {
        return "A" + System.currentTimeMillis();
    }
}