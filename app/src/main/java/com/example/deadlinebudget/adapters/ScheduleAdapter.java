package com.example.deadlinebudget.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinebudget.R;
import com.example.deadlinebudget.models.Cours;
import com.example.deadlinebudget.models.TacheEmploi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM_COURS = 1;
    private static final int TYPE_ITEM_DEVOIR = 2;

    private List<Object> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cours cours);
        void onItemLongClick(Cours cours);
    }

    public ScheduleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Cours> coursList, List<TacheEmploi> devoirList) {
        items.clear();
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        
        for (String jour : jours) {
            List<Cours> dayCours = new ArrayList<>();
            for (Cours c : coursList) {
                if (jour.equalsIgnoreCase(c.getJour())) dayCours.add(c);
            }
            // Trier les cours par heure
            Collections.sort(dayCours, (c1, c2) -> {
                if (c1.getHeureDebut() != c2.getHeureDebut()) 
                    return Integer.compare(c1.getHeureDebut(), c2.getHeureDebut());
                return Integer.compare(c1.getMinuteDebut(), c2.getMinuteDebut());
            });

            List<TacheEmploi> dayDevoirs = new ArrayList<>();
            for (TacheEmploi d : devoirList) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(d.getDeadlineTimestamp());
                String dayOfDevoir = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.LONG, Locale.FRANCE);
                if (jour.equalsIgnoreCase(dayOfDevoir)) dayDevoirs.add(d);
            }
            // Trier les devoirs par urgence (couleur) puis par priorité (décroissante)
            Collections.sort(dayDevoirs, (d1, d2) -> {
                int c1 = getUrgencyLevel(d1);
                int c2 = getUrgencyLevel(d2);
                if (c1 != c2) return Integer.compare(c1, c2); // Urgence d'abord
                return Float.compare(d2.getPriorite(), d1.getPriorite()); // Priorité 4 avant 2
            });

            items.add(jour);
            items.addAll(dayCours);
            items.addAll(dayDevoirs);
        }
        notifyDataSetChanged();
    }

    private int getUrgencyLevel(TacheEmploi d) {
        long diff = d.getDeadlineTimestamp() - System.currentTimeMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        if (diff < 0) return 0; // Noir
        if (days <= 1) return 1; // Rouge
        if (days <= 3) return 2; // Orange
        if (days <= 5) return 3; // Jaune
        if (days <= 7) return 4; // Jaune-Vert
        return 5; // Vert
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) return TYPE_HEADER;
        if (item instanceof Cours) return TYPE_ITEM_COURS;
        return TYPE_ITEM_DEVOIR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_ITEM_COURS) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cours, parent, false);
            return new ItemCoursViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tache_emploi, parent, false);
            return new ItemDevoirViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvDayName.setText((String) items.get(position));
        } else if (holder instanceof ItemCoursViewHolder) {
            Cours c = (Cours) items.get(position);
            ItemCoursViewHolder itemHolder = (ItemCoursViewHolder) holder;
            itemHolder.tvNom.setText(c.getNom());
            itemHolder.tvSalle.setText(c.getSalle());
            itemHolder.tvHeureDeb.setText(String.format(Locale.FRANCE, "%02d:%02d", c.getHeureDebut(), c.getMinuteDebut()));
            itemHolder.tvHeureFin.setText(String.format(Locale.FRANCE, "%02d:%02d", c.getHeureFin(), c.getMinuteFin()));

            itemHolder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(c); });
            itemHolder.itemView.setOnLongClickListener(v -> { if (listener != null) listener.onItemLongClick(c); return true; });
        } else if (holder instanceof ItemDevoirViewHolder) {
            TacheEmploi d = (TacheEmploi) items.get(position);
            ItemDevoirViewHolder itemHolder = (ItemDevoirViewHolder) holder;
            itemHolder.tvNom.setText(d.getNom());
            itemHolder.tvMatiere.setText(d.getMatiere());
            
            // Appliquer la couleur d'urgence
            int level = getUrgencyLevel(d);
            int color = Color.GREEN;
            if (level == 0) color = Color.BLACK;
            else if (level == 1) color = Color.RED;
            else if (level == 2) color = Color.parseColor("#FFA500");
            else if (level == 3) color = Color.YELLOW;
            else if (level == 4) color = Color.parseColor("#ADFF2F");
            
            if (itemHolder.priorityIndicator != null) {
                itemHolder.priorityIndicator.setBackgroundColor(color);
            }

            if (itemHolder.cbDone != null) {
                itemHolder.cbDone.setChecked(d.getStatut() == 2);
                itemHolder.cbDone.setEnabled(false);
            }
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        HeaderViewHolder(View v) { super(v); tvDayName = v.findViewById(R.id.tvDayName); }
    }

    static class ItemCoursViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeureDeb, tvHeureFin, tvNom, tvSalle;
        ItemCoursViewHolder(View v) {
            super(v);
            tvHeureDeb = v.findViewById(R.id.tvHeureDeb);
            tvHeureFin = v.findViewById(R.id.tvHeureFin);
            tvNom = v.findViewById(R.id.tvNomCours);
            tvSalle = v.findViewById(R.id.tvSalle);
        }
    }

    static class ItemDevoirViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom, tvMatiere;
        android.widget.CheckBox cbDone;
        View priorityIndicator;
        ItemDevoirViewHolder(View v) {
            super(v);
            tvNom = v.findViewById(R.id.nom);
            tvMatiere = v.findViewById(R.id.matiere);
            cbDone = v.findViewById(R.id.cbDone);
            priorityIndicator = v.findViewById(R.id.priorityIndicator);
        }
    }
}
