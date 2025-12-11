package com.example.myapplication.models;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private double quantity;
    private String unit;
    private String category;
    private String sellerId;
    private String sellerName;
    private String imageUrl;
    private long createdAt;

    // Constructeur vide OBLIGATOIRE pour Firebase
    public Product() {
        // Ne rien mettre ici
    }

    // Constructeur avec paramètres
    public Product(String id, String name, String description, double price,
                   double quantity, String unit, String category,
                   String sellerId, String sellerName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.imageUrl = imageUrl;
        this.createdAt = System.currentTimeMillis();
    }

    // ========== GETTERS ==========

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getCategory() {
        return category;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // ========== SETTERS ==========
    // ATTENTION: Un seul setter par champ !

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}