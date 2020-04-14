package de.kgs.vertretungsplan.ui.adapters.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverItem;
import de.kgs.vertretungsplan.ui.interfaces.OnCoverListItemClicked;

public class CoverItemViewHolder extends RecyclerView.ViewHolder {

    private TextView tvClass;
    private TextView tvHour;
    private TextView tvSubject;
    private TextView tvRoom;
    private ImageView imageInfo;
    private LinearLayout background;

    private CoverItem representedData;

    public CoverItemViewHolder(@NonNull View root, OnCoverListItemClicked callback) {
        super(root);
        tvClass = root.findViewById(R.id.textViewKlasse);
        tvHour = root.findViewById(R.id.textViewStunde);
        tvSubject = root.findViewById(R.id.textViewFach);
        tvRoom = root.findViewById(R.id.textViewRaum);
        imageInfo = root.findViewById(R.id.imageViewInfo);
        background = root.findViewById(R.id.listViewLn);

        root.setOnClickListener(view -> callback.onCoverItemClicked(representedData));
    }

    public void setData(CoverItem item) {

        boolean hasAdditionalInformation = !(item.getAnnotation() + item.getRelocated()).isEmpty();
        imageInfo.setVisibility((hasAdditionalInformation) ? View.VISIBLE : View.INVISIBLE);
        tvClass.setText(item.getTargetClass());
        tvHour.setText(item.getHour());
        tvSubject.setText(item.getSubject());
        tvRoom.setText(item.getRoom());

        if (item.isCanceled()) {
            setTheme(Color.parseColor("#ffffff"), Color.parseColor("#ff2419"), R.drawable.ic_action_info_light);
        } else {
            setTheme(Color.parseColor("#000000"), 0, R.drawable.ic_action_info);
        }

        representedData = item;
    }

    private void setTheme(int textColor, int backgroundColor, int imageRes) {
        background.setBackgroundColor(backgroundColor);
        tvClass.setTextColor(textColor);
        tvHour.setTextColor(textColor);
        tvSubject.setTextColor(textColor);
        tvRoom.setTextColor(textColor);
        imageInfo.setImageResource(imageRes);
    }

}
