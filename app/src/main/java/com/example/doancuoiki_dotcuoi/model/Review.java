package com.example.doancuoiki_dotcuoi.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Review implements Serializable {
    private String reviewId;
    private String userId;
    private String productId;
    private String content;
    private int rating;
    private Timestamp createdAt;

    public Review() {}

    // Getter & Setter
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
