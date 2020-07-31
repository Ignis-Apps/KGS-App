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

public class MoodleBridge {

    private static final String LOGIN_URL = "https://moodle-s.kgs.hd.bw.schule.de/moodle/blocks/exa2fa/login/";
    private final Credentials credentials = Credentials.getInstance();

    private boolean sessionValid = false;
    private boolean inRecreationMode = false;

    public MoodleBridge() {

    }

    public void createSession(String username, String password) throws IOException, CredentialException, NetworkErrorException {

        if (sessionValid)
            return;

        // Check if the session is expired
        boolean isTestSessionValid = testSession(credentials.getMoodleCookie());

        // Perform login if cookie is expired (test session fails)
        if (!isTestSessionValid) {
            System.out.println(username + " " + password + "---------------------------");
            String moodleCookie = performLogin(username, password);
            System.out.println(moodleCookie);
            credentials.setMoodleCookie(moodleCookie);
            credentials.setMoodleCookieLastUse(System.nanoTime());
        }

        sessionValid = true;

    }


    public String downloadResources(String url) throws IOException, ContentNotProvidedException, DownloadException {

        if (!sessionValid) {
            throw new AssertionError("Session has not been created yet!");
        }

        URL obj = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", credentials.getMoodleCookie());
        connection.setConnectTimeout(5000);
        connection.setInstanceFollowRedirects(false);

        int response = connection.getResponseCode();

        if (response == 404) {
            throw new ContentNotProvidedException();
        }

        if (response == 303) {

            String location = connection.getHeaderField("Location");
            if (location != null && location.contains("/login/") && !inRecreationMode) {
                try {
                    inRecreationMode = true;
                    createSession(credentials.getUsername(), credentials.getPassword());
                    return downloadResources(url);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    inRecreationMode = false;
                }
            }
        }

        if (response != 200) {
            throw new DownloadException(String.valueOf(response));
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        return content.toString();

    }

    protected boolean testSession(@Nullable String moodleSessionKey) {

        if (moodleSessionKey == null || moodleSessionKey.isEmpty()) {
            return false;
        }

        try {
            URL obj = new URL("https://moodle-s.kgs.hd.bw.schule.de/moodle/");
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("Cookie", moodleSessionKey);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5000);
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;


    }


    protected String performLogin(String user, String password) throws IOException, CredentialException, NetworkErrorException {

        String parameterString = "username=" + user + "&password=" + password + "&ajax=true";


        URL obj = new URL(LOGIN_URL);
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

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        System.out.println(content.toString());


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

        //System.out.println(cookieString);
        if (!cookieString.toString().contains("MOODLEID1_=deleted")) {
            throw new CredentialException(null);
        }

        //String moodleCoockie = cookieString.substring(cookieString.indexOf("MoodleSession"));
        // return moodleCoockie.substring(0,40);

        Pattern pattern = Pattern.compile("(MoodleSession=.*?);");
        Matcher matcher = pattern.matcher(cookieString.toString());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new CredentialException();
        }


    }

    public boolean isSessionValid() {
        return sessionValid;
    }

}
