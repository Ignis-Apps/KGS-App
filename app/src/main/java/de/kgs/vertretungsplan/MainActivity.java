package de.kgs.vertretungsplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoader;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoaderCallback;
import de.kgs.vertretungsplan.coverPlan.DataInjector;
import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.singetones.DataStorage;
import de.kgs.vertretungsplan.storage.StorageKeys;
import de.kgs.vertretungsplan.views.AppToolBar;
import de.kgs.vertretungsplan.views.KgsWebView;
import de.kgs.vertretungsplan.views.NavigationHandler;
import de.kgs.vertretungsplan.views.NavigationItem;
import de.kgs.vertretungsplan.views.SpinnerHandler;
import de.kgs.vertretungsplan.views.dialogs.DownloadError;
import de.kgs.vertretungsplan.views.dialogs.LoginRequired;
import de.kgs.vertretungsplan.views.dialogs.SwipeHintDialog;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements CoverPlanLoaderCallback, MainActivityInterface {
    public static final int SIGN_UP_RC = 7234;

    public Broadcast broadcast;
    public RelativeLayout contentMain;
    public int currentDay;
    public FirebaseManager firebaseManager;
    public KgsWebView kgsWebView;
    public NavigationHandler navigationHandler;
    public CoverPlanLoader loader;
    public Editor sharedEditor;
    public SharedPreferences sharedPreferences;
    public SpinnerHandler spinnerHandler;
    public AppToolBar toolbar2;
    public ViewPagerManager viewPagerManager;

    private DataStorage dateStorage;

    @Override
    protected void onPause() {

        super.onPause();
        CoverPlanLoader coverPlanLoader = this.loader;
        if (coverPlanLoader != null) {
            coverPlanLoader.onPause();
        }
        if (dateStorage.responseCode == CoverPlanLoader.RC_LATEST_DATASET) {
            dateStorage.timeMillsLastView = System.currentTimeMillis();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        DataInjector.inject(this);

        loader = new CoverPlanLoader(this, this, false);
        loader.onlyLoadData = true;
        dateStorage = DataStorage.getInstance();

        setupBrowser();
        CoverPlanLoader coverPlanLoader = this.loader;
        if (coverPlanLoader != null && coverPlanLoader.isRunning) {
            loader.onStart();
        }
        if (System.currentTimeMillis() - this.dateStorage.timeMillsLastView > 600000) {
            loader = new CoverPlanLoader(this, this, false);
            loader.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.dateStorage.timeMillsLastView = 0;
    }


    public void restart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Runtime.getRuntime().exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dateStorage = DataStorage.getInstance();
        firebaseManager = new FirebaseManager(this, this.dateStorage);
        broadcast = new Broadcast();
        setupDataStorage();
        setupUI();
        spinnerHandler = new SpinnerHandler(this, this.broadcast);
        viewPagerManager = new ViewPagerManager(this, broadcast, firebaseManager);
    }

    private void setupDataStorage() {

        sharedPreferences = getSharedPreferences(StorageKeys.SHARED_PREF, 0);
        sharedEditor = sharedPreferences.edit();
        sharedEditor.apply();
        DataStorage dataStorage = this.dateStorage;
        SharedPreferences sharedPreferences2 = this.sharedPreferences;
        String str = StorageKeys.CURRENT_GRADE_LEVEL;
        dataStorage.currentGradeLevel = sharedPreferences2.getInt(str, 0);
        if (this.dateStorage.currentGradeLevel > 8 || this.dateStorage.currentGradeLevel < 0) {
            this.dateStorage.currentGradeLevel = 0;
            this.sharedEditor.putInt(str, 0);
            this.sharedEditor.apply();
            Crashlytics.logException(new Exception("Magic is happening (currentGradeLevel)"));
        }
        FirebaseManager firebaseManager2 = this.firebaseManager;
        StringBuilder sb = new StringBuilder();
        sb.append(this.dateStorage.currentGradeLevel);
        String str2 = "";
        sb.append(str2);
        firebaseManager2.setUserProperty("Stufe", sb.toString());
        DataStorage dataStorage2 = this.dateStorage;
        SharedPreferences sharedPreferences3 = this.sharedPreferences;
        String str3 = StorageKeys.CURRENT_CLASS;
        dataStorage2.currentClass = sharedPreferences3.getInt(str3, 0);
        if (this.dateStorage.currentClass > 5 || this.dateStorage.currentClass < 0) {
            this.dateStorage.currentClass = 0;
            this.sharedEditor.putInt(str3, 0);
            this.sharedEditor.commit();
            Crashlytics.logException(new Exception("Magic is happening (currentClass)"));
        }
        FirebaseManager firebaseManager3 = this.firebaseManager;
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.dateStorage.currentClass);
        sb2.append(str2);
        firebaseManager3.setUserProperty("Klasse", sb2.toString());
        this.dateStorage.password = this.sharedPreferences.getString(StorageKeys.PASSWORD, str2);
        this.dateStorage.username = this.sharedPreferences.getString(StorageKeys.USERNAME, str2);
        this.dateStorage.moodleCookie = this.sharedPreferences.getString(StorageKeys.LAST_VALID_MOODLE_COOKIE, str2);
        this.dateStorage.moodleCookieLastUse = this.sharedPreferences.getLong(StorageKeys.MOODLE_COOKIE_LAST_USE, 0);
    }

    private void setupUI() {

        setContentView((int) R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        this.toolbar2 = new AppToolBar(this, toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        this.navigationHandler = new NavigationHandler(this, drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        this.contentMain = findViewById(R.id.contentMainRl);
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        if (today.hour <= 6 || today.hour >= 15 || today.weekDay == 6 || today.weekDay == 0) {
            dateStorage.currentNavigationItem = NavigationItem.COVER_PLAN_TOMORROW;
            this.currentDay = 1;
        } else {
            dateStorage.currentNavigationItem = NavigationItem.COVER_PLAN_TODAY;
            this.currentDay = 0;
        }
    }

    private void setupBrowser() {
        this.kgsWebView = new KgsWebView(this, this.broadcast);
        this.contentMain.addView(this.kgsWebView);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 111) {
            refreshCoverPlan();
            return;
        }
        Crashlytics.logException(new Exception("Login Crash: No Response Code!"));
        finish();
    }

    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!this.kgsWebView.consumesBackPress()) {
            this.broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
        }
    }

    public void showInfoDialog() {
        SharedPreferences sharedPreferences2 = this.sharedPreferences;
        String str = StorageKeys.SHOW_SWIPE_INFO;
        if (sharedPreferences2.getBoolean(str, true)) {
            SwipeHintDialog.show(this);
            this.sharedEditor.putBoolean(str, false);
            this.sharedEditor.commit();
        }
    }

    public void loaderFinishedWithResponseCode(int ResponseCode) {
        this.dateStorage.responseCode = ResponseCode;
        switch (ResponseCode) {
            case CoverPlanLoader.RC_LOGIN_REQUIRED:
                LoginRequired.show(this);
                return;
            case CoverPlanLoader.RC_ERROR:
                DownloadError.show(this);
                return;
            case CoverPlanLoader.RC_NO_INTERNET_NO_DATASET:
                Snackbar.make(this.contentMain, "Keine Internetverbindung! Bitte aktiviere WLAN oder mobile Daten.", Snackbar.LENGTH_LONG).show();
                return;
            case CoverPlanLoader.RC_LATEST_DATASET:
                refreshCoverPlan();
                sharedPreferences = getSharedPreferences(StorageKeys.SHARED_PREF, 0);
                sharedEditor = this.sharedPreferences.edit();
                sharedEditor.putString(StorageKeys.LAST_VALID_MOODLE_COOKIE, this.dateStorage.moodleCookie);
                sharedEditor.putLong(StorageKeys.MOODLE_COOKIE_LAST_USE, this.dateStorage.moodleCookieLastUse);
                sharedEditor.apply();
                return;
            case CoverPlanLoader.RC_NO_INTERNET_DATASET_EXIST:
                refreshCoverPlan();
                Snackbar.make(this.contentMain, "Keine Internetverbindung! Der Vertretungsplan ist möglicherweise veraltet.", Snackbar.LENGTH_LONG).show();
                return;
            default:
        }
    }

    public void refreshCoverPlan() {
        this.broadcast.send(BroadcastEvent.DATA_PROVIDED);
        showInfoDialog();
        this.viewPagerManager.refreshPageViewer();
    }

    public void onReloadRequested() {
        this.loader = new CoverPlanLoader(this, this, false);
        this.loader.execute();
    }
}
