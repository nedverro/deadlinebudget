package com.example.deadlinebudget.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinebudget.R;

public class DepenseViewHolder extends RecyclerView.ViewHolder {
    public TextView textViewDescription;
    public TextView textViewCategorie;
    public TextView textViewDate;
    public TextView textViewMontant;

    public DepenseViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewDescription = itemView.findViewById(R.id.textViewDescription);
        textViewCategorie = itemView.findViewById(R.id.textViewCategorie);
        textViewDate = itemView.findViewById(R.id.textViewDate);
        textViewMontant = itemView.findViewById(R.id.textViewMontant);
    }
}