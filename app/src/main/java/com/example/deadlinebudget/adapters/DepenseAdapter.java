package com.example.deadlinebudget.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.models.Depense;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DepenseAdapter extends RecyclerView.Adapter<DepenseAdapter.DepenseViewHolder> {

    private List<Depense> depenses = new ArrayList<>();
    private OnDepenseClickListener listener;

    public interface OnDepenseClickListener {
        void onDepenseClick(Depense depense);
        void onDepenseLongClick(Depense depense);
    }

    public DepenseAdapter(OnDepenseClickListener listener) {
        this.listener = listener;
    }

    public void setDepenses(List<Depense> depenses) {
        this.depenses = depenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DepenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_depense, parent, false);
        return new DepenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepenseViewHolder holder, int position) {
        Depense depense = depenses.get(position);
        holder.tvDescription.setText(depense.getDescription());
        holder.tvCategorie.setText(depense.getCategorie());
        holder.tvMontant.setText(String.format(Locale.FRANCE, "%.2f DH", depense.getMontant()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        holder.tvDate.setText(sdf.format(new Date(depense.getDate())));

        // Définir l'icône selon la catégorie
        int iconRes = R.drawable.ic_settings; // Défaut
        String cat = depense.getCategorie();
        if (cat != null) {
            switch (cat) {
                case "Nourriture": iconRes = R.drawable.nourriture; break;
                case "Transport": iconRes = R.drawable.transport; break;
                case "Loisirs": iconRes = R.drawable.loisirs; break;
                case "Études": iconRes = R.drawable.etudes; break;
                case "Logement": iconRes = R.drawable.logement; break;
                case "Scolaire": iconRes = R.drawable.scolaire; break;
            }
        }
        holder.ivCategoryIcon.setImageResource(iconRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDepenseClick(depense);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDepenseLongClick(depense);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return depenses.size();
    }

    static class DepenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCategorie, tvMontant, tvDate;
        ImageView ivCategoryIcon;

        public DepenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.textViewDescription);
            tvCategorie = itemView.findViewById(R.id.textViewCategorie);
            tvMontant = itemView.findViewById(R.id.textViewMontant);
            tvDate = itemView.findViewById(R.id.textViewDate);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}