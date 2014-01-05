package edu.project.hoodwatch;

/*
 * This activity allows for the list view to be displayed when a user clicks on
 * "followed issues" from the homepage.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class FollowedIssuesListActivity extends MyListActivity {

	// ------------------------------------------------------------------
	// Constructor
    public FollowedIssuesListActivity() {
            super("followed");
    }

	// ------------------------------------------------------------------
	// This code deals with the menu options.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_of_issues_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuBackToTop:
			backToTop();
			return true;
		case R.id.mnuRefresh:
			refreshActivityList();
			return true;
		case R.id.mnuHelp:
			Toast.makeText(this, "Help for followed issues", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.mnuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case android.R.id.home:
			// go back to home screen
			// FLAG_ACTIVITY_CLEAR_TOP clears the stack of activities
			Intent i = new Intent(this, HomeActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ------------------------------------------------------------------
	// set up the action bar for the activity.
	void setActionBar() {
		// set up the action bar
		super.setActionBar();
		// ActionBar actionBar = getActionBar();
	}

	// ------------------------------------------------------------------
	// This code deals with showing and hiding the progress dialog.
	protected void showProgDialog() {
		if (progDialog == null) {
			progDialog = ProgressDialog.show(this, "Search",
					"Retrieving followed issues", true, false);
		}
	}

}
