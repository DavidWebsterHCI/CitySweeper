package edu.project.hoodwatch;
/*
 *The three classes "ListOfIssuesTaskxxxx.java" all work in coordination in order to allow for a list of
 *issues to be displayed to the user in the two cases list views are implemented.
 */
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.os.AsyncTask;

// AsyncTask arguments:
// URL - passed to doInBackground
// Void - passed to onProgressUpdate
// ListOfIssuesTaskResponse - returned from doInBackground and passed to onPostExecute

public class ListOfIssuesTask extends AsyncTask<URL, Void, ListOfIssuesTaskResponse> {

	private static ListOfIssuesTaskDelegate taskDelegate;
	private static byte[] buff = new byte[1024];
	
	// -------------------------------------------------------------------
	// Constructor
	public ListOfIssuesTask(MyListActivity activity) {
		super();
		attachActivity(activity);
	}

	// -------------------------------------------------------------------
	// Called here and from MyListActivity onCreate.
	void attachActivity(ListOfIssuesTaskDelegate activity) {
		taskDelegate = activity;
	}

	// -------------------------------------------------------------------
	// Called only from MyListActivity onRetainNonConfigurationInstance.
	void detachActivity() {
		taskDelegate = null;
	}

	// -------------------------------------------------------------------
	// Has access to user interface.
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		taskDelegate.taskWillExecute();
	}

	// -------------------------------------------------------------------
	// No access to user interface here.
	@Override
	protected ListOfIssuesTaskResponse doInBackground(URL... url) {
		try {
//			Thread.sleep(5000); // only for the testing purposes
			ListOfIssuesTaskResponse result = downloadFromServer(url);
			return result; //
		} catch (Exception e) {
			return null;
		}
	}

	// -------------------------------------------------------------------
	// Synchronized means thread safe.
	// No access to user interface here.
	protected synchronized ListOfIssuesTaskResponse downloadFromServer(URL... url)
			throws ApiException {
		
//////////////////////////////////////////////////////////////////////////////////
//Communication with the web server:
//		String retval = null;
		ListOfIssuesTaskResponse retval = new ListOfIssuesTaskResponse();

		// Create an http client.
		HttpClient client = new DefaultHttpClient();

		// Create a request object from the url.
		HttpGet request = new HttpGet(url[0].toString());

		try {
			// Execute the request.
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();
			
			if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
				retval.data = null;
				retval.statusCode = statusCode;
				retval.reason = null;
				return retval;
			}
			else if (status.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
				throw new ApiException("Invalid response from web service" + status.toString());
			}

			// An entity is what can be sent or received with an HTTP message.
			HttpEntity entity = response.getEntity();

			// Create input stream from the received entity.
			InputStream inStream = entity.getContent();

			// Prepare output stream.
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();

			// Read from input stream and write into output stream.
			int readCount = 0;
			while ((readCount = inStream.read(buff)) != -1) {
				outContent.write(buff, 0, readCount);
			}
			// Convert output stream to string; expecting string in the JSON format.
			String out = outContent.toString();
			
			JSONObject object = (JSONObject) new JSONTokener(out).nextValue();
			String result = object.getString("result");
			String reason = object.getString("reason");
			
			JSONArray issues = object.getJSONArray("issues");
			retval.data = issues.toString();
			retval.statusCode = status.getStatusCode();
			retval.reason = reason;
			
///////////////////////////////////////////////////////////////////////////////////
			
		} catch (Exception e) {

			throw new ApiException("Problems connecting to the server "
					+ e.getMessage(), e);
		}
		return retval;
	}

	// -------------------------------------------------------------------
	// Executed on the main thread and only once.
	@Override
	protected void onPostExecute(ListOfIssuesTaskResponse downloadedData) {
		taskDelegate.taskDidExecute(downloadedData);
	}

}
