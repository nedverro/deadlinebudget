package com.example.deadlinebudget.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.deadlinebudget.R;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Utilisateur;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private String currentUsername;
    private DataManager dataManager;
    private ImageView ivProfile;
    private TextView tvUsername, tvUserLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dataManager = DataManager.getInstance();
        currentUsername = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("current_user", "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivProfile = findViewById(R.id.ivProfile);
        tvUsername = findViewById(R.id.tvUsername);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);

        loadUserData();

        // Dark Mode
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        findViewById(R.id.btnChangeEmail).setOnClickListener(v -> showUpdateDialog("Email", true));
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showUpdateDialog("Mot de passe", false));

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "📚 DeadlineBudget - L'application ultime pour gérer mes études !\n\n" +
                    "✅ Devoirs avec système XP\n" +
                    "💰 Budget étudiant\n" +
                    "📅 Emploi du temps\n" +
                    "🏆 Classement\n\n" +
                    "Téléchargez-la dès maintenant !");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Partager via"));
        });

        findViewById(R.id.btnFeedback).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"deadlinebudget@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback DeadlineBudget");
            intent.putExtra(Intent.EXTRA_TEXT, "Bonjour l'équipe DeadlineBudget,\n\nVoici mon retour sur l'application :\n\n");
            try {
                startActivity(Intent.createChooser(intent, "Envoyer un email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Aucun client email installé.", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout logic
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Déconnexion")
                    .setMessage("Voulez-vous vraiment vous déconnecter ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        // Supprimer l'utilisateur de la session
                        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().remove("current_user").apply();
                        
                        // Rediriger vers l'écran de connexion et vider la pile d'activités
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });
    }

    private void loadUserData() {
        Utilisateur user = dataManager.getUtilisateur(currentUsername);
        if (user != null) {
            tvUsername.setText(user.getPseudo());
            tvUserLevel.setText("Niveau " + user.getNiveau() + " (" + user.getXp() + " XP)");
            if (user.getPhotoProfil() != null && !user.getPhotoProfil().isEmpty()) {
                try {
                    ivProfile.setImageURI(Uri.parse(user.getPhotoProfil()));
                } catch (Exception e) {
                    ivProfile.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                ivProfile.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            tvUsername.setText(currentUsername);
            tvUserLevel.setText("Niveau 1 (0 XP)");
        }
    }

    private void showUpdateDialog(String title, boolean isEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier " + title);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_field, null);
        EditText etInput = view.findViewById(R.id.etInput);
        etInput.setHint("Nouveau " + title);
        builder.setView(view);
        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String value = etInput.getText().toString().trim();
            if (!value.isEmpty()) {
                if (isEmail) {
                    dataManager.updateProfile(currentUsername, value, null, null);
                    Toast.makeText(this, "Email mis à jour", Toast.LENGTH_SHORT).show();
                } else {
                    if (value.length() >= 4) {
                        dataManager.updateProfile(currentUsername, null, value, null);
                        Toast.makeText(this, "Mot de passe mis à jour", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Le mot de passe doit contenir au moins 4 caractères", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Veuillez saisir une valeur", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImage = data.getData();
            ivProfile.setImageURI(selectedImage);
            dataManager.updateProfile(currentUsername, null, null, selectedImage.toString());
            Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.close();
        }
    }
}