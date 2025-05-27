package com.example.myapplication.Activitys;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Activitys.MainActivity;
import com.example.myapplication.R;

public class splash_screen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Récupération de la vue bleue et du texte "QUIZQUEST"
        View blueBackground = findViewById(R.id.blue_background);
        TextView appNameTextView = findViewById(R.id.nom);

        // Rendre visible le nom de l'app avant l'animation (au cas où il ne s'affiche pas immédiatement)
        appNameTextView.setVisibility(View.VISIBLE);

        // Animation pour faire disparaître le fond bleu après un délai
        ObjectAnimator fadeOutBackground = ObjectAnimator.ofFloat(blueBackground, "alpha", 1f, 0f);
        fadeOutBackground.setDuration(3000); // 3 secondes
        fadeOutBackground.setStartDelay(1000); // Ajouter un délai de 1 seconde avant de commencer l'animation
        fadeOutBackground.start();

        // Animation pour faire apparaître le texte "QUIZQUEST" progressivement
        ObjectAnimator fadeInText = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f);
        fadeInText.setDuration(2000); // 2 secondes pour l'apparition du texte
        fadeInText.start();

        // Après l'animation, lancer l'activité principale
        fadeOutBackground.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                // Passer à l'activité principale après l'animation
                Intent intent = new Intent(splash_screen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
