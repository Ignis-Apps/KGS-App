package de.kgs.vertretungsplan.loader;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.storage.Credentials;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.ui.dialogs.DownloadError;
import de.kgs.vertretungsplan.ui.dialogs.LoginRequired;
import de.kgs.vertretungsplan.ui.dialogs.SwipeHintDialog;

public class LoadManager implements CoverPlanLoaderCallback, Broadcast.Receiver {

    private final Context context;
    private final View contentMain;
    private final Broadcast broadcast;

    private CoverPlanLoader loader;

    public LoadManager(Context c, Broadcast broadcast) {
        this.context = c;
        this.broadcast = broadcast;
        contentMain = ((Activity) c).findViewById(R.id.contentMainRl);
        loader = new CoverPlanLoader(context, this, false);
        broadcast.subscribe(this, BroadcastEvent.REQUEST_DATA_RELOAD);
    }

    public void loadData() {
        loader.onlyLoadOfflineData = false;
        loader.execute();
    }

    public void loadOfflineData() {

        if (loader.isRunning())
            return;

        loader = new CoverPlanLoader(context, this, false);
        loader.onlyLoadOfflineData = true;
        loader.execute();
    }

    public void onPause() {
        if (loader != null)
            loader.onPause();

        if (GlobalVariables.getInstance().responseCode == LoaderResponseCode.LATEST_DATA_SET)
            GlobalVariables.getInstance().lastRefreshTime = System.currentTimeMillis();

    }

    public void onStart() {

        // Prevents the execution of multiple load tasks
        if (loader != null && loader.isRunning()) {
            loader.onStart();
            return;
        }

        // Reload data if app has slept for more than 10 minutes
        if (System.currentTimeMillis() - GlobalVariables.getInstance().lastRefreshTime > 600000) {
            loader = new CoverPlanLoader(context, this, false);
            loader.execute();
        }
    }

    private void refreshCoverPlan() {
        broadcast.send(BroadcastEvent.DATA_PROVIDED);
        SwipeHintDialog.showOnce(context);
    }

    @Override
    public void loaderFinishedWithResponseCode(LoaderResponseCode responseCode) {

        GlobalVariables.getInstance().responseCode = responseCode;

        switch (responseCode) {
            case LOGIN_REQUIRED:
                LoginRequired.show(context);
                return;
            case ERROR:
                DownloadError.show(context);
                return;
            case NO_INTERNET_NO_DATA_SET:
                Snackbar.make(contentMain, "Keine Internetverbindung! Bitte aktiviere WLAN oder mobile Daten.", Snackbar.LENGTH_LONG).show();
                return;
            case LATEST_DATA_SET:
                refreshCoverPlan();
                Credentials.getInstance().saveCookie(context);
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

    @Override
    public void onEventTriggered(BroadcastEvent broadcastEvent) {
        if (broadcastEvent == BroadcastEvent.REQUEST_DATA_RELOAD)
            loadOfflineData();
    }
}
