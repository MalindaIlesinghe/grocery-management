package com.grocery.service;

import com.grocery.model.Order;
import com.grocery.model.Product;
import com.grocery.model.PublicReview;
import com.grocery.model.Review;
import com.grocery.model.VerifiedReview;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReviewService {

    private static final String FILE_PATH = "src/main/resources/data/reviews.txt";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // OOP: Composition — uses OrderService to check purchase history
    private final OrderService   orderService   = new OrderService();
    private final ProductService productService = new ProductService();


    // CREATE — Submit a new review
    // Automatically determines PUBLIC vs VERIFIED

    public String submitReview(String userId, String username,
                               String productId, int rating, String comment) {
        // One review per user per product
        if (hasAlreadyReviewed(userId, productId)) return "ALREADY_REVIEWED";

        Product product = productService.findById(productId);
        if (product == null) return "PRODUCT_NOT_FOUND";

        // Check if the user has a delivered order for this product
        boolean isVerified = orderService.getOrdersByUser(userId).stream()
                .anyMatch(o -> o.getProductId().equals(productId)
                        && o.isDelivered());

        Review review;
        if (isVerified) {
            // OOP: Polymorphism — create VerifiedReview at runtime
            review = new VerifiedReview();
        } else {
            // OOP: Polymorphism — create PublicReview at runtime
            review = new PublicReview();
        }

        review.setId(generateId());
        review.setUserId(userId);
        review.setUsername(username);
        review.setProductId(productId);
        review.setProductName(product.getName());
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDateTime.now().format(FORMATTER));

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {
            writer.write(review.toLine());
            writer.newLine();
            return "SUCCESS";
        } catch (IOException e) {
            e.printStackTrace();
            return "SAVE_FAILED";
        }
    }


    // READ — Get all reviews

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return reviews;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Review r = Review.fromLine(line);
                    if (r != null) reviews.add(r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    // READ — Get approved reviews for a specific product
    public List<Review> getApprovedReviewsForProduct(String productId) {
        return getAllReviews().stream()
                .filter(r -> r.getProductId().equals(productId)
                        && r.isApproved())
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .collect(Collectors.toList());
    }

    // READ — Get all reviews by a specific user
    public List<Review> getReviewsByUser(String userId) {
        return getAllReviews().stream()
                .filter(r -> r.getUserId().equals(userId))
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .collect(Collectors.toList());
    }

    // READ — Find review by ID
    public Review findById(String id) {
        return getAllReviews().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Get pending reviews for admin moderation
    public List<Review> getPendingReviews() {
        return getAllReviews().stream()
                .filter(Review::isPending)
                .collect(Collectors.toList());
    }

    // READ — Calculate average rating for a product
    public double getAverageRating(String productId) {
        List<Review> approved = getApprovedReviewsForProduct(productId);
        if (approved.isEmpty()) return 0.0;
        return approved.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // READ — Check if user has already reviewed this product
    public boolean hasAlreadyReviewed(String userId, String productId) {
        return getAllReviews().stream()
                .anyMatch(r -> r.getUserId().equals(userId)
                        && r.getProductId().equals(productId));
    }


    // UPDATE — Edit a review comment and rating
    // Users can only edit their own PENDING reviews

    public String updateReview(String reviewId, String userId,
                               int rating, String comment) {
        Review review = findById(reviewId);
        if (review == null)                         return "NOT_FOUND";
        if (!review.getUserId().equals(userId))     return "UNAUTHORIZED";
        if (!review.isPending())                    return "CANNOT_EDIT";

        review.setRating(rating);
        review.setComment(comment);

        List<Review> reviews = getAllReviews();
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getId().equals(reviewId)) {
                reviews.set(i, review);
                break;
            }
        }
        return writeAllReviews(reviews) ? "SUCCESS" : "FAILED";
    }


    // UPDATE — Admin moderation: approve or reject

    public boolean moderateReview(String reviewId, String newStatus) {
        List<Review> reviews = getAllReviews();
        boolean found = false;

        for (Review r : reviews) {
            if (r.getId().equals(reviewId)) {
                r.setStatus(newStatus);
                found = true;
                break;
            }
        }

        if (!found) return false;
        return writeAllReviews(reviews);
    }


    // DELETE — Remove a review
    // Users delete their own; admins delete any

    public String deleteReview(String reviewId, String userId, boolean isAdmin) {
        Review review = findById(reviewId);
        if (review == null) return "NOT_FOUND";
        if (!isAdmin && !review.getUserId().equals(userId)) return "UNAUTHORIZED";

        List<Review> reviews = getAllReviews();
        reviews.removeIf(r -> r.getId().equals(reviewId));
        return writeAllReviews(reviews) ? "SUCCESS" : "FAILED";
    }


    // HELPER — Rewrite the entire file

    private boolean writeAllReviews(List<Review> reviews) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, false))) {
            for (Review r : reviews) {
                writer.write(r.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateId() {
        return "REV" + System.currentTimeMillis();
    }
}