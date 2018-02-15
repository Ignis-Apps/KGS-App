package de.kgs.vertretungsplan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import de.kgs.vertretungsplan.Slide.BlackboardFragment;
import de.kgs.vertretungsplan.Slide.ListViewFragment;
import de.kgs.vertretungsplan.Slide.ListViewPagerAdapter;
import de.kgs.vertretungsplan.CoverPlan.CoverItem;
import de.kgs.vertretungsplan.CoverPlan.CoverPlanLoader;
import de.kgs.vertretungsplan.CoverPlan.CoverPlanLoaderCallback;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static de.kgs.vertretungsplan.DataStorage.CURRENT_CLASS;
import static de.kgs.vertretungsplan.DataStorage.CURRENT_GRADE_LEVEL;
import static de.kgs.vertretungsplan.DataStorage.PASSWORD;
import static de.kgs.vertretungsplan.DataStorage.SHARED_PREF;
import static de.kgs.vertretungsplan.DataStorage.SHOW_SWIPE_INFO;
import static de.kgs.vertretungsplan.DataStorage.USERNAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,ViewPager.OnPageChangeListener, AdapterView.OnItemSelectedListener,CoverPlanLoaderCallback, AdapterView.OnItemClickListener, View.OnLongClickListener {
    public static final int SIGN_UP_RC = 7234;
    public static final String ANALYTICS_MENU_INTERNAL = "internal";
    public static final String ANALYTICS_MENU_EXTERNAL = "external";


    public int currentday;

    public String currentGradeLevel ="";
    public String currentClass = "";

    private DataStorage ds;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedEditor;

    public NavigationView navigationView;

    public RelativeLayout contentMain;
    public Toolbar toolbar;

    //private LinearLayout spinnerView,listviewItemHeader;
    private LinearLayout listviewLegendGroup;
    private int listviewLegendGroupHeight = -1;
   private int previousPosition = 0;

    public WebView webView;
    public ProgressDialog progressLoadingPage;
    private Spinner spinnerClass;
    public boolean showsWebView = false;

    public CoverPlanLoader loader;
    private boolean quickStart = false;

    private ViewPager viewPager;
    private ListViewFragment today;
    private ListViewFragment tomorrow;
    private BlackboardFragment blackboard;

    private Trace loadWebpageTrace;
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    private Context context;
    private CoverPlanLoaderCallback coverPlanLoaderCallback;

    @Override
    protected void onPause() {
        super.onPause();
        if(loader!=null)
            loader.onPause();
        ds.timeMillsLastView = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ds = DataStorage.getInstance();

        context = this;
        coverPlanLoaderCallback = this;

        if(ds.username == null){
            restart();
        }

        if(loader != null){
            if(loader.isRunning){
                loader.onStart();
            }
        }

        if(System.currentTimeMillis() - ds.timeMillsLastView > 600000){
            loader = new CoverPlanLoader(this,this, false);
            loader.execute();
            showCoverplanListview();
        }

    }

    public void restart(){

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        finish();
        Runtime.getRuntime().exit(0);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ds = DataStorage.getInstance();

        setupFirebase();
        setupDataStorage();
        setupUI();
        setupPageViewer();
        setupBrowser();
        setupAnimations();


    }

    private void setupFirebase(){
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        loadWebpageTrace = FirebasePerformance.getInstance().newTrace("load_webpage");

        firebaseRemoteConfig  = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        ds.login_page_url = firebaseRemoteConfig.getString(DataStorage.LOGIN_PAGE_URL);

        ds.cover_plan_today = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TODAY);
        ds.cover_plan_tomorrow = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TOMORROW);

        ds.school_news_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWS_URL);
        ds.school_events_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_EVENTS_URL);
        ds.school_press_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_PRESS_URL);

        ds.school_newsletter_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWSLETTER_URL);
        ds.school_moodle_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MOODLE_URL);
        ds.school_webpage_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_WEBPAGE_URL);
        ds.school_mensa_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MENSA_URL);

        firebaseRemoteConfig.fetch(86400)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            firebaseRemoteConfig.activateFetched();

                            ds.login_page_url = firebaseRemoteConfig.getString(DataStorage.LOGIN_PAGE_URL);

                            ds.cover_plan_today = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TODAY);
                            ds.cover_plan_tomorrow = firebaseRemoteConfig.getString(DataStorage.COVER_PLAN_TOMORROW);

                            ds.school_news_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWS_URL);
                            ds.school_events_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_EVENTS_URL);
                            ds.school_press_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_PRESS_URL);

                            ds.school_newsletter_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_NEWSLETTER_URL);
                            ds.school_moodle_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MOODLE_URL);
                            ds.school_webpage_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_WEBPAGE_URL);
                            ds.school_mensa_url = firebaseRemoteConfig.getString(DataStorage.SCHOOL_MENSA_URL);

                        } else {
                            FirebaseCrash.report(task.getException());
                        }
                    }
                });
    }

    private void setupDataStorage(){
        sharedPreferences = this.getSharedPreferences(SHARED_PREF, 0);
        sharedEditor = sharedPreferences.edit();
        sharedEditor.commit();
        ds.currentGradeLevel = sharedPreferences.getInt(CURRENT_GRADE_LEVEL, 0);
        if(ds.currentGradeLevel > 8 || ds.currentGradeLevel < 0) {
            ds.currentGradeLevel = 0;
            sharedEditor.putInt(CURRENT_GRADE_LEVEL, 0);
            sharedEditor.commit();
            FirebaseCrash.report(new Exception("Magic is happening (currentGradeLevel)"));
        }
        firebaseAnalytics.setUserProperty("Stufe", ds.currentGradeLevel + "");
        ds.currentClass = sharedPreferences.getInt(CURRENT_CLASS,0);
        if(ds.currentClass > 5 || ds.currentClass < 0) {
            ds.currentClass = 0;
            sharedEditor.putInt(CURRENT_CLASS, 0);
            sharedEditor.commit();
            FirebaseCrash.report(new Exception("Magic is happening (currentClass)"));
        }
        firebaseAnalytics.setUserProperty("Klasse", ds.currentClass + "");
        ds.password = sharedPreferences.getString(PASSWORD, "");
        ds.username = sharedPreferences.getString(USERNAME, "");
    }

    private void setupUI(){
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_title));
        setSupportActionBar(toolbar);

        listviewLegendGroup = (LinearLayout) findViewById(R.id.listview_legend_group);
        contentMain         = (RelativeLayout) findViewById(R.id.contentMainRl);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        listviewLegendGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

               /// System.out.println("GLOBAL LAYOUT CALLED");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    listviewLegendGroup.getViewTreeObserver().removeOnGlobalLayoutListener (this);

                    if(listviewLegendGroupHeight==-1){
                        listviewLegendGroupHeight = listviewLegendGroup.getHeight();
                        setupAnimations();
                    }

                    listviewLegendGroup.setVisibility(View.GONE);
                }
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        if(today.hour > 6 && today.hour < 15 && today.weekDay != 6 && today.weekDay != 0){
            currentday = 0;
        }else {
            currentday = 1;
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupSpinner();

    }

    public void setupSpinner(){

        Spinner spinnerGradeLevel = (Spinner) findViewById(R.id.spinnerGrade);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGradeLevel.setAdapter(adapter);
        spinnerGradeLevel.setSelection(ds.currentGradeLevel);
        spinnerGradeLevel.setOnItemSelectedListener(this);

        spinnerClass = (Spinner) findViewById(R.id.spinnerClass);

        ArrayAdapter<CharSequence> adapterClass = ArrayAdapter.createFromResource(this, R.array.spinner_array_class, R.layout.spinner_item);
        adapterClass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapterClass);
        spinnerClass.setSelection(ds.currentClass);
        spinnerClass.setOnItemSelectedListener(this);

        if(ds.currentGradeLevel!=0&&ds.currentGradeLevel<7)
            spinnerClass.setVisibility(View.VISIBLE);
        else
            spinnerClass.setVisibility(View.GONE);

    }

    public void setupPageViewer(){

        if(getSupportFragmentManager().getFragments().size()>0){

            for(int pos = 0;pos<getSupportFragmentManager().getFragments().size();pos++){
                String tag = "android:switcher:"+ R.id.viewpage+":"+pos;
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if(fragment!=null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
            getSupportFragmentManager().executePendingTransactions();
        }

        viewPager = findViewById(R.id.viewpage);

        ListViewPagerAdapter ad = new ListViewPagerAdapter(getSupportFragmentManager());

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

    }

    public void setupBrowser(){

        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

        webView = new WebView(this);
        webView.setLayoutParams(parms);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            public void onPageFinished(WebView view, String url) {
                loadWebpageTrace.stop();

                if(progressLoadingPage != null)
                    progressLoadingPage.dismiss();
            }

            /*public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return false;
            }*/
        });

        webView.setVisibility(View.GONE);
        contentMain.addView(webView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == LoginActivity.SUCCESS_RC){
            refreshCoverPlan();
        }else {
            FirebaseCrash.report(new Exception("Login Crash: No Response Code!"));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.nav_black_board){
            viewPager.setCurrentItem(0);

        } else if (id == R.id.nav_today) {
            currentday = 0;
            viewPager.setCurrentItem(1);
            showCoverplanListview();
        } else if (id == R.id.nav_tomorrow) {
            currentday = 1;
            viewPager.setCurrentItem(2);
            showCoverplanListview();
        } else if (id == R.id.nav_school_mensa) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mensa");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_EXTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_mensa_url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_school_website) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "schulwebseite");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_EXTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_webpage_url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_moodle) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "moodle");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_EXTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_moodle_url));
            startActivity(browserIntent);
        } else if(id == R.id.nav_school_newsletter){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "newsletter");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_EXTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_newsletter_url));
            startActivity(browserIntent);
        } else if(id == R.id.nav_school_website_news){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "nachrichten");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_INTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            toolbar.setTitle("Nachrichten");
            showPageInWebview(ds.school_news_url);
        } else if(id == R.id.nav_school_website_events){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "termine");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_INTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            toolbar.setTitle("Termine");
            showPageInWebview(ds.school_events_url);
        } else if( id == R.id.nav_school_website_press){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "presse");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ANALYTICS_MENU_INTERNAL);
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            toolbar.setTitle("Presse");
            showPageInWebview(ds.school_press_url);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void refreshCoverPlan(){

        String datum1 = ds.coverPlanToday.title.split(" ")[0];
        String tag1 = ds.coverPlanToday.title.split(" ")[1].replace(",", "");
        navigationView.getMenu().getItem(1).setTitle(tag1 + ", " + datum1);

        String datum2 = ds.coverPlanTomorow.title.split(" ")[0];
        String tag2 = ds.coverPlanTomorow.title.split(" ")[1].replace(",", "");
        navigationView.getMenu().getItem(2).setTitle(tag2 + ", " + datum2);


        switch (viewPager.getCurrentItem()){

            case 0:
                toolbar.setTitle("Schwarzes Brett");
                break;

            case 1:
                toolbar.setTitle(getResources().getString(R.string.app_title) + " - " + tag1);
                break;

            case 2:
                toolbar.setTitle(getResources().getString(R.string.app_title) + " - " + tag2);
                break;

        }

        navigationView.getMenu().getItem(viewPager.getCurrentItem()).setChecked(true);
        showInfoDialog();
        refreshPageViewer();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

        switch (adapterView.getItemAtPosition(pos).toString()){

            case "Alle Klassen":
                currentGradeLevel = "";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 0);
                spinnerClass.setVisibility(View.GONE);
                break;
            case "5. Klasse":
                currentGradeLevel = "5";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 1);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "6. Klasse":
                currentGradeLevel = "6";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 2);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "7. Klasse":
                currentGradeLevel = "7";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 3);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "8. Klasse":
                currentGradeLevel = "8";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 4);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "9. Klasse":
                currentGradeLevel = "9";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 5);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "10. Klasse":
                currentGradeLevel = "10";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 6);
                spinnerClass.setVisibility(View.VISIBLE);
                break;
            case "Jahrgangsstufe 1":
                currentGradeLevel = "J1";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 7);
                spinnerClass.setVisibility(View.GONE);
                break;
            case "Jahrgangsstufe 2":
                currentGradeLevel = "J2";
                sharedEditor.putInt(CURRENT_GRADE_LEVEL, 8);
                spinnerClass.setVisibility(View.GONE);
                break;

            case "Alle":
                currentClass ="";
                sharedEditor.putInt(CURRENT_CLASS, 0);
                break;

            case "a":
                currentClass ="a";
                sharedEditor.putInt(CURRENT_CLASS, 1);
                break;

            case "b":
                currentClass ="b";
                sharedEditor.putInt(CURRENT_CLASS, 2);
                break;

            case "c":
                currentClass ="c";
                sharedEditor.putInt(CURRENT_CLASS, 3);
                break;

            case "d":
                currentClass ="d";
                sharedEditor.putInt(CURRENT_CLASS, 4);
                break;

            case "e":
                currentClass ="e";
                sharedEditor.putInt(CURRENT_CLASS, 5);
                break;

        }

        sharedEditor.commit();

        if(ds.responseCode>100)
            refreshCoverPlan();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        List<CoverItem> ci = null;

        if(currentday == 0){
            ci = today.getDataset();
        }else if(currentday == 1){
            ci = tomorrow.getDataset();
        }



        if(i == ci.size()-1){
            Bundle bundle = new Bundle();
            firebaseAnalytics.logEvent("rate_app", bundle);

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.kgs.vertretungsplan")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=de.kgs.vertretungsplan")));
            }
        }else if(i==0){

        } else{
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alertdialog_item_info, null);

            if(!ci.get(i).Class.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.klasseTv)).setText(ci.get(i).Class);
            }else {
                dialogView.findViewById(R.id.llClass).setVisibility(View.GONE);
            }

            if(!ci.get(i).Hour.trim().equals("")){
                if(ci.get(i).getsDropped()){
                    ((TextView)dialogView.findViewById(R.id.stundeTv)).setText(ci.get(i).Hour + "  (Entfall)");
                }else {
                    ((TextView)dialogView.findViewById(R.id.stundeTv)).setText(ci.get(i).Hour);
                }
            }else {
                dialogView.findViewById(R.id.llHour).setVisibility(View.GONE);
            }

            if(!ci.get(i).Fach.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.fachTv)).setText(ci.get(i).Fach);
            }else {
                dialogView.findViewById(R.id.llFach).setVisibility(View.GONE);
            }

            if(!ci.get(i).Room.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.raumTv)).setText(ci.get(i).Room);
            }else {
                dialogView.findViewById(R.id.llRoom).setVisibility(View.GONE);
            }

            if(!ci.get(i).Annotation.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.annotationTv)).setText(ci.get(i).Annotation);
            }else {
                dialogView.findViewById(R.id.llAnnotation).setVisibility(View.GONE);
            }

            if(!ci.get(i).Ver_From.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.ver_fromTv)).setText(ci.get(i).Ver_From);
            }else {
                dialogView.findViewById(R.id.llVerFrom).setVisibility(View.GONE);
            }

            if(!ci.get(i).Annotation_Lesson.trim().equals("")){
                ((TextView)dialogView.findViewById(R.id.annotation_lessonTv)).setText(ci.get(i).Annotation_Lesson);
            }else {
                dialogView.findViewById(R.id.llAnnotationLesson).setVisibility(View.GONE);
            }


            alertBuilder.setTitle("Informationen");
            alertBuilder.setIcon(R.drawable.ic_action_info);

            alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            alertBuilder.setView(dialogView);
            alertBuilder.create().show();
        }

    }

    public void showPageInWebview(String url){

        loadWebpageTrace.start();

        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
        showsWebView = true;

        progressLoadingPage = ProgressDialog.show(this, null , "Lädt ...", true);

    }

    public void showCoverplanListview(){

        webView.setVisibility(View.GONE);
        showsWebView = false;

    }

    public void showInfoDialog(){

        if(sharedPreferences.getBoolean(SHOW_SWIPE_INFO,true)){

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Info");
            alertBuilder.setMessage("Wische nach links bzw. nach rechts, um den Tag zu wechseln.");
            alertBuilder.setIcon(R.drawable.ic_action_info);
            alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            alertBuilder.create().show();

            sharedEditor.putBoolean(SHOW_SWIPE_INFO,false);
            sharedEditor.commit();

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void loaderFinishedWithResponseCode(int ResponseCode) {

        ds.responseCode = ResponseCode;

        switch (ResponseCode){

            case CoverPlanLoader.RC_LATEST_DATASET:
                refreshCoverPlan();
                break;

            case CoverPlanLoader.RC_NO_INTERNET_DATASET_EXIST:
                refreshCoverPlan();
                Snackbar.make(contentMain, "Keine Internetverbindung! Der Vertretungsplan ist möglicherweise veraltet.", Snackbar.LENGTH_LONG).show();
                break;

            case CoverPlanLoader.RC_NO_INTERNET_NO_DATASET:
                Snackbar.make(contentMain, "Keine Internetverbindung! Bitte aktiviere WLAN oder mobile Daten.", Snackbar.LENGTH_LONG).show();
                break;

            case CoverPlanLoader.RC_LOGIN_REQUIRED:

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(false);
                alertBuilder.setTitle("Anmeldung erforderlich");
                alertBuilder.setMessage("Um auf den Vertretungsplan zugreifen zu können, müssen Sie sich aus Datenschutzgründen mit ihrem Moodle-Account oder dem offiziellen Zugang anmelden. Um den offiziellen Zugang zu erhalten wenden Sie sich bitte an die Schulleitung. Der Nutzername und das Passwort für diesen Zugang werden aus Sicherheitsgründen in regelmäßigen Abständen geändert.");
                alertBuilder.setIcon(R.drawable.ic_alert_error);
                alertBuilder.setPositiveButton("Anmelden", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, SIGN_UP_RC);
                    }
                });
                alertBuilder.setNegativeButton("App beenden", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                });
                alertBuilder.create().show();

                break;

            case CoverPlanLoader.RC_ERROR:

                AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
                aBuilder.setCancelable(false);
                aBuilder.setTitle("Netzwerkfehler");
                aBuilder.setMessage("Beim Herrunterladen der Daten ist ein Fehler aufgetreten. Bitte überprüfen Sie Ihre Internetverbindung und versuchen Sie es erneut.");
                aBuilder.setIcon(R.drawable.ic_alert_error);
                aBuilder.setPositiveButton("Wiederholen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        loader =  new CoverPlanLoader(context,coverPlanLoaderCallback,false);
                        loader.execute();


                    }
                });
                aBuilder.setNegativeButton("App beenden", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                });
                aBuilder.create().show();

                break;

        }

        //ds.coverPlanToday.log();
        //ds.coverPlanTomorow.log();


    }

    public void refreshPageViewer(){

        if(spinnerClass.getVisibility()==View.VISIBLE){
            today.setDataset(ds.coverPlanToday.getCoverItemsForClass(currentGradeLevel+currentClass));
            tomorrow.setDataset(ds.coverPlanTomorow.getCoverItemsForClass(currentGradeLevel+currentClass));
        }
        else{
            today.setDataset(ds.coverPlanToday.getCoverItemsForClass(currentGradeLevel));
            tomorrow.setDataset(ds.coverPlanTomorow.getCoverItemsForClass(currentGradeLevel));
        }

        today.setDailyMessage(ds.coverPlanToday.dailyInfoHeader,ds.coverPlanToday.getDailyInfoMessage());
        tomorrow.setDailyMessage(ds.coverPlanTomorow.dailyInfoHeader,ds.coverPlanTomorow.getDailyInfoMessage());

        today.setItemClickListener(this);
        tomorrow.setItemClickListener(this);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private void setHeight(View view, int height){
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public void onPageSelected(int position){

        if(ds.coverPlanToday==null||ds.coverPlanTomorow==null){
            return;
        }

        System.out.println("Page Selected "  + position);
        currentday = position;

        String datum1 = ds.coverPlanToday.title.split(" ")[0];
        String tag1 = ds.coverPlanToday.title.split(" ")[1].replace(",", "");
        navigationView.getMenu().getItem(1).setTitle(tag1 + ", " + datum1);

        String datum2 = ds.coverPlanTomorow.title.split(" ")[0];
        String tag2 = ds.coverPlanTomorow.title.split(" ")[1].replace(",", "");
        navigationView.getMenu().getItem(2).setTitle(tag2 + ", " + datum2);


        navigationView.getMenu().getItem(position).setChecked(true);

        switch (position){

            case 0:
                toolbar.setTitle("Schwarzes Brett");
                hideLegendGroup();

                break;

            case 1:
                toolbar.setTitle(getResources().getString(R.string.app_title) + " - " + tag1);
                if(previousPosition==0&&listviewLegendGroupHeight!=-1){
                    showLegendGroup();
                }


                break;

            case 2:
                toolbar.setTitle(getResources().getString(R.string.app_title) + " - " + tag2);
                if(previousPosition==0){
                    showLegendGroup();
                }
                break;

        }

        previousPosition = position;

    }

    ScaleAnimation animationLegendgroupHide, animationLegendgroupShow;
    TranslateAnimation animationViewpagerHide,animationViewpagerShow;

    private void setupAnimations(){

        System.out.println("SETTING UP ANIMATIONS WITH HEIGHT = " + listviewLegendGroupHeight);

        animationLegendgroupHide = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0f);
        animationLegendgroupHide.setDuration(500);
        animationLegendgroupHide.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listviewLegendGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationLegendgroupShow = new ScaleAnimation(1.0f, 1.0f, 0f, 1f);
        animationLegendgroupShow.setDuration(500);
        animationLegendgroupShow.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                listviewLegendGroup.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationViewpagerHide = new TranslateAnimation(0,0,0,-listviewLegendGroupHeight);
        animationViewpagerHide.setDuration(500);
        animationViewpagerHide.setAnimationListener(new AnimationListener() {
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

        animationViewpagerShow = new TranslateAnimation(0,0,0,listviewLegendGroupHeight);
        animationViewpagerShow.setDuration(500);
        animationViewpagerShow.setAnimationListener(new AnimationListener() {
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

    private void showLegendGroup(){
        listviewLegendGroup.startAnimation(animationLegendgroupShow);
        viewPager.startAnimation(animationViewpagerShow);
    }

    private void hideLegendGroup(){
        listviewLegendGroup.startAnimation(animationLegendgroupHide);
        viewPager.startAnimation(animationViewpagerHide);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onLongClick(View view) {

        System.out.println("onLongClick " + view);

        return false;
    }

}
