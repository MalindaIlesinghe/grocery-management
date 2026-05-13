package com.grocery.groceryordersystem.controller;

import com.grocery.groceryordersystem.model.Category;
import com.grocery.groceryordersystem.model.User;
import com.grocery.groceryordersystem.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/category")
public class CategoryServlet {

    private final CategoryService categoryService = new CategoryService();

    // Public: browse all active categories
    @GetMapping("/list")
    public String listCategories(Model model) {
        Map<String, Long> productCounts = categoryService.getProductCountPerCategory();
        model.addAttribute("categories",    categoryService.getAllCategories());
        model.addAttribute("productCounts", productCounts);
        return "category/category-list";
    }

    // Public: view products inside a category
    @GetMapping("/view")
    public String viewCategory(@RequestParam String id, Model model) {
        Category category = categoryService.findById(id);
        if (category == null) return "redirect:/category/list";

        model.addAttribute("category", category);
        model.addAttribute("products", categoryService.getProductsInCategory(category.getName()));
        return "category/category-view";
    }

    //Admin: show add-category form
    @GetMapping("/add")
    public String showAddForm(HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        return "category/add-category";
    }

    //Admin: handle add-category submit
    @PostMapping("/add")
    public String addCategory(@RequestParam String name,
                              @RequestParam String description,
                              @RequestParam(defaultValue = "") String imageUrl,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);

        boolean success = categoryService.addCategory(category);
        if (success) {
            return "redirect:/category/list?added=true";
        }
        model.addAttribute("error", "A category with this name already exists.");
        return "category/add-category";
    }

    // Admin: show edit form
    @GetMapping("/edit")
    public String showEditForm(@RequestParam String id,
                               HttpSession session,
                               Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Category category = categoryService.findById(id);
        if (category == null) return "redirect:/category/list";

        model.addAttribute("category", category);
        return "category/edit-category";
    }

    // Admin: handle edit submit
    @PostMapping("/edit")
    public String editCategory(@RequestParam String id,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam(defaultValue = "") String imageUrl,
                               HttpSession session,
                               Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        Category category = categoryService.findById(id);
        if (category == null) return "redirect:/category/list";

        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);

        boolean success = categoryService.update(category);
        if (success) {
            return "redirect:/category/list?updated=true";
        }
        model.addAttribute("error", "Update failed. Please try again.");
        model.addAttribute("category", category);
        return "category/edit-category";
    }

    // Admin: toggle active/inactive
    @PostMapping("/toggle")
    public String toggleActive(@RequestParam String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        categoryService.toggleActive(id);
        return "redirect:/category/list?toggled=true";
    }

    //Admin: delete category
    @PostMapping("/delete")
    public String deleteCategory(@RequestParam String id,
                                 HttpSession session,
                                 Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";

        String result = categoryService.delete(id);
        switch (result) {
            case "SUCCESS":     return "redirect:/category/list?deleted=true";
            case "HAS_PRODUCTS":return "redirect:/category/list?hasProducts=true";
            default:            return "redirect:/category/list?error=true";
        }
    }

    // Helper
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}