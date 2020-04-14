package de.kgs.vertretungsplan;

import android.content.Intent;
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
import de.kgs.vertretungsplan.loader.DataInjector;
import de.kgs.vertretungsplan.loader.LoaderResponseCode;
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.manager.firebase.FirebaseManager;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.Credentials;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.ui.AppToolBar;
import de.kgs.vertretungsplan.ui.dialogs.DownloadError;
import de.kgs.vertretungsplan.ui.dialogs.LoginRequired;
import de.kgs.vertretungsplan.ui.dialogs.SwipeHintDialog;
import de.kgs.vertretungsplan.ui.handler.NavigationHandler;
import de.kgs.vertretungsplan.ui.handler.SpinnerHandler;
import de.kgs.vertretungsplan.ui.handler.WebViewHandler;

public class MainActivity extends AppCompatActivity implements CoverPlanLoaderCallback {

    public static final int SIGN_UP_RC = 7234;

    private Broadcast broadcast = new Broadcast();
    private RelativeLayout contentMain;
    private WebViewHandler webViewHandler;
    private CoverPlanLoader loader;

    private boolean DEBUG_FLAG_REMOVE_BEFORE_RELEASE = true;

    @Override
    protected void onPause() {
        super.onPause();

        if (loader != null)
            loader.onPause();

        if (GlobalVariables.getInstance().responseCode == LoaderResponseCode.LATEST_DATA_SET)
            GlobalVariables.getInstance().lastRefreshTime = System.currentTimeMillis();

        ApplicationData.getInstance().saveData(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (DEBUG_FLAG_REMOVE_BEFORE_RELEASE)
            return;

        // Prevents the execution of multiple load tasks
        if (loader != null && loader.isRunning()) {
            loader.onStart();
            return;
        }

        // Reload data if app has slept for more than 10 minutes
        if (System.currentTimeMillis() - GlobalVariables.getInstance().lastRefreshTime > 600000) {
            loader = new CoverPlanLoader(this, this, false);
            loader.execute();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalVariables.getInstance().lastRefreshTime = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Load data from shared preferences
        Credentials.getInstance().load(this);
        ApplicationData.getInstance().loadData(this);

        // Prepare/Load everything from firebase
        new FirebaseManager(this);

        // Init UI Components
        setContentView(R.layout.activity_main);
        contentMain = findViewById(R.id.contentMainRl);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        new AppToolBar(this, broadcast);
        new NavigationHandler(this, drawer, broadcast);
        new SpinnerHandler(this, broadcast);
        new ViewPagerManager(this, broadcast);

        // Inject data ( for testing uses only )
        if (DEBUG_FLAG_REMOVE_BEFORE_RELEASE)
            DataInjector.inject(this);

        // Start loader
        loader = new CoverPlanLoader(this, this, false);
        loader.onlyLoadOfflineData = true;
        loader.execute();

        // Prepare web view
        webViewHandler = new WebViewHandler(this, broadcast);
        contentMain.addView(webViewHandler);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 111) {
            refreshCoverPlan();
            return;
        }
        Crashlytics.logException(new Exception("Login Crash: No Response Code!"));
        finish();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (webViewHandler.consumesBackPress())
            return;

        super.onBackPressed();
    }


    @Override
    public void loaderFinishedWithResponseCode(LoaderResponseCode responseCode) {

        GlobalVariables.getInstance().responseCode = responseCode;

        switch (responseCode) {
            case LOGIN_REQUIRED:
                LoginRequired.show(this);
                return;
            case ERROR:
                DownloadError.show(this);
                return;
            case NO_INTERNET_NO_DATA_SET:
                Snackbar.make(contentMain, "Keine Internetverbindung! Bitte aktiviere WLAN oder mobile Daten.", Snackbar.LENGTH_LONG).show();
                return;
            case LATEST_DATA_SET:
                refreshCoverPlan();
                Credentials.getInstance().saveCookie(this);
                return;
            case NO_INTERNET_DATA_SET_EXISTS:
                refreshCoverPlan();
                Snackbar.make(contentMain, "Keine Internetverbindung! Der Vertretungsplan ist möglicherweise veraltet.", Snackbar.LENGTH_LONG).show();
                return;
            case COVER_PLAN_NOT_PROVIDED:
                // TODO
                break;
            default:
        }
    }

    private void refreshCoverPlan() {
        broadcast.send(BroadcastEvent.DATA_PROVIDED);
        SwipeHintDialog.showOnce(this);
    }

}
