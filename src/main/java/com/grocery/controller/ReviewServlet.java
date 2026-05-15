package com.grocery.controller;

import com.grocery.model.Review;
import com.grocery.model.User;
import com.grocery.service.ProductService;
import com.grocery.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewServlet {

    private final ReviewService  reviewService  = new ReviewService();
    private final ProductService productService = new ProductService();

    // Public: view approved reviews for a product
    @GetMapping("/view")
    public String viewReviews(@RequestParam String productId, Model model) {
        model.addAttribute("product",  productService.findById(productId));
        model.addAttribute("reviews",  reviewService.getApprovedReviewsForProduct(productId));
        model.addAttribute("avgRating",reviewService.getAverageRating(productId));
        return "review/view-reviews";
    }

    //  Show submit-review form
    @GetMapping("/submit")
    public String showSubmitForm(@RequestParam String productId,
                                 HttpSession session,
                                 Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        User user = (User) session.getAttribute("loggedInUser");

        // Guard: already reviewed
        if (reviewService.hasAlreadyReviewed(user.getId(), productId)) {
            return "redirect:/review/view?productId=" + productId + "&alreadyReviewed=true";
        }

        model.addAttribute("product", productService.findById(productId));
        return "review/submit-review";
    }

    // Handle review form submit
    @PostMapping("/submit")
    public String submitReview(@RequestParam String productId,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               HttpSession session,
                               Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";

        User user = (User) session.getAttribute("loggedInUser");
        String result = reviewService.submitReview(
                user.getId(), user.getUsername(), productId, rating, comment);

        switch (result) {
            case "SUCCESS":
                return "redirect:/review/view?productId=" + productId + "&submitted=true";
            case "ALREADY_REVIEWED":
                return "redirect:/review/view?productId=" + productId + "&alreadyReviewed=true";
            case "PRODUCT_NOT_FOUND":
                model.addAttribute("error", "Product not found.");
                break;
            default:
                model.addAttribute("error", "Could not save review. Please try again.");
        }

        model.addAttribute("product", productService.findById(productId));
        return "review/submit-review";
    }

    // User: view and manage their own reviews
    @GetMapping("/my-reviews")
    public String myReviews(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/user/login";
        User user = (User) session.getAttribute("loggedInUser");
        model.addAttribute("reviews", reviewService.getReviewsByUser(user.getId()));
        return "review/my-reviews";
    }

    // User: delete their own review
    @PostMapping("/delete")
    public String deleteReview(@RequestParam String id,
                               HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/user/login";
        User user  = (User) session.getAttribute("loggedInUser");
        boolean admin = isAdmin(session);
        reviewService.deleteReview(id, user.getId(), admin);

        return admin
                ? "redirect:/review/moderate?deleted=true"
                : "redirect:/review/my-reviews?deleted=true";
    }

    // Admin: moderation panel
    @GetMapping("/moderate")
    public String moderationPanel(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";
        model.addAttribute("pendingReviews", reviewService.getPendingReviews());
        model.addAttribute("allReviews",     reviewService.getAllReviews());
        return "review/moderation";
    }

    // Admin: approve or reject a review
    @PostMapping("/moderate")
    public String moderateReview(@RequestParam String id,
                                 @RequestParam String status,
                                 HttpSession session) {
        if (!isAdmin(session)) return "redirect:/user/login";
        reviewService.moderateReview(id, status);
        return "redirect:/review/moderate?moderated=true";
    }

    // Admin: view all reviews
    @GetMapping("/list")
    public String listAllReviews(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/user/login";
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "review/view-reviews";
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