package com.example.deadlinebudget.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Note extends RealmObject {
    @PrimaryKey
    private int id;
    private String titre;
    private String contenu;
    private String categorie;
    private String couleur;
    private long dateCreation;

    public Note() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public long getDateCreation() { return dateCreation; }
    public void setDateCreation(long dateCreation) { this.dateCreation = dateCreation; }
}