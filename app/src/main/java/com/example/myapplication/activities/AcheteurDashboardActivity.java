package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.ProductAdapter;
import com.example.myapplication.models.Product;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AcheteurDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navView;
    private EditText etSearch;
    private ImageButton btnFilter;
    private RecyclerView rvAllProducts;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProductAdapter allProductsAdapter;
    private List<Product> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acheteur_dashboard);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        allProducts = new ArrayList<>();

        initViews();
        setupToolbar();
        setupNavigationView();
        setupRecyclerViews();
        loadAllProducts();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        rvAllProducts = findViewById(R.id.rv_all_products);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
            getSupportActionBar().setTitle("Produits Disponibles");
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

                if (id == R.id.nav_home) {
                    // Déjà ici
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                drawerLayout.closeDrawer(navView);
                return true;
            }
        });
    }

    private void setupRecyclerViews() {
        // Grille de 2 colonnes
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvAllProducts.setLayoutManager(layoutManager);

        allProductsAdapter = new ProductAdapter(this, allProducts);
        rvAllProducts.setAdapter(allProductsAdapter);
    }

    private void loadAllProducts() {
        Toast.makeText(this, "Chargement des produits...", Toast.LENGTH_SHORT).show();

        // Charger TOUS les produits
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            allProducts.add(product);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Trier par date (plus récent en premier)
                    Collections.sort(allProducts, new Comparator<Product>() {
                        @Override
                        public int compare(Product p1, Product p2) {
                            return Long.compare(p2.getCreatedAt(), p1.getCreatedAt());
                        }
                    });

                    // Mettre à jour l'adapter
                    allProductsAdapter.updateProducts(allProducts);

                    if (allProducts.isEmpty()) {
                        Toast.makeText(this, "Aucun produit disponible", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, allProducts.size() + " produit(s) disponible(s)", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AcheteurDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}