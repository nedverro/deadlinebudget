package com.example.deadlinebudget.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.deadlinebudget.R;
import com.example.deadlinebudget.models.Note;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitre());
        holder.tvCategory.setText(note.getCategorie());
        holder.tvContent.setText(note.getContenu());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        holder.tvDate.setText(sdf.format(new Date(note.getDateCreation())));

        if (note.getCouleur() != null) {
            try {
                holder.cardView.setCardBackgroundColor(Color.parseColor(note.getCouleur()));
            } catch (Exception e) {
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onNoteLongClick(note);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvTitle, tvCategory, tvContent, tvDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvCategory = itemView.findViewById(R.id.tvNoteCategory);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
        }
    }
}