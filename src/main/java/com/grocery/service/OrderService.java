package com.grocery.service;

import com.grocery.model.Order;
import com.grocery.model.Product;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class OrderService {

    private static final String FILE_PATH = "src/main/resources/data/orders.txt";

    private final ProductService productService = new ProductService();


    // CREATE — Place a new order

    public String placeOrder(String userId, String productId, int quantity) {
        // Validate product exists and has enough stock
        Product product = productService.findById(productId);
        if (product == null)              return "PRODUCT_NOT_FOUND";
        if (!product.isAvailable())       return "PRODUCT_UNAVAILABLE";
        if (product.getStock() < quantity) return "INSUFFICIENT_STOCK";

        // Reduce stock in products.txt
        boolean stockReduced = productService.reduceStock(productId, quantity);
        if (!stockReduced) return "STOCK_UPDATE_FAILED";

        // Build and save the order
        Order order = new Order();
        order.setId(generateId());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice() * quantity);
        order.setStatus("PENDING");
        order.setOrderDate(Order.now());

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            writer.write(order.toLine());
            writer.newLine();
            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "SAVE_FAILED";
        }
    }


    // READ — Get all orders

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return orders;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Order o = Order.fromLine(line);
                    if (o != null) orders.add(o);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // READ — Get orders for a specific user
    public List<Order> getOrdersByUser(String userId) {
        return getAllOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    // READ — Find a single order by ID
    public Order findById(String id) {
        return getAllOrders().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Get orders filtered by status (admin use)
    public List<Order> getOrdersByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }


    // UPDATE — Change order status (admin)

    public boolean updateStatus(String orderId, String newStatus) {
        List<Order> orders = getAllOrders();
        boolean found = false;

        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                o.setStatus(newStatus);
                found = true;
                break;
            }
        }

        if (!found) return false;
        return writeAllOrders(orders);
    }


    // DELETE / CANCEL — Cancel an order
    // Only PENDING orders can be cancelled by the user

    public String cancelOrder(String orderId, String userId) {
        Order order = findById(orderId);
        if (order == null)                          return "NOT_FOUND";
        if (!order.getUserId().equals(userId))      return "UNAUTHORIZED";
        if (!order.isPending())                     return "CANNOT_CANCEL";

        // Restore stock when order is cancelled
        productService.restock(order.getProductId(), order.getQuantity());

        // Mark as CANCELLED (keep record — don't delete)
        return updateStatus(orderId, "CANCELLED") ? "SUCCESS" : "FAILED";
    }

    // Admin can hard-delete an order record
    public boolean deleteOrder(String orderId) {
        List<Order> orders = getAllOrders();
        boolean removed = orders.removeIf(o -> o.getId().equals(orderId));
        if (!removed) return false;
        return writeAllOrders(orders);
    }


    // HELPER — Rewrite the entire file

    private boolean writeAllOrders(List<Order> orders) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, false))) {
            for (Order o : orders) {
                writer.write(o.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // HELPER — Generate a unique order ID
    private String generateId() {
        return "ORD" + System.currentTimeMillis();
    }
}