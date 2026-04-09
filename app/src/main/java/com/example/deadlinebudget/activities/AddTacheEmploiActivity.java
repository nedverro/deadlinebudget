package com.example.deadlinebudget.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.deadlinebudget.R;
import com.example.deadlinebudget.TaskReminderManager;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.TacheEmploi;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTacheEmploiActivity extends AppCompatActivity {

    private TextInputEditText etNom, etMatiere, etDescription;
    private TextView tvSelectedDate;
    private long selectedTimestamp = 0;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tache_emploi);

        dataManager = DataManager.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNom = findViewById(R.id.etNom);
        etMatiere = findViewById(R.id.etMatiere);
        etDescription = findViewById(R.id.etDescription);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        Button btnDatePicker = findViewById(R.id.btnDatePicker);
        Button btnSave = findViewById(R.id.btnSave);

        btnDatePicker.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String matiere = etMatiere.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir un nom", Toast.LENGTH_SHORT).show();
                return;
            }

            if (matiere.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir une matière", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimestamp == 0) {
                Toast.makeText(this, "Veuillez choisir une date", Toast.LENGTH_SHORT).show();
                return;
            }

            dataManager.ajouterTacheEmploi(nom, description, matiere, selectedTimestamp, 3, 0, 0.0f);
            
            // Programmez les rappels pour le nouveau devoir
            List<TacheEmploi> all = dataManager.getAllTacheEmplois();
            if (!all.isEmpty()) {
                TacheEmploi lastDevoir = all.get(all.size() - 1); // Approximation pour récupérer le dernier ajouté
                TaskReminderManager.scheduleRemindersForDevoir(this, lastDevoir);
            }

            Toast.makeText(this, "Devoir ajouté avec succès", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year1, monthOfYear, dayOfMonth);
                    selectedTimestamp = selectedCal.getTimeInMillis();
                    tvSelectedDate.setText(String.format(Locale.FRANCE, "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1));
                }, year, month, day);
        datePickerDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
