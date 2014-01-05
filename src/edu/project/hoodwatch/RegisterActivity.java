package edu.project.hoodwatch;
/*
 * This class allows for a user to create a new account via a registering webservices
 */
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

public class RegisterActivity extends Activity implements OnClickListener, WebTaskDelegate {
	private ProgressDialog progDialog;
	private EditText name;
	private EditText password;
	private EditText email;
	private AlertDialog alertDialog;
	private static byte[] buff = new byte[128];

	// --------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// To make sure that keyboard does not show up from start
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		name = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		email = (EditText) findViewById(R.id.emailaddress);
		Button btnRegister = (Button) findViewById(R.id.register);
		btnRegister.setOnClickListener(this);

	}

	// --------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.min_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.mnuHelp:
			Toast.makeText(this, "Help for registering", Toast.LENGTH_SHORT)
					.show();
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
	
	// --------------------------------------------------------------------
		@Override
		protected void onDestroy() {
			super.onDestroy();
			if (alertDialog != null) {
			alertDialog.dismiss();
			}
		}

	// -------------------------------------------------------------------
	void showProgDialog() {
		if (progDialog == null) {
			progDialog = ProgressDialog.show(this, "Registration",
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
	public void onClick(View v) {
		// Button register was clicked.

		// Validation.
		boolean valid = true;
		String sName = name.getText().toString();
		String sEmail = email.getText().toString();
		String sPassword = password.getText().toString();
		String msg = "";
		
		if (sName.isEmpty()) {
			valid = false;
			msg = "Please enter name.";
		}
		if (valid && sEmail.isEmpty()) {
			valid = false;
			msg = "Please enter email.";
		}
		if (valid && sPassword.isEmpty()) {
			valid = false;
			msg = "Please enter password.";
		}
		if (!valid) {
			displayMessage(msg);
		}
		// Only if valid.
		else {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);

			nameValuePairs.add(new BasicNameValuePair(MyApp.API_KEY,
					"KHF4KH6498GFHJ3J37XBNSHD"));
			nameValuePairs.add(new BasicNameValuePair(MyApp.USER_EMAIL_KEY,
					email.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair(
					MyApp.USER_DISPLAYNAME_KEY, name.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair(MyApp.USER_PASSWORD_KEY,
					password.getText().toString()));

			try {
				URL url = new URL("http://sfsuswe.com/~s13g01/sweeper/web_services/register_user.php");
				WebServiceTask task = new WebServiceTask(nameValuePairs);
				task.setDelegate(this);
				task.execute(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
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
			displayMessage("Unable to connect.\nTry again later.");
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
				JSONObject object = (JSONObject) new JSONTokener(out).nextValue();
				
				String result = object.getString("result");
				String reason = object.getString("reason");

				if (statusCode != HttpURLConnection.HTTP_CREATED) {
					String errorcode = object.getString("errorcode");
					String errorMessage = "Unknown error.";
					if (errorcode.equalsIgnoreCase("AccountAlreadyExists")) {
						errorMessage = "This email address is already used.";
					}
					showToast(errorMessage);
				}
				else {
					if (result.equals("false")) {
						// !!! THIS SHOULD NEVER HAPPEN !!!
						showToast(reason);
					}
					// If result = true.
					else {
						String id = object.getString("id");
						String displayname = object.getString("displayname");
						String token = object.getString("token");
						
						String msg = "Result: " + result + "\nReason: "
								+ reason + "\nId: " + id;
						showToast(msg);
						
						// Data in Shared Preferences will persist across user sessions
						// even if your application is killed.
						SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString(MyApp.USER_ID_KEY, id);
						editor.putString(MyApp.USER_DISPLAYNAME_KEY, displayname);
						editor.putString(MyApp.USER_TOKEN_KEY, token);
						editor.commit();
						// Take user home.
						startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
					}
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}

	// -------------------------------------------------------------------
	private void displayMessage(String msg) {
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
	
	// -------------------------------------------------------------------------
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

}
