package com.grocery.controller;

import java.io.BufferedReader;
import java.io.IOException;

public class ProductServlet {

import com.grocery.model.Product;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

    public class ProductService {

        private static final String FILE_PATH = "src/main/resources/data/products.txt";


        // CREATE — Add a new product

        public boolean addProduct(Product product) {
            // Prevent duplicate product names
            if (findByName(product.getName()) != null) return false;

            product.setId(generateId());
            product.setAvailable(true);

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(FILE_PATH, true))) {
                writer.write(product.toLine());
                writer.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }


        // READ — Get all products

        public List<Product> getAllProducts() {
            List<Product> products = new ArrayList<>();
            File file = new File(FILE_PATH);
            if (!file.exists()) return products;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        Product p = Product.fromLine(line);
                        if (p != null) products.add(p);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return products;
        }

        // READ — Find product by ID
        public Product findById(String id) {
            return getAllProducts().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        // READ — Find product by exact name
        public Product findByName(String name) {
            return getAllProducts().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }

        // READ — Search products by keyword (name or description)
        public List<Product> search(String keyword) {
            if (keyword == null || keyword.trim().isEmpty()) return getAllProducts();
            String lower = keyword.toLowerCase();
            return getAllProducts().stream()
                    .filter(p -> p.getName().toLowerCase().contains(lower) ||
                            p.getDescription().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        // READ — Get products filtered by category
        public List<Product> getByCategory(String category) {
            if (category == null || category.trim().isEmpty()) return getAllProducts();
            return getAllProducts().stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        // READ — Get only available (in-stock) products
        public List<Product> getAvailableProducts() {
            return getAllProducts().stream()
                    .filter(p -> p.isAvailable() && p.getStock() > 0)
                    .collect(Collectors.toList());
        }

        // READ — Get all distinct category names (used to populate dropdowns)
        public List<String> getAllCategories() {
            return getAllProducts().stream()
                    .map(Product::getCategory)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ──────────────────────────────────────────
        // UPDATE — Edit an existing product
        // ──────────────────────────────────────────
        public boolean update(Product updatedProduct) {
            List<Product> products = getAllProducts();
            boolean found = false;

            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(updatedProduct.getId())) {
                    products.set(i, updatedProduct);
                    found = true;
                    break;
                }
            }

            if (!found) return false;
            return writeAllProducts(products);
        }

        // UPDATE — Reduce stock when an order is placed
        public boolean reduceStock(String productId, int quantity) {
            Product product = findById(productId);
            if (product == null) return false;
            if (product.getStock() < quantity) return false; // not enough stock

            product.setStock(product.getStock() - quantity);
            if (product.getStock() == 0) {
                product.setAvailable(false); // auto-mark unavailable when stock hits 0
            }
            return update(product);
        }

        // UPDATE — Restock a product
        public boolean restock(String productId, int quantity) {
            Product product = findById(productId);
            if (product == null) return false;

            product.setStock(product.getStock() + quantity);
            product.setAvailable(true); // back in stock
            return update(product);
        }


        // DELETE — Remove a product by ID

        public boolean delete(String id) {
            List<Product> products = getAllProducts();
            boolean removed = products.removeIf(p -> p.getId().equals(id));
            if (!removed) return false;
            return writeAllProducts(products);
        }


        // HELPER — Rewrite the entire file

        private boolean writeAllProducts(List<Product> products) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(FILE_PATH, false))) {
                for (Product p : products) {
                    writer.write(p.toLine());
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // HELPER — Generate a unique product ID
        private String generateId() {
            return "P" + System.currentTimeMillis();
        }
    }

}
