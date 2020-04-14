package de.kgs.vertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.crashlytics.android.Crashlytics;

import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.firebase.FirebaseManager;
import de.kgs.vertretungsplan.loader.DataInjector;
import de.kgs.vertretungsplan.loader.LoadManager;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.Credentials;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.ui.dialogs.SwipeHintDialog;
import de.kgs.vertretungsplan.ui.handler.AppToolBarHandler;
import de.kgs.vertretungsplan.ui.handler.NavigationHandler;
import de.kgs.vertretungsplan.ui.handler.SpinnerHandler;
import de.kgs.vertretungsplan.ui.handler.ViewPagerHandler;
import de.kgs.vertretungsplan.ui.handler.WebViewHandler;

public class MainActivity extends AppCompatActivity {

    public static final int SIGN_UP_RC = 7234;

    public Broadcast broadcast = new Broadcast();
    private WebViewHandler webViewHandler;
    private LoadManager loadManager;

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
        RelativeLayout contentMain = findViewById(R.id.contentMainRl);

        new NavigationHandler(this, broadcast);
        new SpinnerHandler(this, broadcast);
        new ViewPagerHandler(this, broadcast);

        // AppToolBar must be initialised after NavigationHandler
        new AppToolBarHandler(this, broadcast);

        // Inject data ( for testing uses only ) // FIXME
        DataInjector.inject(this);

        // Start loader
        loadManager = new LoadManager(this, broadcast);
        loadManager.loadOfflineData(); // FIXME

        // Prepare web view
        webViewHandler = new WebViewHandler(this, broadcast);
        contentMain.addView(webViewHandler);

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadManager.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadManager.onPause();
        ApplicationData.getInstance().saveData(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalVariables.getInstance().lastRefreshTime = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 111) {
            broadcast.send(BroadcastEvent.DATA_PROVIDED);
            SwipeHintDialog.showOnce(this);
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


}
