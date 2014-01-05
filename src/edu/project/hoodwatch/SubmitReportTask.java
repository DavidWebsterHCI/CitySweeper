package edu.project.hoodwatch;

/*
 * This class allows for the data gathered via NewReport to be transfered to the server
 */

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;

// AsyncTask arguments:
// URL - passed to doInBackground
// Void - passed to onProgressUpdate
// Integer - returned from doInBackground and passed to onPostExecute
public class SubmitReportTask extends AsyncTask<URL, Void, Integer> {
	private NewReportActivity myReportActivity;
	private String myReportJSONData;
	private List<NameValuePair> myReportFormData;
	private SubmitReportTaskDelegate taskDelegate;

	// ----------------------------------------------------------------------------
	// Constructor
	public SubmitReportTask(NewReportActivity activity, String reportJSONData,
			List<NameValuePair> reportFormData) {
		super();
		this.myReportActivity = activity;
		this.myReportJSONData = reportJSONData;
		this.myReportFormData = reportFormData;
	}

	// ----------------------------------------------------------------------------
	@Override
	protected Integer doInBackground(URL... url) {
		try {	
			//Thread.sleep(5000); // only for the testing purposes
			Integer result = uploadToServer(url);
			return result; //returns byte Array
		} catch (Exception e) {
			return new Integer(-1);
		}
	}
	
	// ----------------------------------------------------------------------------
	protected synchronized Integer uploadToServer(URL... url)
			throws ApiException {

	    DefaultHttpClient httpclient = new DefaultHttpClient();
	    HttpPost httpost = new HttpPost(url[0].toString());
	    try {
	    	// If web service expects data via JSON string.
	    	if (myReportJSONData != null) {
				StringEntity se = new StringEntity(myReportJSONData);
				httpost.setEntity(se);
				httpost.setHeader("Accept", "application/json");
				httpost.setHeader("Content-type", "application/json");
	    	}
	    	// If web service expects Myltiform mode for sending data.
	    	else if (myReportFormData != null) {
				File imagefile = new File(myReportActivity.getApplicationContext().getFilesDir(), MyApp.USER_IMAGE_FILENAME);
				if (imagefile.exists()) {
		    		MultipartEntity mpe = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		    		FileBody bin = new FileBody(imagefile);
		    		mpe.addPart("picture", bin);
		    		
		    		ListIterator<NameValuePair> iter = myReportFormData.listIterator();
		    		while( iter.hasNext() ) {
			    		NameValuePair pair = iter.next();
			    		mpe.addPart(pair.getName(), new StringBody(pair.getValue()));
		    		}
					httpost.setEntity(mpe);
		    				
		    		
//		    		MultipartEntity entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );
//		    		// For File parameters
//		    		entity.addPart( paramName, new FileBody((( File ) paramValue ), "application/zip" ));
//		    		// For usual String parameters
//		    		 entity.addPart( paramName, new StringBody( paramValue.toString(), "text/plain",
//		    		                                       Charset.forName( "UTF-8" )));
//		    		 post.setEntity( entity );
				}
				else {
					httpost.setEntity(new UrlEncodedFormEntity(myReportFormData));
				}
	    	}
	    	else {
	    		return -1; // Failure
	    	}
		    ResponseHandler<String>  responseHandler = new BasicResponseHandler();
		    String responseBody = httpclient.execute(httpost, responseHandler);
		    responseBody.toString();
		    // Blocked until the server responds to the JSON POST request
		    
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return 1; //issue ID
	}
	
	// ----------------------------------------------------------------------------
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (taskDelegate != null) {
			taskDelegate.reportWillSubmit();
		}
	}
	
	// ----------------------------------------------------------------------------
	// result of doInBackground is passed in.
	// Executed on the main thread and only once.
	@Override
	protected void onPostExecute(Integer result) {
		if (taskDelegate != null) {
			taskDelegate.reportDidSubmit();
		}
	}
	
	// ----------------------------------------------------------------------------
	// called from NewReportActivity.java in submitForm()
	public void setDelegate(SubmitReportTaskDelegate inDelegate) {
		taskDelegate = inDelegate;
	}


}
