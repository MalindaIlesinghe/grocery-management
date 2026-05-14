package com.grocery.model;

// OOP: Inheritance — PublicReview IS-A Review
// Anyone can write a public review; goes through moderation
public class PublicReview extends Review {

    public PublicReview() {
        super();
        setType("PUBLIC");
        setStatus("PENDING"); // public reviews need approval
    }

    public PublicReview(String id, String userId, String username,
                        String productId, String productName,
                        int rating, String comment,
                        String reviewDate, String status) {
        super(id, userId, username, productId, productName,
                rating, comment, reviewDate, status, "PUBLIC");
    }

    // OOP: Polymorphism — overrides abstract method from Review
    @Override
    public String getDisplayLabel() {
        return "Public Review";
    }

    // OOP: Polymorphism — different badge for public vs verified
    @Override
    public String getBadgeStyle() {
        return "bg-secondary";
    }
}