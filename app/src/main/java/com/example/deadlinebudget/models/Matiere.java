package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Matiere extends RealmObject {
    @PrimaryKey
    private int id;
    private String nom;
    private float coefficient;

    public Matiere() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public float getCoefficient() { return coefficient; }
    public void setCoefficient(float coefficient) { this.coefficient = coefficient; }
}