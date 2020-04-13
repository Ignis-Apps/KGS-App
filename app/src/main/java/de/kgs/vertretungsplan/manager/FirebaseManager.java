package de.kgs.vertretungsplan.manager;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import de.kgs.vertretungsplan.BuildConfig;
import de.kgs.vertretungsplan.manager.firebase.Analytics;
import de.kgs.vertretungsplan.manager.firebase.RemoteConfig;
import io.fabric.sdk.android.Fabric;

public class FirebaseManager {

    public static final String ANALYTICS_BLACK_BOARD = "black_board";
    public static final String ANALYTICS_MENU_EXTERNAL = "external";
    public static final String ANALYTICS_MENU_INTERNAL = "internal";

    public FirebaseManager(Context context) {
        Analytics.getInstance().init(context);
        RemoteConfig.load();

        // Disables crashlytics for debug builds
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(context, new Crashlytics.Builder().core(crashlyticsCore).build());

    }

}
