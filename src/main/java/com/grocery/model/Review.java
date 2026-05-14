package com.grocery.model;

// OOP: Abstraction — Review is a base class
// PublicReview and VerifiedReview extend it with different display behaviour
public abstract class Review {

    private String id;
    private String userId;
    private String username;
    private String productId;
    private String productName;
    private int    rating;      // 1 to 5
    private String comment;
    private String reviewDate;
    private String status;      // PENDING, APPROVED, REJECTED
    private String type;        // PUBLIC or VERIFIED

    public Review() {}

    public Review(String id, String userId, String username,
                  String productId, String productName,
                  int rating, String comment,
                  String reviewDate, String status, String type) {
        this.id          = id;
        this.userId      = userId;
        this.username    = username;
        this.productId   = productId;
        this.productName = productName;
        this.rating      = rating;
        this.comment     = comment;
        this.reviewDate  = reviewDate;
        this.status      = status;
        this.type        = type;
    }

    // Convert a comma-separated line from reviews.txt into the right subclass
    public static Review fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 10) return null;
        try {
            String type = parts[9].trim();
            if ("VERIFIED".equals(type)) {
                return new VerifiedReview(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3].trim(), parts[4].trim(),
                        Integer.parseInt(parts[5].trim()),
                        parts[6].trim(), parts[7].trim(), parts[8].trim()
                );
            } else {
                return new PublicReview(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(),
                        parts[3].trim(), parts[4].trim(),
                        Integer.parseInt(parts[5].trim()),
                        parts[6].trim(), parts[7].trim(), parts[8].trim()
                );
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert this Review into a line for reviews.txt
    public String toLine() {
        return id + "," + userId + "," + username + "," +
                productId + "," + productName + "," +
                rating + "," + comment + "," +
                reviewDate + "," + status + "," + type;
    }

    // OOP: Polymorphism — each subclass displays differently
    public abstract String getDisplayLabel();
    public abstract String getBadgeStyle();

    // Convenience booleans — used in templates
    public boolean isPending()  { return "PENDING".equals(status); }
    public boolean isApproved() { return "APPROVED".equals(status); }
    public boolean isRejected() { return "REJECTED".equals(status); }

    // Generate star string for display e.g. rating=4 → "★★★★☆"
    public String getStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= rating ? "★" : "☆");
        }
        return stars.toString();
    }

    // OOP: Encapsulation
    public String getId()                { return id; }
    public void   setId(String id)       { this.id = id; }

    public String getUserId()                  { return userId; }
    public void   setUserId(String userId)     { this.userId = userId; }

    public String getUsername()                    { return username; }
    public void   setUsername(String username)     { this.username = username; }

    public String getProductId()                     { return productId; }
    public void   setProductId(String productId)     { this.productId = productId; }

    public String getProductName()                       { return productName; }
    public void   setProductName(String productName)     { this.productName = productName; }

    public int  getRating()              { return rating; }
    public void setRating(int rating)    { this.rating = rating; }

    public String getComment()                   { return comment; }
    public void   setComment(String comment)     { this.comment = comment; }

    public String getReviewDate()                      { return reviewDate; }
    public void   setReviewDate(String reviewDate)     { this.reviewDate = reviewDate; }

    public String getStatus()                  { return status; }
    public void   setStatus(String status)     { this.status = status; }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    @Override
    public String toString() {
        return "Review{id='" + id + "', user='" + username +
                "', product='" + productName + "', rating=" + rating +
                ", status='" + status + "', type='" + type + "'}";
    }
}