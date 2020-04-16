package de.kgs.vertretungsplan.ui.handler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.firebase.Analytics;
import de.kgs.vertretungsplan.firebase.FirebaseManager;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.ui.NavigationItem;

public final class NavigationHandler implements OnNavigationItemSelectedListener {

    private final Broadcast broadcast;
    private final GlobalVariables globalVariables = GlobalVariables.getInstance();
    private final DrawerLayout drawerLayout;
    private final Context context;
    private final NavigationView navigationView;

    public NavigationHandler(MainActivity activity, Broadcast broadcast) {
        this.broadcast = broadcast;
        this.context = activity;
        this.drawerLayout = activity.findViewById(R.id.drawer_layout);
        this.navigationView = activity.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        setupReceiver(broadcast);
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_black_board:
            case R.id.nav_today:
            case R.id.nav_tomorrow:
                break;
            case R.id.nav_moodle:
                logEvent("moodle", false);
                openInBrowser(globalVariables.school_moodle_url);
                break;
            case R.id.nav_school_mensa:
                logEvent("mensa", false);
                openInBrowser(globalVariables.school_mensa_url);
                break;
            case R.id.nav_school_newsletter:
                logEvent("newsletter", false);
                openInBrowser(globalVariables.school_newsletter_url);
                break;
            case R.id.nav_school_website:
                logEvent("schulwebseite", false);
                openInBrowser(globalVariables.school_web_page_url);
                break;
            case R.id.nav_school_website_events:
                logEvent("termine", true);
                break;
            case R.id.nav_school_website_news:
                logEvent("nachrichten", true);
                break;
            case R.id.nav_school_website_press:
                logEvent("presse", true);
                break;
            default:
                throw new AssertionError("NavigationHandler case not covered");
        }

        System.out.println("Updating");
        ApplicationData.getInstance().setCurrentNavigationItem(NavigationItem.getNavigationItemById(menuItem.getItemId()));
        broadcast.send(BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupReceiver(Broadcast broadcast) {

        broadcast.subscribe(event -> setCurrentSelectedItem(ApplicationData.getInstance().getCurrentlySelectedViewPage()), BroadcastEvent.CURRENT_PAGE_CHANGED);

        broadcast.subscribe(event -> {
            ApplicationData data = ApplicationData.getInstance();
            setTextOfMenuItem(data.getCoverPlanToday().getNavigationText(), 1);
            setTextOfMenuItem(data.getCoverPlanTomorrow().getNavigationText(), 2);
        }, BroadcastEvent.DATA_PROVIDED);
    }

    private void setCurrentSelectedItem(int index) {

        MenuItem current = navigationView.getMenu().getItem(index);
        current.setChecked(true);
        ApplicationData.getInstance().setCurrentNavigationItem(NavigationItem.getNavigationItemById(current.getItemId()));
    }

    private void setTextOfMenuItem(String text, int index) {
        this.navigationView.getMenu().getItem(index).setTitle(text);
    }

    private void logEvent(String event, boolean internal) {

        Analytics analytics = Analytics.getInstance();

        if (internal) {
            analytics.logContentSelectEvent(event, FirebaseManager.ANALYTICS_MENU_INTERNAL);
        } else {
            analytics.logContentSelectEvent(event, FirebaseManager.ANALYTICS_MENU_EXTERNAL);
        }

    }

    private void openInBrowser(String url) {
        this.context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
    }
}
