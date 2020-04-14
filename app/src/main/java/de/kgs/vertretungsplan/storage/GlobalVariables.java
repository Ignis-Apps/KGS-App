package de.kgs.vertretungsplan.storage;

import de.kgs.vertretungsplan.loader.LoaderResponseCode;

public class GlobalVariables {

    private static final GlobalVariables ourInstance = new GlobalVariables();

    public String cover_plan_today_url;
    public String cover_plan_tomorrow_url;
    public String login_page_url;
    public String school_events_url;
    public String school_mensa_url;
    public String school_moodle_url;
    public String school_news_url;
    public String school_newsletter_url;
    public String school_press_url;
    public String school_web_page_url;
    public String student_newspaper;

    public long moodleCookieMaxAgeSeconds;

    public long lastRefreshTime;
    public LoaderResponseCode responseCode;

    private GlobalVariables() {
    }

    public static GlobalVariables getInstance() {
        return ourInstance;
    }

}
