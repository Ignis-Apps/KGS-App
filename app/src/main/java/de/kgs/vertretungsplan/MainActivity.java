package de.kgs.vertretungsplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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
import de.kgs.vertretungsplan.loader.CoverPlanLoader;
import de.kgs.vertretungsplan.loader.CoverPlanLoaderCallback;
import de.kgs.vertretungsplan.coverPlan.DataInjector;
import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.storage.StorageKeys;
import de.kgs.vertretungsplan.views.AppToolBar;
import de.kgs.vertretungsplan.views.handler.WebViewHandler;
import de.kgs.vertretungsplan.views.handler.NavigationHandler;
import de.kgs.vertretungsplan.views.handler.SpinnerHandler;
import de.kgs.vertretungsplan.views.dialogs.DownloadError;
import de.kgs.vertretungsplan.views.dialogs.LoginRequired;
import de.kgs.vertretungsplan.views.dialogs.SwipeHintDialog;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements CoverPlanLoaderCallback {
    public static final int SIGN_UP_RC = 7234;

    public Broadcast broadcast;
    public RelativeLayout contentMain;

    public FirebaseManager firebaseManager;
    public WebViewHandler webViewHandler;
    public NavigationHandler navigationHandler;
    public CoverPlanLoader loader;
    public Editor sharedEditor;
    public SharedPreferences sharedPreferences;
    public SpinnerHandler spinnerHandler;
    public AppToolBar toolBar;
    public ViewPagerManager viewPagerManager;

    private GlobalVariables dateStorage;

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

        ApplicationData.getInstance().saveData(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        DataInjector.inject(this);

        loader = new CoverPlanLoader(this, this, false);
        loader.onlyLoadData = true;
        dateStorage = GlobalVariables.getInstance();

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

        ApplicationData.getInstance().loadData(this);

        dateStorage = GlobalVariables.getInstance();
        firebaseManager = new FirebaseManager(this);
        broadcast = new Broadcast();
        setupDataStorage();
        setupUI();

    }

    private void setupDataStorage() {

        sharedPreferences = getSharedPreferences(StorageKeys.SHARED_PREF, 0);

        dateStorage.password = sharedPreferences.getString(StorageKeys.PASSWORD, "");
        dateStorage.username = sharedPreferences.getString(StorageKeys.USERNAME, "");
        dateStorage.moodleCookie = sharedPreferences.getString(StorageKeys.LAST_VALID_MOODLE_COOKIE, "");
        dateStorage.moodleCookieLastUse = sharedPreferences.getLong(StorageKeys.MOODLE_COOKIE_LAST_USE, 0);
    }

    private void setupUI() {

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolBar = new AppToolBar(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationHandler = new NavigationHandler(this, drawer);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        contentMain = findViewById(R.id.contentMainRl);

        spinnerHandler = new SpinnerHandler(this, broadcast);
        viewPagerManager = new ViewPagerManager(this, broadcast, firebaseManager);

    }

    private void setupBrowser() {
        webViewHandler = new WebViewHandler(this, this.broadcast);
        contentMain.addView(this.webViewHandler);
    }

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
            return;
        }

        if (webViewHandler.consumesBackPress()) {
            broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
            return;
        }

        super.onBackPressed();
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

}
