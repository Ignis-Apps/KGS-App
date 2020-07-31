package de.kgs.vertretungsplan.loader;

import android.accounts.NetworkErrorException;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import de.kgs.vertretungsplan.coverplan.CoverPlan;
import de.kgs.vertretungsplan.loader.exceptions.ContentNotProvidedException;
import de.kgs.vertretungsplan.loader.exceptions.CredentialException;
import de.kgs.vertretungsplan.loader.exceptions.DownloadException;
import de.kgs.vertretungsplan.loader.network.MoodleBridge;
import de.kgs.vertretungsplan.storage.ApplicationData;
import de.kgs.vertretungsplan.storage.Credentials;
import de.kgs.vertretungsplan.storage.GlobalVariables;
import de.kgs.vertretungsplan.storage.json.JsonConverter;
import de.kgs.vertretungsplan.storage.json.JsonDataStorage;


public class CoverPlanLoader extends AsyncTask<String, Void, LoaderResponseCode> {

    private static final String COVERPLAN_TODAY_FILE = "coverPlanToday.json";
    private static final String COVERPLAN_TOMORROW_FILE = "coverPlanTomorrow.json";

    private final boolean login;
    private final CoverPlanLoaderCallback callback;
    private final Trace loadDataTrace;
    private final GlobalVariables global = GlobalVariables.getInstance();
    private final ApplicationData applicationData = ApplicationData.getInstance();
    private final MoodleBridge moodleBridge;
    private final Credentials credential = Credentials.getInstance();

    boolean onlyLoadOfflineData = false;
    private final Context context;
    private boolean isRunning = false;
    private ProgressDialog dialog;


    public CoverPlanLoader(Context context, CoverPlanLoaderCallback callback, MoodleBridge bridge, boolean login) {
        this.context = context;
        this.callback = callback;
        this.login = login;
        this.moodleBridge = bridge;

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
            } catch (IOException | NetworkErrorException e) {
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

    private LoaderResponseCode downloadData() throws IOException, NetworkErrorException {

        Document documentToday;
        Document documentTomorrow;

        try {

            moodleBridge.createSession(credential.getUsername(), credential.getPassword());
            documentToday = Jsoup.parse(moodleBridge.downloadResources(GlobalVariables.getInstance().cover_plan_today_url));
            documentTomorrow = Jsoup.parse(moodleBridge.downloadResources(GlobalVariables.getInstance().cover_plan_tomorrow_url));


        } catch (CredentialException e) {
            e.printStackTrace();
            return LoaderResponseCode.LOGIN_REQUIRED;
        } catch (DownloadException | ContentNotProvidedException e) {
            e.printStackTrace();
            // Server Data not available
            return LoaderResponseCode.COVER_PLAN_NOT_PROVIDED;
        }

        CoverPlan coverPlanToday = CoverPlanParser.getCoverPlan(documentToday);
        CoverPlan coverPlanTomorrow = CoverPlanParser.getCoverPlan(documentTomorrow);

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
