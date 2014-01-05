package edu.project.hoodwatch;
/*
 * This class is utilized for webservice communications protocols 
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;

public class WebServiceTask extends AsyncTask<URL, Void, HttpResponse> {
	private List<NameValuePair> nameValuePairs;
	private WebTaskDelegate delegate;

	// -------------------------------------------------------------------
	// constructor
	public WebServiceTask(List<NameValuePair> data) {
		this.nameValuePairs = data;
	}

	// -------------------------------------------------------------------
	public void setDelegate(WebTaskDelegate inDelegate) {
		delegate = inDelegate;
	}
	
	// -------------------------------------------------------------------
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		delegate.taskWillExecute();
	}

	// -------------------------------------------------------------------
	@Override
	protected HttpResponse doInBackground(URL... url) {
		HttpResponse response = null;

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url[0].toString());

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			response = httpclient.execute(httppost);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	// -------------------------------------------------------------------
	@Override
	protected void onPostExecute(HttpResponse responce) {
		super.onPostExecute(responce);
		delegate.taskDidExecute(responce);
	}

	// -------------------------------------------------------------------
	@Override
	protected void onCancelled() {
		super.onCancelled();
		this.cancel(true);
	}

}
