package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Tache extends RealmObject {
    @PrimaryKey
    private int id;
    private String titre;
    private String description;
    private String categorie;
    private int priorite;
    private boolean estTerminee;
    private long dateLimite; // Contient maintenant date + heure

    public Tache() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public int getPriorite() { return priorite; }
    public void setPriorite(int priorite) { this.priorite = priorite; }
    public boolean isEstTerminee() { return estTerminee; }
    public void setEstTerminee(boolean estTerminee) { this.estTerminee = estTerminee; }
    public long getDateLimite() { return dateLimite; }
    public void setDateLimite(long dateLimite) { this.dateLimite = dateLimite; }
}