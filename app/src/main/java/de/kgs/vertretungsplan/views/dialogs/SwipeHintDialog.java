package de.kgs.vertretungsplan.views.dialogs;

import android.content.Context;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.R;

public final class SwipeHintDialog {

    private static final String title = "Info";
    private static final String description = "Wische nach links bzw. nach rechts, um zwischen den Tagen oder dem Schwarzen Brett zu wechseln.";
    private static final String ok = "Ok";

    public static void show(Context context) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(description);
        alertBuilder.setIcon(R.drawable.ic_action_info);
        alertBuilder.setPositiveButton(ok, null);
        alertBuilder.create().show();
    }
}
