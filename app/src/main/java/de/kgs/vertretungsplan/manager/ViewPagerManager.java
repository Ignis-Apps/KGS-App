package de.kgs.vertretungsplan.manager;

import android.content.Context;
import android.os.Handler;
import android.text.format.Time;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.ApplicationData;
import de.kgs.vertretungsplan.singetones.GlobalVariables;
import de.kgs.vertretungsplan.views.CoverPlanListHead;
import de.kgs.vertretungsplan.views.NavigationItem;
import de.kgs.vertretungsplan.views.adapters.ViewPageAdapter;
import de.kgs.vertretungsplan.views.fragments.BlackboardFragment;
import de.kgs.vertretungsplan.views.fragments.CoverPlanFragment;

public class ViewPagerManager implements Broadcast.Receiver {

    private final Handler scheduledRefresher = new Handler();

    private MainActivity activity;
    private GlobalVariables globalVariables;

    private ViewPager2 viewPager;
    private CoverPlanFragment today;
    private CoverPlanFragment tomorrow;
    private FirebaseManager firebaseManager;


    private Runnable refreshLaterRunnable = new Runnable() {
        @Override
        public void run() {
            refreshPageViewer();
        }
    };

    public ViewPagerManager(Context context, final Broadcast broadcast, FirebaseManager firebaseManager) {

        this.activity = (MainActivity) context;
        this.globalVariables = GlobalVariables.getInstance();
        this.firebaseManager = firebaseManager;

        broadcast.subscribe(this, BroadcastEvent.DATA_PROVIDED,
                BroadcastEvent.CURRENT_GRADE_CHANGED,
                BroadcastEvent.CURRENT_MENU_ITEM_CHANGED,
                BroadcastEvent.CURRENT_CLASS_CHANGED);

        removePreviousFragments();

        viewPager = activity.findViewById(R.id.viewpage);

        new CoverPlanListHead(activity, broadcast);

        ViewPageAdapter adapter = new ViewPageAdapter(activity);

        BlackboardFragment blackboard = new BlackboardFragment();
        today = new CoverPlanFragment(broadcast);
        tomorrow = new CoverPlanFragment(broadcast);

        adapter.addFragment(blackboard);
        adapter.addFragment(today);
        adapter.addFragment(tomorrow);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        setPageBasedOnTime(broadcast);
        viewPager.setDrawingCacheEnabled(true);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ApplicationData.getInstance().setCurrentlySelectedViewPage(position);
                broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
            }
        });


    }

    private void removePreviousFragments() {

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

        //viewPager.setCurrentItem(pageIndex);
        //ApplicationData.getInstance().setCurrentlySelectedViewPage(pageIndex);

        broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);

    }

    public void refreshPageViewer() {

        if (!this.today.isCreated() || !this.tomorrow.isCreated()) {
            scheduledRefresher.postDelayed(refreshLaterRunnable, 50);
            return;
        }

        ApplicationData applicationData = ApplicationData.getInstance();
        if (applicationData.getCoverPlanToday() == null || applicationData.getCoverPlanTomorrow() == null) {
            return;
        }

        today.setDataSet(applicationData.getCoverPlanToday());
        tomorrow.setDataSet(applicationData.getCoverPlanTomorrow());

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
        } else {
            refreshPageViewer();
        }
    }
}
