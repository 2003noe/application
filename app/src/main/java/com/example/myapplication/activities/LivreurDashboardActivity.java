package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LivreurDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStatus;
    private SwitchCompat switchAvailability;
    private TextView tvPendingDeliveries, tvCompletedDeliveries, tvEarnings;
    private RecyclerView rvAvailableOrders;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Order> availableOrders;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livreur_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        availableOrders = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadAvailableOrders();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStatus = findViewById(R.id.tv_status);
        switchAvailability = findViewById(R.id.switch_availability);
        tvPendingDeliveries = findViewById(R.id.tv_pending_deliveries);
        tvCompletedDeliveries = findViewById(R.id.tv_completed_deliveries);
        tvEarnings = findViewById(R.id.tv_earnings);
        rvAvailableOrders = findViewById(R.id.rv_available_missions);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dashboard Livreur");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvAvailableOrders.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Créer OrderAdapter
    }

    private void setupListeners() {
        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStatus.setText("Disponible");
                loadAvailableOrders();
            } else {
                tvStatus.setText("Non disponible");
            }
        });
    }

    private void loadAvailableOrders() {
        // Charger les commandes en attente (status = "pending")
        db.collection("orders")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableOrders.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        availableOrders.add(order);
                    }

                    // Mettre à jour l'adapter
                    tvPendingDeliveries.setText(String.valueOf(availableOrders.size()));

                    if (availableOrders.isEmpty()) {
                        Toast.makeText(this, "Aucune commande disponible", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void acceptOrder(Order order) {
        if (currentUserId == null) return;

        // Récupérer le nom du livreur
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String deliveryName = documentSnapshot.getString("fullName");

                    // Mettre à jour la commande
                    db.collection("orders").document(order.getOrderId())
                            .update(
                                    "status", "accepted",
                                    "deliveryId", currentUserId,
                                    "deliveryName", deliveryName,
                                    "updatedAt", System.currentTimeMillis()
                            )
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Commande acceptée !", Toast.LENGTH_SHORT).show();
                                loadAvailableOrders();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    public void validateDelivery(Order order) {
        // Marquer comme livrée
        db.collection("orders").document(order.getOrderId())
                .update(
                        "status", "delivered",
                        "updatedAt", System.currentTimeMillis()
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Livraison validée !", Toast.LENGTH_SHORT).show();
                });
    }
}