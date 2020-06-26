package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;

public final class DownloadError {

    public static void show(final Context context) {

        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle(R.string.dialog_download_error_title);
        alertBuilder.setMessage(R.string.dialog_download_error_message);
        alertBuilder.setIcon(R.drawable.ic_alert_error);
        alertBuilder.setPositiveButton(R.string.dialog_download_error_positive, (dialogInterface, i) -> ((MainActivity) context).broadcast.send(BroadcastEvent.REQUEST_DATA_RELOAD));
        alertBuilder.setNegativeButton(R.string.dialog_download_error_negative, (dialogInterface, i) -> System.exit(0));
        alertBuilder.create().show();

    }
}
