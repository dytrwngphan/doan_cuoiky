package com.example.doancuoiki_dotcuoi.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String cartItemId;     // ID của document trên Firestore
    private String userId;         // Quan trọng: để filter cart của từng user
    private String productId;
    private String offerId;        // Nếu có, sẽ là id của offer đã được đồng ý
    private String title;
    private String imageUrl;
    private double price;          // Giá thực tế sẽ thanh toán (offer hoặc gốc)
    private String status;         // pending, paid, delivered, canceled, v.v.

    public CartItem() {}

    public CartItem(String cartItemId, String userId, String productId, String offerId, String title, String imageUrl, double price, String status) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.offerId = offerId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.price = price;
        this.status = status;
    }

    // Getter & Setter
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
