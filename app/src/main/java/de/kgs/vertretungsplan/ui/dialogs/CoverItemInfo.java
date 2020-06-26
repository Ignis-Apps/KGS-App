package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverplan.CoverItem;

public final class CoverItemInfo {

    public static void showDialog(Context context, CoverItem coverItem) {

        Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle(R.string.dialog_cover_item_title);
        alertBuilder.setIcon((R.drawable.ic_action_info));
        alertBuilder.setPositiveButton(R.string.dialog_cover_item_positive, (dialogInterface, i) -> dialogInterface.cancel());
        alertBuilder.setView(getDialogView(context, coverItem));
        alertBuilder.create();
        alertBuilder.show();
    }

    private static View getDialogView(Context context, CoverItem coverItem) {

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout linearLayout = new LinearLayout(context);
        View root = inflater.inflate(R.layout.dialog_item_view, linearLayout, false);

        // Class
        applyText(root, R.id.tv_class_title, R.id.tv_class_content, coverItem.getTargetClass());
        // Hour
        applyText(root, R.id.tv_hour_title, R.id.tv_hour_content, coverItem.isCanceled() ? coverItem.getHour() + " (Entfall)" : "");
        // Subject
        applyText(root, R.id.tv_subject_title, R.id.tv_subject_content, coverItem.getSubject());
        // Room
        applyText(root, R.id.tv_room_title, R.id.tv_room_content, coverItem.getRoom());
        // Annotation
        applyText(root, R.id.tv_annotation_title, R.id.tv_annotation_content, coverItem.getAnnotation());
        // Relocated
        applyText(root, R.id.tv_relocated_title, R.id.tv_relocated_content, coverItem.getRelocated());
        // Fresh
        applyText(root, R.id.tv_new_entry_title, R.id.tv_new_entry_content, coverItem.isNewEntry() ? "X" : "");

        return root;
    }

    private static void applyText(@NonNull View root, @IdRes int textViewRes, @IdRes int containerRes, @NonNull String text) {

        View container = root.findViewById(textViewRes);
        TextView textView = root.findViewById(containerRes);

        if (text.isEmpty()) {
            container.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }

    }

}
