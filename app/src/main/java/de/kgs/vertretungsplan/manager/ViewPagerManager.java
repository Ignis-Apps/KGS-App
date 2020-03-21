package de.kgs.vertretungsplan.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.broadcaster.Broadcast;
import de.kgs.vertretungsplan.broadcaster.BroadcastEvent;
import de.kgs.vertretungsplan.singetones.DataStorage;
import de.kgs.vertretungsplan.slide.BlackboardFragment;
import de.kgs.vertretungsplan.slide.ListViewFragment;
import de.kgs.vertretungsplan.slide.ListViewPagerAdapter;

public class ViewPagerManager implements OnPageChangeListener, AnimationListener {

    private MainActivity act;
    private DataStorage ds;
    private FirebaseManager firebaseManager;

    private ViewPager viewPager;
    private ListViewFragment today;
    private ListViewFragment tomorrow;
    private BlackboardFragment blackboard;

    private LinearLayout coverplanLegend;
    private int coverplanLegendHeight = -1;
    private boolean isLegendGroupVisible;

    private ScaleAnimation animationLegendgroupHide, animationLegendgroupShow;
    private TranslateAnimation animationViewpagerHide, animationViewpagerShow, steady_animation;

    private final Handler animationScheduler = new Handler();

    @SuppressWarnings("FieldCanBeLocal")
    private int animationDuration = 400;
    private int animationDelay = 100;

    private Broadcast broadcast;

