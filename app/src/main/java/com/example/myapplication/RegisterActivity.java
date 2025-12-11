package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etPassword;
    private RadioGroup rgUserType;
    private Button btnRegister;
    private TextView tvLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        rgUserType = findViewById(R.id.rg_user_type);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Inscription en cours...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Entrez votre nom");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Entrez votre email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Entrez votre téléphone");
            etPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Entrez un mot de passe");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 caractères");
            etPassword.requestFocus();
            return;
        }

        int selectedUserType = rgUserType.getCheckedRadioButtonId();
        if (selectedUserType == -1) {
            Toast.makeText(this, "Veuillez sélectionner un type d'utilisateur", Toast.LENGTH_SHORT).show();
            return;
        }

        String userType;
        if (selectedUserType == R.id.rb_agriculteur) {
            userType = "agriculteur";
        } else if (selectedUserType == R.id.rb_acheteur) {
            userType = "acheteur";
        } else {
            userType = "livreur";
        }

        // Inscription avec Firebase
        registerUserWithFirebase(fullName, email, phone, password, userType);
    }

    private void registerUserWithFirebase(String fullName, String email, String phone,
                                          String password, String userType) {
        progressDialog.show();

        // Créer l'utilisateur dans Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Utilisateur créé avec succès
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Sauvegarder les infos dans Firestore
                            saveUserToFirestore(userId, fullName, email, phone, userType);
                        }
                    } else {
                        // Erreur lors de la création
                        progressDialog.dismiss();
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Erreur d'inscription";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String email,
                                     String phone, String userType) {
        // Créer un document utilisateur dans Firestore
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("phone", phone);
        user.put("userType", userType);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Inscription réussie !", Toast.LENGTH_SHORT).show();

                    // Rediriger vers le login
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,
                            "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}