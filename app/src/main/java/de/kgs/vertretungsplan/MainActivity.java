package de.kgs.vertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.material.snackbar.Snackbar;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.loader.CoverPlanLoader;
import de.kgs.vertretungsplan.loader.CoverPlanLoaderCallback;
import de.kgs.vertretungsplan.loader.DataInjector;
import de.kgs.vertretungsplan.loader.LoaderResponseCode;
import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.Credentials;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.views.AppToolBar;
import de.kgs.vertretungsplan.views.dialogs.DownloadError;
import de.kgs.vertretungsplan.views.dialogs.LoginRequired;
import de.kgs.vertretungsplan.views.dialogs.SwipeHintDialog;
import de.kgs.vertretungsplan.views.handler.NavigationHandler;
import de.kgs.vertretungsplan.views.handler.SpinnerHandler;
import de.kgs.vertretungsplan.views.handler.WebViewHandler;
import io.fabric.sdk.android.Fabric;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements CoverPlanLoaderCallback {

    public static final int SIGN_UP_RC = 7234;
    public final String TAG = "MainActivity";

    private Broadcast broadcast;
    private RelativeLayout contentMain;
    private FirebaseManager firebaseManager;
    private WebViewHandler webViewHandler;
    private CoverPlanLoader loader;

    private GlobalVariables dateStorage;

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        CoverPlanLoader coverPlanLoader = this.loader;
        if (coverPlanLoader != null) {
            coverPlanLoader.onPause();
        }
        if (dateStorage.responseCode == LoaderResponseCode.LATEST_DATA_SET) {
            dateStorage.lastRefreshTime = System.currentTimeMillis();
        }

        ApplicationData.getInstance().saveData(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        
        /*
        CoverPlanLoader coverPlanLoader = this.loader;
        if (coverPlanLoader != null && coverPlanLoader.isRunning) {
            loader.onStart();
        }
        if (System.currentTimeMillis() - this.dateStorage.lastRefreshTime > 600000) {
            loader = new CoverPlanLoader(this, this, false);
            loader.execute();
        }
         */

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        this.dateStorage.lastRefreshTime = 0;
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
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());


        Credentials.getInstance().load(this);
        ApplicationData.getInstance().loadData(this);

        dateStorage = GlobalVariables.getInstance();
        firebaseManager = new FirebaseManager(this);
        broadcast = new Broadcast();
        setupUI();

        DataInjector.inject(this);

        loader = new CoverPlanLoader(this, this, false);
        loader.onlyLoadOfflineData = true;
        loader.execute();

        dateStorage = GlobalVariables.getInstance();

        setupBrowser();

        CoverPlanLoader loader = new CoverPlanLoader(this, this, false);
        loader.onlyLoadOfflineData = true;

    }

    private void setupUI() {

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        contentMain = findViewById(R.id.contentMainRl);

        new AppToolBar(this, broadcast);
        new NavigationHandler(this, drawer, broadcast);
        new SpinnerHandler(this, broadcast);
        new ViewPagerManager(this, broadcast);

    }

    private void setupBrowser() {
        webViewHandler = new WebViewHandler(this, this.broadcast);
        contentMain.addView(this.webViewHandler);
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
        dateStorage.responseCode = responseCode;
        System.out.println("RESPONSE CODE " + responseCode);
        switch (responseCode) {
            case LOGIN_REQUIRED:
                LoginRequired.show(this);
                return;
            case ERROR:
                DownloadError.show(this);
                return;
            case NO_INTERNET_NO_DATA_SET:
                Snackbar.make(this.contentMain, "Keine Internetverbindung! Bitte aktiviere WLAN oder mobile Daten.", Snackbar.LENGTH_LONG).show();
                return;
            case LATEST_DATA_SET:
                refreshCoverPlan();
                Credentials.getInstance().saveCookie(this);
                return;
            case NO_INTERNET_DATA_SET_EXISTS:
                refreshCoverPlan();
                Snackbar.make(this.contentMain, "Keine Internetverbindung! Der Vertretungsplan ist möglicherweise veraltet.", Snackbar.LENGTH_LONG).show();
                return;
            case COVER_PLAN_NOT_PROVIDED:
                loader.onlyLoadOfflineData = true;
                //loader.execute();
                break;
            default:
        }
    }

    public void refreshCoverPlan() {

        if (ApplicationData.getInstance().getCoverPlanToday() == null)
            return;

        System.out.println("Refreshing coverplan");
        this.broadcast.send(BroadcastEvent.DATA_PROVIDED);
        SwipeHintDialog.showOnce(this);
        //this.viewPagerManager.refreshPageViewer();
    }

}
