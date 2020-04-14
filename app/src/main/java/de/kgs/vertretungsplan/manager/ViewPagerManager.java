package de.kgs.vertretungsplan.manager;

import android.text.format.Time;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.ui.CoverPlanListHead;
import de.kgs.vertretungsplan.ui.NavigationItem;
import de.kgs.vertretungsplan.ui.adapters.ViewPageAdapter;

public class ViewPagerManager implements Broadcast.Receiver {

    private ViewPager2 viewPager;

    public ViewPagerManager(MainActivity activity, final Broadcast broadcast) {

        broadcast.subscribe(this, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);

        // Might not be required anymore but needs further testing
        removePreviousFragments(activity);

        viewPager = activity.findViewById(R.id.viewpage);

        new CoverPlanListHead(activity, broadcast);

        ViewPageAdapter adapter = new ViewPageAdapter(activity, broadcast);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.setDrawingCacheEnabled(true);

        setPageBasedOnTime(broadcast);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                ApplicationData.getInstance().setCurrentlySelectedViewPage(position);
                broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
            }
        });


    }

    private void removePreviousFragments(MainActivity activity) {

        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        if (fragments.isEmpty())
            return;
        for (Fragment fragment : fragments) {
            activity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        activity.getSupportFragmentManager().executePendingTransactions();

    }

    private void setPageBasedOnTime(Broadcast broadcast) {

        Time timeToday = new Time(Time.getCurrentTimezone());
        timeToday.setToNow();

        int pageIndex = 2;
        if (timeToday.hour > 6 && timeToday.hour < 15 && timeToday.weekDay != 6 && timeToday.weekDay != 0)
            pageIndex = 1;

        viewPager.setCurrentItem(pageIndex, false);
        ApplicationData.getInstance().setCurrentlySelectedViewPage(pageIndex);

        broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);

    }

    private void setPageBasedOnCurrentNavigationItem() {

        NavigationItem navigation = ApplicationData.getInstance().getCurrentNavigationItem();

        int pageIndex;
        if (navigation == NavigationItem.BLACK_BOARD)
            pageIndex = 0;
        else if (navigation == NavigationItem.COVER_PLAN_TODAY)
            pageIndex = 1;
        else if (navigation == NavigationItem.COVER_PLAN_TOMORROW)
            pageIndex = 2;
        else
            return;

        viewPager.setCurrentItem(pageIndex);
        ApplicationData.getInstance().setCurrentlySelectedViewPage(pageIndex);

    }

    @Override
    public void onEventTriggered(BroadcastEvent broadcastEvent) {
        if (broadcastEvent == BroadcastEvent.CURRENT_MENU_ITEM_CHANGED) {
            if (ApplicationData.getInstance().getCurrentNavigationItem().isOnPageViewer())
                setPageBasedOnCurrentNavigationItem();
        }
    }
}
