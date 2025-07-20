package com.example.doancuoiki_dotcuoi.model;

import java.io.Serializable;

public class Discount implements Serializable {
    private String discountId;
    private String code;
    private String postId;
    private double amount;

    public Discount() {}

    public String getDiscountId() { return discountId; }
    public void setDiscountId(String discountId) { this.discountId = discountId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
}
