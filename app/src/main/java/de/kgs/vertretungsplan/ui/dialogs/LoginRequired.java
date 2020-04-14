package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.LoginActivity;
import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;

public final class LoginRequired {
    private static final String description = "\"Um auf den Vertretungsplan zugreifen zu können, müssen Sie sich aus Datenschutzgründen mit ihrem Moodle-Account oder dem offiziellen Zugang anmelden. Um den offiziellen Zugang zu erhalten wenden Sie sich bitte an die Schulleitung. Der Nutzername und das Passwort für diesen Zugang werden aus Sicherheitsgründen in regelmäßigen Abständen geändert.\"";
    private static final String exit = "App beenden";
    private static final String login = "Anmelden";
    private static final String title = "Anmeldung erforderlich";

    public static void show(final Context context) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(description);
        alertBuilder.setIcon(R.drawable.ic_alert_error);
        alertBuilder.setPositiveButton(login, (dialogInterface, i) -> ((MainActivity) context).startActivityForResult(new Intent(context, LoginActivity.class), MainActivity.SIGN_UP_RC));
        alertBuilder.setNegativeButton(exit, (dialogInterface, i) -> System.exit(0));
        alertBuilder.create().show();
    }
}
