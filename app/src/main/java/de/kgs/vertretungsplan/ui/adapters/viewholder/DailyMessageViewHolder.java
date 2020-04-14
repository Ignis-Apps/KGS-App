package de.kgs.vertretungsplan.ui.adapters.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.kgs.vertretungsplan.R;

public class DailyMessageViewHolder extends RecyclerView.ViewHolder {

    private TextView title;
    private TextView body;

    public DailyMessageViewHolder(@NonNull View root) {
        super(root);
        title = root.findViewById(R.id.dailyViewTitle);
        body = root.findViewById(R.id.dailyViewMessage);
    }

    public void setMessage(String title, String message) {
        this.title.setText(title);
        this.body.setText(message);
    }

}
