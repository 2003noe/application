package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.activities.AcheteurDashboardActivity;
import com.example.myapplication.activities.AgriculteurDashboardActivity;
import com.example.myapplication.activities.LivreurDashboardActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connexion en cours...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Entrez votre email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Entrez votre mot de passe");
            etPassword.requestFocus();
            return;
        }

        // Connexion avec Firebase
        loginWithFirebase(email, password);
    }

    private void loginWithFirebase(String email, String password) {
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Connexion réussie
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Récupérer le type d'utilisateur depuis Firestore
                            getUserTypeAndRedirect(userId);
                        }
                    } else {
                        // Erreur de connexion
                        progressDialog.dismiss();
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Erreur de connexion";
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getUserTypeAndRedirect(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressDialog.dismiss();

                    if (documentSnapshot.exists()) {
                        String userType = documentSnapshot.getString("userType");
                        String fullName = documentSnapshot.getString("fullName");

                        Toast.makeText(LoginActivity.this,
                                "Bienvenue " + fullName + " !", Toast.LENGTH_SHORT).show();

                        // Rediriger vers le dashboard approprié
                        redirectToDashboard(userType);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Erreur: utilisateur introuvable", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,
                            "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void redirectToDashboard(String userType) {
        Intent intent;

        switch (userType) {
            case "agriculteur":
                intent = new Intent(LoginActivity.this, AgriculteurDashboardActivity.class);
                break;
            case "acheteur":
                intent = new Intent(LoginActivity.this, AcheteurDashboardActivity.class);
                break;
            case "livreur":
                intent = new Intent(LoginActivity.this, LivreurDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Type d'utilisateur inconnu", Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Entrez votre email");
            etEmail.requestFocus();
            return;
        }

        progressDialog.setMessage("Envoi du lien de réinitialisation...");
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Email de réinitialisation envoyé !", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Erreur: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}