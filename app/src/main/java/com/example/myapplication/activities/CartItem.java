package com.example.myapplication.activities;

public class CartItem {
    private String productId;
    private String productName;
    private String productImageUrl;
    private double price;
    private double quantity;
    private String unit;
    private String sellerId;
    private String sellerName;

    // Constructeur vide (requis par Firebase)
    public CartItem() {}

    public CartItem(String productId, String productName, String productImageUrl,
                    double price, double quantity, String unit,
                    String sellerId, String sellerName) {
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }

    // Getters et Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public double getTotalPrice() {
        return price * quantity;
    }
}