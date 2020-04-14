package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;

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
        alertBuilder.setPositiveButton(retry, (dialogInterface, i) -> ((MainActivity) context).broadcast.send(BroadcastEvent.REQUEST_DATA_RELOAD));
        alertBuilder.setNegativeButton(exit, (dialogInterface, i) -> System.exit(0));
        alertBuilder.create().show();
    }
}
