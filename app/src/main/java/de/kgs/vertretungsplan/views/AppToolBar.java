package de.kgs.vertretungsplan.views;

import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.coverPlan.CoverPlan;
import de.kgs.vertretungsplan.singetones.DataStorage;

public final class AppToolBar implements Broadcast.Observer {
    private Toolbar toolbar;

    public AppToolBar(MainActivity activity, Toolbar toolbar2) {
        this.toolbar = toolbar2;
        this.toolbar.setTitle(activity.getResources().getString(R.string.app_title));
        activity.setSupportActionBar(toolbar2);
        setupObserver(activity.broadcast);
    }

    public void setText(String title, String subtitle) {
        setTitle(title);
        setSubTitle(subtitle);
    }

    public void setTitle(String title) {
        this.toolbar.setTitle(title);
    }

    public void setSubTitle(String subTitle) {
        this.toolbar.setSubtitle(subTitle);
    }

    private void setupObserver(Broadcast broadcast) {
        broadcast.subscribe(this, BroadcastEvent.CURRENT_PAGE_CHANGED, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED, BroadcastEvent.DATA_PROVIDED);
    }

    private void onSelectedMenuItemChanged() {
        DataStorage dataStorage = DataStorage.getInstance();
        switch (dataStorage.currentNavigationItem) {
            case BLACK_BOARD:
                setText("Schwarzes Brett", null);
                return;
            case COVER_PLAN_TODAY:
                CoverPlan today = dataStorage.coverPlanToday;
                setText(parseDayFromCoverPlan(today), parseLastUpdateFromCoverPlan(today));
                return;
            case COVER_PLAN_TOMORROW:
                CoverPlan tomorrow = dataStorage.coverPlanTomorow;
                setText(parseDayFromCoverPlan(tomorrow), parseLastUpdateFromCoverPlan(tomorrow));
                return;
            case NEWS:
                setText("Nachrichten", null);
                return;
            case APPOINTMENTS:
                setText("Termine", null);
                return;
            case PRESS:
                setText("Presse", null);
                return;
            case CANTEEN_PLAN:
            case WEBSITE:
            case MOODLE:
            case NEWSLETTER:
                return;
            default:
                throw new AssertionError("AppToolBar | unknown case");
        }
    }

    private void onViewPageChanged() {
        DataStorage dataStorage = DataStorage.getInstance();
        int i = dataStorage.currentlySelectedViewPage;
        if (i == 0) {
            setText("Schwarzes Brett", null);
        } else if (i == 1) {
            CoverPlan today = dataStorage.coverPlanToday;
            setText(parseDayFromCoverPlan(today), parseLastUpdateFromCoverPlan(today));
        } else if (i == 2) {
            CoverPlan tomorrow = dataStorage.coverPlanTomorow;
            setText(parseDayFromCoverPlan(tomorrow), parseLastUpdateFromCoverPlan(tomorrow));
        }
    }

    private void onDataReceived() {
        NavigationItem item = DataStorage.getInstance().currentNavigationItem;
        if (item == NavigationItem.COVER_PLAN_TODAY || item == NavigationItem.COVER_PLAN_TOMORROW) {
            onViewPageChanged();
        }
    }

    private String parseDayFromCoverPlan(CoverPlan coverPlan) {
        if (coverPlan == null || coverPlan.title == null) {
            return "Fehler";
        }
        return coverPlan.title.split(" ")[1].replace(",", "");
    }

    private String parseLastUpdateFromCoverPlan(CoverPlan coverPlan) {
        String str = "Fehler";
        if (coverPlan == null || coverPlan.lastUpdate == null) {
            return str;
        }
        DateFormat sourceFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        DateFormat targetFormatToolbar = new SimpleDateFormat("'Stand:' d. MMMM | HH:mm", Locale.GERMANY);
        try {
            Date date = sourceFormat.parse(coverPlan.lastUpdate);
            return targetFormatToolbar.format(date != null ? date : str);
        } catch (ParseException e) {
            return str;
        }
    }

    public void onEventTriggered(BroadcastEvent event) {

        switch (event) {
            case CURRENT_MENU_ITEM_CHANGED:
                onSelectedMenuItemChanged();
                break;
            case CURRENT_PAGE_CHANGED:
                onViewPageChanged();
                break;
            case DATA_PROVIDED:
                onDataReceived();
                break;
        }

    }
}
