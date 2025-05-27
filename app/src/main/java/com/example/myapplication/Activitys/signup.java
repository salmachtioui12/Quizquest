package com.example.myapplication.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Models.User;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.UUID;

public class signup extends AppCompatActivity {

    private TextInputEditText usernameInput, emailInput, passwordInput;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout;
    private MaterialButton signupButton;
    private TextView loginRedirectText;
    private ProgressBar progressBar;
    private ChipGroup genderGroup;
    private Chip maleChip, femaleChip;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupListeners();

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("utilisateurs");
    }

    private void initializeViews() {
        // Input fields
        usernameInput = findViewById(R.id.username);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);

        // Get TextInputLayout parent views
        usernameLayout = (TextInputLayout) usernameInput.getParent().getParent();
        emailLayout = (TextInputLayout) emailInput.getParent().getParent();
        passwordLayout = (TextInputLayout) passwordInput.getParent().getParent();

        // Gender selection
        genderGroup = findViewById(R.id.genderGroup);
        maleChip = findViewById(R.id.maleChip);
        femaleChip = findViewById(R.id.femaleChip);

        // Other views
        signupButton = findViewById(R.id.signup);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        signupButton.setOnClickListener(v -> registerUser());
        loginRedirectText.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        // Reset error messages
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        // Get user input
        String userId = UUID.randomUUID().toString();
        String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        // Validate input
        if (!validateInput(username, email, password)) {
            return;
        }

        // Validate gender selection
        if (genderGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(this, "Veuillez sélectionner votre sexe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected gender
        String gender = maleChip.isChecked() ? "Homme" : "Femme";

        // Show loading state
        setLoadingState(true);

        // Create user object and save to Firebase
        User user = new User(userId, username, email, password, gender);
        databaseReference.child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(signup.this, "Inscription réussie!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(signup.this, "Erreur lors de l'inscription. Veuillez réessayer.", Toast.LENGTH_SHORT).show();
                        setLoadingState(false);
                    }
                });
    }

    private boolean validateInput(String username, String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Le nom d'utilisateur est requis");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("L'email est requis");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email invalide");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Le mot de passe est requis");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Le mot de passe doit contenir au moins 6 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void setLoadingState(boolean isLoading) {
        signupButton.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signupButton.setText(isLoading ? "" : "S'inscrire");
        genderGroup.setEnabled(!isLoading);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(signup.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
        startActivity(intent);
        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
        finish();
    }
}
