package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ProductAdapter;
import com.example.myapplication.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AgriculteurDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navView;
    private TextView tvTotalProducts, tvTotalSales;
    private MaterialButton btnAddProduct;
    private RecyclerView rvMyProducts;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProductAdapter productAdapter;
    private List<Product> myProducts;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agriculteur_dashboard);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        myProducts = new ArrayList<>();

        initViews();
        setupToolbar();
        setupNavigationView();
        setupRecyclerView();
        setupListeners();
        loadMyProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        tvTotalProducts = findViewById(R.id.tv_total_products);
        tvTotalSales = findViewById(R.id.tv_total_sales);
        btnAddProduct = findViewById(R.id.btn_add_product);
        rvMyProducts = findViewById(R.id.rv_my_products);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
            getSupportActionBar().setTitle("Mes Produits");
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.closeDrawer(navView);
            } else {
                drawerLayout.openDrawer(navView);
            }
        });
    }

    private void setupNavigationView() {
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_dashboard) {
                    // Déjà ici
                } else if (id == R.id.menu_add_product) {
                    Intent intent = new Intent(AgriculteurDashboardActivity.this, AddProductActivity.class);
                    startActivity(intent);
                } else if (id == R.id.menu_logout) {
                    logout();
                }

                drawerLayout.closeDrawer(navView);
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        rvMyProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, myProducts);
        rvMyProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AgriculteurDashboardActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyProducts() {
        if (currentUserId == null) {
            Toast.makeText(this, "Erreur: utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Chargement des produits...", Toast.LENGTH_SHORT).show();

        // VERSION SANS INDEX - Charge tous les produits puis filtre localement
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myProducts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());

                            // Filtrer par sellerId localement
                            if (product.getSellerId() != null &&
                                    product.getSellerId().equals(currentUserId)) {
                                myProducts.add(product);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Trier localement par date (plus récent en premier)
                    Collections.sort(myProducts, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return Long.compare(p2.getCreatedAt(), p1.getCreatedAt());
                        }
                    });

                    // Mettre à jour l'adapter
                    productAdapter.updateProducts(myProducts);

                    // Mettre à jour les statistiques
                    tvTotalProducts.setText(String.valueOf(myProducts.size()));

                    // Calculer le total
                    double totalValue = 0;
                    for (Product p : myProducts) {
                        totalValue += p.getPrice() * p.getQuantity();
                    }
                    tvTotalSales.setText(String.format("%.0f FCFA", totalValue));

                    if (myProducts.isEmpty()) {
                        Toast.makeText(this, "Aucun produit. Ajoutez-en un !", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, myProducts.size() + " produit(s) chargé(s)", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AgriculteurDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}