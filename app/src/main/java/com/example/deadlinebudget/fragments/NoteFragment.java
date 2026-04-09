package com.example.deadlinebudget.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.adapters.NoteAdapter;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Note;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteFragment extends Fragment implements NoteAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private DataManager dataManager;
    private SearchView searchView;
    private Spinner spinnerFilter;
    private String currentFilter = "Toutes";
    private Map<String, String> categoryColors = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        dataManager = DataManager.getInstance();

        initCategoryColors();

        recyclerView = view.findViewById(R.id.recyclerViewNotes);
        searchView = view.findViewById(R.id.searchViewNotes);
        spinnerFilter = view.findViewById(R.id.spinnerFilterNoteCategory);
        FloatingActionButton fab = view.findViewById(R.id.fabAddNote);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NoteAdapter(this);
        recyclerView.setAdapter(adapter);

        setupFilter();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) loadNotes();
                else adapter.setNotes(dataManager.rechercherNotes(newText));
                return true;
            }
        });

        fab.setOnClickListener(v -> showNoteDialog(null));

        loadNotes();
        return view;
    }

    private void initCategoryColors() {
        categoryColors.put("Cours", "#E1F5FE"); // Bleu clair
        categoryColors.put("Idées", "#FFF9C4"); // Jaune clair
        categoryColors.put("Rappel", "#F8BBD0"); // Rose clair
        categoryColors.put("Important", "#FFEBEE"); // Rouge clair
    }

    private String getColorForCategory(String category) {
        String color = categoryColors.get(category);
        return color != null ? color : "#FFFFFF"; // Blanc par défaut
    }

    private void setupFilter() {
        List<String> categories = dataManager.getNoteCategories(requireContext());
        categories.add(0, "Toutes");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = categories.get(position);
                loadNotes();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadNotes() {
        adapter.setNotes(dataManager.getNotesByCategorie(currentFilter));
    }

    private void showNoteDialog(@Nullable Note note) {
        boolean isEdit = note != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_note, null);

        EditText etTitle = view.findViewById(R.id.etNoteTitle);
        EditText etContent = view.findViewById(R.id.etNoteContent);
        Spinner spCategory = view.findViewById(R.id.spNoteCategory);
        // On cache ou supprime le spinner de couleur car on utilise les couleurs par catégorie
        View colorLabel = view.findViewById(R.id.tvColorLabel);
        Spinner spColor = view.findViewById(R.id.spNoteColor);
        if (colorLabel != null) colorLabel.setVisibility(View.GONE);
        if (spColor != null) spColor.setVisibility(View.GONE);

        MaterialButton btnAddCat = view.findViewById(R.id.btnAddNoteCategory);
        MaterialButton btnDelCat = view.findViewById(R.id.btnDeleteNoteCategory);

        updateCategorySpinner(spCategory, isEdit ? note.getCategorie() : null);

        btnAddCat.setOnClickListener(v -> {
            EditText etNewCat = new EditText(requireContext());
            new AlertDialog.Builder(requireContext())
                    .setTitle("Nouvelle catégorie")
                    .setView(etNewCat)
                    .setPositiveButton("Ajouter", (d, w) -> {
                        String newCat = etNewCat.getText().toString().trim();
                        if (!newCat.isEmpty()) {
                            dataManager.addNoteCategory(requireContext(), newCat);
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
                        dataManager.removeNoteCategory(requireContext(), selected);
                        updateCategorySpinner(spCategory, null);
                        refreshFilter();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });

        if (isEdit) {
            etTitle.setText(note.getTitre());
            etContent.setText(note.getContenu());
        }

        builder.setView(view)
                .setTitle(isEdit ? R.string.title_edit_note : R.string.title_add_note)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty()) return;

                    String category = spCategory.getSelectedItem().toString();
                    String color = getColorForCategory(category);

                    if (isEdit) {
                        dataManager.modifierNote(note.getId(), title, content, category, color);
                        Toast.makeText(getContext(), R.string.msg_note_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.ajouterNote(title, content, category, color);
                        Toast.makeText(getContext(), R.string.msg_note_added, Toast.LENGTH_SHORT).show();
                    }
                    loadNotes();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void updateCategorySpinner(Spinner spinner, String selection) {
        List<String> categories = dataManager.getNoteCategories(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adapter);
        if (selection != null) {
            int pos = categories.indexOf(selection);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    private void refreshFilter() {
        List<String> categories = dataManager.getNoteCategories(requireContext());
        categories.add(0, "Toutes");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);
    }

    @Override
    public void onNoteClick(Note note) {
        showNoteDialog(note);
    }

    @Override
    public void onNoteLongClick(Note note) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dataManager.supprimerNote(note.getId());
                    loadNotes();
                    Toast.makeText(getContext(), R.string.msg_note_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}