package com.example.myapplication.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.Locale;

public class resultat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultat);

        // Initialize UI components
        TextView winnerTextView = findViewById(R.id.winner_textview);
        TextView winnerStatsTextView = findViewById(R.id.player1_score_textview);
        TextView loserStatsTextView = findViewById(R.id.player2_score_textview);
        TextView maxScoreTextView = findViewById(R.id.score_calcul);
        Button restartButton = findViewById(R.id.home_button);
        Button exitButton = findViewById(R.id.exit_button);

        // Get all data from intent
        Intent intent = getIntent();
        String winnerName = intent.getStringExtra("winnerName");
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String userId = intent.getStringExtra("userId");
        int maxScore = intent.getIntExtra("maxScore", 150);

        // Get winner stats
        int winnerScore = intent.getIntExtra("winnerScore", 0);
        int winnerCorrect = intent.getIntExtra("winnerCorrect", 0);
        int winnerIncorrect = intent.getIntExtra("winnerIncorrect", 0);

        // Get loser stats
        String loserName = intent.getStringExtra("loserName");
        int loserScore = intent.getIntExtra("loserScore", 0);
        int loserCorrect = intent.getIntExtra("loserCorrect", 0);
        int loserIncorrect = intent.getIntExtra("loserIncorrect", 0);

        // Display results
        if ("Equality".equals(winnerName)) {
            winnerTextView.setText("Match nul!");
            // Show both players' stats side by side for equality
            String player1Name = intent.getStringExtra("player1Name");
            String player2Name = intent.getStringExtra("player2Name");

            winnerStatsTextView.setText(String.format(Locale.ENGLISH, "%s:\nScore: %d\n✔ %d\n❌ %d",
                    player1Name, winnerScore, winnerCorrect, winnerIncorrect));

            loserStatsTextView.setText(String.format(Locale.ENGLISH,"%s:\nScore: %d\n✔ %d\n❌ %d",
                    player2Name, loserScore, loserCorrect, loserIncorrect));
        } else {
            // Show winner and loser stats
            winnerTextView.setText(String.format("Winner: %s!", winnerName));

            winnerStatsTextView.setText(String.format(Locale.ENGLISH,"Winner : %s \nScore: %d\n✔ %d\n❌ %d",
                    winnerName, winnerScore, winnerCorrect, winnerIncorrect));

            loserStatsTextView.setText(String.format(Locale.ENGLISH,"Loser : %s \nScore: %d\n✔ %d\n❌ %d",
                    loserName, loserScore, loserCorrect, loserIncorrect));
        }

        maxScoreTextView.setText(String.format(Locale.ENGLISH,"Top score: %d", maxScore));

        // Configure restart button
        restartButton.setOnClickListener(v -> {
            Intent restartIntent = new Intent(resultat.this, home.class);
            restartIntent.putExtra("userName", userName);
            restartIntent.putExtra("userEmail", userEmail);
            restartIntent.putExtra("userId", userId);
            startActivity(restartIntent);
            finish();
        });

        // Configure exit button
        exitButton.setOnClickListener(v -> {
            finishAffinity(); // Closes all activities and exits the app
        });
    }
}