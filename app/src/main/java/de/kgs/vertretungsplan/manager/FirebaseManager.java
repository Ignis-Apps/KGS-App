package de.kgs.vertretungsplan.manager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.singetones.GlobalVariables;

public class FirebaseManager {

    public static final String ANALYTICS_BLACK_BOARD = "black_board";
    public static final String ANALYTICS_MENU_EXTERNAL = "external";
    public static final String ANALYTICS_MENU_INTERNAL = "internal";

    private Context context;

    /* renamed from: ds */
    private GlobalVariables ds = GlobalVariables.getInstance();
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    private Trace loadWebpageTrace = FirebasePerformance.getInstance().newTrace("load_webpage");

    public FirebaseManager(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        this.context = context;
        loadIntoSingleton();

        firebaseRemoteConfig.fetch(1800).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activateFetched();
                    loadIntoSingleton();
                    return;
                }
                Crashlytics.logException(task.getException());
            }
        });
    }

    private void loadIntoSingleton() {

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        ds.login_page_url = this.firebaseRemoteConfig.getString("login_page_url");
        ds.cover_plan_today_url = this.firebaseRemoteConfig.getString("cover_plan_today");
        ds.cover_plan_tomorrow_url = this.firebaseRemoteConfig.getString("cover_plan_tomorrow");
        ds.school_news_url = this.firebaseRemoteConfig.getString("school_news_url");
        ds.school_events_url = this.firebaseRemoteConfig.getString("school_events_url");
        ds.school_press_url = this.firebaseRemoteConfig.getString("school_press_url");
        ds.student_newspaper = this.firebaseRemoteConfig.getString("student_newspaper");
        ds.school_newsletter_url = this.firebaseRemoteConfig.getString("school_newsletter_url");
        ds.school_moodle_url = this.firebaseRemoteConfig.getString("school_moodle_url");
        ds.school_webpage_url = this.firebaseRemoteConfig.getString("school_webpage_url");
        ds.school_mensa_url = this.firebaseRemoteConfig.getString("school_mensa_url");
        ds.moodleCookieMaxAgeSecounds = this.firebaseRemoteConfig.getLong("moodle_cookie_max_age_secounds");
    }

    public void setUserProperty(String key, String content) {
        this.firebaseAnalytics.setUserProperty(key, content);
    }

    public void logEventSelectContent(String itemId, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(Param.ITEM_ID, itemId);
        bundle.putString(Param.CONTENT_TYPE, contentType);
        this.firebaseAnalytics.logEvent(Event.SELECT_CONTENT, bundle);
    }

    public void logEvent(String event) {
        this.firebaseAnalytics.logEvent(event, new Bundle());
    }
}
