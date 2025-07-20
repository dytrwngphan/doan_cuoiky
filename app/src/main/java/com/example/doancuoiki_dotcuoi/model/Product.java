package com.example.doancuoiki_dotcuoi.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String postId;
    private String title;
    private String description;
    private double price;
    private String category;
    private String condition;
    private String location;
    private List<String> imageUrls;
    private String status;
    private String ownerId;
    private long createdAt;
    private long updatedAt;
    private double latitude;
    private double longitude;
    private String addressText;

    public Product() {}

    // Getter & Setter cho tất cả trường
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
}
