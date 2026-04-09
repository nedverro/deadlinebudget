package com.example.deadlinebudget.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.adapters.DepenseAdapter;
import com.example.deadlinebudget.data.DataManager;
import com.example.deadlinebudget.models.Depense;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetFragment extends Fragment implements DepenseAdapter.OnDepenseClickListener {

    private DataManager dataManager;
    private RecyclerView recyclerView;
    private DepenseAdapter adapter;
    private TextView tvBudgetStatus;
    private LinearProgressIndicator budgetProgress;
    private float monthlyBudget;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        dataManager = DataManager.getInstance();

        tvBudgetStatus = view.findViewById(R.id.tvBudgetStatus);
        budgetProgress = view.findViewById(R.id.budgetProgress);
        recyclerView = view.findViewById(R.id.recyclerViewDepenses);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DepenseAdapter(this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnSetBudget).setOnClickListener(v -> showSetBudgetDialog());
        
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddExpense);
        fab.setOnClickListener(v -> showExpenseDialog(null));

        updateUI();
        return view;
    }

    private void updateUI() {
        monthlyBudget = dataManager.getMonthlyBudget(requireContext());
        float spent = dataManager.getDepensesMoisActuel();
        
        tvBudgetStatus.setText(String.format(Locale.FRANCE, "%.2f / %.2f DH", spent, monthlyBudget));
        
        if (monthlyBudget > 0) {
            int progress = (int) ((spent / monthlyBudget) * 100);
            budgetProgress.setProgress(Math.min(progress, 100));
            
            if (progress >= 100) {
                budgetProgress.setIndicatorColor(getResources().getColor(R.color.error));
                Toast.makeText(getContext(), R.string.msg_budget_alert_100, Toast.LENGTH_SHORT).show();
            } else if (progress >= 80) {
                budgetProgress.setIndicatorColor(getResources().getColor(R.color.warning));
                Toast.makeText(getContext(), R.string.msg_budget_alert_80, Toast.LENGTH_SHORT).show();
            } else {
                budgetProgress.setIndicatorColor(getResources().getColor(R.color.primary));
            }
        }
        
        adapter.setDepenses(dataManager.getAllDepenses());
    }

    private void showSetBudgetDialog() {
        EditText etAmount = new EditText(getContext());
        etAmount.setHint(R.string.hint_amount);
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setText(String.valueOf(monthlyBudget));

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.label_monthly_budget)
                .setView(etAmount)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String input = etAmount.getText().toString();
                    if (!input.isEmpty()) {
                        dataManager.setMonthlyBudget(requireContext(), Float.parseFloat(input));
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showExpenseDialog(@Nullable Depense depense) {
        boolean isEdit = depense != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_depense, null);

        EditText etDesc = view.findViewById(R.id.etExpenseDescription);
        EditText etAmount = view.findViewById(R.id.etExpenseAmount);
        Spinner spCat = view.findViewById(R.id.spExpenseCategory);
        EditText etDate = view.findViewById(R.id.etExpenseDate);

        String[] categories = {"Nourriture", "Transport", "Loisirs", "Études", "Logement", "Scolaire"};
        spCat.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, categories));

        final Calendar calendar = Calendar.getInstance();
        if (isEdit) {
            etDesc.setText(depense.getDescription());
            etAmount.setText(String.valueOf(depense.getMontant()));
            calendar.setTimeInMillis(depense.getDate());
            for (int i=0; i<categories.length; i++) {
                if (categories[i].equals(depense.getCategorie())) {
                    spCat.setSelection(i);
                    break;
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        etDate.setText(sdf.format(calendar.getTime()));

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view)
                .setTitle(isEdit ? R.string.title_edit_expense : R.string.title_add_expense)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String desc = etDesc.getText().toString().trim();
                    String amountStr = etAmount.getText().toString().trim();
                    if (desc.isEmpty() || amountStr.isEmpty()) return;

                    float amount = Float.parseFloat(amountStr);
                    String category = spCat.getSelectedItem().toString();
                    long date = calendar.getTimeInMillis();

                    if (isEdit) {
                        dataManager.modifierDepense(depense.getId(), desc, category, amount, date);
                        Toast.makeText(getContext(), "Dépense modifiée", Toast.LENGTH_SHORT).show();
                    } else {
                        dataManager.ajouterDepense(desc, category, amount, date);
                        Toast.makeText(getContext(), R.string.msg_expense_added, Toast.LENGTH_SHORT).show();
                    }
                    updateUI();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onDepenseClick(Depense depense) {
        showExpenseDialog(depense);
    }

    @Override
    public void onDepenseLongClick(Depense depense) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    dataManager.supprimerDepense(depense.getId());
                    updateUI();
                    Toast.makeText(getContext(), "Dépense supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}