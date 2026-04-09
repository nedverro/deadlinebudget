package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Utilisateur extends RealmObject {
    @PrimaryKey
    private String pseudo;
    private String motDePasse;
    private String email;
    private long dateInscription;
    private String photoProfil;
    private int niveau;
    private int xp;

    public Utilisateur() {
    }

    // Getters
    public String getPseudo() { return pseudo; }
    public String getMotDePasse() { return motDePasse; }
    public String getEmail() { return email; }
    public long getDateInscription() { return dateInscription; }
    public String getPhotoProfil() { return photoProfil; }
    public int getNiveau() { return niveau; }
    public int getXp() { return xp; }

    // Setters
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setEmail(String email) { this.email = email; }
    public void setDateInscription(long dateInscription) { this.dateInscription = dateInscription; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }
    public void setNiveau(int niveau) { this.niveau = niveau; }
    public void setXp(int xp) { this.xp = xp; }
}