package de.kgs.vertretungsplan;

import java.util.Date;

import de.kgs.vertretungsplan.coverPlan.CoverPlan;

public class DataStorage {
    private final static DataStorage ourInstance = new DataStorage();

    public final static String LOGIN_PAGE_URL = "login_page_url";

    public final static String COVER_PLAN_TODAY = "cover_plan_today";
    public final static String COVER_PLAN_TOMORROW = "cover_plan_tomorrow";

    public final static String STUDENT_NEWSPAPER = "student_newspaper";

    public final static String SCHOOL_NEWS_URL = "school_news_url";
    public final static String SCHOOL_EVENTS_URL = "school_events_url";
    public final static String SCHOOL_PRESS_URL ="school_press_url";

    public final static String SCHOOL_NEWSLETTER_URL = "school_newsletter_url";
    public final static String SCHOOL_MOODLE_URL = "school_moodle_url";
    public final static String SCHOOL_WEBPAGE_URL = "school_webpage_url";
    public final static String SCHOOL_MENSA_URL = "school_mensa_url";

    public static final String MOODLE_COOKIE_MAX_AGE_SECOUNDS = "moodle_cookie_max_age_secounds";

    static final String SHARED_PREF = "privateSharedPreferences";
    static final String CURRENT_GRADE_LEVEL = "currentGradeLevel";
    static final String CURRENT_CLASS = "currentClass";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String SHOW_SWIPE_INFO = "swipe_tutorial_2";

    static final String LAST_VALID_MOODLE_COOKIE = "lastValidMoodleCookie";
    static final String MOODLE_COOKIE_LAST_USE = "moodleCookieAgeSecounds";

    public String login_page_url;

    public String cover_plan_today;
    public String cover_plan_tomorrow;

    public String school_news_url;
    public String school_events_url;
    public String school_press_url;

    public String student_newspaper;

    public String school_newsletter_url;
    public String school_moodle_url;
    public String school_webpage_url;
    public String school_mensa_url;


    int currentGradeLevel, currentClass;
    long timeMillsLastView;

    public CoverPlan coverPlanToday,coverPlanTomorow;
    public Date lastUpdated;
    Integer responseCode = 0;

    public String username;
    public String password;

    public String moodleCookie;
    public long moodleCookieMaxAgeSecounds;
    public long moodleCookieLastUse;

    public static DataStorage getInstance() {
        return ourInstance;
    }

    private DataStorage() {

    }
}
