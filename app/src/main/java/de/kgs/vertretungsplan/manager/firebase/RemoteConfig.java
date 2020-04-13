package de.kgs.vertretungsplan.manager.firebase;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.singetones.GlobalVariables;

public class RemoteConfig {

    public static void load() {

        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        config.setDefaultsAsync(R.xml.remote_config_defaults);
        loadIntoSingleton(config);

        config.fetch(1800).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                config.activate();
                loadIntoSingleton(config);
                return;
            }
            Crashlytics.logException(task.getException());
        });

    }

    private static void loadIntoSingleton(FirebaseRemoteConfig config) {

        GlobalVariables gv = GlobalVariables.getInstance();

        gv.login_page_url = config.getString("login_page_url");
        gv.cover_plan_today_url = config.getString("cover_plan_today");
        gv.cover_plan_tomorrow_url = config.getString("cover_plan_tomorrow");
        gv.school_news_url = config.getString("school_news_url");
        gv.school_events_url = config.getString("school_events_url");
        gv.school_press_url = config.getString("school_press_url");
        gv.student_newspaper = config.getString("student_newspaper");
        gv.school_newsletter_url = config.getString("school_newsletter_url");
        gv.school_moodle_url = config.getString("school_moodle_url");
        gv.school_web_page_url = config.getString("school_webpage_url");
        gv.school_mensa_url = config.getString("school_mensa_url");
        gv.moodleCookieMaxAgeSeconds = config.getLong("moodle_cookie_max_age_secounds");

    }

}
