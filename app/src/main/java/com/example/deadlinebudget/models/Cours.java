package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Cours extends RealmObject {
    @PrimaryKey
    private int id;
    private String nom;
    private String salle;
    private String jour; // Lundi, Mardi, etc.
    private int heureDebut; // Format 24h (ex: 8, 14)
    private int minuteDebut;
    private int heureFin;
    private int minuteFin;
    private String photoNote; // Image path if the user wants to take a photo of the board/notes
    private String weekYear; // Identifier for the week (e.g., "2024-W12")

    public Cours() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }
    public int getHeureDebut() { return heureDebut; }
    public void setHeureDebut(int heureDebut) { this.heureDebut = heureDebut; }
    public int getMinuteDebut() { return minuteDebut; }
    public void setMinuteDebut(int minuteDebut) { this.minuteDebut = minuteDebut; }
    public int getHeureFin() { return heureFin; }
    public void setHeureFin(int heureFin) { this.heureFin = heureFin; }
    public int getMinuteFin() { return minuteFin; }
    public void setMinuteFin(int minuteFin) { this.minuteFin = minuteFin; }
    public String getPhotoNote() { return photoNote; }
    public void setPhotoNote(String photoNote) { this.photoNote = photoNote; }
    public String getWeekYear() { return weekYear; }
    public void setWeekYear(String weekYear) { this.weekYear = weekYear; }
}