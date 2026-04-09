package com.example.deadlinebudget.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.adapters.ScheduleAdapter;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Cours;
import com.example.deadlinebudget.models.TacheEmploi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements ScheduleAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private DataManager dataManager;
    private TextView tvCurrentWeek;
    private Calendar currentWeekCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.FRANCE);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        dataManager = DataManager.getInstance();

        recyclerView = view.findViewById(R.id.rvSchedule);
        tvCurrentWeek = view.findViewById(R.id.tvCurrentWeek);
        MaterialButton btnPrev = view.findViewById(R.id.btnPrevWeek);
        MaterialButton btnNext = view.findViewById(R.id.btnNextWeek);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapter(this);
        recyclerView.setAdapter(adapter);

        currentWeekCalendar = Calendar.getInstance();
        currentWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        updateWeekDisplay();

        btnPrev.setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            updateWeekDisplay();
        });

        btnNext.setOnClickListener(v -> {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            updateWeekDisplay();
        });

        FloatingActionButton fab = view.findViewById(R.id.fabAddCours);
        fab.setOnClickListener(v -> showCoursDialog(null));

        return view;
    }

    private void updateWeekDisplay() {
        Calendar first = (Calendar) currentWeekCalendar.clone();
        first.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        Calendar last = (Calendar) first.clone();
        last.add(Calendar.DAY_OF_WEEK, 6);

        String range = "Semaine du " + dateFormat.format(first.getTime()) + " au " + dateFormat.format(last.getTime());
        tvCurrentWeek.setText(range);

        loadScheduleData(first.getTimeInMillis(), last.getTimeInMillis());
    }

    private String getWeekIdentifier(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int year = c.get(Calendar.YEAR);
        int week = c.get(Calendar.WEEK_OF_YEAR);
        if (week == 1 && c.get(Calendar.MONTH) == Calendar.DECEMBER) year++;
        return year + "-W" + String.format(Locale.US, "%02d", week);
    }

    private void loadScheduleData(long startMs, long endMs) {
        String weekId = getWeekIdentifier(currentWeekCalendar);
        List<Cours> weekCours = dataManager.getCoursByWeek(weekId);
        
        List<TacheEmploi> allDevoirs = dataManager.getAllTacheEmplois();
        List<TacheEmploi> weekDevoirs = new ArrayList<>();
        for (TacheEmploi d : allDevoirs) {
            if (d.getDeadlineTimestamp() >= startMs && d.getDeadlineTimestamp() <= endMs) {
                weekDevoirs.add(d);
            }
        }

        adapter.setData(weekCours, weekDevoirs);
    }

    private void showCoursDialog(@Nullable Cours existingCours) {
        boolean isEdit = existingCours != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_cours, null);

        EditText etNom = view.findViewById(R.id.etNomCours);
        EditText etSalle = view.findViewById(R.id.etSalle);
        Spinner spinnerJour = view.findViewById(R.id.spinnerJour);
        Button btnHeureDeb = view.findViewById(R.id.btnHeureDeb);
        Button btnHeureFin = view.findViewById(R.id.btnHeureFin);

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        spinnerJour.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, jours));

        final int[] hDeb = {isEdit ? existingCours.getHeureDebut() : 8};
        final int[] mDeb = {isEdit ? existingCours.getMinuteDebut() : 0};
        final int[] hFin = {isEdit ? existingCours.getHeureFin() : 10};
        final int[] mFin = {isEdit ? existingCours.getMinuteFin() : 0};

        if (isEdit) {
            etNom.setText(existingCours.getNom());
            etSalle.setText(existingCours.getSalle());
            for (int i = 0; i < jours.length; i++) {
                if (jours[i].equalsIgnoreCase(existingCours.getJour())) {
                    spinnerJour.setSelection(i);
                    break;
                }
            }
        }
        btnHeureDeb.setText(String.format(Locale.FRANCE, "%02d:%02d", hDeb[0], mDeb[0]));
        btnHeureFin.setText(String.format(Locale.FRANCE, "%02d:%02d", hFin[0], mFin[0]));

        btnHeureDeb.setOnClickListener(v -> new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
            hDeb[0] = hourOfDay; mDeb[0] = minute;
            btnHeureDeb.setText(String.format(Locale.FRANCE, "%02d:%02d", hourOfDay, minute));
        }, hDeb[0], mDeb[0], true).show());

        btnHeureFin.setOnClickListener(v -> new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
            hFin[0] = hourOfDay; mFin[0] = minute;
            btnHeureFin.setText(String.format(Locale.FRANCE, "%02d:%02d", hourOfDay, minute));
        }, hFin[0], mFin[0], true).show());

        builder.setView(view)
                .setTitle(isEdit ? "Modifier le cours" : "Ajouter un cours")
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String nom = etNom.getText().toString().trim();
                    String salle = etSalle.getText().toString().trim();
                    String jour = spinnerJour.getSelectedItem().toString();
                    if (nom.isEmpty()) return;

                    String weekId = getWeekIdentifier(currentWeekCalendar);
                    if (isEdit) {
                        dataManager.modifierCours(existingCours.getId(), nom, salle, jour, hDeb[0], mDeb[0], hFin[0], mFin[0], weekId);
                    } else {
                        dataManager.ajouterCours(nom, salle, jour, hDeb[0], mDeb[0], hFin[0], mFin[0], weekId);
                    }
                    updateWeekDisplay();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onItemClick(Cours cours) {
        showCoursDialog(cours);
    }

    @Override
    public void onItemLongClick(Cours cours) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dataManager.supprimerCours(cours.getId());
                    updateWeekDisplay();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
