package com.grocery.groceryordersystem.model;

public class Category {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;

    public Category() {}

    public Category(String id, String name, String description,
                    String imageUrl, boolean active) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.imageUrl    = imageUrl;
        this.active      = active;
    }

    // Convert a comma-separated line from categories.txt into a Category object
    public static Category fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;
        try {
            return new Category(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    Boolean.parseBoolean(parts[4].trim())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert this Category object into a line for categories.txt
    public String toLine() {
        return id + "," + name + "," + description + "," + imageUrl + "," + active;
    }

    // OOP: Encapsulation — all fields private, accessed via getters/setters
    public String getId()                { return id; }
    public void   setId(String id)       { this.id = id; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getImageUrl()                  { return imageUrl; }
    public void   setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }

    public boolean isActive()                { return active; }
    public void    setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Category{id='" + id + "', name='" + name +
                "', active=" + active + "}";
    }
}