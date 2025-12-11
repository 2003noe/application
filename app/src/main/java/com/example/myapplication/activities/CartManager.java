package com.example.myapplication.activities;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(CartItem item) {
        // Vérifier si le produit existe déjà dans le panier
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductId().equals(item.getProductId())) {
                // Augmenter la quantité
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        // Ajouter le nouvel item
        cartItems.add(item);
    }

    public void removeItem(String productId) {
        cartItems.removeIf(item -> item.getProductId().equals(productId));
    }

    public void updateQuantity(String productId, double quantity) {
        for (CartItem item : cartItems) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clear() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}