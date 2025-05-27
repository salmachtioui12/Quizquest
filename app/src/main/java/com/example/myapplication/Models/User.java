package com.example.myapplication.Models;

public class User {
    public String userId;
    public String nom;
    public String email;
    public String motDePasse;
    public String sexe;

    public User() {
        // Nécessaire pour Firebase
    }

    public User(String userId, String nom, String email, String motDePasse, String sexe) {
        this.userId = userId;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.sexe = sexe;
    }
}