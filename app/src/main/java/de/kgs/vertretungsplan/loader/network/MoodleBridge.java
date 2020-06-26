package de.kgs.vertretungsplan.loader.network;

import android.accounts.NetworkErrorException;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import de.kgs.vertretungsplan.loader.exceptions.ContentNotProvidedException;
import de.kgs.vertretungsplan.loader.exceptions.CredentialException;
import de.kgs.vertretungsplan.loader.exceptions.DownloadException;
import de.kgs.vertretungsplan.storage.Credentials;
import de.kgs.vertretungsplan.storage.GlobalVariables;

public class MoodleBridge {

    private static final String LOGIN_URL = "https://moodle-s.kgs.hd.bw.schule.de/moodle/blocks/exa2fa/login/";
    private final int cookieMaxAge = 7199;
    private Credentials credentials = Credentials.getInstance();

    private boolean isSessionValid = false;


    public MoodleBridge() {

    }

    public void createSession(String username, String password) throws IOException, CredentialException, NetworkErrorException {

        // Check if the session is expired
        long elapsedTime = (System.nanoTime() - credentials.getMoodleCookieLastUse()) / 1000000000;
        boolean isExpired = elapsedTime > GlobalVariables.getInstance().moodleCookieMaxAgeSeconds;

        // Perform login if cookie is expired or test session fails
        if (isExpired || testSession(credentials.getMoodleCookie())) {
            String moodleCookie = performLogin(username, password);
            isSessionValid = true;
            credentials.setMoodleCookie(moodleCookie);
            credentials.setMoodleCookieLastUse(System.nanoTime());
        }

    }

    public String downloadResources(String url) throws IOException, ContentNotProvidedException, DownloadException {

        if (!isSessionValid) {
            throw new AssertionError("Session has not been created yet!");
        }

        URL obj = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);

        int response = conn.getResponseCode();

        if (response == 404) {
            throw new ContentNotProvidedException();
        }

        if (response != 200) {
            throw new DownloadException();
        }


        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();

    }

    protected boolean testSession(@Nullable String moodleSessionKey) throws IOException {

        if (moodleSessionKey == null || moodleSessionKey.isEmpty()) {
            return false;
        }

        URL obj = new URL("https://moodle-s.kgs.hd.bw.schule.de/moodle/");
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(5000);
        return conn.getResponseCode() == 200;

    }


    protected String performLogin(String user, String password) throws IOException, CredentialException, NetworkErrorException {

        String parameterString = "username=" + user + "&password=" + password + "&ajax=true";

        URL obj = new URL(LOGIN_URL);
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
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
            throw new NetworkErrorException();
        }

        return extractMoodleSession(conn.getHeaderFields().get("Set-Cookie"));

    }


    protected String extractMoodleSession(List<String> cookies) throws CredentialException {

        if (cookies == null || cookies.isEmpty()) {
            return null;
        }

        StringBuilder cookieString = new StringBuilder();
        for (String c : cookies) {
            cookieString.append(c);
        }

        if (!cookieString.toString().contains("MOODLEID1_=deleted")) {
            throw new CredentialException(null);
        }

        Pattern pattern = Pattern.compile("(MoodleSession=.*?);");
        Matcher matcher = pattern.matcher(cookieString.toString());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new CredentialException();
        }

    }

}
