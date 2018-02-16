package de.kgs.vertretungsplan.manager;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import de.kgs.vertretungsplan.DataStorage;
import de.kgs.vertretungsplan.MainActivity;
import de.kgs.vertretungsplan.R;
import de.kgs.vertretungsplan.slide.BlackboardFragment;
import de.kgs.vertretungsplan.slide.ListViewFragment;
import de.kgs.vertretungsplan.slide.ListViewPagerAdapter;

/**
 * Created by janik on 15.02.2018.
 */

public class ViewPagerManager implements ViewPager.OnPageChangeListener{

    private Context context;
    private MainActivity act;
    private DataStorage ds;
    private FirebaseManager firebaseManager;

    public ViewPager viewPager;
    public ListViewFragment today;
    public ListViewFragment tomorrow;
    public BlackboardFragment blackboard;

    private LinearLayout coverplanLegend;
    private int coverplanLegendHeight = -1;
    private int previousPosition = 0;

    private ScaleAnimation animationLegendgroupHide, animationLegendgroupShow;
    private TranslateAnimation animationViewpagerHide,animationViewpagerShow;


    public ViewPagerManager(Context context, DataStorage dataStorage, FirebaseManager firebaseManager){
        this.context = context;
        this.act = (MainActivity) context;
        this.ds = dataStorage;
        this.firebaseManager = firebaseManager;

        coverplanLegend = (LinearLayout) act.findViewById(R.id.listview_legend_group);

        coverplanLegend.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(coverplanLegendHeight ==-1){
                    coverplanLegendHeight = coverplanLegend.getHeight();
                    setupAnimations();
                }

                coverplanLegend.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    coverplanLegend.getViewTreeObserver().removeOnGlobalLayoutListener (this);
                }
            }
        });

        if(act.getSupportFragmentManager().getFragments().size()>0){

            for(int pos = 0;pos<act.getSupportFragmentManager().getFragments().size();pos++){
                String tag = "android:switcher:"+ R.id.viewpage+":"+pos;
                Fragment fragment = act.getSupportFragmentManager().findFragmentByTag(tag);
                if(fragment!=null)
                    act.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
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

        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(ad);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);

        setupAnimations();
    }

    private void setupAnimations(){

        System.out.println("SETTING UP ANIMATIONS WITH HEIGHT = " + coverplanLegendHeight);

        animationLegendgroupHide = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0f);
        animationLegendgroupHide.setDuration(200);
        animationLegendgroupHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                coverplanLegend.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationLegendgroupShow = new ScaleAnimation(1.0f, 1.0f, 0f, 1f);
        animationLegendgroupShow.setDuration(200);
        animationLegendgroupShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                coverplanLegend.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationViewpagerHide = new TranslateAnimation(0,0,0,-coverplanLegendHeight);
        animationViewpagerHide.setDuration(200);
        animationViewpagerHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation keep_steady = new TranslateAnimation(0,0,0,0);
                keep_steady.setDuration(1);
                viewPager.startAnimation(keep_steady);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationViewpagerShow = new TranslateAnimation(0,0,0, coverplanLegendHeight);
        animationViewpagerShow.setDuration(200);
        animationViewpagerShow.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation keep_steady = new TranslateAnimation(0,0,0,0);
                keep_steady.setDuration(1);
                viewPager.startAnimation(keep_steady);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    public void refreshPageViewer(){

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
        blackboard.setOnClickListener(act);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position){

        if(ds.coverPlanToday==null||ds.coverPlanTomorow==null){
            return;
        }

        System.out.println("Page Selected: "  + position);
        if(position == 1){
            act.currentday = 0;
        }else if(position == 2){
            act.currentday = 1;
        }

        String datum1 = ds.coverPlanToday.title.split(" ")[0];
        String tag1 = ds.coverPlanToday.title.split(" ")[1].replace(",", "");
        act.navigationView.getMenu().getItem(1).setTitle(tag1 + ", " + datum1);

        String datum2 = ds.coverPlanTomorow.title.split(" ")[0];
        String tag2 = ds.coverPlanTomorow.title.split(" ")[1].replace(",", "");
        act.navigationView.getMenu().getItem(2).setTitle(tag2 + ", " + datum2);


        act.navigationView.getMenu().getItem(position).setChecked(true);

        switch (position){

            case 0:
                act.toolbar.setTitle("Schwarzes Brett");
                hideLegendGroup();

                break;

            case 1:
                act.toolbar.setTitle(act.getResources().getString(R.string.app_title) + " - " + tag1);
                if(previousPosition==0&& coverplanLegendHeight !=-1){
                    showLegendGroup();
                }


                break;

            case 2:
                act.toolbar.setTitle(act.getResources().getString(R.string.app_title) + " - " + tag2);
                if(previousPosition==0){
                    showLegendGroup();
                }
                break;

        }

        previousPosition = position;

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void onClick(View view){
        blackboard.onClick(view, context, firebaseManager, ds);
    }

    public void showLegendGroup(){
        coverplanLegend.startAnimation(animationLegendgroupShow);
        viewPager.startAnimation(animationViewpagerShow);
    }

    public void hideLegendGroup(){
        coverplanLegend.startAnimation(animationLegendgroupHide);
        viewPager.startAnimation(animationViewpagerHide);
    }

}
