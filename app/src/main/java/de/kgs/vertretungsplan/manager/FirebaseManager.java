package de.kgs.vertretungsplan.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import de.kgs.vertretungsplan.DataStorage;
import de.kgs.vertretungsplan.R;


public class FirebaseManager {
    public static final String ANALYTICS_MENU_INTERNAL = "internal";
    public static final String ANALYTICS_MENU_EXTERNAL = "external";
    public static final String ANALYTICS_BLACK_BOARD = "black_board";

    private Context c;
    private DataStorage ds;

    public Trace loadWebpageTrace;
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    public FirebaseManager(Context context, DataStorage dataStorage){
        c = context;
        ds = dataStorage;

        firebaseAnalytics = FirebaseAnalytics.getInstance(c);

        loadWebpageTrace = FirebasePerformance.getInstance().newTrace("load_webpage");

        firebaseRemoteConfig  = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        ds.login_page_url = firebaseRemoteConfig.getString(DataStorage.LOGIN_PAGE_URL);

        ds.cover_plan_today = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TODAY);
        ds.cover_plan_tomorrow = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TOMORROW);

        ds.school_news_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWS_URL);
        ds.school_events_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_EVENTS_URL);
        ds.school_press_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_PRESS_URL);

        ds.school_newsletter_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWSLETTER_URL);
        ds.school_moodle_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MOODLE_URL);
        ds.school_webpage_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_WEBPAGE_URL);
        ds.school_mensa_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MENSA_URL);

        firebaseRemoteConfig.fetch(1800)
                .addOnCompleteListener((Activity) c, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            firebaseRemoteConfig.activateFetched();

                            ds.login_page_url = firebaseRemoteConfig.getString(DataStorage.LOGIN_PAGE_URL);

                            ds.cover_plan_today = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TODAY);
                            ds.cover_plan_tomorrow = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TOMORROW);

                            ds.school_news_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWS_URL);
                            ds.school_events_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_EVENTS_URL);
                            ds.school_press_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_PRESS_URL);

                            ds.student_newspaper = firebaseRemoteConfig.getString(DataStorage.STUDENT_NEWSPAPER);

                            ds.school_newsletter_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWSLETTER_URL);
                            ds.school_moodle_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MOODLE_URL);
                            ds.school_webpage_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_WEBPAGE_URL);
                            ds.school_mensa_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MENSA_URL);

                        } else {
                            Crashlytics.logException(task.getException());
                        }
                    }
                });
    }

    public void setUserProperty(String key, String content){
        firebaseAnalytics.setUserProperty(key, content);
    }

    public void logEventSelectContent(String itemId, String contentType){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void logEvent(String event){
        firebaseAnalytics.logEvent(event, new Bundle());
    }
}