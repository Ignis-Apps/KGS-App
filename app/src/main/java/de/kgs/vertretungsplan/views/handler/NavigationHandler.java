package de.kgs.vertretungsplan.views.handler;

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
import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.views.NavigationItem;

public final class NavigationHandler implements OnNavigationItemSelectedListener {

    private Broadcast broadcast;
    private GlobalVariables globalVariables = GlobalVariables.getInstance();
    private DrawerLayout drawerLayout;
    private MainActivity mainActivity;
    private NavigationView navigationView;

    public NavigationHandler(MainActivity activity, DrawerLayout drawerLayout2) {
        this.broadcast = activity.broadcast;
        this.mainActivity = activity;
        this.drawerLayout = drawerLayout2;
        this.navigationView = activity.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        setupReceiver(activity.broadcast);
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {
        String str = "nachrichten";
        switch (menuItem.getItemId()) {
            case R.id.nav_black_board:
            case R.id.nav_today:
            case R.id.nav_tomorrow:
                break;
            case R.id.nav_moodle:
                logEvent("moodle", false);
                openInBrowser(globalVariables.school_moodle_url, false);
                break;
            case R.id.nav_school_mensa:
                logEvent("mensa", false);
                openInBrowser(globalVariables.school_mensa_url, false);
                break;
            case R.id.nav_school_newsletter:
                logEvent("newsletter", false);
                openInBrowser(globalVariables.school_newsletter_url, false);
                break;
            case R.id.nav_school_website:
                logEvent("schulwebseite", false);
                openInBrowser(globalVariables.school_webpage_url, false);
                break;
            case R.id.nav_school_website_events:
                logEvent("termine", true);
                openInBrowser(globalVariables.school_events_url, true);
                break;
            case R.id.nav_school_website_news:
                logEvent(str, true);
                openInBrowser(globalVariables.school_news_url, true);
                break;
            case R.id.nav_school_website_press:
                logEvent(str, true);
                openInBrowser(globalVariables.school_press_url, true);
                break;
            default:
                throw new AssertionError("NavigationHandler case not covered");
        }

        ApplicationData.getInstance().setCurrentNavigationItem(NavigationItem.getNavigationItemById(menuItem.getItemId()));
        broadcast.send(BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupReceiver(Broadcast broadcast) {

        broadcast.subscribe(event -> setCurrentSelectedItem(ApplicationData.getInstance().getCurrentlySelectedViewPage()), BroadcastEvent.CURRENT_PAGE_CHANGED);

        broadcast.subscribe(event -> {
            ApplicationData data = ApplicationData.getInstance();
            setTextOfMenuItem(data.getCoverPlanToday().getNavigationText(),1);
            setTextOfMenuItem(data.getCoverPlanTomorrow().getNavigationText(),2);
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

        FirebaseManager firebaseManager = this.mainActivity.firebaseManager;
        if (internal) {
            firebaseManager.logEventSelectContent(event, FirebaseManager.ANALYTICS_MENU_INTERNAL);
        } else {
            firebaseManager.logEventSelectContent(event, FirebaseManager.ANALYTICS_MENU_EXTERNAL);
        }
    }

    private void openInBrowser(String url, boolean inApp) {
        if (inApp) {
            this.mainActivity.webViewHandler.loadWebPage(url, false);
            return;
        }
        this.mainActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
    }
}
