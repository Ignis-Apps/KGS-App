package de.kgs.vertretungsplan.coverPlan;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.Date;

import de.kgs.vertretungsplan.singetones.DataStorage;


public class CoverPlanLoader extends AsyncTask<String,Void,Integer> {

    public static final int RC_LOGIN_REQUIRED = 98;
    public static final int RC_ERROR = 99;
    public static final int RC_NO_INTERNET_NO_DATASET = 100;
    public static final int RC_LATEST_DATASET = 101;
    public static final int RC_NO_INTERNET_DATASET_EXIST = 102;

    @SuppressWarnings("WeakerAccess")
    public boolean onlyLoadData = false;
    public boolean isRunning = false;

    @SuppressLint("StaticFieldLeak")
    private Context c;
    private boolean login;

    private CoverPlanLoaderCallback callback;
    private ProgressDialog dialog;

    private Trace loadDataTrace;

    private DataStorage ds = DataStorage.getInstance();


    public CoverPlanLoader(Context c, CoverPlanLoaderCallback cpli, boolean login){
        this.c= c;
        this.callback=cpli;
        this.login = login;

        loadDataTrace = FirebasePerformance.getInstance().newTrace("load_data");
    }

    // Runs on UI Thread
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        isRunning = true;

        loadDataTrace.start();

        if(login)
            dialog = ProgressDialog.show(c, "", "Anmeldung...", true);
        else
            dialog = ProgressDialog.show(c, "", "Lade Vertretungsplan...", true);

    }

    // Runs on UI Thread
    @Override
    protected void onPostExecute(Integer i) {
        super.onPostExecute(i);

        isRunning = false;

        loadDataTrace.stop();

        callback.loaderFinishedWithResponseCode(i);
        if(dialog != null) {
            if (dialog.getWindow() != null) {
                dialog.dismiss();
            }
        }
    }


    @Override
    protected Integer doInBackground(String... parameters) {

        Long startTime = System.currentTimeMillis();

        Date currentTime = Calendar.getInstance().getTime();
        DataStorage dataStorage = DataStorage.getInstance();
        JsonDataStorage storage = new JsonDataStorage();

        String COVERPLAN_TODAY_FILE = "coverPlanToday.json";
        String COVERPLAN_TOMORROW_FILE = "coverPlanTomorrow.json";

        if( isNetworkAvailable(c)&&!onlyLoadData){

            Document documentToday;
            Document documentTomorrow;

            try {

                HttpUrlConnectionHandler httpHandler = new HttpUrlConnectionHandler();
                documentToday       = httpHandler.getParsedDocument(ds.cover_plan_today);
                documentTomorrow    = httpHandler.getParsedDocument(ds.cover_plan_tomorrow);

                //documentToday       = httpHandler.getParsedDocument("http://46.38.232.163/kgsvp/Wartung.html");
                //documentTomorrow    = httpHandler.getParsedDocument("http://46.38.232.163/kgsvp/Wartung.html");

            } catch (Exception e) {
                if(e.getMessage().equals("Login needed")){
                    return RC_LOGIN_REQUIRED;
                }
                e.printStackTrace();
                Crashlytics.logException(e);
                return RC_ERROR;
            }

            Log.d("Time-Info", "Download-Time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            CoverPlan coverPlanToday;
            CoverPlan coverPlanTomorrow;

            try {
                coverPlanToday = CoverPlanAnalyser.getCoverPlan(documentToday);
                coverPlanTomorrow = CoverPlanAnalyser.getCoverPlan(documentTomorrow);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return RC_ERROR;
            }

            Log.d("Time-Info", "Analyze-Time: " + (System.currentTimeMillis() - startTime) + " ms");
            startTime = System.currentTimeMillis();

            ds.lastUpdated = currentTime;
            ds.coverPlanToday = coverPlanToday;
            ds.coverPlanTomorow = coverPlanTomorrow;

            try {
                storage.writeJSONToFile(c,JsonConverter.getJSONFromCoverPlan(coverPlanToday), COVERPLAN_TODAY_FILE);
                storage.writeJSONToFile(c, JsonConverter.getJSONFromCoverPlan(coverPlanTomorrow), COVERPLAN_TOMORROW_FILE);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

            Log.d("Time-Info", "Save-Time: " + (System.currentTimeMillis() - startTime) + " ms");

            return RC_LATEST_DATASET;

        }else {

            JSONObject json_today = storage.readJSONFromFile(c, COVERPLAN_TODAY_FILE);
            JSONObject json_tomorrow = storage.readJSONFromFile(c, COVERPLAN_TOMORROW_FILE);

            if(json_today!=null&&json_tomorrow!=null){

                try {
                    CoverPlan coverPlanToday = JsonConverter.getCoverPlanFromJSON(json_today);
                    CoverPlan coverPlanTomorrow = JsonConverter.getCoverPlanFromJSON(json_tomorrow);

                    dataStorage.coverPlanToday = coverPlanToday;
                    dataStorage.coverPlanTomorow = coverPlanTomorrow;
                } catch (Exception e){
                    Crashlytics.logException(new Exception("Files (or Code is) are broken!"));
                    System.err.println("Files (or Code is) are broken!");
                    return RC_NO_INTERNET_NO_DATASET;
                }

                if(onlyLoadData)
                    return RC_LATEST_DATASET;

                return RC_NO_INTERNET_DATASET_EXIST;

            }else {

                return RC_NO_INTERNET_NO_DATASET;
            }
        }
    }

    public void onPause(){
        if(dialog!=null)
            dialog.dismiss();
    }

    public void onStart(){
        dialog = ProgressDialog.show(c, "", "Lade Vertretungsplan...", true);
    }

    private boolean isNetworkAvailable(Context context) {

        return false;
        /*
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

         */
    }
}
