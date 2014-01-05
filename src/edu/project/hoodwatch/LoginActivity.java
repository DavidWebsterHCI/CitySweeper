package edu.project.hoodwatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
 * LoginActivity uses shared preferences to store and retrieve the following 
 * information about the user: 
 * user's ID, display name, email, and token.
 * In the onCreate method the user's name and email are retrieved from shared preferences;
 * if the user is already logged in, the name is displayed in the action bar 
 * and clicking on the 'Login' button displays a dialog box with "You are logged in.";
 * the name of the activity to start after logging in is retrieved from the Bundle;
 * when the user clicks the 'Login' button, WebServiceTask runs in the background thread 
 * and returns HttpResponse which is then processed in the taskDidExecute method;
 * if the 'Register' button is clicked when a user is already logged in, the dialog 
 * box offers a choice to cancel or logout before switching to RegisterActivity;
 * if the user is logged in, clicking on 'Register' button brings up the dialog 
 * asking the user to logout and go to RegisterActivity or stay logged in and 
 * remain on LoginActivity;
 * 'Logout' clears all the records in shared preferences, no web service is required.
 */
public class LoginActivity extends Activity implements OnClickListener,
		WebTaskDelegate {

	private EditText etEmail;
	private EditText etPassword;
	private String nextActivity;
	private ProgressDialog progDialog;
	private String name;
	private Menu myMenu;
	private boolean loggedin;
	private SharedPreferences prefs;
	private AlertDialog alertDialog;
	private static byte[] buff = new byte[128];

	// ---------------------------------------------------------------------
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_login);

		// Set up the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// To make sure that keyboard does not show up from start
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// Locate resources.
		etEmail = (EditText) this.findViewById(R.id.txtEmail);
		etPassword = (EditText) this.findViewById(R.id.txtPwd);
		Button btnLogin = (Button) this.findViewById(R.id.buttonLogin);
		Button btnRegister = (Button) this.findViewById(R.id.buttonRegister);
		Button btnLogout = (Button) findViewById(R.id.buttonLogout);

		// Set up on click listeners.
		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		btnLogout.setOnClickListener(this);

		// Get user name, email, and password from shared preferences.
		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		name = prefs.getString(MyApp.USER_DISPLAYNAME_KEY, "");
		if (!name.isEmpty()) {
			loggedin = true;
		} else {
			loggedin = false;
		}
		if (loggedin) {
			// Retrieve user email saved.
			String emailvalue = prefs.getString(MyApp.USER_EMAIL_KEY, "");
			if (!emailvalue.isEmpty()) {
				etEmail.setText(emailvalue);
			}
			// User password is not displayed, 
			// it is not returned by the login web service, 
			// and should not saved in register activity.
		}

		// Get activity to start after logging in.
		Bundle extra = this.getIntent().getExtras();
		nextActivity = extra.getString("Activity");
	}

	// --------------------------------------------------------------------
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
	}

	// --------------------------------------------------------------------
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.buttonLogin:
			if (!loggedin) {
				String enteredEmail = etEmail.getText().toString();
				String enteredPassword = etPassword.getText().toString();

				// Make sure user entered both email and password.
				if (!enteredEmail.isEmpty() && !enteredPassword.isEmpty()) {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);

					nameValuePairs.add(new BasicNameValuePair(MyApp.API_KEY, "KHF4KH6498GFHJ3J37XBNSHD"));
					nameValuePairs.add(new BasicNameValuePair(MyApp.USER_PASSWORD_KEY, enteredPassword));
					nameValuePairs.add(new BasicNameValuePair(MyApp.USER_EMAIL_KEY, enteredEmail));

					try {
						URL url = new URL("http://sfsuswe.com/~s13g01/sweeper/web_services/login.php");
						WebServiceTask task = new WebServiceTask(nameValuePairs);
						task.setDelegate(this);
						task.execute(url);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				} 
				// 
				else {
					displayMessage1("Please enter email and password.");
					return;
				}
			}
			// If the user is already logged in.
			else {
				displayMessage1("You are logged in.");
			}
			break;

		case R.id.buttonRegister:

			if (loggedin) {
				displayMessage2("Would you like to logout?");
			} else {
				// Clear the edit boxes.
				etEmail.setText("");
				etPassword.setText("");
				
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				intent.putExtra("Activity", nextActivity);
				startActivity(intent);
			}
			break;

		case R.id.buttonLogout:
			if (loggedin) {
				logoutUser();
			}
			break;
		}
	}

	// -------------------------------------------------------------------
	void showProgDialog() {
		if (progDialog == null) {
			progDialog = ProgressDialog.show(this, "Login",
					"Contacting webservices", true, false);
		}
	}

	// -------------------------------------------------------------------
	void stopProgDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

	// -------------------------------------------------------------------
	@Override
	public void taskWillExecute() {
		showProgDialog();
	}

	// -------------------------------------------------------------------
	@Override
	public void taskDidExecute(HttpResponse response) {
		stopProgDialog();
		// if no network or some problems getting data
		if (response == null) {
			displayMessage1("Unable to connect.\nTry again later.");
		}
		// if network works fine and we have some HTTP response
		else {
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();

			// An entity is what can be sent or received with an HTTP message.
			HttpEntity entity = response.getEntity();

			// Create input stream from the received entity.
			InputStream inStream;
			try {
				inStream = entity.getContent();

				// Prepare output stream.
				ByteArrayOutputStream outContent = new ByteArrayOutputStream();

				// Read from input stream and write into output stream.
				int readCount = 0;
				while ((readCount = inStream.read(buff)) != -1) {
					outContent.write(buff, 0, readCount);
				}
				// Convert output stream to string; expecting string in the JSON
				// format.
				String out = outContent.toString();
				JSONObject object = (JSONObject) new JSONTokener(out)
						.nextValue();

				String result = object.getString("result");
				String reason = object.getString("reason");

				if (statusCode != HttpURLConnection.HTTP_CREATED) {
					showToast("Email address or password is incorrect.");
				} else {
					if (result.equals("false")) {
						// !!! THIS SHOULD NEVER HAPPEN !!!
						showToast(reason);
					}
					// When result = true.
					else {
						String id = object.getString("id");
						String displayname = object.getString("displayname");
						String token = object.getString("token");

//						String msg = "Result: " + result + "\nReason: "
//								+ reason + "\nId: " + id;
//						showToast(msg);

						// Data in Shared Preferences persists.
						prefs = getSharedPreferences(MyApp.PREFS_NAME,
								MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString(MyApp.USER_DISPLAYNAME_KEY,
								displayname);
						editor.putString(MyApp.USER_ID_KEY, id);
						editor.putString(MyApp.USER_TOKEN_KEY, token);
						editor.putString(MyApp.USER_EMAIL_KEY, etEmail
								.getText().toString());
						editor.commit();
					}
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// Display name.
			myMenu.findItem(R.id.mnuLogin).setTitle(name);

			// Take user to chosen activity.
			Intent i;

			if (nextActivity.equals(MyApp.MY_ISSUES)) {
				i = new Intent(this, MyIssuesListActivity.class);
				startActivity(i);

			} else if (nextActivity.equals(MyApp.FOLLOWED_ISSUES)) {
				i = new Intent(this, FollowedIssuesListActivity.class);
				startActivity(i);

			} else if (nextActivity.equals(MyApp.SETTINGS)) {
				i = new Intent(this, FragmentPreferencesActivity.class);
				startActivity(i);

			} else if (nextActivity.equals(MyApp.HOME)) {
				i = new Intent(this, HomeActivity.class);
				startActivity(i);

			} else if (nextActivity.equals(MyApp.NEW_REPORT)) {
				i = new Intent(this, NewReportActivity.class);
				startActivity(i);
			}
		}
	}

	// ------------------------------------------------------------------------
	// If user is logged in, show name in the upper right corner.
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.myMenu = menu;
		if (!name.isEmpty()) {
			menu.findItem(R.id.mnuLogin).setTitle(name);
		}
		else {
			menu.findItem(R.id.mnuLogin).setTitle("");
		}
		return true;
	}

	// -------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.simple_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuHelp:
			Toast.makeText(this, "Help for login", Toast.LENGTH_SHORT).show();
			return true;
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
	// -------------------------------------------------------------------
		private void displayMessage1(String msg) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("Alert").setMessage(msg)
					.setIcon(android.R.drawable.ic_dialog_alert);

			alertDialog = builder.create();

			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// stay on this activity.
						}
					});

			alertDialog.setCancelable(false);
			alertDialog.show();
		}
		
	// -------------------------------------------------------------------
	private void displayMessage2(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("Alert").setMessage(msg)
				.setIcon(android.R.drawable.ic_dialog_alert);

		alertDialog = builder.create();

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						logoutUser();
						startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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
	
	// -------------------------------------------------------------------------
	// Login out the user requires clearing out user info in shared preferences.
	
	private void logoutUser() {
		etEmail.setText("");
		etPassword.setText("");
		myMenu.findItem(R.id.mnuLogin).setTitle("login");
		loggedin = false;

		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(MyApp.USER_ID_KEY, "");
		editor.putString(MyApp.USER_DISPLAYNAME_KEY, "");
		editor.putString(MyApp.USER_TOKEN_KEY, "");
		editor.putString(MyApp.USER_EMAIL_KEY, "");
		editor.commit();
	}
	
	// -------------------------------------------------------------------------
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

}
