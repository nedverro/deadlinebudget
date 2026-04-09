package com.example.deadlinebudget.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Représente une tâche spécifique à l'emploi du temps (anciennement Devoir).
 */
public class TacheEmploi extends RealmObject {
    @PrimaryKey
    private int id;
    private String nom;
    private String description;
    private String matiere;
    private long deadlineTimestamp;
    private long creationTimestamp;
    private float priorite;
    private int statut; // 0: À faire, 1: En cours, 2: Terminé
    private float noteObtenue;
    private float coutAssocie;

    public TacheEmploi() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }
    public long getDeadlineTimestamp() { return deadlineTimestamp; }
    public void setDeadlineTimestamp(long deadlineTimestamp) { this.deadlineTimestamp = deadlineTimestamp; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(long creationTimestamp) { this.creationTimestamp = creationTimestamp; }
    public float getPriorite() { return priorite; }
    public void setPriorite(float priorite) { this.priorite = priorite; }
    public int getStatut() { return statut; }
    public void setStatut(int statut) { this.statut = statut; }
    public float getNoteObtenue() { return noteObtenue; }
    public void setNoteObtenue(float noteObtenue) { this.noteObtenue = noteObtenue; }
    public float getCoutAssocie() { return coutAssocie; }
    public void setCoutAssocie(float coutAssocie) { this.coutAssocie = coutAssocie; }

    public String getFormattedDeadline() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        return sdf.format(new Date(deadlineTimestamp));
    }

    public boolean estDepasse() {
        return System.currentTimeMillis() > deadlineTimestamp;
    }

    public int getJoursRestants() {
        long diff = deadlineTimestamp - System.currentTimeMillis();
        if (diff < 0) return 0;
        return (int) TimeUnit.MILLISECONDS.toDays(diff);
    }
}