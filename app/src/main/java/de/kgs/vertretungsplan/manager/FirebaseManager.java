package de.kgs.vertretungsplan.manager;

import android.content.Context;

import de.kgs.vertretungsplan.manager.firebase.Analytics;
import de.kgs.vertretungsplan.manager.firebase.RemoteConfig;

public class FirebaseManager {

    public static final String ANALYTICS_BLACK_BOARD = "black_board";
    public static final String ANALYTICS_MENU_EXTERNAL = "external";
    public static final String ANALYTICS_MENU_INTERNAL = "internal";

    public FirebaseManager(Context context) {
        Analytics.getInstance().init(context);
        RemoteConfig.load();
    }

}
