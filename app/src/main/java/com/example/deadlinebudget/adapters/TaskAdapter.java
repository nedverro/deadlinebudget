package com.example.deadlinebudget.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.models.Tache;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Tache> tasks = new ArrayList<>();
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Tache tache);
        void onTaskStatusChanged(Tache tache, boolean isChecked);
        void onTaskLongClick(Tache tache);
    }

    public TaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Tache> tasks) {
        this.tasks = new ArrayList<>(tasks);
        sortTasks();
        notifyDataSetChanged();
    }

    private void sortTasks() {
        Collections.sort(this.tasks, (t1, t2) -> {
            if (t1.isEstTerminee() != t2.isEstTerminee()) {
                return t1.isEstTerminee() ? 1 : -1;
            }
            // Pour les tâches non terminées, les plus urgentes en premier
            return Long.compare(t1.getDateLimite(), t2.getDateLimite());
        });
    }

    private int getColorForDeadline(long deadlineTimestamp) {
        long now = System.currentTimeMillis();
        long diffInMillis = deadlineTimestamp - now;
        
        if (diffInMillis < 0) {
            return Color.parseColor("#9C27B0"); // Violet pour "EN RETARD"
        }
        
        long minutesRemaining = diffInMillis / (1000 * 60);
        long hoursRemaining = minutesRemaining / 60;
        long daysRemaining = hoursRemaining / 24;
        
        if (minutesRemaining <= 60) { // Moins d'1 heure
            return Color.parseColor("#D32F2F"); // Rouge foncé
        } else if (hoursRemaining <= 24) { // Moins d'1 jour
            return Color.parseColor("#F44336"); // Rouge
        } else if (daysRemaining <= 2) { // 2 jours ou moins
            return Color.parseColor("#FF9800"); // Orange
        } else if (daysRemaining <= 5) { // 5 jours ou moins
            return Color.parseColor("#FFC107"); // Jaune
        } else {
            return Color.parseColor("#4CAF50"); // Vert
        }
    }

    private String getTimeRemainingText(long deadlineTimestamp) {
        long now = System.currentTimeMillis();
        long diffInMillis = deadlineTimestamp - now;
        
        if (diffInMillis < 0) {
            return "⏰ EN RETARD !";
        }
        
        long minutesRemaining = diffInMillis / (1000 * 60);
        long hoursRemaining = minutesRemaining / 60;
        long daysRemaining = hoursRemaining / 24;
        
        if (minutesRemaining < 60) {
            return "⚠️ " + minutesRemaining + " min restantes";
        } else if (hoursRemaining < 24) {
            return "⚠️ " + hoursRemaining + " h restantes";
        } else {
            return "📅 " + daysRemaining + " j restants";
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Tache tache = tasks.get(position);
        holder.tvTitle.setText(tache.getTitre());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        String details = tache.getCategorie() + " • " + sdf.format(new Date(tache.getDateLimite()));
        holder.tvDetails.setText(details);
        
        holder.rbPriority.setRating(tache.getPriorite());
        holder.cbStatus.setChecked(tache.isEstTerminee());

        int color;
        if (tache.isEstTerminee()) {
            color = Color.parseColor("#BDBDBD"); // Gris
            holder.tvLateIndicator.setVisibility(View.GONE);
        } else {
            color = getColorForDeadline(tache.getDateLimite());
            String timeText = getTimeRemainingText(tache.getDateLimite());
            holder.tvLateIndicator.setText(" " + timeText);
            holder.tvLateIndicator.setVisibility(View.VISIBLE);
            
            // Couleur spécifique pour l'indicateur de retard
            if (tache.getDateLimite() < System.currentTimeMillis()) {
                holder.tvLateIndicator.setTextColor(Color.parseColor("#9C27B0"));
            } else {
                holder.tvLateIndicator.setTextColor(Color.parseColor("#F44336"));
            }
        }
        
        holder.viewDeadlineColor.setBackgroundColor(color);

        if (tache.isEstTerminee()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDetails.setAlpha(0.5f);
            holder.viewDeadlineColor.setAlpha(0.5f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1.0f);
            holder.tvDetails.setAlpha(1.0f);
            holder.viewDeadlineColor.setAlpha(1.0f);
        }

        holder.cbStatus.setOnClickListener(v -> {
            if (listener != null) listener.onTaskStatusChanged(tache, holder.cbStatus.isChecked());
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(tache);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onTaskLongClick(tache);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvLateIndicator;
        CheckBox cbStatus;
        RatingBar rbPriority;
        View viewDeadlineColor;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDetails = itemView.findViewById(R.id.tvTaskDetails);
            tvLateIndicator = itemView.findViewById(R.id.tvLateIndicator);
            cbStatus = itemView.findViewById(R.id.cbTaskStatus);
            rbPriority = itemView.findViewById(R.id.rbTaskPriority);
            viewDeadlineColor = itemView.findViewById(R.id.viewDeadlineColor);
        }
    }
}