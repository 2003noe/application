package com.example.myapplication.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvProductQuantity;
    private TextView tvProductCategory;
    private TextView tvProductDescription;
    private TextView tvSellerName;
    private Button btnAddToCart;
    private Button btnBuyNow;

    private String productId;
    private String productName;
    private String productDescription;
    private double productPrice;
    private double productQuantity;
    private String productUnit;
    private String productCategory;
    private String productImageUrl;
    private String sellerId;
    private String sellerName;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        getDataFromIntent();
        displayProductInfo();
        setupListeners();
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductQuantity = findViewById(R.id.tv_product_quantity);
        tvProductCategory = findViewById(R.id.tv_product_category);
        tvProductDescription = findViewById(R.id.tv_product_description);
        tvSellerName = findViewById(R.id.tv_seller_name);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBuyNow = findViewById(R.id.btn_buy_now);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails du produit");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void getDataFromIntent() {
        productId = getIntent().getStringExtra("productId");
        productName = getIntent().getStringExtra("productName");
        productDescription = getIntent().getStringExtra("productDescription");
        productPrice = getIntent().getDoubleExtra("productPrice", 0);
        productQuantity = getIntent().getDoubleExtra("productQuantity", 0);
        productUnit = getIntent().getStringExtra("productUnit");
        productCategory = getIntent().getStringExtra("productCategory");
        productImageUrl = getIntent().getStringExtra("productImageUrl");
        sellerId = getIntent().getStringExtra("sellerId");
        sellerName = getIntent().getStringExtra("sellerName");
    }

    private void displayProductInfo() {
        tvProductName.setText(productName);

        NumberFormat formatter = NumberFormat.getInstance(Locale.FRENCH);
        String priceText = formatter.format(productPrice) + " FCFA/" + productUnit;
        tvProductPrice.setText(priceText);

        String quantityText = "Disponible: " + formatter.format(productQuantity) + " " + productUnit;
        tvProductQuantity.setText(quantityText);

        if (productCategory != null && !productCategory.isEmpty()) {
            tvProductCategory.setText("Catégorie: " + productCategory);
        }

        if (productDescription != null && !productDescription.isEmpty()) {
            tvProductDescription.setText(productDescription);
        } else {
            tvProductDescription.setText("Aucune description disponible");
        }

        if (sellerName != null && !sellerName.isEmpty()) {
            tvSellerName.setText("Vendu par: " + sellerName);
        }

        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .centerCrop()
                    .into(ivProductImage);
        }
    }

    private void setupListeners() {
        btnAddToCart.setOnClickListener(v -> showQuantityDialog(false));
        btnBuyNow.setOnClickListener(v -> showQuantityDialog(true));
    }

    private void showQuantityDialog(boolean buyNow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantité");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Entrez la quantité (" + productUnit + ")");
        builder.setView(input);

        builder.setPositiveButton("Confirmer", (dialog, which) -> {
            String quantityStr = input.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer une quantité", Toast.LENGTH_SHORT).show();
                return;
            }

            double quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                Toast.makeText(this, "Quantité invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity > productQuantity) {
                Toast.makeText(this, "Quantité insuffisante", Toast.LENGTH_SHORT).show();
                return;
            }

            if (buyNow) {
                buyNowAction(quantity);
            } else {
                addToCartAction(quantity);
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addToCartAction(double quantity) {
        CartItem item = new CartItem(
                productId,
                productName,
                productImageUrl,
                productPrice,
                quantity,
                productUnit,
                sellerId,
                sellerName
        );

        CartManager.getInstance().addItem(item);
        Toast.makeText(this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
    }

    private void buyNowAction(double quantity) {
        // Créer un item temporaire pour l'achat direct
        CartItem item = new CartItem(
                productId,
                productName,
                productImageUrl,
                productPrice,
                quantity,
                productUnit,
                sellerId,
                sellerName
        );

        // Créer une liste avec cet item unique
        List<CartItem> items = new ArrayList<>();
        items.add(item);

        // Créer la commande directement
        createOrder(items);
    }

    private void createOrder(List<CartItem> items) {
        String currentUserId = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Erreur: utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer les infos de l'acheteur
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String buyerName = documentSnapshot.getString("fullName");
                    String buyerPhone = documentSnapshot.getString("phone");

                    double totalAmount = 0;
                    for (CartItem item : items) {
                        totalAmount += item.getTotalPrice();
                    }

                    Order order = new Order(currentUserId, buyerName, buyerPhone, items, totalAmount);

                    // Sauvegarder la commande dans Firestore
                    db.collection("orders")
                            .add(order)
                            .addOnSuccessListener(documentReference -> {
                                String orderId = documentReference.getId();
                                order.setOrderId(orderId);

                                // Mettre à jour avec l'ID
                                documentReference.update("orderId", orderId);

                                Toast.makeText(this, "Commande créée avec succès !", Toast.LENGTH_LONG).show();

                                // Rediriger vers l'historique des commandes
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}