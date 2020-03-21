package de.kgs.vertretungsplan.views.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverItem;

public final class CoverItemInfo {

    private static final String ok = "OK";
    private static final String title = "Informationen";

    public static void showDialog(Context context, CoverItem coverItem) {

        Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setIcon((R.drawable.ic_action_info));
        alertBuilder.setPositiveButton(ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertBuilder.setView(getDialogView(context, coverItem));
        alertBuilder.create();
        alertBuilder.show();
    }

    private static View getDialogView(Context context, CoverItem coverItem) {

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout linearLayout = new LinearLayout(context);
        View root = inflater.inflate(R.layout.alertdialog_item_info, linearLayout, false);

        // Class
        View classContainer = root.findViewById(R.id.llClass);
        TextView classTv = root.findViewById(R.id.klasseTv);
        setTextOrHide(classTv, classContainer, coverItem.getTargetClass());

        // Hour
        View hourContainer = root.findViewById(R.id.llHour);
        TextView hourTv = root.findViewById(R.id.stundeTv);
        String hourText = coverItem.getHour();
        if (coverItem.isCanceled())
            hourText += " (Entfall)";
        setTextOrHide(hourTv, hourContainer, hourText);

        // Subject
        View subjectContainer = root.findViewById(R.id.llFach);
        TextView subjectTv = root.findViewById(R.id.fachTv);
        setTextOrHide(subjectTv, subjectContainer, coverItem.getSubject());

        // Room
        View roomContainer = root.findViewById(R.id.llRoom);
        TextView roomTv = root.findViewById(R.id.raumTv);
        setTextOrHide(roomTv, roomContainer, coverItem.getRoom());

        // Annotation
        View annotationContainer = root.findViewById(R.id.llAnnotation);
        TextView annotationTv = root.findViewById(R.id.annotationTv);
        setTextOrHide(annotationTv, annotationContainer, coverItem.getAnnotation());

        // Relocated
        View relocatedContainer = root.findViewById(R.id.llVerFrom);
        TextView relocatedTv = root.findViewById(R.id.ver_fromTv);
        setTextOrHide(relocatedTv, relocatedContainer, coverItem.getRelocated());

        // Fresh
        View freshEntryContainer = root.findViewById(R.id.llAnnotationLesson);
        TextView freshEntryTv = root.findViewById(R.id.annotation_lessonTv);
        setTextOrHide(freshEntryTv, freshEntryContainer, coverItem.isNewEntry() ? "X" : "");

        return root;
    }

    private static void setTextOrHide(TextView textView, View container, String text) {
        if (text.isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }
}
