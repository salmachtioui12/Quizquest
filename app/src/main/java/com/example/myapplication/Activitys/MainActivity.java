package com.example.myapplication.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView signupRedirectText;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("utilisateurs");
    }

    private void initializeViews() {
        // Input fields
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);

        // Get TextInputLayout parent views
        emailLayout = (TextInputLayout) emailInput.getParent().getParent();
        passwordLayout = (TextInputLayout) passwordInput.getParent().getParent();

        // Other views
        loginButton = findViewById(R.id.login);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> loginUser());
        signupRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, signup.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loginUser() {
        // Reset error messages
        emailLayout.setError(null);
        passwordLayout.setError(null);

        // Get input values
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Validate user in Firebase
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            handleUserLogin(snapshot, password);
                        } else {
                            emailLayout.setError("Email non trouvé");
                            setLoadingState(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase error: " + error.getMessage());
                        Toast.makeText(MainActivity.this,
                                "Erreur de connexion. Veuillez réessayer.",
                                Toast.LENGTH_SHORT).show();
                        setLoadingState(false);
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

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
        }

        return isValid;
    }

    private void handleUserLogin(@NonNull DataSnapshot snapshot, String password) {
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            String storedPassword = userSnapshot.child("motDePasse").getValue(String.class);
            String name = userSnapshot.child("nom").getValue(String.class);
            String userId = userSnapshot.child("userId").getValue(String.class);
            String userEmail = userSnapshot.child("email").getValue(String.class);

            if (storedPassword != null && storedPassword.equals(password)) {
                // Login successful
                Toast.makeText(MainActivity.this, "Connexion réussie!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, home.class);
                intent.putExtra("userName", name);
                intent.putExtra("userEmail", userEmail);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                passwordLayout.setError("Mot de passe incorrect");
                setLoadingState(false);
            }
            break;
        }
    }

    private void setLoadingState(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setText(isLoading ? "" : "Se connecter");
    }
}
