package com.example.deadlinebudget.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.deadlinebudget.R;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.TacheEmploi;

import java.util.Calendar;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private EditText editTextNom;
    private EditText editTextDescription;
    private EditText editTextMatiere;
    private EditText editTextCout;
    private EditText editTextNote;
    private RatingBar ratingBarPriorite;
    private Spinner spinnerStatut;
    private Button buttonSupprimer;
    private Button buttonModifier;
    private TextView textViewDeadline;
    private Button buttonChangeDeadline;

    private DataManager dataManager;
    private TacheEmploi devoir;
    private int devoirId;
    private long currentDeadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.d("DetailActivity", "onCreate appelé");

        dataManager = DataManager.getInstance();

        devoirId = getIntent().getIntExtra("devoir_id", -1);

        if (devoirId == -1) {
            Toast.makeText(this, "Erreur: Aucun devoir spécifié", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadDevoir();
        setupListeners();
    }

    private void initViews() {
        editTextNom = findViewById(R.id.editTextNom);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextMatiere = findViewById(R.id.editTextMatiere);
        editTextCout = findViewById(R.id.editTextCout);
        editTextNote = findViewById(R.id.editTextNote);
        ratingBarPriorite = findViewById(R.id.ratingBarPriorite);
        spinnerStatut = findViewById(R.id.spinnerStatut);
        buttonSupprimer = findViewById(R.id.buttonSupprimer);
        buttonModifier = findViewById(R.id.buttonModifier);
        textViewDeadline = findViewById(R.id.textViewDeadline);
        buttonChangeDeadline = findViewById(R.id.buttonChangeDeadline);

        String[] statuts = {"À faire", "En cours", "Rendu"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatut.setAdapter(adapter);
    }

    private void loadDevoir() {
        devoir = dataManager.getTacheEmploiById(devoirId);

        if (devoir == null) {
            Toast.makeText(this, "Devoir non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextNom.setText(devoir.getNom());
        editTextDescription.setText(devoir.getDescription());
        editTextMatiere.setText(devoir.getMatiere());
        editTextCout.setText(String.valueOf(devoir.getCoutAssocie()));
        editTextNote.setText(String.valueOf(devoir.getNoteObtenue()));
        ratingBarPriorite.setRating(devoir.getPriorite());
        spinnerStatut.setSelection(devoir.getStatut());
        currentDeadline = devoir.getDeadlineTimestamp();
        updateDeadlineDisplay();

        Log.d("DetailActivity", "Devoir chargé: " + devoir.getNom());
    }

    private void updateDeadlineDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        textViewDeadline.setText("Deadline: " + sdf.format(new java.util.Date(currentDeadline)));
    }

    private void setupListeners() {
        buttonModifier.setOnClickListener(v -> modifierDevoir());
        buttonSupprimer.setOnClickListener(v -> confirmerSuppression());

        if (buttonChangeDeadline != null) {
            buttonChangeDeadline.setOnClickListener(v -> showDatePicker());
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(currentDeadline);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year1, monthOfYear, dayOfMonth);
            currentDeadline = selected.getTimeInMillis();
            updateDeadlineDisplay();
        }, year, month, day).show();
    }

    private void modifierDevoir() {
        String nom = editTextNom.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String matiere = editTextMatiere.getText().toString().trim();
        String coutStr = editTextCout.getText().toString().trim();
        String noteStr = editTextNote.getText().toString().trim();
        float priorite = ratingBarPriorite.getRating();
        int statut = spinnerStatut.getSelectedItemPosition();

        if (nom.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un nom", Toast.LENGTH_SHORT).show();
            return;
        }

        if (matiere.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir une matière", Toast.LENGTH_SHORT).show();
            return;
        }

        float cout = 0;
        try {
            cout = Float.parseFloat(coutStr);
        } catch (NumberFormatException e) {
            cout = 0;
        }

        float note = 0;
        try {
            note = Float.parseFloat(noteStr);
            if (note < 0 || note > 20) {
                Toast.makeText(this, "La note doit être entre 0 et 20", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            note = 0;
        }

        dataManager.modifierTacheEmploi(devoirId, nom, description, matiere, currentDeadline,
                priorite, statut, note, cout);

        Toast.makeText(this, "Devoir modifié avec succès", Toast.LENGTH_SHORT).show();
        Log.d("DetailActivity", "Devoir modifié: " + nom);

        finish();
    }

    private void confirmerSuppression() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le devoir")
                .setMessage("Voulez-vous vraiment supprimer ce devoir ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    dataManager.supprimerTacheEmploi(devoirId);
                    Toast.makeText(this, "Devoir supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DetailActivity", "onDestroy");
    }
}
