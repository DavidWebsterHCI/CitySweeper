package edu.project.hoodwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/*
 * HomeActivity works with both portrait and landscape mode layouts; 
 * displays user's name in the action bar if the user is logged in; 
 * 'Recent Issues' is the only feature that works without logging in; 
 * if the user is not logged in and clicked another button, an alert dialog 
 * requests the user login, the LoginActivity will remember which button was clicked 
 * and will take user there after login process is complete;
 * most activities in the application have a 'Home' button in the action bar.
 */
public class HomeActivity extends Activity implements OnClickListener {
	private boolean loggedin;
	private String name;

	// ------------------------------------------------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is user logged in", loggedin);

	}

	// ---------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newhome);

		// Locate resources.
		Button bntNewReport = (Button) findViewById(R.id.new_report);
		Button bntMyIssues = (Button) findViewById(R.id.my_issues);
		Button bntRecentIssues = (Button) findViewById(R.id.recent_issues);
		Button bntFollowedIssues = (Button) findViewById(R.id.followed_issues);
		Button bntSettings = (Button) findViewById(R.id.settings);

		// Set on click listeners.
		bntNewReport.setOnClickListener(this);
		bntMyIssues.setOnClickListener(this);
		bntRecentIssues.setOnClickListener(this);
		bntFollowedIssues.setOnClickListener(this);
		bntSettings.setOnClickListener(this);

		if (savedInstanceState == null) {
			// Check if user is logged in.
			SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME,
					MODE_PRIVATE);
			name = prefs.getString(MyApp.USER_DISPLAYNAME_KEY, "");
			if (!name.isEmpty()) {
				loggedin = true;
			} else {
				loggedin = false;
			}
		}
		// After rotation.
		else {
			loggedin = savedInstanceState.getBoolean("is user logged in");
			if (loggedin) {
				SharedPreferences prefs = getSharedPreferences(
						MyApp.PREFS_NAME, MODE_PRIVATE);
				name = prefs.getString(MyApp.USER_DISPLAYNAME_KEY, "");
			}
		}
	}

	// ---------------------------------------------------------------------
	// Only 'Recent Issues' does not require user to login right away.
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.recent_issues:
			startActivity(new Intent(this, RecentIssuesListActivity.class));
			break;
		case R.id.new_report:
			if (!loggedin) {
				showDialogBox(MyApp.NEW_REPORT);
			} else {
				startActivity(new Intent(this, NewReportActivity.class));
			}
			break;
		case R.id.my_issues:
			if (!loggedin) {
				showDialogBox(MyApp.MY_ISSUES);
			} else {
				startActivity(new Intent(this, MyIssuesListActivity.class));
			}
			break;
		case R.id.followed_issues:
			if (!loggedin) {
				showDialogBox(MyApp.FOLLOWED_ISSUES);
			} else {
				startActivity(new Intent(this, FollowedIssuesListActivity.class));
			}
			break;
		case R.id.settings:
			if (!loggedin) {
				showDialogBox(MyApp.SETTINGS);
			} else {
				startActivity(new Intent(this,	SettingsActivity.class));
			}
			break;
		}
	}

	// ------------------------------------------------------------------------
	// User name should be displayed in the upper right corner
	// if user is logged in.
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if(name != null){
			if (!name.isEmpty() ) {
				menu.findItem(R.id.mnuLogin).setTitle(name);
				loggedin = true;
			} else {
				menu.findItem(R.id.mnuLogin).setTitle("Login");
				loggedin = false;
			}
		}
		else{
			menu.findItem(R.id.mnuLogin).setTitle("Login");
			loggedin = false;
		}
		return true;
	}

	// ------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuLogin:
			Intent i = new Intent(this, LoginActivity.class);
			i.putExtra("Activity", MyApp.HOME);
			startActivity(i);
			return true;
		case R.id.mnuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ----------------------------------------------------------------------
	// This dialog box will pass 'activity' to the LoginActivity,
	// so it will know which activity to start after
	// user logged in.
	private void showDialogBox(final String activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("Alert")
				.setMessage(
						"To use this feature, you need to be logged in. \n"
								+ "Would you like to login or register now?")
				.setIcon(android.R.drawable.ic_dialog_alert);

		AlertDialog alertDialog = builder.create();

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(HomeActivity.this,
								LoginActivity.class);
						i.putExtra("Activity", activity);
						startActivity(i);
					}
				});

		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// nothing to do
					}
				});
		alertDialog.setCancelable(false);
		alertDialog.show();
	}
}
