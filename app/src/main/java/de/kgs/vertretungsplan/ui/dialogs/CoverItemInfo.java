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

    private static final String OK = "OK";
    private static final String TITLE = "Informationen";

    public static void showDialog(Context context, CoverItem coverItem) {

        Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle(TITLE);
        alertBuilder.setIcon((R.drawable.ic_action_info));
        alertBuilder.setPositiveButton(OK, (dialogInterface, i) -> dialogInterface.cancel());
        alertBuilder.setView(getDialogView(context, coverItem));
        alertBuilder.create();
        alertBuilder.show();
    }

    private static View getDialogView(Context context, CoverItem coverItem) {

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout linearLayout = new LinearLayout(context);
        View root = inflater.inflate(R.layout.alertdialog_item_info, linearLayout, false);

        // Class
        applyText(root, R.id.llClass, R.id.klasseTv, coverItem.getTargetClass());
        // Hour
        applyText(root, R.id.llHour, R.id.stundeTv, coverItem.isCanceled() ? coverItem.getHour() + " (Entfall)" : "");
        // Subject
        applyText(root, R.id.llFach, R.id.fachTv, coverItem.getSubject());
        // Room
        applyText(root, R.id.llRoom, R.id.raumTv, coverItem.getRoom());
        // Annotation
        applyText(root, R.id.llAnnotation, R.id.annotationTv, coverItem.getAnnotation());
        // Relocated
        applyText(root, R.id.llVerFrom, R.id.ver_fromTv, coverItem.getRelocated());
        // Fresh
        applyText(root, R.id.llAnnotationLesson, R.id.annotation_lessonTv, coverItem.isNewEntry() ? "X" : "");

        return root;
    }

    private static void applyText(@NonNull View root, @IdRes int textViewRes, @IdRes int containerRes, @NonNull String text) {

        View container = root.findViewById(textViewRes);
        TextView textView = root.findViewById(containerRes);

        if (text.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }

    }

}
