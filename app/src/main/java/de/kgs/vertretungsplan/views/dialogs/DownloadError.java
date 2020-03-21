package de.kgs.vertretungsplan.views.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoader;

public final class DownloadError {

    private static final String description = "Beim Herrunterladen der Daten ist ein Fehler aufgetreten. Bitte überprüfen Sie Ihre Internetverbindung und versuchen Sie es erneut.";
    private static final String title = "Netzwerkfehler";
    private static final String retry = "Wiederholen";
    private static final String exit = "App beenden";

    public static void show(final Context context) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(description);
        alertBuilder.setIcon(R.drawable.ic_alert_error);
        alertBuilder.setPositiveButton(retry, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new CoverPlanLoader(context, (MainActivity) context, false).execute();
            }
        });
        alertBuilder.setNegativeButton(exit, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(0);
            }
        });
        alertBuilder.create().show();
    }
}
