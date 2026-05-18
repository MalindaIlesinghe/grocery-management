package com.grocery.controller;

import com.grocery.model.Admin;
import com.grocery.model.User;
import com.grocery.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminServlet {

    private final AdminService adminService = new AdminService();

    // Show admin login form
    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/admin-login";
    }

    // Handle admin login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            session.setAttribute("loggedInUser", admin);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid credentials or account is inactive.");
        return "admin/admin-login";
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";

        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);

        return "admin/dashboard";
    }

    // Show register new admin form
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/dashboard";
        return "admin/admin-register";
    }

    // Handle register new admin
    @PostMapping("/register")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String adminLevel,
                                HttpSession session,
                                Model model) {
        if (!isSuperAdmin(session)) return "redirect:/admin/dashboard";

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setPhone(phone);
        admin.setAddress(address);
        admin.setAdminLevel(adminLevel);

        boolean success = adminService.register(admin);
        if (success) {
            return "redirect:/admin/list?added=true";
        }
        model.addAttribute("error", "Username or email already exists.");
        return "admin/admin-register";
    }

    // List all admins
    @GetMapping("/list")
    public String listAdmins(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/admin/login";
        model.addAttribute("admins", adminService.getAllAdmins());
        return "admin/admin-list";
    }

    // Toggle admin active/inactive
    @PostMapping("/toggle")
    public String toggleActive(@RequestParam String id, HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/dashboard";
        adminService.toggleActive(id);
        return "redirect:/admin/list?toggled=true";
    }

    //  Change admin level
    @PostMapping("/level")
    public String changeLevel(@RequestParam String id,
                              @RequestParam String adminLevel,
                              HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/dashboard";
        adminService.changeLevel(id, adminLevel);
        return "redirect:/admin/list?updated=true";
    }

    // Delete admin
    @PostMapping("/delete")
    public String deleteAdmin(@RequestParam String id, HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/dashboard";

        // Prevent self-deletion
        Admin current = (Admin) session.getAttribute("loggedInUser");
        if (current.getId().equals(id)) {
            return "redirect:/admin/list?selfDelete=true";
        }

        adminService.delete(id);
        return "redirect:/admin/list?deleted=true";
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // Helpers
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    private boolean isSuperAdmin(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        if (!(obj instanceof Admin)) return false;
        Admin admin = (Admin) obj;
        return admin.isActive() && admin.canManageAdmins();
    }
}