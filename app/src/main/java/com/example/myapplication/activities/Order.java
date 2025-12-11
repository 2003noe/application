package com.example.myapplication.activities;

import java.util.List;

public class Order {
    private String orderId;
    private String buyerId;
    private String buyerName;
    private String buyerPhone;
    private List<CartItem> items;
    private double totalAmount;
    private String status; // "pending", "accepted", "in_delivery", "delivered", "cancelled"
    private String deliveryId; // ID du livreur qui accepte
    private String deliveryName;
    private long createdAt;
    private long updatedAt;

    // Constructeur vide
    public Order() {}

    public Order(String buyerId, String buyerName, String buyerPhone,
                 List<CartItem> items, double totalAmount) {
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = "pending";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters et Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }

    public String getDeliveryName() { return deliveryName; }
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getStatusText() {
        switch (status) {
            case "pending": return "En attente";
            case "accepted": return "Acceptée";
            case "in_delivery": return "En livraison";
            case "delivered": return "Livrée";
            case "cancelled": return "Annulée";
            default: return status;
        }
    }
}