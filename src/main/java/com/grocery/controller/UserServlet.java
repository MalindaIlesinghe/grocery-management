package com.grocery.controller;

import com.grocery.model.User;
import com.grocery.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserServlet {

    private final UserService userService = new UserService();

    @GetMapping("/register")
    public String showRegisterForm() {
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String phone,
                               @RequestParam String address,
                               Model model) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setPhone(phone);
        user.setAddress(address);

        boolean success = userService.register(user);
        if (success) {
            return "redirect:/user/login?registered=true";
        } else {
            model.addAttribute("error", "Username or email already exists.");
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/user/profile";
        } else {
            model.addAttribute("error", "Invalid username or password.");
            return "user/login";
        }
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateUser(@RequestParam String id,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             HttpSession session,
                             Model model) {
        User existing = userService.findById(id);
        if (existing == null) return "redirect:/user/login";

        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setAddress(address);

        boolean success = userService.update(existing);
        if (success) {
            session.setAttribute("loggedInUser", existing);
            return "redirect:/user/profile?updated=true";
        } else {
            model.addAttribute("error", "Update failed. Please try again.");
            model.addAttribute("user", existing);
            return "user/profile";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String id, HttpSession session) {
        userService.delete(id);
        session.invalidate();
        return "redirect:/user/login?deleted=true";
    }

    @GetMapping("/list")
    public String listAllUsers(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        if (loggedIn == null || !loggedIn.getRole().equals("ADMIN")) {
            return "redirect:/user/login";
        }
        model.addAttribute("users", userService.getAllUsers());
        return "user/user-list";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}