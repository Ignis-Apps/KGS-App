package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.R;

public final class SwipeHintDialog {

    private static final String title = "Info";
    private static final String description = "Wische nach links bzw. nach rechts, um zwischen den Tagen oder dem Schwarzen Brett zu wechseln.";
    private static final String ok = "Ok";

    private static void show(Context context) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(description);
        alertBuilder.setIcon(R.drawable.ic_action_info);
        alertBuilder.setPositiveButton(ok, null);
        alertBuilder.create().show();
    }

    public static void showOnce(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("SwipeHintDialog", Context.MODE_PRIVATE);
        String lastShownDialog = preferences.getString("version", "");

        // Change this String if you want to show the dialog again
        String shownFLAG = "dialog#1";

        if (lastShownDialog.equals(shownFLAG))
            return;

        preferences.edit().putString("version", shownFLAG).apply();
        show(context);

    }
}
