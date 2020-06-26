package de.kgs.vertretungsplan.ui.dialogs;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog.Builder;

import de.kgs.vertretungsplan.LoginActivity;
import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;

public final class LoginRequired {

    public static void show(final Context context) {

        Builder alertBuilder = new Builder(context);
        alertBuilder.setCancelable(false);
        alertBuilder.setTitle(R.string.dialog_login_required_title);
        alertBuilder.setMessage(R.string.dialog_login_required_message);
        alertBuilder.setIcon(R.drawable.ic_alert_error);
        alertBuilder.setPositiveButton(R.string.dialog_login_required_positive, (dialogInterface, i) -> ((MainActivity) context).startActivityForResult(new Intent(context, LoginActivity.class), MainActivity.SIGN_UP_RC));
        alertBuilder.setNegativeButton(R.string.dialog_login_required_negative, (dialogInterface, i) -> System.exit(0));
        alertBuilder.create().show();

    }
}
