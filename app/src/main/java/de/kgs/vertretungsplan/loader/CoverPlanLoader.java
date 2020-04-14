package de.kgs.vertretungsplan.loader;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.IOException;

import de.kgs.vertretungsplan.coverplan.CoverPlan;
import de.kgs.vertretungsplan.loader.exceptions.CredentialException;
import de.kgs.vertretungsplan.loader.exceptions.DownloadException;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.storage.json.JsonConverter;
import de.kgs.vertretungsplan.storage.json.JsonDataStorage;


public class CoverPlanLoader extends AsyncTask<String, Void, LoaderResponseCode> {

    private static final String COVERPLAN_TODAY_FILE = "coverPlanToday.json";
    private static final String COVERPLAN_TOMORROW_FILE = "coverPlanTomorrow.json";

    boolean onlyLoadOfflineData = false;

    private boolean isRunning = false;

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private boolean login;
    private CoverPlanLoaderCallback callback;
    private ProgressDialog dialog;
    private Trace loadDataTrace;
    private GlobalVariables global = GlobalVariables.getInstance();
    private ApplicationData applicationData = ApplicationData.getInstance();

    public CoverPlanLoader(Context context, CoverPlanLoaderCallback callback, boolean login) {
        this.context = context;
        this.callback = callback;
        this.login = login;

        loadDataTrace = FirebasePerformance.getInstance().newTrace("load_data");
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        isRunning = true;

        loadDataTrace.start();

        if (login)
            dialog = ProgressDialog.show(context, "", "Anmeldung...", true);
        else
            dialog = ProgressDialog.show(context, "", "Lade Vertretungsplan...", true);

    }

    @Override
    protected LoaderResponseCode doInBackground(String... parameters) {

        if (isNetworkAvailable(context) && !onlyLoadOfflineData) {
            try {
                return downloadData();
            } catch (IOException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }

        return loadOffline();

    }

    @Override
    protected void onPostExecute(LoaderResponseCode i) {
        super.onPostExecute(i);

        isRunning = false;
        loadDataTrace.stop();

        callback.loaderFinishedWithResponseCode(i);
        if (dialog != null) {
            if (dialog.getWindow() != null) {
                dialog.dismiss();
            }
        }
    }

    private LoaderResponseCode downloadData() throws IOException {

        Document documentToday;
        Document documentTomorrow;

        try {
            HttpUrlConnectionHandler httpHandler = new HttpUrlConnectionHandler();
            //final String url = "https://moodle-s.kgs.hd.bw.schule.de/moodle/mod/page/view.php?id=537";
            //documentToday = httpHandler.getParsedDocument(url);
            //documentTomorrow = httpHandler.getParsedDocument(url);

            documentToday = httpHandler.getParsedDocument(GlobalVariables.getInstance().cover_plan_today_url);
            documentTomorrow = httpHandler.getParsedDocument(GlobalVariables.getInstance().cover_plan_tomorrow_url);

        } catch (CredentialException e) {
            e.printStackTrace();
            return LoaderResponseCode.LOGIN_REQUIRED;
        } catch (DownloadException e) {
            e.printStackTrace();
            // Server Data not available
            return LoaderResponseCode.COVER_PLAN_NOT_PROVIDED;
        }

        CoverPlan coverPlanToday = CoverPlanAnalyser.getCoverPlan(documentToday);
        CoverPlan coverPlanTomorrow = CoverPlanAnalyser.getCoverPlan(documentTomorrow);

        global.lastRefreshTime = System.currentTimeMillis();
        applicationData.setCoverPlanToday(coverPlanToday);
        applicationData.setCoverPlanTomorrow(coverPlanTomorrow);

        JsonDataStorage storage = new JsonDataStorage();
        storage.writeJSONToFile(context, JsonConverter.getJSONFromCoverPlan(coverPlanToday), COVERPLAN_TODAY_FILE);
        storage.writeJSONToFile(context, JsonConverter.getJSONFromCoverPlan(coverPlanTomorrow), COVERPLAN_TOMORROW_FILE);


        return LoaderResponseCode.LATEST_DATA_SET;


    }

    private LoaderResponseCode loadOffline() {

        JsonDataStorage storage = new JsonDataStorage();
        JSONObject json_today = storage.readJSONFromFile(context, COVERPLAN_TODAY_FILE);
        JSONObject json_tomorrow = storage.readJSONFromFile(context, COVERPLAN_TOMORROW_FILE);

        if (json_today == null || json_tomorrow == null)
            return LoaderResponseCode.NO_INTERNET_NO_DATA_SET;

        try {
            CoverPlan coverPlanToday = JsonConverter.getCoverPlanFromJSON(json_today);
            CoverPlan coverPlanTomorrow = JsonConverter.getCoverPlanFromJSON(json_tomorrow);
            applicationData.setCoverPlanToday(coverPlanToday);
            applicationData.setCoverPlanTomorrow(coverPlanTomorrow);
        } catch (JSONException e) {
            Crashlytics.logException(new Exception("Files (or Code is) are broken!"));
            System.err.println("Files (or Code is) are broken!");
            return LoaderResponseCode.NO_INTERNET_NO_DATA_SET;
        }

        if (onlyLoadOfflineData)
            return LoaderResponseCode.LATEST_DATA_SET;

        return LoaderResponseCode.NO_INTERNET_DATA_SET_EXISTS;


    }

    void onPause() {
        if (dialog != null)
            dialog.dismiss();
    }

    void onStart() {
        if (dialog == null || dialog.isShowing())
            return;
        dialog = ProgressDialog.show(context, "", "Lade Vertretungsplan...", true);
    }

    public void loadOfflineData() {
        this.onlyLoadOfflineData = true;
        super.execute();
    }

    boolean isRunning() {
        return isRunning;
    }

    private boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();


    }
}
