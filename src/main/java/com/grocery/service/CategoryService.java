package com.grocery .service;

import com.grocery .model.Category;
import com.grocery.model.Product;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CategoryService {

    private static final String FILE_PATH = "src/main/resources/data/categories.txt";

    // ProductService used to count products per category
    // OOP: Composition — CategoryService uses ProductService
    private final ProductService productService = new ProductService();

    // ──────────────────────────────────────────
    // CREATE — Add a new category
    // ──────────────────────────────────────────
    public boolean addCategory(Category category) {
        // Prevent duplicate category names
        if (findByName(category.getName()) != null) return false;

        category.setId(generateId());
        category.setActive(true);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            writer.write(category.toLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────
    // READ — Get all categories
    // ──────────────────────────────────────────
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return categories;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Category c = Category.fromLine(line);
                    if (c != null) categories.add(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // READ — Get only active categories (used in product forms and shop)
    public List<Category> getActiveCategories() {
        return getAllCategories().stream()
                .filter(Category::isActive)
                .collect(Collectors.toList());
    }

    // READ — Find category by ID
    public Category findById(String id) {
        return getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Find category by name (case-insensitive)
    public Category findByName(String name) {
        return getAllCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // READ — Get product count for each category (used on category list page)
    // OOP: Abstraction — hides the cross-service lookup behind a clean method
    public Map<String, Long> getProductCountPerCategory() {
        List<Product> products = productService.getAllProducts();
        return products.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.counting()
                ));
    }

    // READ — Get products belonging to a specific category
    public List<Product> getProductsInCategory(String categoryName) {
        return productService.getByCategory(categoryName);
    }

    // ──────────────────────────────────────────
    // UPDATE — Edit an existing category
    // ──────────────────────────────────────────
    public boolean update(Category updatedCategory) {
        List<Category> categories = getAllCategories();
        boolean found = false;

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(updatedCategory.getId())) {
                categories.set(i, updatedCategory);
                found = true;
                break;
            }
        }

        if (!found) return false;
        return writeAllCategories(categories);
    }

    // UPDATE — Toggle active/inactive
    public boolean toggleActive(String categoryId) {
        Category category = findById(categoryId);
        if (category == null) return false;
        category.setActive(!category.isActive());
        return update(category);
    }

    // ──────────────────────────────────────────
    // DELETE — Remove a category by ID
    // Guard: cannot delete if products still use it
    // ──────────────────────────────────────────
    public String delete(String id) {
        Category category = findById(id);
        if (category == null) return "NOT_FOUND";

        // Check if any products still belong to this category
        List<Product> linked = getProductsInCategory(category.getName());
        if (!linked.isEmpty()) return "HAS_PRODUCTS";

        List<Category> categories = getAllCategories();
        categories.removeIf(c -> c.getId().equals(id));
        return writeAllCategories(categories) ? "SUCCESS" : "FAILED";
    }

    // ──────────────────────────────────────────
    // HELPER — Rewrite the entire file
    // ──────────────────────────────────────────
    private boolean writeAllCategories(List<Category> categories) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, false))) {
            for (Category c : categories) {
                writer.write(c.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateId() {
        return "CAT" + System.currentTimeMillis();
    }
}