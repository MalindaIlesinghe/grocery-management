package com.grocery.model;

// OOP: Inheritance — VerifiedReview IS-A Review
// Only users who actually ordered the product can write a verified review
public class VerifiedReview extends Review {

    public VerifiedReview() {
        super();
        setType("VERIFIED");
        setStatus("APPROVED"); // verified reviews auto-approved
    }

    public VerifiedReview(String id, String userId, String username,
                          String productId, String productName,
                          int rating, String comment,
                          String reviewDate, String status) {
        super(id, userId, username, productId, productName,
                rating, comment, reviewDate, status, "VERIFIED");
    }

    // OOP: Polymorphism — overrides abstract method from Review
    @Override
    public String getDisplayLabel() {
        return "Verified Purchase";
    }

    // OOP: Polymorphism — different badge style from public review
    @Override
    public String getBadgeStyle() {
        return "bg-success";
    }
}