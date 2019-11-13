package de.kgs.vertretungsplan.coverPlan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

import de.kgs.vertretungsplan.DataStorage;

class HttpUrlConnectionHandler {


	public static String getCoverplanData(String url, String moodleSession) throws Exception {

		if(isCookieExpired()){
			System.out.println("Cookie is too old !");
			performLogin();
		}



		URL obj = new URL(url);
		HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Cookie", moodleSession);
		connection.setConnectTimeout(5000);
		connection.setInstanceFollowRedirects(false);

		int responseCode = connection.getResponseCode();
		System.out.println(responseCode);
		// MoodleSession expired / invalid
		if( responseCode == 303 ){
			performLogin();
			return getCoverplanData(url, DataStorage.getInstance().moodleCookie);
		}

		if( responseCode != 200){
			throw new Exception("Error while loading page");
		}

		DataStorage.getInstance().moodleCookieLastUse = System.nanoTime();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();

	}

	public static void performLogin() throws Exception{

		System.out.println("Performing Login");
		String parameterString = "username=" + DataStorage.getInstance().username + "&password=" + DataStorage.getInstance().password +"&ajax=true";

		URL obj = new URL(DataStorage.getInstance().login_page_url);
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

		if(responseCode!=200){
			throw new Exception("Fehler");
		}

		List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
		StringBuilder cookieConcad = new StringBuilder();

		if(cookies == null){
			throw new Exception("Fehler");
		}

		for (String s: cookies ){
			cookieConcad.append(s);
		}


		if(!cookieConcad.toString().contains("MOODLEID1_=deleted"))
			throw new Exception("Login needed");


		String moodleCoockie = cookieConcad.substring(cookieConcad.indexOf("MoodleSession"));
		DataStorage.getInstance().moodleCookie = moodleCoockie.substring(0,40);

	}


	private static boolean isCookieExpired(){

		long elapsedTime = ( System.nanoTime() - DataStorage.getInstance().moodleCookieLastUse ) / 1000000000;
		return elapsedTime > DataStorage.getInstance().moodleCookieMaxAgeSecounds;

	}

	Document getParsedDocument(String url)throws Exception{
		return Jsoup.parse(getCoverplanData(url,DataStorage.getInstance().moodleCookie));
	}
}
