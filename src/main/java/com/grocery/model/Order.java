package com.grocery.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {

    private String id;
    private String userId;
    private String productId;
    private String productName;
    private int quantity;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, DELIVERED, CANCELLED
    private String orderDate;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Order() {}

    public Order(String id, String userId, String productId, String productName,
                 int quantity, double totalPrice, String status, String orderDate) {
        this.id          = id;
        this.userId      = userId;
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.totalPrice  = totalPrice;
        this.status      = status;
        this.orderDate   = orderDate;
    }

    // Convert a comma-separated line from orders.txt into an Order object
    public static Order fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 8) return null;
        try {
            return new Order(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Integer.parseInt(parts[4].trim()),
                    Double.parseDouble(parts[5].trim()),
                    parts[6].trim(),
                    parts[7].trim()
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert this Order object into a line for orders.txt
    public String toLine() {
        return id + "," + userId + "," + productId + "," + productName + "," +
                quantity + "," + totalPrice + "," + status + "," + orderDate;
    }

    // Generate a formatted timestamp for new orders
    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    // OOP: Encapsulation — all fields private, accessed via getters/setters
    public String getId()                { return id; }
    public void   setId(String id)       { this.id = id; }

    public String getUserId()                  { return userId; }
    public void   setUserId(String userId)     { this.userId = userId; }

    public String getProductId()                     { return productId; }
    public void   setProductId(String productId)     { this.productId = productId; }

    public String getProductName()                       { return productName; }
    public void   setProductName(String productName)     { this.productName = productName; }

    public int  getQuantity()                { return quantity; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }

    public double getTotalPrice()                    { return totalPrice; }
    public void   setTotalPrice(double totalPrice)   { this.totalPrice = totalPrice; }

    public String getStatus()                  { return status; }
    public void   setStatus(String status)     { this.status = status; }

    public String getOrderDate()                     { return orderDate; }
    public void   setOrderDate(String orderDate)     { this.orderDate = orderDate; }

    // Convenience — used in templates to colour-code status badges
    public boolean isPending()    { return "PENDING".equals(status); }
    public boolean isConfirmed()  { return "CONFIRMED".equals(status); }
    public boolean isDelivered()  { return "DELIVERED".equals(status); }
    public boolean isCancelled()  { return "CANCELLED".equals(status); }

    @Override
    public String toString() {
        return "Order{id='" + id + "', userId='" + userId +
                "', product='" + productName + "', status='" + status + "'}";
    }
}
