package de.kgs.vertretungsplan.ui.handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.coverplan.CoverPlan;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.ui.NavigationItem;

public final class AppToolBarHandler implements Broadcast.Receiver {

    private final Toolbar toolbar;

    public AppToolBarHandler(@NonNull MainActivity activity, Broadcast broadcast) {
        this.toolbar = activity.findViewById(R.id.toolbar);
        this.toolbar.setTitle(activity.getResources().getString(R.string.app_title));
        activity.setSupportActionBar(toolbar);
        setupObserver(broadcast);

        // Sync toolbar with drawer
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }


    private void setOnlyTitle(Integer titleResourceId) {
        toolbar.setTitle(titleResourceId);
        toolbar.setSubtitle(null);
    }

    private void setupObserver(@NonNull Broadcast broadcast) {
        broadcast.subscribe(this, BroadcastEvent.CURRENT_PAGE_CHANGED, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED, BroadcastEvent.DATA_PROVIDED);
    }

    private void onSelectedMenuItemChanged() {

        ApplicationData applicationData = ApplicationData.getInstance();

        NavigationItem navigationItem = ApplicationData.getInstance().getCurrentNavigationItem();

        if (navigationItem == NavigationItem.BLACK_BOARD)
            setOnlyTitle(R.string.toolbar_black_board);

        else if (navigationItem == NavigationItem.COVER_PLAN_TODAY)
            setToolbarTextByCoverPlan(applicationData.getCoverPlanToday(), "Heute");

        else if (navigationItem == NavigationItem.COVER_PLAN_TOMORROW)
            setToolbarTextByCoverPlan(applicationData.getCoverPlanTomorrow(), "Morgen");

        else if (navigationItem == NavigationItem.NEWS)
            setOnlyTitle(R.string.toolbar_news);

        else if (navigationItem == NavigationItem.APPOINTMENTS)
            setOnlyTitle(R.string.toolbar_events);

        else if (navigationItem == NavigationItem.PRESS)
            setOnlyTitle(R.string.toolbar_press);

        else if (navigationItem == NavigationItem.STUDENT_NEWS_PAPER)
            setOnlyTitle(R.string.toolbar_student_newspaper);

    }

    private void onViewPageChanged() {

        ApplicationData applicationData = ApplicationData.getInstance();

        int i = applicationData.getCurrentlySelectedViewPage();
        if (i == 0) {
            setOnlyTitle(R.string.toolbar_black_board);
        } else if (i == 1) {
            setToolbarTextByCoverPlan(applicationData.getCoverPlanToday(), "Heute");
        } else if (i == 2) {
            setToolbarTextByCoverPlan(applicationData.getCoverPlanTomorrow(), "Morgen");
        }
    }

    private void onDataReceived() {
        NavigationItem item = ApplicationData.getInstance().getCurrentNavigationItem();
        if (item == NavigationItem.COVER_PLAN_TODAY || item == NavigationItem.COVER_PLAN_TOMORROW) {
            onViewPageChanged();
        }
    }

    private void setToolbarTextByCoverPlan(CoverPlan plan, String alternative) {
        if (plan != null) {
            toolbar.setTitle(plan.getWeekDay());
            toolbar.setSubtitle(plan.getLastUpdateText());
        } else {
            toolbar.setTitle(alternative);
            toolbar.setSubtitle(null);
        }
    }

    @Override
    public void onEventTriggered(BroadcastEvent event) {

        if (event == BroadcastEvent.CURRENT_MENU_ITEM_CHANGED)
            onSelectedMenuItemChanged();
        else if (event == BroadcastEvent.CURRENT_PAGE_CHANGED)
            onViewPageChanged();
        else if (event == BroadcastEvent.DATA_PROVIDED)
            onDataReceived();
        else
            throw new AssertionError("Unhandled broadcast event !");

    }
}
