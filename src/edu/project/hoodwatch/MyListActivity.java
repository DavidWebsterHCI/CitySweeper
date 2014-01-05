package edu.project.hoodwatch;
/*
 * The point of this class is to create the list of activities that the user has either submitted or followed so that
 * the user can view their activities.  It in essence pulls the data from the backend that is sorted into the categories
 * of a user submitted issue, a followed issue, or a recent activity.
 */
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import com.google.gson.Gson;

public abstract class MyListActivity extends ListActivity implements
		ListOfIssuesTaskDelegate {

	private static final String kIssueListKey = "issueList";
	public static final String kContentViewKey = "contentView";
	public static final String kListPageNumber = "listPageNumber";
	private ArrayList<IssueData> issues; // provided by the task
	private static ListOfIssuesTask task;
	protected ProgressDialog progDialog;
	private String activityCode;
	protected URL url;
	protected String sURL;
	private int listPageNumber;
	private static final String recent = "http://sfsuswe.com/~s13g01/sweeper/web_services/get_issues.php?api_key=KHF4KH6498GFHJ3J37XBNSHD";
	private static final String my = "http://sfsuswe.com/~s13g01/sweeper/web_services/get_my_issues.php?api_key=KHF4KH6498GFHJ3J37XBNSHD&user_token=";
	private static final String followed = "http://sfsuswe.com/~s13g01/sweeper/web_services/get_followed_issues.php?api_key=KHF4KH6498GFHJ3J37XBNSHD&user_token=";

	// ------------------------------------------------------------------
	// Constructor, also called when device is rotated.
	public MyListActivity(String activity) {
		super();
		activityCode = activity;
		listPageNumber = 1;
	}

	// ------------------------------------------------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(kIssueListKey, issues);
		outState.putInt(kListPageNumber, listPageNumber);

		// Asks the background task to let go of this activity.
		if (MyListActivity.task != null) {
			MyListActivity.task.detachActivity();
		}

		if (progDialog != null) { // avoids a window leak
			stopProgDialog();
		}
	}

	// ------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setActionBar();
		SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		String usertoken = prefs.getString(MyApp.USER_TOKEN_KEY, "");
		
		if (activityCode.equals("recent")) {
			try {
				url = new URL(recent);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		else if (activityCode.equals("my")) {
			try {
				url = new URL(my.concat(usertoken));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		else if (activityCode.equals("followed")) {
			try {
				url = new URL(followed.concat(usertoken));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}	

		// First time creating activity.
		if (savedInstanceState == null) {
			showProgDialog();
			// recent, my, or followed activity
			MyListActivity.task = new ListOfIssuesTask(this); 
			MyListActivity.task.execute(url); // calls setIssues
		}
		
		// After rotation.
		else {
			issues = (ArrayList<IssueData>) savedInstanceState.getSerializable(kIssueListKey);
			listPageNumber = savedInstanceState.getInt(kListPageNumber);

			// If issue list already exists.
			if (issues != null) {
				setIssues(issues);
			}
			
			// The result is not ready yet, task still running.
			else {
				// Get information about the running background task.
				if (MyListActivity.task != null) {
					showProgDialog();
					MyListActivity.task.attachActivity(this);
				}
			}
		}

		if (activityCode.equals("recent")) {
			this.getListView().setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					int lastInScreen = firstVisibleItem + visibleItemCount;
					if (lastInScreen == totalItemCount
							&& MyListActivity.task == null) {
						listPageNumber++;
						try {
							URL pagedurl = new URL(String.format("%s&page=%d",
									url.toString(), listPageNumber));
							MyListActivity.task = new ListOfIssuesTask(
									MyListActivity.this);
							MyListActivity.task
									.attachActivity(MyListActivity.this); // sets delegate
							MyListActivity.task.execute(pagedurl);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	// ------------------------------------------------------------------
	// Set up the action bar for the activity.
	void setActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	// ------------------------------------------------------------------
	protected void refreshActivityList() {
		// Set the list of issues to empty, then launch task to load issues.
		listPageNumber = 1;
		setIssues(null);
		MyListActivity.task = new ListOfIssuesTask(this);
		MyListActivity.task.execute(url);
	}

	// ------------------------------------------------------------------
	// When a list item is clicked.
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// gets all data for the selected issue
		IssueData issue = (IssueData) l.getItemAtPosition(position);
		Gson gson = new Gson();
		String issueJSONData = gson.toJson(issue);

		// new intent
		Intent i = new Intent(this, IssueActivity.class);
		i.putExtra("IssueJSONData", issueJSONData);
		startActivity(i);
	}

	// ------------------------------------------------------------------
	// The background task provides a list of issues.
	// Called here or from onPostExecute of the task.
	public void setIssues(ArrayList<IssueData> issues) {
		this.issues = issues;
		if (progDialog != null) {
			stopProgDialog();
		}

		// Issues is set to null on refresh.
		if (issues != null) {
			setListAdapter(new IssueAdapter(this, R.layout.item, issues));

			// If you scroll it using the track bar you will not get any focus.
			this.getListView().setItemsCanFocus(false);
		}
	}

	// ------------------------------------------------------------------
	// Each list activity has its own version.
	abstract protected void showProgDialog();

	// ------------------------------------------------------------------
	// Hides an existing progress dialog.
	void stopProgDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

	// ------------------------------------------------------------------
	// Executes when backToTop button is clicked.
	public void backToTop() {
		getListView().setSelection(0);
	}

	// ------------------------------------------------------------------
	// ListOfIssuesTaskDelegate method.
	public void taskWillExecute() {
		if (listPageNumber > 1) { // anything other than the first page
			// maybe show a progress at the very bottom of the page ???
		}
		else {
			showProgDialog();
		}
	}

	// ------------------------------------------------------------------
	// ListOfIssuesTaskDelegate method.
	public void taskDidExecute(ListOfIssuesTaskResponse taskResponse) {

		if (taskResponse == null) {
			stopProgDialog();
			displayMessage("Unable to connect.\nTry again later.");
			return;
		}

		String downloadedData = taskResponse.data;

		if (taskResponse.statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
			if (listPageNumber > 1)
				listPageNumber--; // set the page number back to the
									// previous count
			return;
		}

		// if no network or some problems getting data
		if (downloadedData.length() == 0) {

			stopProgDialog();

			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("Alert")
					.setMessage("Unable to download data.\nTry again later.")
					.setIcon(android.R.drawable.ic_dialog_alert);

			AlertDialog alertDialog = builder.create();

			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(MyListActivity.this,
									HomeActivity.class));
						}
					});

			alertDialog.setCancelable(false);
			alertDialog.show();
		}

		// If network is good and received data.
		else {
			// Gson is a Java library that converts JSON to Java and vice-versa.
			Gson gson = new Gson();

			// Create an ArrayList of issue items from the string.
			ArrayList<IssueData> itemList = new ArrayList<IssueData>(
					Arrays.asList(gson.fromJson(downloadedData,
							IssueData[].class)));

			if (listPageNumber > 1) { // anything other than the first page
				// this.issues.addAll(itemList);
				((IssueAdapter) getListAdapter()).addAll(itemList);
			} else {
				setIssues(itemList); // this calls setAdapter
			}
		}
		MyListActivity.task = null;
	}
	
	// -------------------------------------------------------------------
		private void displayMessage(String msg) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("Alert").setMessage(msg)
					.setIcon(android.R.drawable.ic_dialog_alert);

			AlertDialog alertDialog = builder.create();

			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(MyListActivity.this, HomeActivity.class));
						}
					});

			alertDialog.setCancelable(false);
			alertDialog.show();
		}
}
