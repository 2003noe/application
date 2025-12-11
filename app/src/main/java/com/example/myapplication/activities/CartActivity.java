package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvTotal, tvEmptyCart;
    private Button btnCheckout;

    private CartManager cartManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        cartManager = CartManager.getInstance();

        initViews();
        setupToolbar();
        displayCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotal = findViewById(R.id.tv_total);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        btnCheckout = findViewById(R.id.btn_checkout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mon panier");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void displayCart() {
        List<CartItem> items = cartManager.getCartItems();

        if (items.isEmpty()) {
            tvEmptyCart.setVisibility(android.view.View.VISIBLE);
            rvCartItems.setVisibility(android.view.View.GONE);
            btnCheckout.setEnabled(false);
        } else {
            tvEmptyCart.setVisibility(android.view.View.GONE);
            rvCartItems.setVisibility(android.view.View.VISIBLE);
            btnCheckout.setEnabled(true);

            // TODO: Créer CartAdapter pour afficher les items
            rvCartItems.setLayoutManager(new LinearLayoutManager(this));

            updateTotals();
        }

        btnCheckout.setOnClickListener(v -> checkout());
    }

    private void updateTotals() {
        double total = cartManager.getTotalAmount();
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);

        String totalText = formatter.format(total) + " FCFA";
        tvSubtotal.setText(totalText);
        tvTotal.setText(totalText);
    }

    private void checkout() {
        String currentUserId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Erreur: utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> items = cartManager.getCartItems();
        if (items.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer les infos de l'acheteur
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String buyerName = documentSnapshot.getString("fullName");
                    String buyerPhone = documentSnapshot.getString("phone");

                    double totalAmount = cartManager.getTotalAmount();
                    Order order = new Order(currentUserId, buyerName, buyerPhone, items, totalAmount);

                    // Créer la commande
                    db.collection("orders")
                            .add(order)
                            .addOnSuccessListener(documentReference -> {
                                String orderId = documentReference.getId();
                                documentReference.update("orderId", orderId);

                                // Vider le panier
                                cartManager.clear();

                                Toast.makeText(this, "Commande créée avec succès !", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }
}