    public ViewPagerManager(Context context, Broadcast broadcast, FirebaseManager firebaseManager) {

        this.broadcast = broadcast;
        this.act = (MainActivity) context;
        this.ds = DataStorage.getInstance();
        this.firebaseManager = firebaseManager;
        setupObserver(broadcast);

        coverplanLegend = act.findViewById(R.id.listview_legend_group);


        coverplanLegend.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(coverplanLegendHeight ==-1){
                    coverplanLegendHeight = coverplanLegend.getHeight();
                    setupAnimations();

                    //coverplanLegend.setVisibility(View.GONE);
                    isLegendGroupVisible = true;
                    // coverplanLegend.setVisibility(View.VISIBLE);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    coverplanLegend.getViewTreeObserver().removeOnGlobalLayoutListener (this);
                }
                //showLegendGroup();
            }
        });
        if(act.getSupportFragmentManager().getFragments().size()>0){

            for(int pos = 0;pos<act.getSupportFragmentManager().getFragments().size();pos++){
                String tag = "android:switcher:"+ R.id.viewpage+":"+pos;
                Fragment fragment = act.getSupportFragmentManager().findFragmentByTag(tag);
                if(fragment!=null){
                    System.out.println("REMOVING FRAGMENT FROM FRAGMENTMANAGER");
                    act.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }

            }
            act.getSupportFragmentManager().executePendingTransactions();
        }

        viewPager = act.findViewById(R.id.viewpage);

        ListViewPagerAdapter ad = new ListViewPagerAdapter(act.getSupportFragmentManager());

        blackboard  = new BlackboardFragment();
        today = new ListViewFragment(broadcast);
        tomorrow = new ListViewFragment(broadcast);

        ad.addFragment(blackboard);
        ad.addFragment(today);
        ad.addFragment(tomorrow);

        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(ad);

        Time timeToday = new Time(Time.getCurrentTimezone());
        timeToday.setToNow();
        if(timeToday.hour > 6 && timeToday.hour < 15 && timeToday.weekDay != 6 && timeToday.weekDay != 0){
            viewPager.setCurrentItem(1);
        }else {
            viewPager.setCurrentItem(2);
        }


        viewPager.setDrawingCacheEnabled(true);
        viewPager.addOnPageChangeListener(this);

        setupAnimations();
    }

    private void setupAnimations(){

        animationLegendgroupHide = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0f);
        animationLegendgroupHide.setDuration(animationDuration);
        animationLegendgroupHide.setAnimationListener(this);

        animationLegendgroupShow = new ScaleAnimation(1.0f, 1.0f, 0f, 1f);
        animationLegendgroupShow.setDuration(animationDuration);
        animationLegendgroupShow.setAnimationListener(this);

        animationViewpagerHide = new TranslateAnimation(0,0,0,-coverplanLegendHeight);
        animationViewpagerHide.setDuration(animationDuration);
        animationViewpagerHide.setAnimationListener(this);

        animationViewpagerShow = new TranslateAnimation(0,0,0, coverplanLegendHeight);
        animationViewpagerShow.setDuration(animationDuration);
        animationViewpagerShow.setAnimationListener(this);

        steady_animation = new TranslateAnimation(0,0,0,0);
        steady_animation.setDuration(1);

    }

    private void setupObserver(Broadcast broadcast) {

        broadcast.subscribe(new Broadcast.Observer() {
            @Override
            public void onEventTriggered(BroadcastEvent event) {

                switch (ds.currentNavigationItem) {
                    case BLACK_BOARD:
                    case COVER_PLAN_TODAY:
                    case COVER_PLAN_TOMORROW:
                        viewPager.setCurrentItem(ds.currentlySelectedViewPage);
                }
            }
        }, BroadcastEvent.CURRENT_MENU_ITEM_CHANGED);

        broadcast.subscribe(new Broadcast.Observer() {
            @Override
            public void onEventTriggered(BroadcastEvent broadcastEvent) {
                refreshDataSet();
            }
        }, BroadcastEvent.CURRENT_CLASS_CHANGED, BroadcastEvent.CURRENT_GRADE_CHANGED);

        broadcast.subscribe(new Broadcast.Observer() {
            @Override
            public void onEventTriggered(BroadcastEvent event) {
                refreshPageViewer();
            }
        }, BroadcastEvent.DATA_PROVIDED);
    }

    private void refreshDataSet() {
        this.today.setDataset(ds.coverPlanToday.getCoverItems(ds.currentGrade, ds.currentGradeSubClass));
        this.tomorrow.setDataset(ds.coverPlanTomorow.getCoverItems(ds.currentGrade, ds.currentGradeSubClass));

        this.today.setDailyMessage(ds.coverPlanToday.dailyInfoHeader, ds.coverPlanToday.getDailyInfoMessage());
        this.tomorrow.setDailyMessage(ds.coverPlanTomorow.dailyInfoHeader, ds.coverPlanTomorow.getDailyInfoMessage());
    }

    private final Handler scheduledRefresher = new Handler();

    private Runnable refreshLaterRunnable = new Runnable() {
        @Override
        public void run() {
            refreshPageViewer();
        }
    };

    public void refreshPageViewer() {
        if (!this.today.isCreated() || !this.tomorrow.isCreated()) {
            scheduledRefresher.postDelayed(refreshLaterRunnable, 50);
            return;
        }
        System.out.println("REFRESHING PAGE VIEWER");
        if (this.ds.coverPlanToday != null && this.ds.coverPlanTomorow != null) {
            refreshDataSet();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

        if (position == 0) {
            hideLegendGroup();
        } else if (position == 1 || position == 2) {
            showLegendGroup();
        }
        DataStorage.getInstance().currentlySelectedViewPage = position;
        this.broadcast.send(BroadcastEvent.CURRENT_PAGE_CHANGED);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    private boolean executingAnimation;

    private void showLegendGroup(){

        if(isLegendGroupVisible||executingAnimation)
            return;

        animationScheduler.removeCallbacks(null);
        animationScheduler.postDelayed(showLegendGroupRunnable,animationDelay);

    }

    private void hideLegendGroup(){

        if(!isLegendGroupVisible||executingAnimation)
            return;

        animationScheduler.removeCallbacks(null);
        animationScheduler.postDelayed(hideLegendGroupRunnable, animationDelay);

    }

    private Runnable showLegendGroupRunnable = new Runnable() {
        @Override
        public void run() {
            coverplanLegend.startAnimation(animationLegendgroupShow);
            viewPager.startAnimation(animationViewpagerShow);
        }
    };

    private Runnable hideLegendGroupRunnable = new Runnable() {
        @Override
        public void run() {
            coverplanLegend.startAnimation(animationLegendgroupHide);
            viewPager.startAnimation(animationViewpagerHide);
        }
    };

    @Override
    public void onAnimationStart(Animation animation) {
        // FLAG die dafür sorgt, dass nur eine Animation aufeinmal abgespielt werden kann
        executingAnimation = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        executingAnimation = false;

        if(animation == animationLegendgroupShow){
            isLegendGroupVisible = true;
            coverplanLegend.setVisibility(View.VISIBLE);
        }else if(animation == animationLegendgroupHide){
            isLegendGroupVisible = false;
            coverplanLegend.setVisibility(View.GONE);
        }else if(animation == animationViewpagerShow){
            viewPager.startAnimation(steady_animation);
        }else if(animation == animationViewpagerHide){
            viewPager.startAnimation(steady_animation);
        }
        verifyAnimation();

    }

    private void verifyAnimation(){

        // Falls die Legende nicht/ungewollt abgebildet ist , wird dies hier nachträglich behoben
        if(coverplanLegend.getVisibility()==View.VISIBLE&&(viewPager.getCurrentItem()==0)){
            hideLegendGroup();
        }else if(coverplanLegend.getVisibility()==View.GONE&&(viewPager.getCurrentItem()!=0)){
            showLegendGroup();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public boolean hasDailyMessage(){
/*

/
        if(act.currentday==0)
            return !ds.coverPlanToday.getDailyInfoMessage().isEmpty();
        else if(act.currentday==1)
            return !ds.coverPlanTomorow.getDailyInfoMessage().isEmpty();

        // Vieleicht irgendwann mal...

        //else if(act.currentday==2)
        //    return !ds.coverPlanDayAfterTomorow.getDailyInfoMessage().isEmpty();
*/
        return false;

    }

}
