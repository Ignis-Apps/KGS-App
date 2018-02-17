package de.kgs.vertretungsplan.coverPlan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import de.kgs.vertretungsplan.DataStorage;

class HttpUrlConnectionHandler {
	private DataStorage ds = DataStorage.getInstance();

	private String pagestring = "=&=&=&=&=&=&keywords=&=&=&=&=&robots=&viewport=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&username=" + ds.username + "&=&=&=&=&password=" + ds.password + "&=&=&anchor=&=&=Login&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=";
	

	HttpUrlConnectionHandler () throws IOException{
	    CookieHandler.setDefault(new CookieManager());

	    sendPost(ds.login_page_url, pagestring);
	}


	private void sendPost(String url, String postParams) throws IOException {
		HttpsURLConnection conn;
		URL	obj = new URL(url);

		conn = (HttpsURLConnection) obj.openConnection();

		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(5000);

		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		conn.connect();

		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		/*String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}*/

		in.close();

	}

	
	private String GetPageContent(String url) throws Exception {
		if(url.contains("https")){
			return  getPageContentHttps(url);
		}else {
			return getPageContentHttp(url);
		}
	}


	private String getPageContentHttp(String url)throws Exception{
	    URL obj = new URL(url);

	    HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
	    httpURLConnection.setRequestMethod("GET");
	    httpURLConnection.setInstanceFollowRedirects(false);
	    httpURLConnection.setConnectTimeout(5000);

	    int responseCode = httpURLConnection.getResponseCode();

	    System.out.println("\nSending 'GET' request to URL : " + url);
	    System.out.println("Response Code : " + responseCode);

	    // (RC 303) HTTP Connection shoud redirect to another page (Login Page)
	    if(responseCode==303){
	  	    // DO NOT remove/modify this exception because it triggers the CREDENTIALS_NEEDED response
	  	    throw new Exception("Login needed");
	    }
	    // (RC 200) HTTP Connection OK!
	    else if(responseCode!=200){
	  	    throw new Exception("Error while loading page");
	    }

	    BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
	    String inputLine;
	    StringBuilder response = new StringBuilder();

	    while ((inputLine = in.readLine()) != null) {
	  	    response.append(inputLine);
	    }
	    in.close();

	    return response.toString();

	}


	private String getPageContentHttps(String url)throws Exception{
		URL obj = new URL(url);

		HttpsURLConnection httpsURLConnection = (HttpsURLConnection) obj.openConnection();
		httpsURLConnection.setRequestMethod("GET");
		httpsURLConnection.setInstanceFollowRedirects(false);
		httpsURLConnection.setConnectTimeout(5000);

		int responseCode = httpsURLConnection.getResponseCode();

		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		// (RC 303) HTTP Connection shoud redirect to another page (Login Page)
		if(responseCode==303){
			// DO NOT remove/modify this exception because it triggers the CREDENTIALS_NEEDED response
			throw new Exception("Login needed");
		}
		// (RC 200) HTTP Connection OK!
		else if(responseCode!=200){
			throw new Exception("Error while loading page");
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}


	Document getParsedDocument(String url)throws Exception{
		return Jsoup.parse(GetPageContent(url));
	}
}
