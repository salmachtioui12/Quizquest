package com.example.myapplication.Activitys;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Questions extends AppCompatActivity {
    private TextView questionTextView, timerTextView, scoreTextView, question_indicator_textview;
    private LinearLayout optionsLayout;
    private LinearLayout ln_timer;
    private LinearProgressIndicator progressIndicator;

    private CountDownTimer currentCountDownTimer;
    // 60 seconds per question
    private int playerScore = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private int questionCounter = 0;
    private final int maxQuestions = 15;

    private String category, sessionId, userName, userId, userEmail;
    private boolean isPlayer1;
    private DatabaseReference sessionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        // Initialize UI components
        questionTextView = findViewById(R.id.qst);
        optionsLayout = findViewById(R.id.options_layout);
        ln_timer = findViewById(R.id.ln_timer);
        question_indicator_textview = findViewById(R.id.question_indicator_textview);
        timerTextView = findViewById(R.id.timer_indicator_textview);
        scoreTextView = findViewById(R.id.score_textview);
        progressIndicator = findViewById(R.id.question_progress_indicator);

        // Retrieve data from Intent
        category = getIntent().getStringExtra("category");
        sessionId = getIntent().getStringExtra("sessionId");
        userName = getIntent().getStringExtra("userName");
        userEmail = getIntent().getStringExtra("userEmail");
        userId = getIntent().getStringExtra("userId");
        isPlayer1 = getIntent().getBooleanExtra("isPlayer1", false);

        // Firebase session reference
        sessionRef = FirebaseDatabase.getInstance().getReference("sessions").child(category).child(sessionId);

        // Add or update player information in the session
        String playerKey = isPlayer1 ? "player1" : "player2";
        createOrUpdatePlayer(playerKey);

        // Load the first question
        fetchNextQuestion();
    }

    private void createOrUpdatePlayer(String playerKey) {
        // Prepare player data
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("name", userName);
        playerData.put("score", playerScore);
        playerData.put("correct_answers", correctAnswers);
        playerData.put("incorrect_answers", incorrectAnswers);
        playerData.put("question_counter", questionCounter);

        // Add or update player data in Firebase
        sessionRef.child(playerKey).updateChildren(playerData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", playerKey + " updated successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error updating " + playerKey, e));
    }

    private void updatePlayerData(String playerKey) {
        // Update player data in the session
        Map<String, Object> updates = new HashMap<>();
        updates.put("score", playerScore);
        updates.put("correct_answers", correctAnswers);
        updates.put("incorrect_answers", incorrectAnswers);
        updates.put("question_counter", questionCounter);

        sessionRef.child(playerKey).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", playerKey + " updated successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error updating " + playerKey, e));
    }

    private void fetchNextQuestion() {
        // Check if we've reached the maximum number of questions
        if (questionCounter >= maxQuestions) {
            onPlayerFinished();
            return;
        }

        // Update progress indicator
        progressIndicator.setProgress((questionCounter * 100) / maxQuestions);

        DatabaseReference questionRef = FirebaseDatabase.getInstance()
                .getReference("sujets")
                .child(category)
                .child("questions")
                .child(String.valueOf(questionCounter));

        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String questionText = snapshot.child("question").getValue(String.class);
                    questionTextView.setText(questionText);

                    // Show current question number out of total
                    String questionProgress = "Question " + (questionCounter + 1) + "/" + maxQuestions;
                    scoreTextView.setText(questionProgress);

                    // Load options
                    fetchOptionsFromQuestion(questionRef);
                    startCountdownTimer();
                } else {
                    questionTextView.setText("No question found.");
                    onPlayerFinished();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    private void fetchOptionsFromQuestion(@NonNull DatabaseReference questionRef) {
        questionRef.child("options").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                optionsLayout.removeAllViews();

                for (DataSnapshot optionSnapshot : snapshot.getChildren()) {
                    String optionText = optionSnapshot.getValue(String.class);

                    if (optionText != null) {
                        Button optionButton = new Button(Questions.this);
                        optionButton.setText(optionText);
                        optionButton.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT)
                        );
                        optionButton.setTextColor(Color.WHITE);
                        optionButton.setTextSize(16);
                        optionButton.setTop(10);
                        optionButton.setLeft(10);
                        optionButton.setRight(10);
                        optionButton.setBackgroundResource(R.drawable.button_background);
                        optionButton.setOnClickListener(v -> checkAnswer(optionButton, optionText));
                        optionsLayout.addView(optionButton);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    private void checkAnswer(Button selectedButton, String selectedOption) {
        // Disable all option buttons to prevent multiple answers
        for (int i = 0; i < optionsLayout.getChildCount(); i++) {
            optionsLayout.getChildAt(i).setEnabled(false);
        }

        DatabaseReference currentQuestionRef = FirebaseDatabase.getInstance()
                .getReference("sujets")
                .child(category)
                .child("questions")
                .child(String.valueOf(questionCounter));

        currentQuestionRef.child("correct_answer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String correctAnswer = snapshot.getValue(String.class);

                if (correctAnswer != null && correctAnswer.equals(selectedOption)) {
                    selectedButton.setBackgroundResource(R.drawable.button_reponse_correct);
                    updateScore(true);
                } else {
                    selectedButton.setBackgroundResource(R.drawable.button_reponse_incorrect);
                    updateScore(false);
                }

                if (currentCountDownTimer != null) {
                    currentCountDownTimer.cancel();
                }

                // Update player data
                String playerKey = isPlayer1 ? "player1" : "player2";
                updatePlayerData(playerKey);

                questionCounter++;

                // Check if this was the last question
                if (questionCounter == maxQuestions) {
                    onPlayerFinished();
                } else {
                    // Small delay before next question
                    optionsLayout.postDelayed(() -> fetchNextQuestion(), 1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    private void updateScore(boolean isCorrect) {
        if (isCorrect) {
            correctAnswers++;
            playerScore += 10;
        } else {
            incorrectAnswers++;
        }
    }

    private void onPlayerFinished() {
        String playerKey = isPlayer1 ? "player1" : "player2";

        // Mark player status as "completed"
        sessionRef.child(playerKey).child("status").setValue("completed");

        // Hide question UI and show waiting screen

        optionsLayout.removeAllViews();
        if (currentCountDownTimer != null) {
            currentCountDownTimer.cancel();
        }
        ln_timer.setVisibility(View.GONE);
        question_indicator_textview.setVisibility(View.GONE);
        scoreTextView.setText("Game completed! Please wait...");
        questionTextView.setText("Waiting for other player to finish...");
        timerTextView.setText("");

        // Add listener to monitor other player's status
        String otherPlayerKey = isPlayer1 ? "player2" : "player1";
        sessionRef.child(otherPlayerKey).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String otherPlayerStatus = snapshot.getValue(String.class);
                    if ("completed".equals(otherPlayerStatus)) {
                        // Both players have finished, determine winner
                        determineWinner();
                        // Remove this listener
                        sessionRef.child(otherPlayerKey).child("status").removeEventListener(this);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error monitoring other player: " + error.getMessage());
            }
        });
    }

    private void determineWinner() {
        sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot player1Data = snapshot.child("player1");
                    DataSnapshot player2Data = snapshot.child("player2");

                    // Get scores and statistics
                    Integer player1Score = player1Data.child("score").getValue(Integer.class);
                    Integer player2Score = player2Data.child("score").getValue(Integer.class);
                    String player1Name = player1Data.child("name").getValue(String.class);
                    String player2Name = player2Data.child("name").getValue(String.class);

                    // Get correct and incorrect answers
                    Integer player1Correct = player1Data.child("correct_answers").getValue(Integer.class);
                    Integer player1Incorrect = player1Data.child("incorrect_answers").getValue(Integer.class);
                    Integer player2Correct = player2Data.child("correct_answers").getValue(Integer.class);
                    Integer player2Incorrect = player2Data.child("incorrect_answers").getValue(Integer.class);

                    // Set defaults if null
                    if (player1Score == null) player1Score = 0;
                    if (player2Score == null) player2Score = 0;
                    if (player1Correct == null) player1Correct = 0;
                    if (player1Incorrect == null) player1Incorrect = 0;
                    if (player2Correct == null) player2Correct = 0;
                    if (player2Incorrect == null) player2Incorrect = 0;

                    Intent intent = new Intent(Questions.this, resultat.class);

                    // Add basic game info
                    intent.putExtra("maxScore", maxQuestions * 10);

                    // Determine winner and loser
                    if (player1Score > player2Score) {
                        setPlayerResultData(player1Score, player2Score, player1Name, player2Name, player1Correct,
                                player1Incorrect, player2Correct, player2Incorrect, intent, false);
                    } else if (player2Score > player1Score) {
                        setPlayerResultData(player2Score, player1Score, player2Name, player1Name, player2Correct,
                                player2Incorrect, player1Correct, player1Incorrect, intent, false);
                    } else {
                        setPlayerResultData(player1Score, player2Score, "", "", player1Correct,
                                player1Incorrect, player2Correct, player2Incorrect, intent, true);
                    }

                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    private void setPlayerResultData(Integer winnerScore, Integer loserScore, String winnerPlayer, String loserName, Integer winnerCorrect, Integer winnerIncorrect, Integer loserCorrect, Integer loserIncorrect, @NonNull Intent intent, boolean isEquality) {
        if (isEquality){
            intent.putExtra("winnerName", "Equality");
        }else {
            intent.putExtra("winnerName", winnerPlayer);
            intent.putExtra("loserName", loserName);
        }
        intent.putExtra("winnerScore", winnerScore);
        intent.putExtra("winnerCorrect", winnerCorrect);
        intent.putExtra("winnerIncorrect", winnerIncorrect);
        intent.putExtra("loserScore", loserScore);
        intent.putExtra("loserCorrect", loserCorrect);
        intent.putExtra("loserIncorrect", loserIncorrect);
        intent.putExtra("userName", userName);  // Passer le nom
        intent.putExtra("userEmail", userEmail);
        intent.putExtra("userId", userId);
    }

    private void startCountdownTimer() {
        if (currentCountDownTimer != null) {
            currentCountDownTimer.cancel();
        }

        currentCountDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                timerTextView.setText(secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                updateScore(false);

                // Disable all option buttons
                for (int i = 0; i < optionsLayout.getChildCount(); i++) {
                    optionsLayout.getChildAt(i).setEnabled(false);
                }

                // Update player data in Firebase
                String playerKey = isPlayer1 ? "player1" : "player2";
                updatePlayerData(playerKey);

                questionCounter++;

                // Check if this was the last question
                if (questionCounter == maxQuestions) {
                    onPlayerFinished();
                } else {
                    // Small delay before next question
                    optionsLayout.postDelayed(() -> fetchNextQuestion(), 1000);
                }
            }
        };
        currentCountDownTimer.start();
    }
}
