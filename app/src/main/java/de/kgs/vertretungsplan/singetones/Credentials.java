package de.kgs.vertretungsplan.singetones;

import android.content.Context;
import android.content.SharedPreferences;

public class Credentials {

    private static final Credentials instance = new Credentials();

    private static final String PREFERENCE_NAME = "credentials";
    private static final String USERNAME_PREF = "username";
    private static final String PASSWORD_PREF = "password";

    private static final String MOODLE_COOKIE_PREF = "lastValidMoodleCookie";
    private static final String MOODLE_COOKIE_LAST_USE_PREF = "moodleCookieAgeSecounds";

    private String username;
    private String password;
    private String moodleCookie;
    private long moodleCookieLastUse;

    private Credentials() {

    }

    public static Credentials getInstance() {
        return instance;
    }

    public void load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        username = preferences.getString(USERNAME_PREF, "");
        password = preferences.getString(PASSWORD_PREF, "");
        moodleCookie = preferences.getString(MOODLE_COOKIE_PREF, "");
        moodleCookieLastUse = preferences.getLong(MOODLE_COOKIE_LAST_USE_PREF, Long.MAX_VALUE);
    }

    public void saveCredentials(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(USERNAME_PREF, username);
        editor.putString(PASSWORD_PREF, password);
        editor.apply();
    }

    public void saveCookie(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(MOODLE_COOKIE_PREF, moodleCookie);
        editor.putLong(MOODLE_COOKIE_LAST_USE_PREF, moodleCookieLastUse);
        editor.apply();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMoodleCookie() {
        return moodleCookie;
    }

    public void setMoodleCookie(String moodleCookie) {
        this.moodleCookie = moodleCookie;
    }

    public long getMoodleCookieLastUse() {
        return moodleCookieLastUse;
    }

    public void setMoodleCookieLastUse(long moodleCookieLastUse) {
        this.moodleCookieLastUse = moodleCookieLastUse;
    }
}
