package com.grocery.controller;

import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.service.CategoryService;
import com.grocery.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductServlet {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    // Public: browse all available products
    @GetMapping("/list")
    public String listProducts(@RequestParam(required = false) String category,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        List<Product> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.search(keyword);
            model.addAttribute("keyword", keyword);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAvailableProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        return "product/product-list";
    }

    //Public: search products
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String keyword,
                                 Model model) {
        List<Product> results = productService.search(keyword);
        model.addAttribute("products", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", productService.getAllCategories());
        return "product/search-product";
    }

    // Admin: show add-product form
    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";
        model.addAttribute("categories", categoryService.getActiveCategories()
                .stream()
                .map(c -> c.getName())
                .collect(java.util.stream.Collectors.toList()));
        return "product/add-product";
    }

    // Admin: handle add-product form submit
    @PostMapping("/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam int stock,
                             @RequestParam String category,
                             @RequestParam(defaultValue = "") String imageUrl,
                             HttpSession session,
                             Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);

        boolean success = productService.addProduct(product);
        if (success) {
            return "redirect:/product/list?added=true";
        } else {
            model.addAttribute("error", "A product with this name already exists.");
            model.addAttribute("categories", productService.getAllCategories());
            return "product/add-product";
        }
    }

    // Admin: show edit form
    @GetMapping("/edit")
    public String showEditForm(@RequestParam String id,
                               HttpSession session,
                               Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Product product = productService.findById(id);
        if (product == null) return "redirect:/product/list";

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getActiveCategories()
                .stream()
                .map(c -> c.getName())
                .collect(java.util.stream.Collectors.toList()));
        return "product/edit-product";
    }

    // Admin: handle edit form submit
    @PostMapping("/edit")
    public String editProduct(@RequestParam String id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam double price,
                              @RequestParam int stock,
                              @RequestParam String category,
                              @RequestParam(defaultValue = "") String imageUrl,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Product product = productService.findById(id);
        if (product == null) return "redirect:/product/list";

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setAvailable(stock > 0);

        boolean success = productService.update(product);
        if (success) {
            return "redirect:/product/list?updated=true";
        } else {
            model.addAttribute("error", "Update failed. Please try again.");
            model.addAttribute("product", product);
            model.addAttribute("categories", productService.getAllCategories());
            return "product/edit-product";
        }
    }

    // Admin: delete product
    @PostMapping("/delete")
    public String deleteProduct(@RequestParam String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        productService.delete(id);
        return "redirect:/product/list?deleted=true";
    }

    //  Helper: check if logged-in user is ADMIN
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}