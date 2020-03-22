package de.kgs.vertretungsplan.views;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.coverPlan.CoverPlan;
import de.kgs.vertretungsplan.singetones.ApplicationData;

public final class AppToolBar implements Broadcast.Receiver {

    private Toolbar toolbar;

    public AppToolBar(@NonNull MainActivity activity) {
        this.toolbar = activity.findViewById(R.id.toolbar);
        this.toolbar.setTitle(activity.getResources().getString(R.string.app_title));
        activity.setSupportActionBar(toolbar);
        setupObserver(activity.broadcast);
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
            setToolbarTextByCoverPlan(applicationData.getCoverPlanToday());

        else if (navigationItem == NavigationItem.COVER_PLAN_TOMORROW)
            setToolbarTextByCoverPlan(applicationData.getCoverPlanTomorrow());

        else if (navigationItem == NavigationItem.NEWS)
            setOnlyTitle(R.string.toolbar_news);

        else if (navigationItem == NavigationItem.APPOINTMENTS)
            setOnlyTitle(R.string.toolbar_events);

        else if (navigationItem == NavigationItem.PRESS)
            setOnlyTitle(R.string.toolbar_press);

    }

    private void onViewPageChanged() {

        ApplicationData applicationData = ApplicationData.getInstance();

        int i = applicationData.getCurrentlySelectedViewPage();
        if (i == 0) {
            setOnlyTitle(R.string.toolbar_black_board);
        } else if (i == 1) {
            setToolbarTextByCoverPlan(applicationData.getCoverPlanToday());
        } else if (i == 2) {
            setToolbarTextByCoverPlan(applicationData.getCoverPlanTomorrow());
        }
    }

    private void onDataReceived() {
        NavigationItem item = ApplicationData.getInstance().getCurrentNavigationItem();
        if (item == NavigationItem.COVER_PLAN_TODAY || item == NavigationItem.COVER_PLAN_TOMORROW) {
            onViewPageChanged();
        }
    }

    private void setToolbarTextByCoverPlan(CoverPlan plan) {
        if (plan != null) {
            toolbar.setTitle(plan.getWeekDay());
            toolbar.setSubtitle(plan.getLastUpdateText());
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
