package de.kgs.vertretungsplan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import de.kgs.vertretungsplan.manager.FirebaseManager;
import de.kgs.vertretungsplan.manager.ViewPagerManager;
import de.kgs.vertretungsplan.coverPlan.CoverItem;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoader;
import de.kgs.vertretungsplan.coverPlan.CoverPlanLoaderCallback;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static de.kgs.vertretungsplan.DataStorage.CURRENT_CLASS;
import static de.kgs.vertretungsplan.DataStorage.CURRENT_GRADE_LEVEL;
import static de.kgs.vertretungsplan.DataStorage.PASSWORD;
import static de.kgs.vertretungsplan.DataStorage.SHARED_PREF;
import static de.kgs.vertretungsplan.DataStorage.SHOW_SWIPE_INFO;
import static de.kgs.vertretungsplan.DataStorage.USERNAME;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,AdapterView.OnItemSelectedListener,CoverPlanLoaderCallback, AdapterView.OnItemClickListener, View.OnClickListener {
    public static final int SIGN_UP_RC = 7234;

    public FirebaseManager firebaseManager;
    public ViewPagerManager viewPagerManager;

    public int currentday;

    public String currentGradeLevel ="";
    public String currentClass = "";

    private DataStorage ds;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor sharedEditor;

    public NavigationView navigationView;

    public RelativeLayout contentMain;
    public Toolbar toolbar;

    public WebView webView;
    public ProgressDialog progressLoadingPage;
    public Spinner spinnerClass;
    public boolean showsWebView = false;

    public CoverPlanLoader loader;

    public Context context;
    public CoverPlanLoaderCallback coverPlanLoaderCallback;


    @Override
    protected void onPause() {
        super.onPause();
        if(loader!=null)
            loader.onPause();
        if(ds.responseCode == CoverPlanLoader.RC_LATEST_DATASET)
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
            showViewPager();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ds.timeMillsLastView = 0;
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

        firebaseManager = new FirebaseManager(this, ds);
        setupDataStorage();
        setupUI();
        viewPagerManager = new ViewPagerManager(this, ds, firebaseManager);
        setupBrowser();

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
        firebaseManager.setUserProperty("Stufe", ds.currentGradeLevel + "");
        ds.currentClass = sharedPreferences.getInt(CURRENT_CLASS,0);
        if(ds.currentClass > 5 || ds.currentClass < 0) {
            ds.currentClass = 0;
            sharedEditor.putInt(CURRENT_CLASS, 0);
            sharedEditor.commit();
            FirebaseCrash.report(new Exception("Magic is happening (currentClass)"));
        }
        firebaseManager.setUserProperty("Klasse", ds.currentClass + "");
        ds.password = sharedPreferences.getString(PASSWORD, "");
        ds.username = sharedPreferences.getString(USERNAME, "");
    }

    private void setupUI(){
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_title));
        setSupportActionBar(toolbar);

        contentMain         = findViewById(R.id.contentMainRl);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);


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

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupSpinner();
    }

    public void setupSpinner(){
        Spinner spinnerGradeLevel = findViewById(R.id.spinnerGrade);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGradeLevel.setAdapter(adapter);
        spinnerGradeLevel.setSelection(ds.currentGradeLevel);
        spinnerGradeLevel.setOnItemSelectedListener(this);

        spinnerClass = findViewById(R.id.spinnerClass);

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

    public void setupBrowser(){
        RelativeLayout.LayoutParams parms = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

        webView = new WebView(this);
        webView.setLayoutParams(parms);
        webView.setWebViewClient(new WebViewClient(){

            public void onPageFinished(WebView view, String url) {
                firebaseManager.loadWebpageTrace.stop();

                if(progressLoadingPage != null)
                    progressLoadingPage.dismiss();
            }

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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.nav_black_board){
            viewPagerManager.viewPager.setCurrentItem(0);
            viewPagerManager.updateToolbar();
            showViewPager();
        } else if (id == R.id.nav_today) {
            currentday = 0;
            viewPagerManager.updateToolbar();
            viewPagerManager.viewPager.setCurrentItem(1);
            showViewPager();
        } else if (id == R.id.nav_tomorrow) {
            currentday = 1;
            viewPagerManager.updateToolbar();
            viewPagerManager.viewPager.setCurrentItem(2);
            showViewPager();
        } else if (id == R.id.nav_school_mensa) {
            firebaseManager.logEventSelectContent("mensa", FirebaseManager.ANALYTICS_MENU_EXTERNAL);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_mensa_url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_school_website) {
            firebaseManager.logEventSelectContent("schulwebseite", FirebaseManager.ANALYTICS_MENU_EXTERNAL);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_webpage_url));
            startActivity(browserIntent);
        } else if (id == R.id.nav_moodle) {
            firebaseManager.logEventSelectContent("moodle", FirebaseManager.ANALYTICS_MENU_EXTERNAL);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_moodle_url));
            startActivity(browserIntent);
        } else if(id == R.id.nav_school_newsletter){
            firebaseManager.logEventSelectContent("newsletter", FirebaseManager.ANALYTICS_MENU_EXTERNAL);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ds.school_newsletter_url));
            startActivity(browserIntent);
        } else if(id == R.id.nav_school_website_news){
            firebaseManager.logEventSelectContent("nachrichten", FirebaseManager.ANALYTICS_MENU_INTERNAL);

            toolbar.setTitle("Nachrichten");
            showPageInWebview(ds.school_news_url);
        } else if(id == R.id.nav_school_website_events){
            firebaseManager.logEventSelectContent("termine", FirebaseManager.ANALYTICS_MENU_INTERNAL);

            toolbar.setTitle("Termine");
            showPageInWebview(ds.school_events_url);
        } else if( id == R.id.nav_school_website_press){
            firebaseManager.logEventSelectContent("presse", FirebaseManager.ANALYTICS_MENU_INTERNAL);

            toolbar.setTitle("Presse");
            showPageInWebview(ds.school_press_url);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        refreshCoverPlan();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        List<CoverItem> ci;

        if(currentday == 0){
            ci = viewPagerManager.today.getDataset();
        }else if(currentday == 1){
            ci = viewPagerManager.tomorrow.getDataset();
        }else {
            ci = viewPagerManager.today.getDataset();
        }

        if(i == ci.size()-1){
            firebaseManager.logEvent("rate_app");

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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View v) {
        viewPagerManager.onClick(v);
    }

    public void showPageInWebview(String url){

        firebaseManager.loadWebpageTrace.start();

        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
        showsWebView = true;

        progressLoadingPage = ProgressDialog.show(this, null , "Lädt ...", true);

    }

    public void showViewPager(){

        webView.setVisibility(View.GONE);
        showsWebView = false;
    }

    public void showInfoDialog(){

        if(sharedPreferences.getBoolean(SHOW_SWIPE_INFO,true)){

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle("Info");
            alertBuilder.setMessage("Wische nach links bzw. nach rechts, um zwischen den Tagen oder dem Schwarzen Brett zu wechseln.");
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


    }

    public void refreshCoverPlan(){

        viewPagerManager.updateToolbar();
        navigationView.getMenu().getItem(viewPagerManager.viewPager.getCurrentItem()).setChecked(true);
        showInfoDialog();
        viewPagerManager.refreshPageViewer();
    }
}
