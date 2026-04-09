package com.example.deadlinebudget.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.NotificationHelper;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.TaskReminderManager;
import com.example.deadlinebudget.adapters.TaskAdapter;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Tache;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DataManager dataManager;
    private TabLayout tabLayout;
    private Spinner spinnerFilterCategory;
    private boolean showingCompleted = false;
    private String currentCategoryFilter = "Toutes";

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadTasks();
            refreshHandler.postDelayed(this, 60000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        dataManager = DataManager.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        tabLayout = view.findViewById(R.id.tabLayoutTasks);
        spinnerFilterCategory = view.findViewById(R.id.spinnerFilterTaskCategory);
        
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddTask);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        setupFilter();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingCompleted = tab.getPosition() == 1;
                loadTasks();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fab.setOnClickListener(v -> showTaskDialog(null));

        loadTasks();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void setupFilter() {
        List<String> categories = dataManager.getTaskCategories(requireContext());
        categories.add(0, "Toutes");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(filterAdapter);

        spinnerFilterCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategoryFilter = categories.get(position);
                loadTasks();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadTasks() {
        if (adapter != null) {
            adapter.setTasks(dataManager.getTachesByCategorie(currentCategoryFilter, showingCompleted));
        }
    }

    private void showTaskDialog(@Nullable Tache tache) {
        boolean isEdit = tache != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText etTitle = view.findViewById(R.id.etTaskTitle);
        EditText etDesc = view.findViewById(R.id.etTaskDescription);
        Spinner spCategory = view.findViewById(R.id.spTaskCategory);
        RatingBar rbPriority = view.findViewById(R.id.rbTaskPriorityInput);
        EditText etDate = view.findViewById(R.id.etTaskDate);
        EditText etTime = view.findViewById(R.id.etTaskTime);
        MaterialButton btnAddCat = view.findViewById(R.id.btnAddCategory);
        MaterialButton btnDelCat = view.findViewById(R.id.btnDeleteCategory);

        updateCategorySpinner(spCategory, isEdit ? tache.getCategorie() : null);

        btnAddCat.setOnClickListener(v -> {
            EditText etNewCat = new EditText(requireContext());
            new AlertDialog.Builder(requireContext())
                    .setTitle("Nouvelle catégorie")
                    .setView(etNewCat)
                    .setPositiveButton("Ajouter", (d, w) -> {
                        String newCat = etNewCat.getText().toString().trim();
                        if (!newCat.isEmpty()) {
                            dataManager.addTaskCategory(requireContext(), newCat);
                            updateCategorySpinner(spCategory, newCat);
                            refreshFilter();
                        }
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        btnDelCat.setOnClickListener(v -> {
            String selected = spCategory.getSelectedItem().toString();
            new AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer la catégorie")
                    .setMessage("Voulez-vous supprimer '" + selected + "' ?")
                    .setPositiveButton("Oui", (d, w) -> {
                        dataManager.removeTaskCategory(requireContext(), selected);
                        updateCategorySpinner(spCategory, null);
                        refreshFilter();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        if (isEdit) {
            etTitle.setText(tache.getTitre());
            etDesc.setText(tache.getDescription());
            rbPriority.setRating(tache.getPriorite());
            calendar.setTimeInMillis(tache.getDateLimite());
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
        }
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        
        etDate.setText(sdfDate.format(calendar.getTime()));
        if (isEdit) {
            etTime.setText(sdfTime.format(calendar.getTime()));
        }

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDate.setText(sdfDate.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                etTime.setText(sdfTime.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        builder.setView(view)
                .setTitle(isEdit ? R.string.title_edit_task : R.string.title_add_task)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDesc.getText().toString().trim();
                    if (title.isEmpty()) return;

                    String category = spCategory.getSelectedItem().toString();

                    if (isEdit) {
                        dataManager.modifierTache(tache.getId(), title, description, category, 
                                (int)rbPriority.getRating(), calendar.getTimeInMillis(), tache.isEstTerminee());
                        Tache updatedTache = dataManager.getTacheById(tache.getId());
                        TaskReminderManager.scheduleRemindersForTask(requireContext(), updatedTache);
                        Toast.makeText(getContext(), R.string.msg_task_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        Tache newTache = dataManager.ajouterTache(title, description, category, 
                                (int)rbPriority.getRating(), calendar.getTimeInMillis());
                        TaskReminderManager.scheduleRemindersForTask(requireContext(), newTache);
                        Toast.makeText(getContext(), R.string.msg_task_added, Toast.LENGTH_SHORT).show();
                    }
                    loadTasks();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void updateCategorySpinner(Spinner spinner, String selection) {
        List<String> categories = dataManager.getTaskCategories(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adapter);
        if (selection != null) {
            int pos = categories.indexOf(selection);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    private void refreshFilter() {
        List<String> categories = dataManager.getTaskCategories(requireContext());
        categories.add(0, "Toutes");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCategory.setAdapter(filterAdapter);
    }

    @Override
    public void onTaskClick(Tache tache) {
        showTaskDialog(tache);
    }

    @Override
    public void onTaskStatusChanged(Tache tache, boolean isChecked) {
        dataManager.modifierTache(tache.getId(), tache.getTitre(), tache.getDescription(), tache.getCategorie(),
                tache.getPriorite(), tache.getDateLimite(), isChecked);
        if (isChecked) {
            TaskReminderManager.cancelRemindersForTask(requireContext(), tache.getId(), true);
            NotificationHelper.cancelAllNotificationsForTask(requireContext(), tache.getId());
            // Notification immédiate
            NotificationHelper.showDeadlineNotification(requireContext(), "Tâche terminée !", "Félicitations, vous avez fini : " + tache.getTitre(), tache.getId());
        } else {
            TaskReminderManager.scheduleRemindersForTask(requireContext(), tache);
            // Notification immédiate
            NotificationHelper.showDeadlineNotification(requireContext(), "Tâche réactivée", "La tâche '" + tache.getTitre() + "' est de nouveau active.", tache.getId());
        }
        loadTasks();
    }

    @Override
    public void onTaskLongClick(Tache tache) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    TaskReminderManager.cancelRemindersForTask(requireContext(), tache.getId(), true);
                    NotificationHelper.cancelAllNotificationsForTask(requireContext(), tache.getId());
                    dataManager.supprimerTache(tache.getId());
                    loadTasks();
                    Toast.makeText(getContext(), R.string.msg_task_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}