package com.example.deadlinebudget.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.deadlinebudget.models.Cours;
import com.example.deadlinebudget.models.Depense;
import com.example.deadlinebudget.models.TacheEmploi;
import com.example.deadlinebudget.models.Note;
import com.example.deadlinebudget.models.Tache;
import com.example.deadlinebudget.models.Utilisateur;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.Sort;

public class DataManager {
    private static DataManager instance;

    private DataManager() {}

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public static void init(Context context) {
        // Realm is already initialized in Application class
    }

    public void close() {}

    // ==================== TÂCHES GÉNÉRALES ====================
    public List<Tache> getAllTaches() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Tache.class).findAll());
        }
    }

    public Tache getTacheById(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Tache t = realm.where(Tache.class).equalTo("id", id).findFirst();
            return t != null ? realm.copyFromRealm(t) : null;
        }
    }

    public List<Tache> getTachesByStatus(boolean terminee) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Tache.class)
                    .equalTo("estTerminee", terminee)
                    .sort(new String[]{"priorite", "dateLimite"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING})
                    .findAll());
        }
    }

    public List<Tache> getTachesByCategorie(String categorie, boolean terminee) {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmQuery<Tache> query = realm.where(Tache.class).equalTo("estTerminee", terminee);
            if (!categorie.equals("Toutes")) {
                query.equalTo("categorie", categorie);
            }
            return realm.copyFromRealm(query.sort(new String[]{"priorite", "dateLimite"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING}).findAll());
        }
    }

    public Tache ajouterTache(String titre, String description, String categorie, int priorite, long dateLimite) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final Tache[] result = new Tache[1];
            realm.executeTransaction(r -> {
                Number maxId = r.where(Tache.class).max("id");
                int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                Tache t = r.createObject(Tache.class, nextId);
                t.setTitre(titre);
                t.setDescription(description);
                t.setCategorie(categorie);
                t.setPriorite(priorite);
                t.setDateLimite(dateLimite);
                t.setEstTerminee(false);
                result[0] = r.copyFromRealm(t);
            });
            return result[0];
        }
    }

    public void modifierTache(int id, String titre, String description, String categorie, int priorite, long dateLimite, boolean estTerminee) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Tache t = r.where(Tache.class).equalTo("id", id).findFirst();
                if (t != null) {
                    t.setTitre(titre);
                    t.setDescription(description);
                    t.setCategorie(categorie);
                    t.setPriorite(priorite);
                    t.setDateLimite(dateLimite);
                    t.setEstTerminee(estTerminee);
                }
            });
        }
    }

    public void supprimerTache(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Tache t = r.where(Tache.class).equalTo("id", id).findFirst();
                if (t != null) t.deleteFromRealm();
            });
        }
    }

    public List<String> getTaskCategories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Personnel", "Urgent", "Autre")));
        return new ArrayList<>(set);
    }

    public void addTaskCategory(Context context, String cat) {
        SharedPreferences prefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Personnel", "Urgent", "Autre"))));
        set.add(cat);
        prefs.edit().putStringSet("categories", set).apply();
    }

    public void removeTaskCategory(Context context, String cat) {
        SharedPreferences prefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Personnel", "Urgent", "Autre"))));
        set.remove(cat);
        prefs.edit().putStringSet("categories", set).apply();
    }

    // ==================== NOTES ====================
    public List<Note> getAllNotes() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Note.class).sort("dateCreation", Sort.DESCENDING).findAll());
        }
    }

    public List<Note> getNotesByCategorie(String cat) {
        try (Realm realm = Realm.getDefaultInstance()) {
            if (cat.equals("Toutes")) return getAllNotes();
            return realm.copyFromRealm(realm.where(Note.class).equalTo("categorie", cat).sort("dateCreation", Sort.DESCENDING).findAll());
        }
    }

    public List<Note> rechercherNotes(String query) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Note.class)
                    .contains("titre", query, io.realm.Case.INSENSITIVE)
                    .or()
                    .contains("contenu", query, io.realm.Case.INSENSITIVE)
                    .findAll());
        }
    }

    public void ajouterNote(String titre, String contenu, String categorie, String couleur) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Number maxId = r.where(Note.class).max("id");
                int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                Note n = r.createObject(Note.class, nextId);
                n.setTitre(titre);
                n.setContenu(contenu);
                n.setCategorie(categorie);
                n.setCouleur(couleur);
                n.setDateCreation(System.currentTimeMillis());
            });
        }
    }

    public void modifierNote(int id, String titre, String contenu, String categorie, String couleur) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Note n = r.where(Note.class).equalTo("id", id).findFirst();
                if (n != null) {
                    n.setTitre(titre);
                    n.setContenu(contenu);
                    n.setCategorie(categorie);
                    n.setCouleur(couleur);
                }
            });
        }
    }

    public void supprimerNote(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Note n = r.where(Note.class).equalTo("id", id).findFirst();
                if (n != null) n.deleteFromRealm();
            });
        }
    }

    public List<String> getNoteCategories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("NotePrefs", Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Idées", "Rappel", "Important")));
        return new ArrayList<>(set);
    }

    public void addNoteCategory(Context context, String cat) {
        SharedPreferences prefs = context.getSharedPreferences("NotePrefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Idées", "Rappel", "Important"))));
        set.add(cat);
        prefs.edit().putStringSet("categories", set).apply();
    }

    public void removeNoteCategory(Context context, String cat) {
        SharedPreferences prefs = context.getSharedPreferences("NotePrefs", Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(prefs.getStringSet("categories", new HashSet<>(Arrays.asList("Cours", "Idées", "Rappel", "Important"))));
        set.remove(cat);
        prefs.edit().putStringSet("categories", set).apply();
    }

    // ==================== BUDGET ====================
    public List<Depense> getAllDepenses() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Depense.class).sort("date", Sort.DESCENDING).findAll());
        }
    }

    public float getTotalDepenses() {
        float total = 0;
        for (Depense d : getAllDepenses()) {
            total += d.getMontant();
        }
        return total;
    }

    public float getDepensesMoisActuel() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        long debutMois = cal.getTimeInMillis();
        float total = 0;
        for (Depense d : getAllDepenses()) {
            if (d.getDate() >= debutMois) total += d.getMontant();
        }
        return total;
    }

    public void ajouterDepense(String desc, String cat, float mont, long date) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Number maxId = r.where(Depense.class).max("id");
                int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                Depense d = r.createObject(Depense.class, nextId);
                d.setDescription(desc);
                d.setCategorie(cat);
                d.setMontant(mont);
                d.setDate(date);
            });
        }
    }

    public void modifierDepense(int id, String desc, String cat, float mont, long date) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Depense d = r.where(Depense.class).equalTo("id", id).findFirst();
                if (d != null) {
                    d.setDescription(desc);
                    d.setCategorie(cat);
                    d.setMontant(mont);
                    d.setDate(date);
                }
            });
        }
    }

    public void supprimerDepense(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Depense d = r.where(Depense.class).equalTo("id", id).findFirst();
                if (d != null) d.deleteFromRealm();
            });
        }
    }

    public String exportDepensesToCSV(Context context) {
        List<Depense> depenses = getAllDepenses();
        if (depenses.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        StringBuilder csv = new StringBuilder("Description;Categorie;Montant;Date\n");
        for (Depense d : depenses) {
            csv.append(d.getDescription()).append(";")
               .append(d.getCategorie()).append(";")
               .append(d.getMontant()).append(";")
               .append(sdf.format(new Date(d.getDate()))).append("\n");
        }
        try {
            File file = new File(context.getExternalFilesDir(null), "budget_export.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(csv.toString());
            writer.flush();
            writer.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    public float getMonthlyBudget(Context context) {
        return context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE).getFloat("monthly_budget", 0);
    }

    public void setMonthlyBudget(Context context, float amount) {
        context.getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE).edit().putFloat("monthly_budget", amount).apply();
    }

    // ==================== UTILISATEURS ====================
    public boolean inscrireUtilisateur(String pseudo, String email, String password) {
        try (Realm realm = Realm.getDefaultInstance()) {
            if (realm.where(Utilisateur.class).equalTo("pseudo", pseudo).count() > 0) return false;
            realm.executeTransaction(r -> {
                Utilisateur u = r.createObject(Utilisateur.class, pseudo);
                u.setEmail(email);
                u.setMotDePasse(password);
                u.setDateInscription(System.currentTimeMillis());
                u.setNiveau(1);
                u.setXp(0);
            });
            return true;
        }
    }

    public boolean verifierConnexion(String pseudo, String password) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(Utilisateur.class).equalTo("pseudo", pseudo).equalTo("motDePasse", password).count() > 0;
        }
    }

    public boolean utilisateurExiste(String pseudo) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(Utilisateur.class).equalTo("pseudo", pseudo).count() > 0;
        }
    }

    public Utilisateur getUtilisateur(String pseudo) {
        try (Realm realm = Realm.getDefaultInstance()) {
            Utilisateur u = realm.where(Utilisateur.class).equalTo("pseudo", pseudo).findFirst();
            return u != null ? realm.copyFromRealm(u) : null;
        }
    }

    public void updateProfile(String pseudo, String email, String password, String photoPath) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Utilisateur u = r.where(Utilisateur.class).equalTo("pseudo", pseudo).findFirst();
                if (u != null) {
                    if (email != null) u.setEmail(email);
                    if (password != null) u.setMotDePasse(password);
                    if (photoPath != null) u.setPhotoProfil(photoPath);
                }
            });
        }
    }

    public List<Utilisateur> getLeaderboard() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Utilisateur.class).sort("xp", Sort.DESCENDING).findAll());
        }
    }

    // ==================== COURS ====================
    public List<Cours> getAllCours() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Cours.class).findAll());
        }
    }

    public List<Cours> getCoursByWeek(String weekYear) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(Cours.class).equalTo("weekYear", weekYear).findAll());
        }
    }

    public void ajouterCours(String nom, String salle, String jour, int hDeb, int mDeb, int hFin, int mFin, String weekYear) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Number maxId = r.where(Cours.class).max("id");
                int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                Cours c = r.createObject(Cours.class, nextId);
                c.setNom(nom);
                c.setSalle(salle);
                c.setJour(jour);
                c.setHeureDebut(hDeb);
                c.setMinuteDebut(mDeb);
                c.setHeureFin(hFin);
                c.setMinuteFin(mFin);
                c.setWeekYear(weekYear);
            });
        }
    }

    public void modifierCours(int id, String nom, String salle, String jour, int hDeb, int mDeb, int hFin, int mFin, String weekYear) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Cours c = r.where(Cours.class).equalTo("id", id).findFirst();
                if (c != null) {
                    c.setNom(nom);
                    c.setSalle(salle);
                    c.setJour(jour);
                    c.setHeureDebut(hDeb);
                    c.setMinuteDebut(mDeb);
                    c.setHeureFin(hFin);
                    c.setMinuteFin(mFin);
                    c.setWeekYear(weekYear);
                }
            });
        }
    }

    public void supprimerCours(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                Cours c = r.where(Cours.class).equalTo("id", id).findFirst();
                if (c != null) c.deleteFromRealm();
            });
        }
    }

    // ==================== TACHES EMPLOI (Anciennement DEVOIRS) ====================
    public List<TacheEmploi> getAllTacheEmplois() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(TacheEmploi.class).sort("deadlineTimestamp", Sort.ASCENDING).findAll());
        }
    }

    public TacheEmploi getTacheEmploiById(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            TacheEmploi t = realm.where(TacheEmploi.class).equalTo("id", id).findFirst();
            return t != null ? realm.copyFromRealm(t) : null;
        }
    }

    public List<TacheEmploi> getActiveTacheEmplois() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(TacheEmploi.class).notEqualTo("statut", 2).sort("deadlineTimestamp", Sort.ASCENDING).findAll());
        }
    }

    public List<TacheEmploi> getCompletedTacheEmplois() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(TacheEmploi.class).equalTo("statut", 2).sort("deadlineTimestamp", Sort.DESCENDING).findAll());
        }
    }

    public List<TacheEmploi> getTacheEmploisByMatiere(String matiere) {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.copyFromRealm(realm.where(TacheEmploi.class).equalTo("matiere", matiere).sort("deadlineTimestamp", Sort.ASCENDING).findAll());
        }
    }

    public TacheEmploi ajouterTacheEmploi(String nom, String description, String matiere, long deadline, int priorite, int statut, float cout) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final TacheEmploi[] result = new TacheEmploi[1];
            realm.executeTransaction(r -> {
                Number maxId = r.where(TacheEmploi.class).max("id");
                int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                TacheEmploi d = r.createObject(TacheEmploi.class, nextId);
                d.setNom(nom);
                d.setDescription(description);
                d.setMatiere(matiere);
                d.setDeadlineTimestamp(deadline);
                d.setCreationTimestamp(System.currentTimeMillis());
                d.setPriorite(priorite);
                d.setStatut(statut);
                d.setCoutAssocie(cout);
                result[0] = r.copyFromRealm(d);
            });
            return result[0];
        }
    }

    public void modifierTacheEmploi(int id, String nom, String description, String matiere, long deadline, float priorite, int statut, float note, float cout) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                TacheEmploi d = r.where(TacheEmploi.class).equalTo("id", id).findFirst();
                if (d != null) {
                    d.setNom(nom);
                    d.setDescription(description);
                    d.setMatiere(matiere);
                    d.setDeadlineTimestamp(deadline);
                    d.setPriorite(priorite);
                    d.setStatut(statut);
                    d.setNoteObtenue(note);
                    d.setCoutAssocie(cout);
                }
            });
        }
    }

    public String terminerTacheEmploi(int id, String pseudo) {
        try (Realm realm = Realm.getDefaultInstance()) {
            final String[] message = {"Tâche terminée !"};
            realm.executeTransaction(r -> {
                TacheEmploi d = r.where(TacheEmploi.class).equalTo("id", id).findFirst();
                if (d != null) {
                    d.setStatut(2);
                    Utilisateur u = r.where(Utilisateur.class).equalTo("pseudo", pseudo).findFirst();
                    if (u != null) {
                        int currentXp = u.getXp();
                        u.setXp(currentXp + 50);
                        if (u.getXp() >= u.getNiveau() * 100) {
                            u.setNiveau(u.getNiveau() + 1);
                            message[0] = "Tâche terminée ! Niveau supérieur atteint !";
                        }
                    }
                    if (d.getCoutAssocie() > 0) {
                        Number maxId = r.where(Depense.class).max("id");
                        int nextId = (maxId != null ? maxId.intValue() : 0) + 1;
                        Depense depense = r.createObject(Depense.class, nextId);
                        depense.setDescription("Frais : " + d.getNom());
                        depense.setCategorie("Scolaire");
                        depense.setMontant(d.getCoutAssocie());
                        depense.setDate(System.currentTimeMillis());
                    }
                }
            });
            return message[0];
        }
    }

    public void supprimerTacheEmploi(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(r -> {
                TacheEmploi d = r.where(TacheEmploi.class).equalTo("id", id).findFirst();
                if (d != null) d.deleteFromRealm();
            });
        }
    }
}