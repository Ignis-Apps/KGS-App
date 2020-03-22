package de.kgs.vertretungsplan.singetones;

import java.util.Date;


public class GlobalVariables {

    private static final GlobalVariables ourInstance = new GlobalVariables();

    public Date lastUpdated;

    public String moodleCookie;
    public long moodleCookieLastUse;

    public long moodleCookieMaxAgeSecounds;

    public String username;
    public String password;

    public int responseCode;

    public String cover_plan_today_url;
    public String cover_plan_tomorrow_url;
    public String login_page_url;
    public String school_events_url;
    public String school_mensa_url;
    public String school_moodle_url;
    public String school_news_url;
    public String school_newsletter_url;
    public String school_press_url;
    public String school_webpage_url;
    public String student_newspaper;

    public long timeMillsLastView;

    private GlobalVariables() {
    }

    public static GlobalVariables getInstance() {
        return ourInstance;
    }
}
