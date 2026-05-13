package com.grocery.model;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private String imageUrl;
    private boolean available;

    public Product() {}

    public Product(String id, String name, String description, double price,
                   int stock, String category, String imageUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
    }

    // Convert a comma-separated line from products.txt into a Product object
    public static Product fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 8) return null;
        try {
            return new Product(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    Double.parseDouble(parts[3].trim()),
                    Integer.parseInt(parts[4].trim()),
                    parts[5].trim(),
                    parts[6].trim(),
                    Boolean.parseBoolean(parts[7].trim())
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert this Product object into a line for products.txt
    public String toLine() {
        return id + "," + name + "," + description + "," +
                price + "," + stock + "," + category + "," +
                imageUrl + "," + available;
    }

    // OOP: Encapsulation â all fields private, accessed via getters/setters
    public String getId()                { return id; }
    public void   setId(String id)       { this.id = id; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public double getPrice()               { return price; }
    public void   setPrice(double price)   { this.price = price; }

    public int  getStock()             { return stock; }
    public void setStock(int stock)    { this.stock = stock; }

    public String getCategory()                  { return category; }
    public void   setCategory(String category)   { this.category = category; }

    public String getImageUrl()                  { return imageUrl; }
    public void   setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    public boolean isAvailable()                   { return available; }
    public void    setAvailable(boolean available) { this.available = available; }

    // Convenience method â used in templates and order logic
    public boolean isInStock() {
        return stock > 0;
    }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name +
                "', price=" + price + ", stock=" + stock +
                ", category='" + category + "'}";
    }
}





