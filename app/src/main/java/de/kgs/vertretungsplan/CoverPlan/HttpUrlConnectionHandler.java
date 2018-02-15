package de.kgs.vertretungsplan.CoverPlan;

import com.google.firebase.crash.FirebaseCrash;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import de.kgs.vertretungsplan.DataStorage;

public class HttpUrlConnectionHandler {

	  private List<String> cookies;
	  private HttpsURLConnection httpsURLConnection;
	  private HttpURLConnection httpURLConnection;
	  private DataStorage ds = DataStorage.getInstance();

	  public String pagestring = "=&=&=&=&=&=&keywords=&=&=&=&=&robots=&viewport=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&username=" + ds.username + "&=&=&=&=&password=" + ds.password + "&=&=&anchor=&=&=Login&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=&=";
	
	  public HttpUrlConnectionHandler(){
		
		CookieHandler.setDefault(new CookieManager());
			
		try {

			sendPost(ds.login_page_url, pagestring);
				
		} catch (IOException e) {


			FirebaseCrash.report(e);
			e.printStackTrace();
				
		}
			
	  }

	  private void sendPost(String url, String postParams) throws IOException {

	  	HttpsURLConnection conn;
	  	URL	obj = new URL(url);

		conn = (HttpsURLConnection) obj.openConnection();

		conn.setUseCaches(false);
		//httpsURLConnection.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");

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
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}

		in.close();

	  }
	
	  public String GetPageContent(String url) throws Exception {

	  		if(url.contains("https")){
	  			return  getPageContentHttps(url);
			}else {
	  			return getPageContentHttp(url);
			}

	  }

	  private String getPageContentHttp(String url)throws Exception{

		  URL obj = new URL(url);

		  httpURLConnection = (HttpURLConnection) obj.openConnection();
		  httpURLConnection.setRequestMethod("GET");
		  httpURLConnection.setInstanceFollowRedirects(false);
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
		  StringBuffer response = new StringBuffer();

		  while ((inputLine = in.readLine()) != null) {
			  response.append(inputLine);
		  }
		  in.close();

		  setCookies(httpURLConnection.getHeaderFields().get("Set-Cookie"));

		  return response.toString();

	  }

	  private String getPageContentHttps(String url)throws Exception{
		  URL obj = new URL(url);

		  httpsURLConnection = (HttpsURLConnection) obj.openConnection();
		  httpsURLConnection.setRequestMethod("GET");
		  httpsURLConnection.setInstanceFollowRedirects(false);
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
		  StringBuffer response = new StringBuffer();

		  while ((inputLine = in.readLine()) != null) {
			  response.append(inputLine);
		  }
		  in.close();

		  setCookies(httpsURLConnection.getHeaderFields().get("Set-Cookie"));

		  return response.toString();
	  }


	public Document getParsedDocument(String url)throws Exception{

		return Jsoup.parse(GetPageContent(url));

	  }
	  public String getFormParams(String html, String username, String password)throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		Elements inputElements = doc.getAllElements();
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			
			if (key.equals("username"))
				
				value = username;
			
			else if (key.equals("password"))
				
				value = password;
			
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		StringBuilder result = new StringBuilder();
		
		for (String param : paramList) {
			
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	  }

	  public List<String> getCookies() {
		return cookies;
	  }

	  public void setCookies(List<String> cookies) {
		  	this.cookies = cookies;
	  }
}
