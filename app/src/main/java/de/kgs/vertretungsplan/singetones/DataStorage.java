package de.kgs.vertretungsplan.singetones;

import java.util.Date;

import de.kgs.vertretungsplan.coverPlan.CoverPlan;
import de.kgs.vertretungsplan.views.Grade;
import de.kgs.vertretungsplan.views.GradeSubClass;
import de.kgs.vertretungsplan.views.NavigationItem;


public class DataStorage {

    private static final DataStorage ourInstance = new DataStorage();

    public CoverPlan coverPlanToday;
    public CoverPlan coverPlanTomorow;

    public String cover_plan_today;
    public String cover_plan_tomorrow;
    public int currentClass;
    public Grade currentGrade = Grade.ALL;
    public int currentGradeLevel;
    public GradeSubClass currentGradeSubClass = GradeSubClass.ALL;
    public NavigationItem currentNavigationItem;
    public int currentlySelectedViewPage = 1;
    public Date lastUpdated;
    public String login_page_url;
    public String moodleCookie;
    public long moodleCookieLastUse;
    public long moodleCookieMaxAgeSecounds;
    public String password;
    public int responseCode;
    public String school_events_url;
    public String school_mensa_url;
    public String school_moodle_url;
    public String school_news_url;
    public String school_newsletter_url;
    public String school_press_url;
    public String school_webpage_url;
    public String student_newspaper;
    public long timeMillsLastView;
    public String username;

    public static DataStorage getInstance() {
        return ourInstance;
    }

    private DataStorage() {
    }
}
