package com.grocery.controller;

import com.grocery.model.Order;
import com.grocery.model.Product;
import com.grocery.model.User;
import com.grocery.service.OrderService;
import com.grocery.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderServlet {

    private final OrderService   orderService   = new OrderService();
    private final ProductService productService = new ProductService();

    // Show place-order form
    @GetMapping("/place")
    public String showPlaceOrderForm(@RequestParam(required = false) String productId,
                                     HttpSession session,
                                     Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        // If a productId was passed (e.g. from product list), pre-select it
        if (productId != null) {
            Product product = productService.findById(productId);
            model.addAttribute("selectedProduct", product);
        }

        model.addAttribute("products", productService.getAvailableProducts());
        return "order/place-order";
    }

    // Handle place-order form submit
    @PostMapping("/place")
    public String placeOrder(@RequestParam String productId,
                             @RequestParam int quantity,
                             HttpSession session,
                             Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        User user = (User) session.getAttribute("loggedInUser");
        String result = orderService.placeOrder(user.getId(), productId, quantity);

        switch (result) {
            case "SUCCESS":
                return "redirect:/order/list?placed=true";
            case "INSUFFICIENT_STOCK":
                model.addAttribute("error", "Not enough stock available.");
                break;
            case "PRODUCT_UNAVAILABLE":
                model.addAttribute("error", "This product is currently unavailable.");
                break;
            case "PRODUCT_NOT_FOUND":
                model.addAttribute("error", "Product not found.");
                break;
            default:
                model.addAttribute("error", "Something went wrong. Please try again.");
        }

        model.addAttribute("products", productService.getAvailableProducts());
        return "order/place-order";
    }

    // User: view their own orders
    @GetMapping("/list")
    public String listOrders(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        User user = (User) session.getAttribute("loggedInUser");

        // Admins see all orders; regular users see only their own
        List<Order> orders = isAdmin(session)
                ? orderService.getAllOrders()
                : orderService.getOrdersByUser(user.getId());

        model.addAttribute("orders", orders);
        return "order/order-list";
    }

    // View single order detail
    @GetMapping("/detail")
    public String orderDetail(@RequestParam String id,
                              HttpSession session,
                              Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        Order order = orderService.findById(id);
        if (order == null) return "redirect:/order/list";

        // Regular users can only view their own orders
        User user = (User) session.getAttribute("loggedInUser");
        if (!isAdmin(session) && !order.getUserId().equals(user.getId())) {
            return "redirect:/order/list";
        }

        model.addAttribute("order", order);
        return "order/order-detail";
    }

    // User: cancel a pending order
    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam String id, HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        User user = (User) session.getAttribute("loggedInUser");
        String result = orderService.cancelOrder(id, user.getId());

        switch (result) {
            case "SUCCESS":      return "redirect:/order/list?cancelled=true";
            case "CANNOT_CANCEL":return "redirect:/order/detail?id=" + id + "&error=cannotCancel";
            case "UNAUTHORIZED": return "redirect:/order/list";
            default:             return "redirect:/order/list?error=true";
        }
    }

    // Admin: update order status
    @PostMapping("/status")
    public String updateStatus(@RequestParam String id,
                               @RequestParam String status,
                               HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        orderService.updateStatus(id, status);
        return "redirect:/order/list?updated=true";
    }

    // Admin: hard-delete an order
    @PostMapping("/delete")
    public String deleteOrder(@RequestParam String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        orderService.deleteOrder(id);
        return "redirect:/order/list?deleted=true";
    }

    // Helpers
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}