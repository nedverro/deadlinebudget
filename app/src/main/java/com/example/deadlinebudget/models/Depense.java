package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Depense extends RealmObject {
    @PrimaryKey
    private int id;
    private String description;
    private String categorie;
    private float montant;
    private long date;
    private int devoirId; // -1 si non liée à un devoir

    public Depense() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public float getMontant() { return montant; }
    public void setMontant(float montant) { this.montant = montant; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public int getDevoirId() { return devoirId; }
    public void setDevoirId(int devoirId) { this.devoirId = devoirId; }
}