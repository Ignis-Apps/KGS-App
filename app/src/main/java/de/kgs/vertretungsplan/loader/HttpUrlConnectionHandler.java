package de.kgs.vertretungsplan.loader;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import de.kgs.vertretungsplan.loader.exceptions.CredentialException;
import de.kgs.vertretungsplan.loader.exceptions.DownloadException;
import de.kgs.vertretungsplan.singetones.Credentials;
import de.kgs.vertretungsplan.singetones.GlobalVariables;

class HttpUrlConnectionHandler {

    private static final String TAG = "HttpConnectionHandler";


    private static String getCoverplanData(String url, String moodleSession, boolean hasFreshCookie) throws IOException, DownloadException, CredentialException {

        System.out.println("COOKIIIIIIIIII" + moodleSession);

        if (isCookieExpired()) {
            performLogin();
        }

        Log.d(TAG, "getCoverplanData: start download data");
        URL obj = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", moodleSession);
        connection.setConnectTimeout(5000);
        connection.setInstanceFollowRedirects(false);

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        // MoodleSession expired / invalid
        if (responseCode == 303) {

            if (hasFreshCookie)
                throw new DownloadException();

            performLogin();
            return getCoverplanData(url, Credentials.getInstance().getMoodleCookie(), true);
        }

        if (responseCode != 200) {
            throw new DownloadException("Error while loading page");
        }

        Credentials.getInstance().setMoodleCookieLastUse(System.nanoTime());

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();

    }

    private static void performLogin() throws IOException, DownloadException, CredentialException {

        String parameterString = "username=" + Credentials.getInstance().getUsername() + "&password=" + Credentials.getInstance().getPassword() + "&ajax=true";

        URL obj = new URL(GlobalVariables.getInstance().login_page_url);
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();

        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(parameterString);
        conn.connect();

        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new DownloadException("Fehler");
        }

        List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
        StringBuilder cookieConcad = new StringBuilder();

        if (cookies == null) {
            throw new DownloadException("Fehler");
        }

        for (String s : cookies) {
            cookieConcad.append(s);
        }

        System.out.println(cookieConcad);
        if (!cookieConcad.toString().contains("MOODLEID1_=deleted")) {
            throw new CredentialException(null);
        }

        Pattern pattern = Pattern.compile("(MoodleSession=.*?);");
        Matcher matcher = pattern.matcher(cookieConcad);
        if (matcher.find()) {
            Credentials.getInstance().setMoodleCookie(matcher.group(1));
        } else {
            throw new CredentialException();
        }


    }


    private static boolean isCookieExpired() {

        long elapsedTime = (System.nanoTime() - Credentials.getInstance().getMoodleCookieLastUse()) / 1000000000;
        return elapsedTime > GlobalVariables.getInstance().moodleCookieMaxAgeSeconds;

    }

    Document getParsedDocument(String url) throws CredentialException, IOException, DownloadException {
        return Jsoup.parse(getCoverplanData(url, Credentials.getInstance().getMoodleCookie(), false));
    }
}