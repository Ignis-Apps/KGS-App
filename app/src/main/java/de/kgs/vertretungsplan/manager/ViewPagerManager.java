package de.kgs.vertretungsplan.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.kgs.vertretungsplan.DataStorage;
import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.slide.BlackboardFragment;
import de.kgs.vertretungsplan.slide.ListViewFragment;
import de.kgs.vertretungsplan.slide.ListViewPagerAdapter;

public class ViewPagerManager implements ViewPager.OnPageChangeListener, Animation.AnimationListener{

    private Context context;
    private MainActivity act;
    private DataStorage ds;
    private FirebaseManager firebaseManager;

    public ViewPager viewPager;
    public ListViewFragment today;
    public ListViewFragment tomorrow;
    private BlackboardFragment blackboard;

    private LinearLayout coverplanLegend;
    private int coverplanLegendHeight = -1;
    private boolean isLegendGroupVisible;

    private ScaleAnimation animationLegendgroupHide, animationLegendgroupShow;
    private TranslateAnimation animationViewpagerHide,animationViewpagerShow,steady_animation;

    private final Handler animationScheduler = new Handler();

    @SuppressWarnings("FieldCanBeLocal")
    private int animationDuration = 400;
    private int animationDelay = 100;

    public ViewPagerManager(Context context, DataStorage dataStorage, FirebaseManager firebaseManager){
        this.context = context;
        this.act = (MainActivity) context;
        this.ds = dataStorage;
        this.firebaseManager = firebaseManager;

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
        today       = new ListViewFragment();
        tomorrow    = new ListViewFragment();

        ad.addFragment(blackboard);
        ad.addFragment(today);
        ad.addFragment(tomorrow);

        today.setMainActivityInterface(act);
        tomorrow.setMainActivityInterface(act);

        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(ad);
        //viewPager.setCurrentItem(0);

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

    private final Handler scheduledRefresher = new Handler();

    private Runnable refreshLaterRunnable = new Runnable() {
        @Override
        public void run() {
            refreshPageViewer();
        }
    };

    public void refreshPageViewer(){

        // Falls die Fragmente noch nicht da sind, fügen wir die Daten einfach später hinzu.
        if(!today.isCreated()||!tomorrow.isCreated()){
            scheduledRefresher.postDelayed(refreshLaterRunnable,50);
            return;
        }

        blackboard.setOnClickListener(act);

        System.out.println("REFRESHING PAGE VIEWER");

        if(ds.coverPlanToday==null||ds.coverPlanTomorow==null){
            return;
        }

        if(act.spinnerClass.getVisibility()==View.VISIBLE){
            today.setDataset(ds.coverPlanToday.getCoverItemsForClass(act.currentGradeLevel+act.currentClass));
            tomorrow.setDataset(ds.coverPlanTomorow.getCoverItemsForClass(act.currentGradeLevel+act.currentClass));
        }
        else{
            today.setDataset(ds.coverPlanToday.getCoverItemsForClass(act.currentGradeLevel));
            tomorrow.setDataset(ds.coverPlanTomorow.getCoverItemsForClass(act.currentGradeLevel));
        }

        today.setDailyMessage(ds.coverPlanToday.dailyInfoHeader,ds.coverPlanToday.getDailyInfoMessage());
        tomorrow.setDailyMessage(ds.coverPlanTomorow.dailyInfoHeader,ds.coverPlanTomorow.getDailyInfoMessage());

        today.setItemClickListener(act);
        tomorrow.setItemClickListener(act);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position){

        System.out.println("Page Selected: "  + position + "  coverplanHeight : " + coverplanLegendHeight );
        if(position == 1){
            act.currentday = 0;
        }else if(position == 2){
            act.currentday = 1;
        }

        act.navigationView.getMenu().getItem(position).setChecked(true);

        if(position==0)
            hideLegendGroup();

        if(position==1||position==2){
            showLegendGroup();
        }

        updateToolbar();
}

    public void updateToolbar(){

        if(ds.coverPlanToday==null||ds.coverPlanTomorow==null){
            return;
        }

        String datum1 = ds.coverPlanToday.title.split(" ")[0];
        String tag1 = ds.coverPlanToday.title.split(" ")[1].replace(",", "");
        act.navigationView.getMenu().getItem(1).setTitle(tag1 + ", " + datum1);

        String datum2 = ds.coverPlanTomorow.title.split(" ")[0];
        String tag2 = ds.coverPlanTomorow.title.split(" ")[1].replace(",", "");
        act.navigationView.getMenu().getItem(2).setTitle(tag2 + ", " + datum2);

        switch(viewPager.getCurrentItem()){

            case 0:
                act.toolbar.setTitle("Schwarzes Brett");
                break;
            case 1:
                act.toolbar.setTitle(tag1 + " | Stand:" + ds.coverPlanToday.lastUpdate.substring(ds.coverPlanToday.lastUpdate.indexOf(" ")));
                break;
            case 2:
                act.toolbar.setTitle(tag2 + " | Stand:" + ds.coverPlanTomorow.lastUpdate.substring(ds.coverPlanTomorow.lastUpdate.indexOf(" ")));
                break;

        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void onClick(View view){
        blackboard.onClick(view, context, firebaseManager, ds);
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

        if(act.currentday==0)
            return !ds.coverPlanToday.getDailyInfoMessage().isEmpty();
        else if(act.currentday==1)
            return !ds.coverPlanTomorow.getDailyInfoMessage().isEmpty();

        // Vieleicht irgendwann mal...

        //else if(act.currentday==2)
        //    return !ds.coverPlanDayAfterTomorow.getDailyInfoMessage().isEmpty();

        return false;

    }
}
