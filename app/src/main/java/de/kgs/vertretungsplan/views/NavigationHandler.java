package de.kgs.vertretungsplan.views;

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
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.singetones.DataStorage;


public final class NavigationHandler implements OnNavigationItemSelectedListener {

    private Broadcast broadcast;
    private DataStorage dataStorage = DataStorage.getInstance();
    private DrawerLayout drawerLayout;
    private MainActivity mainActivity;
    private NavigationView navigationView;
    private ViewPagerManager viewPagerManager;
    private KgsWebView webView;

    public NavigationHandler(MainActivity activity, DrawerLayout drawerLayout2) {
        this.broadcast = activity.broadcast;
        this.mainActivity = activity;
        this.drawerLayout = drawerLayout2;
        this.viewPagerManager = activity.viewPagerManager;
        this.webView = activity.kgsWebView;
        this.navigationView = activity.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        setupObserver(activity.broadcast);
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {
        String str = "nachrichten";
        switch (menuItem.getItemId()) {
            case R.id.nav_black_board:
                updateViewPager(0);
                break;
            case R.id.nav_moodle:
                logEvent("moodle", false);
                openInBrowser(this.dataStorage.school_moodle_url, false);
                break;
            case R.id.nav_school_mensa:
                logEvent("mensa", false);
                openInBrowser(this.dataStorage.school_mensa_url, false);
                break;
            case R.id.nav_school_newsletter:
                logEvent("newsletter", false);
                openInBrowser(this.dataStorage.school_newsletter_url, false);
                break;
            case R.id.nav_school_website:
                logEvent("schulwebseite", false);
                openInBrowser(this.dataStorage.school_webpage_url, false);
                break;
            case R.id.nav_school_website_events:
                logEvent("termine", true);
                openInBrowser(this.dataStorage.school_events_url, true);
                break;
            case R.id.nav_school_website_news:
                logEvent(str, true);
                openInBrowser(this.dataStorage.school_news_url, true);
                break;
            case R.id.nav_school_website_press:
                logEvent(str, true);
                openInBrowser(this.dataStorage.school_press_url, true);
                break;
            case R.id.nav_today:
                this.mainActivity.currentDay = 0;
                updateViewPager(1);
                break;
            case R.id.nav_tomorrow:
                this.mainActivity.currentDay = 1;
                updateViewPager(2);
                break;
            default:
                throw new AssertionError("NavigationHandler case not covered");
        }

        dataStorage.currentNavigationItem = NavigationItem.getNavigationItemById(menuItem.getItemId());
        broadcast.send(BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupObserver(Broadcast broadcast) {
        broadcast.subscribe(new Broadcast.Observer() {
            @Override
            public void onEventTriggered(BroadcastEvent event) {
                NavigationHandler.this.setCurrentSelectedItem(DataStorage.getInstance().currentlySelectedViewPage);
            }
        }, BroadcastEvent.CURRENT_PAGE_CHANGED);

        broadcast.subscribe(new Broadcast.Observer() {

            public void onEventTriggered(BroadcastEvent event) {
                DataStorage dataStorage = DataStorage.getInstance();

                String day1 = dataStorage.coverPlanToday.title.split(" ")[0];
                String date1 = dataStorage.coverPlanToday.title.split(" ")[1].replace(",", "");
                setTextOfMenuItem(date1 + ", " + day1, 1);

                String day2 = dataStorage.coverPlanTomorow.title.split(" ")[0];
                String date2 = dataStorage.coverPlanTomorow.title.split(" ")[1].replace(",", "");
                setTextOfMenuItem(date2 + ", " + day2, 2);


            }
        }, BroadcastEvent.DATA_PROVIDED);
    }

    private void setCurrentSelectedItem(int index) {
        this.navigationView.getMenu().getItem(index).setChecked(true);
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

    private void updateViewPager(int index) {
        this.dataStorage.currentlySelectedViewPage = index;
        this.broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
    }

    private void openInBrowser(String url, boolean inApp) {
        if (inApp) {
            this.mainActivity.kgsWebView.loadWebPage(url, false);
            return;
        }
        this.mainActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
    }
}
