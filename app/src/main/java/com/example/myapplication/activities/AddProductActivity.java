package com.example.myapplication.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.utils.CloudinaryHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_PERMISSION = 102;
    private static final int MAX_IMAGES = 5;

    private Toolbar toolbar;
    private CardView cvAddImage;
    private LinearLayout llImagesContainer;
    private TextInputEditText etProductName, etPrice, etQuantity, etDescription;
    private Spinner spinnerCategory, spinnerUnit;
    private Button btnPublishProduct;

    private ArrayList<Uri> productImages = new ArrayList<>();
    private ProgressDialog progressDialog;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cvAddImage = findViewById(R.id.cv_add_image);
        llImagesContainer = findViewById(R.id.ll_images_container);
        etProductName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        etDescription = findViewById(R.id.et_description);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerUnit = findViewById(R.id.spinner_unit);
        btnPublishProduct = findViewById(R.id.btn_publish_product);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        String[] categories = {"Légumes", "Fruits", "Céréales", "Tubercules", "Élevage"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] units = {"kg", "litre", "pièce", "sac", "tonne"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
    }

    private void setupListeners() {
        cvAddImage.setOnClickListener(v -> {
            if (productImages.size() >= MAX_IMAGES) {
                Toast.makeText(this, "Maximum " + MAX_IMAGES + " photos", Toast.LENGTH_SHORT).show();
                return;
            }
            showImageSourceDialog();
        });

        btnPublishProduct.setOnClickListener(v -> validateAndPublish());
    }

    private void showImageSourceDialog() {
        String[] options = {"Prendre une photo", "Choisir de la galerie"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter une photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (checkPermissions()) {
                    openCamera();
                }
            } else {
                if (checkPermissions()) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return false;
        }
        return true;
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    addImageToList(imageUri);
                }
            }
        }
    }

    private void addImageToList(Uri imageUri) {
        productImages.add(imageUri);

        View imageItemView = LayoutInflater.from(this)
                .inflate(R.layout.item_product_image, llImagesContainer, false);

        ImageView ivProductImage = imageItemView.findViewById(R.id.iv_product_image);
        ImageButton btnDeleteImage = imageItemView.findViewById(R.id.btn_delete_image);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ivProductImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            return;
        }

        final int imageIndex = productImages.size() - 1;
        btnDeleteImage.setOnClickListener(v -> {
            productImages.remove(imageIndex);
            llImagesContainer.removeView(imageItemView);
            Toast.makeText(this, "Photo supprimée", Toast.LENGTH_SHORT).show();
        });

        llImagesContainer.addView(imageItemView, llImagesContainer.getChildCount() - 1);
    }

    private void validateAndPublish() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etProductName.setError("Nom requis");
            etProductName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Prix requis");
            etPrice.requestFocus();
            return;
        }

        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantité requise");
            etQuantity.requestFocus();
            return;
        }

        if (productImages.isEmpty()) {
            Toast.makeText(this, "Veuillez ajouter au moins une photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Uploader les images puis publier le produit
        uploadImagesToCloudinary();
    }

    private void uploadImagesToCloudinary() {
        progressDialog.setMessage("Upload des images en cours...");
        progressDialog.show();

        final List<String> uploadedUrls = new ArrayList<>();
        final int totalImages = productImages.size();
        final int[] uploadedCount = {0};

        for (int i = 0; i < productImages.size(); i++) {
            Uri imageUri = productImages.get(i);
            final int index = i;

            CloudinaryHelper.uploadImage(this, imageUri, "products",
                    new CloudinaryHelper.UploadListener() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            runOnUiThread(() -> {
                                uploadedUrls.add(imageUrl);
                                uploadedCount[0]++;

                                progressDialog.setMessage("Images uploadées: " +
                                        uploadedCount[0] + "/" + totalImages);

                                // Si toutes les images sont uploadées
                                if (uploadedUrls.size() == totalImages) {
                                    progressDialog.dismiss();
                                    saveProductToFirestore(uploadedUrls);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(AddProductActivity.this,
                                        "Erreur upload image: " + error, Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onProgress(int progress) {
                            runOnUiThread(() -> {
                                progressDialog.setMessage("Upload image " + (index + 1) +
                                        "/" + totalImages + ": " + progress + "%");
                            });
                        }
                    });
        }
    }

    private void saveProductToFirestore(List<String> imageUrls) {
        progressDialog.setMessage("Publication du produit...");
        progressDialog.show();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etProductName.getText().toString().trim();
        double price = Double.parseDouble(etPrice.getText().toString().trim());
        double quantity = Double.parseDouble(etQuantity.getText().toString().trim());
        String description = etDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String unit = spinnerUnit.getSelectedItem().toString();

        // Créer le document produit
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", description);
        product.put("category", category);
        product.put("price", price);
        product.put("unit", unit);
        product.put("quantity", quantity);
        product.put("images", imageUrls);
        product.put("mainImage", imageUrls.get(0));
        product.put("sellerId", currentUser.getUid());
        product.put("sellerName", currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : currentUser.getEmail());
        product.put("sellerPhone", ""); // À remplir depuis Firestore users
        product.put("isAvailable", true);
        product.put("rating", 0.0);
        product.put("totalReviews", 0);
        product.put("totalSold", 0);
        product.put("createdAt", System.currentTimeMillis());
        product.put("updatedAt", System.currentTimeMillis());

        // Sauvegarder dans Firestore
        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this,
                            "Produit publié avec succès !", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this,
                            "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}