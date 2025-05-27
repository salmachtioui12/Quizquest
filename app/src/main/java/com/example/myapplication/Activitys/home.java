package com.example.myapplication.Activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class home extends AppCompatActivity {

    private TextView playerName, playerEmail, playerScore;
    private ShapeableImageView avatarImageView;
    private MaterialButton animalButton, brainButton, celebritiesButton, entertainmentButton, hobbiesButton,
            moviesButton, musicButton, newestButton, sciencesButton, sportButton;

    private String userName, userEmail, userId;
    private int maxScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setUpButtonListeners();
        loadUserData();
        loadUserScore();
    }

    private void initializeViews() {
        // Profile views
        playerName = findViewById(R.id.playerName);
        playerEmail = findViewById(R.id.playerEmail);
        playerScore = findViewById(R.id.playerScore);
        avatarImageView = findViewById(R.id.avatarImageView);

        // Quiz buttons
        animalButton = findViewById(R.id.animal);
        brainButton = findViewById(R.id.brain);
        celebritiesButton = findViewById(R.id.celebrities);
        entertainmentButton = findViewById(R.id.entertainment);
        hobbiesButton = findViewById(R.id.hobbies);
        moviesButton = findViewById(R.id.movies);
        musicButton = findViewById(R.id.music);
        newestButton = findViewById(R.id.newest);
        sciencesButton = findViewById(R.id.sciences);
        sportButton = findViewById(R.id.sport);
    }

    private void loadUserData() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        userId = intent.getStringExtra("userId");

        playerName.setText(userName != null ? userName : "Joueur");
        playerEmail.setText(userEmail != null ? userEmail : "Email");
    }

    private void loadUserScore() {
        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.child("maxScore").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        maxScore = snapshot.getValue(Integer.class);
                        playerScore.setText("Score Max: " + maxScore);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Failed to load user score: " + error.getMessage());
                }
            });
        }
    }

    private void setUpButtonListeners() {
        animalButton.setOnClickListener(v -> createOrJoinSession("animals"));
        brainButton.setOnClickListener(v -> createOrJoinSession("brain-teasers"));
        celebritiesButton.setOnClickListener(v -> createOrJoinSession("celebrities"));
        entertainmentButton.setOnClickListener(v -> createOrJoinSession("entertainments"));
        hobbiesButton.setOnClickListener(v -> createOrJoinSession("hobbies"));
        moviesButton.setOnClickListener(v -> createOrJoinSession("movies"));
        musicButton.setOnClickListener(v -> createOrJoinSession("music"));
        newestButton.setOnClickListener(v -> createOrJoinSession("newest"));
        sciencesButton.setOnClickListener(v -> createOrJoinSession("sciences-technologies"));
        sportButton.setOnClickListener(v -> createOrJoinSession("sport"));
    }

    private void createOrJoinSession(String category) {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions").child(category);

        if (userName == null || userId == null) {
            Toast.makeText(this, "Missing user details!", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionsRef.orderByChild("status").equalTo("not ready").limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            handleExistingSession(snapshot, category, sessionsRef);
                        } else {
                            createNewSession(sessionsRef);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Sessions query failed: " + error.getMessage());
                    }
                });
    }

    private void handleExistingSession(@NonNull DataSnapshot snapshot, String category, DatabaseReference sessionsRef) {
        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
            String sessionId = sessionSnapshot.getKey();
            DatabaseReference sessionRef = sessionsRef.child(sessionId);

            sessionRef.child("player2").setValue(getPlayerDetails(userName, userId));
            
            sessionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String status = dataSnapshot.child("status").getValue(String.class);

                        if ("not ready".equals(status)) {
                            sessionRef.child("status").setValue("in progress");
                            showStartGameDialog(sessionRef, sessionId, category);
                            sessionRef.removeEventListener(this);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Session tracking failed: " + error.getMessage());
                }
            });
            return;
        }
    }

    private void createNewSession(@NonNull DatabaseReference sessionsRef) {
        String sessionId = sessionsRef.push().getKey();
        if (sessionId != null) {
            DatabaseReference newSessionRef = sessionsRef.child(sessionId);

            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("player1", getPlayerDetails(userName, userId));
            sessionData.put("status", "not ready");
            sessionData.put("player2", null);
            newSessionRef.setValue(sessionData);

            newSessionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        DataSnapshot player2Snapshot = dataSnapshot.child("player2");
                        String status = dataSnapshot.child("status").getValue(String.class);
                        
                        if (player2Snapshot.exists() && "not ready".equals(status)) {
                            newSessionRef.child("status").setValue("in progress");
                            showStartGameDialog(newSessionRef, sessionId, 
                                Objects.requireNonNull(dataSnapshot.getRef().getParent()).getKey());
                            newSessionRef.removeEventListener(this);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Session tracking failed: " + error.getMessage());
                }
            });

            disableButtons();
            Toast.makeText(home.this, "New session created. Waiting for Player 2.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStartGameDialog(DatabaseReference sessionRef, String sessionId, String category) {
        new AlertDialog.Builder(home.this)
                .setTitle("Start the game")
                .setMessage("Both players are ready. Do you want to start the game?")
                .setPositiveButton("start", (dialog, which) -> startGame(sessionRef, sessionId, category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startGame(@NonNull DatabaseReference sessionRef, String sessionId, String category) {
        sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isPlayer1 = false;

                if (snapshot.child("player1").exists()) {
                    String player1Id = snapshot.child("player1").child("id").getValue(String.class);
                    isPlayer1 = userId.equals(player1Id);
                }

                // Proceed to start the game with the isPlayer1 flag
                Intent gameIntent = new Intent(home.this, Questions.class);
                gameIntent.putExtra("sessionId", sessionId);
                gameIntent.putExtra("category", category);
                gameIntent.putExtra("userName", userName);
                gameIntent.putExtra("userEmail", userEmail);
                gameIntent.putExtra("userId", userId);
                gameIntent.putExtra("isPlayer1", isPlayer1);
                startActivity(gameIntent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to retrieve session data: " + error.getMessage());
            }
        });
    }


    @NonNull
    private Map<String, Object> getPlayerDetails(String name, String id) {
        Map<String, Object> playerDetails = new HashMap<>();
        playerDetails.put("name", name);
        playerDetails.put("id", id);
        playerDetails.put("score", 0);
        playerDetails.put("correct_answers", 0);
        playerDetails.put("incorrect_answers", 0);
        playerDetails.put("answers", new HashMap<>()); // Store individual answers
        return playerDetails;
    }


    private void disableButtons() {
        animalButton.setEnabled(false);
        brainButton.setEnabled(false);
        celebritiesButton.setEnabled(false);
        entertainmentButton.setEnabled(false);
        hobbiesButton.setEnabled(false);
        moviesButton.setEnabled(false);
        musicButton.setEnabled(false);
        newestButton.setEnabled(false);
        sciencesButton.setEnabled(false);
        sportButton.setEnabled(false);
    }
}
