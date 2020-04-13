package de.kgs.vertretungsplan.manager.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    private static final Analytics instance = new Analytics();

    private FirebaseAnalytics firebaseAnalytics;

    private Analytics() {
    }

    public void init(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void logContentSelectEvent(String id, String content) {

        if (firebaseAnalytics == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, content);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void setUserProperty(String key, String content) {
        firebaseAnalytics.setUserProperty(key, content);
    }

    public void logEvent(String event) {
        firebaseAnalytics.logEvent(event, new Bundle());
    }

    public static Analytics getInstance() {
        return instance;
    }

}
