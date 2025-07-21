package com.example.doancuoiki_dotcuoi.model;

import java.io.Serializable;

public class Offer implements Serializable {
    private String offerId;
    private String productId;
    private String productTitle;
    private String productImage;
    private String sellerId;
    private String buyerId;
    private String buyerName;
    private double offerPrice;
    private String status;       // pending, accepted, rejected, countered
    private long createdAt;
    private String lastActionBy; // "seller" hoặc "buyer"

    public Offer() {}

    // Getter Setter
    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public double getOfferPrice() { return offerPrice; }
    public void setOfferPrice(double offerPrice) { this.offerPrice = offerPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getLastActionBy() { return lastActionBy; }
    public void setLastActionBy(String lastActionBy) { this.lastActionBy = lastActionBy; }
}
