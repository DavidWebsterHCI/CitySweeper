package edu.project.hoodwatch;

/*
 * This activity allows the user to change their personal settings (currently allows a user to enter 
 * anonymous report mode)
 */

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener {
	private String name; // to be displayed in upper right corner
	CheckBox chboxAnon;
	Button btnSubmit;
	TextView txtResponse;
	private static final String url = "http://sfsuswe.com/~s13g01/sweeper/web_services/set_anonymity.php?api_key=KHF4KH6498GFHJ3J37XBNSHD";

	// ---------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Locate resources.
		btnSubmit = (Button) findViewById(R.id.buttonOK);
		chboxAnon = (CheckBox) findViewById(R.id.checkBoxAnon);
		txtResponse = (TextView) findViewById(R.id.textResponse);
		
		btnSubmit.setOnClickListener(this);

	}

	// ---------------------------------------------------------------------
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME,
				MODE_PRIVATE);

		// If logged in, display user name.
		name = prefs.getString(MyApp.USER_DISPLAYNAME_KEY, "");
		// the name is displayed from onPrepareOptionsMenu method
	}

	// ------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		
		if (v.getId() == R.id.buttonOK) {
			if(chboxAnon.isChecked()) {
				Toast.makeText(this, "Checked", Toast.LENGTH_SHORT).show();
			}
			// Check box unchecked.
			else {
				Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();
			}
		}
		
		
		
	}
	
	// ------------------------------------------------------------------------
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!name.isEmpty()) {
			menu.findItem(R.id.mnuLogin).setTitle(name);
		}
		return true;
	}

	// ------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case android.R.id.home:
			Intent i = new Intent(this, HomeActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	

}
