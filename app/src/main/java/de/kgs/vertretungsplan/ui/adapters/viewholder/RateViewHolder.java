package de.kgs.vertretungsplan.ui.adapters.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.kgs.vertretungsplan.ui.interfaces.OnCoverListItemClicked;

public class RateViewHolder extends RecyclerView.ViewHolder {

    public RateViewHolder(@NonNull View itemView, OnCoverListItemClicked callback) {
        super(itemView);
        itemView.setOnClickListener(view -> callback.onRateItemClicked());
    }
}
