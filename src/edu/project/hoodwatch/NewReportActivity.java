package edu.project.hoodwatch;

/* The goal of this activity is to allow a user to submit a new issue utilizing the android phone, google-based
 * GPS services, and mapview. Simple data that persists across user sessions (i.e. a half filled out form) is 
 * stored using Shared Preferences.  Once complete, the information (picture included) is sent to the server via
 * a connection to the webservice API offered by CitySweeper.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;



public class NewReportActivity extends Activity implements OnClickListener,
		OnItemSelectedListener, SubmitReportTaskDelegate, TextWatcher {

	private final int CAMERA_ACTION_CODE = 0;
	private final int MAP_ACTION_CODE = 1;
	private EditText etDescription;
	private EditText etAddress;
	private boolean trustAddress;
	private ImageView ivPicture;
	private ProgressDialog progDialog;
	private Spinner spinnerCategory;
	private String name; // to be displayed in upper right corner
	private SharedPreferences prefs;
	private boolean loggedin;
	private boolean pictureTaken;
	private static String CLASS_NAME; // used for test logs only
	
	// Data to Submit
	private int mCategorySelectedPosition;
	private LatLng mLatLng;
	private String mStreetAddress;
	private String mDescription;
	

	// -------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_report);

		CLASS_NAME = getClass().getName();
		pictureTaken = false;

		// Locate resources.
		ActionBar actionBar = getActionBar();
		ImageButton ibMap = (ImageButton) findViewById(R.id.ibMap);
		Button btnClear = (Button) findViewById(R.id.button_clear);
		Button btnSubmit = (Button) findViewById(R.id.buttonSubmit);
		spinnerCategory = (Spinner) findViewById(R.id.spinnerIssueCategory);
		ivPicture = (ImageView) findViewById(R.id.imageViewPicture);
		etAddress = (EditText) findViewById(R.id.input_address);
		etDescription = (EditText) findViewById(R.id.input_description);

		// True so home button goes back one level instead of to root.
		actionBar.setDisplayHomeAsUpEnabled(true);

		// To make sure that keyboard does not show up from start
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// Catch any changes to the Address edit field
		trustAddress = false;
		etAddress.addTextChangedListener(this);
		
		// Create an ArrayAdapter using the string array and a default spinner
		// layout.
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.issue_categories,
				android.R.layout.simple_spinner_dropdown_item);

		// Specify the spinner layout to use when the list of choices appears.
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner.
		spinnerCategory.setAdapter(adapter);

		// If there is no camera, disable the camera button.
		PackageManager pm = getApplicationContext().getPackageManager();
		if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			ivPicture.setClickable(false);
		}

		// Set listeners.
		btnClear.setOnClickListener(this);
		btnSubmit.setOnClickListener(this);
		ibMap.setOnClickListener(this);
		spinnerCategory.setOnItemSelectedListener(this);
	}

	// -------------------------------------------------------------------------
	// Called when the activity is at the top of the activity stack and is ready
	// to interact with the user.
	protected void onResume() {
		super.onResume();
	
		// Populate the data from previously entered records.
		String savedData = null;
		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		
		// If user is logged in set the name.
		name = prefs.getString(MyApp.USER_DISPLAYNAME_KEY, "");
		// the name is displayed from onPrepareOptionsMenu method
	
		// Update description.
		savedData = prefs.getString("description", "");
		if (!savedData.isEmpty()) {
			etDescription.setText(savedData);
		}
	
		// Update the category.
		savedData = prefs.getString("category", "");
		if (!savedData.isEmpty()) {
			spinnerCategory.setSelection(Integer.parseInt(savedData));
		}
	
		// Update address.
		savedData = prefs.getString("address", "");
		if (!savedData.isEmpty()) {
			etAddress.setText(savedData);
		}
		etAddress.addTextChangedListener( this );

		
		// Update trustAddress.
		trustAddress = prefs.getBoolean("trustAddress", false);

		// Display photo in the custom image button.
		Boolean pic = false;
		pic = prefs.getBoolean("isPic", false);
		if (pic) {
			FileInputStream fis;
			try {
				fis = openFileInput(MyApp.USER_IMAGE_FILENAME);
				Bitmap bitmap = BitmapFactory.decodeStream(fis);
				ivPicture.setImageBitmap(bitmap);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		// Update latitude and longitude variable.
		String tmpLat = prefs.getString("latitude", "");
		String tmpLong = prefs.getString("longitude", "");
		if (!tmpLat.isEmpty() && !tmpLong.isEmpty()) {
			mLatLng = new LatLng(
					Double.parseDouble(tmpLat),
					Double.parseDouble(tmpLong));
		} 
		// Show San Francisco as default location.
		else {
			mLatLng = new LatLng(37.77711, -122.41963);
		}
	}

	// -------------------------------------------------------------------------
	@Override
	protected void onPause() {
		super.onPause();
		
		etAddress.removeTextChangedListener( this );
		// Save user input into shared preferences.
		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("category", Integer.toString(mCategorySelectedPosition));
		editor.putString("address", etAddress.getText().toString());
		editor.putString("description", etDescription.getText().toString());
		editor.putBoolean("trustAddress", trustAddress);
		if (pictureTaken) {
			editor.putBoolean("isPic", true);
		} else {
			editor.putBoolean("isPic", false);
		}
		editor.commit();
	}

	// -------------------------------------------------------------------------
	// Custom image button was clicked.
	public void cameraClick(View v) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, CAMERA_ACTION_CODE);
	}
	// -------------------------------------------------------------------------
	@Override
	public void onClick(View v) {
		// There are 4 buttons on this form.
		switch (v.getId()) {
		case R.id.ibMap:
			startActivityForResult(new Intent(this, MapActivity.class),
					MAP_ACTION_CODE);
			break;
		case R.id.buttonSubmit:
			if (!loggedin) {
				showDialogBox("new report");
			}
			validateAndSubmitForm();
			break;
		case R.id.button_clear:
			clearForm();
			break;
		}
	}

	// -------------------------------------------------------------------------
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		mCategorySelectedPosition = pos;
	}

	// -------------------------------------------------------------------------
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Nothing was selected from the spinner.
	}

	// -------------------------------------------------------------------------
	@Override
	public void reportWillSubmit() {
		progDialog = ProgressDialog.show(NewReportActivity.this, "Sending",
				"Sending your issue report...", true, false);
	}

	// -------------------------------------------------------------------------
	@Override
	public void reportDidSubmit() {
		stopProgDialog();
		clearForm();
		goHome();
	}

	// ------------------------------------------------------------------------
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!name.isEmpty()) {
			menu.findItem(R.id.mnuLogin).setTitle(name);
			loggedin = true;
		} else {
			menu.findItem(R.id.mnuLogin).setTitle("Login");
			loggedin = false;
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

	// -------------------------------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;

		switch (item.getItemId()) {
		case R.id.mnuLogin:
			i = new Intent(this, LoginActivity.class);
			i.putExtra("Activity", MyApp.HOME);
			startActivity(i);
			return true;
		case R.id.mnuHelp:
			Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.mnuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case android.R.id.home:
			// go back to home screen
			// FLAG_ACTIVITY_CLEAR_TOP clears the stack of activities
			i = new Intent(this, HomeActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// -------------------------------------------------------------------------
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// If result code == 0, the device back button was pressed,
		// so no actual data returned!
		if (resultCode != 0) {

			// Retrieve the picture from 'data' and save in 'reportImage'
			if (requestCode == CAMERA_ACTION_CODE) {
				Bundle extras = data.getExtras();

				// If extras == null, user exited camera app without taking a picture.
				if (extras != null) {
					pictureTaken = true;

					// Get the picture out of the extras.
					Bitmap mImageBitmap = (Bitmap) extras.get("data");
					
					// Display photo in the custom image button.
					ivPicture.setImageBitmap(mImageBitmap);

					// Save a picture file to the application specific storage.
					try {
						FileOutputStream ostr = getApplicationContext()
								.openFileOutput(MyApp.USER_IMAGE_FILENAME,	MODE_PRIVATE);
						mImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, ostr);
						ostr.flush();
						ostr.close();
					} catch (IOException e) {
						 Log.e(CLASS_NAME, "Error: " + e);
					}
				} else {
					pictureTaken = false;
				}
			}
			// Result from the MapActivity.
			else if (requestCode == MAP_ACTION_CODE) {
				Bundle extras = data.getExtras();

				// If extras == null, user exited map without saving a location.
				if (extras != null) {
					trustAddress = true;
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("address", extras.getString("Address"));
					editor.putString("latitude", Double.toString(extras.getDouble("Latitude")));
					editor.putString("longitude", Double.toString(extras.getDouble("Longitude")));
					editor.putBoolean("trustAddress", trustAddress);
					editor.commit();
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// Sends new report data to the web server.
	private void validateAndSubmitForm() {

		// Get text fields.
		String sDescription = etDescription.getText().toString();
		final String sAddress = etAddress.getText().toString();

		/*
		 * // For JSON String encodedImage = ""; if (mImageBitmap != null) { //
		 * Encode the image data. int bytes = getSizeInBytes(mImageBitmap);
		 * ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new
		 * buffer mImageBitmap.copyPixelsToBuffer(buffer); byte[] imageBytes =
		 * buffer.array(); // Base64 encoding is used to send and receive binary
		 * data over the network. encodedImage =
		 * Base64.encodeToString(imageBytes, Base64.NO_WRAP); }
		 */

		/*
		 * // =================================================================
		 * // THE add_issue.php WEB SERVICE CURRENTLY USES FORM DATA, NOT JSON
		 * DATA // Package up in JSON. Gson gson = new Gson(); HashMap<String,
		 * Object> hm = new HashMap<String, Object>(); hm.put("description",
		 * description); hm.put("imageData", encodedImage); hm.put("category",
		 * Integer.toString(categorySelectedPosition)); // more here
		 * 
		 * dataToSend = gson.toJson(hm); //
		 * =================================================================
		 */
		// Validation.
		boolean valid = true;
		String msg = "";

		if (mCategorySelectedPosition == 0) {
			valid = false;
			msg = "Please select a category.";
		}
		if (valid && sAddress.isEmpty()) {
			valid = false;
			msg = "Address is required.";
		}
		if (valid && sDescription.isEmpty()) {
			valid = false;
			msg = "Please provide a short\n" + "description of the problem.";
		}
		else {
			mDescription = sDescription;
		}
		// User either edited the address field or never went to the MapActivity
		if (valid
				&& (!trustAddress || (mLatLng.latitude == 0.0 && mLatLng.longitude == 0.0))) {
			final Geocoder geocoder = new Geocoder(getApplicationContext());
			if (Geocoder.isPresent()) {
				// Verify the address over the network if there is no longitude
				new Runnable() {
					@Override
					public void run() { // this is run on background thread
						try {
							final List<Address> addresses = geocoder
									.getFromLocationName(sAddress, 1);
							NewReportActivity.this
									.runOnUiThread(new Runnable() {
										@Override
										public void run() { // this is run back
															// on main UI thread
											if (addresses == null || addresses.isEmpty()) {
												showToast("The address is not valid.");
												return;
											}
											// A VALID ADDRESS IS RETURNED
											Address addr = addresses.get(0);
											mLatLng = new LatLng(addr.getLatitude(), addr.getLongitude());

											// BUT IS IT REALLY VALID???
											if ( addr.getCountryCode() == null ||
													addr.getPostalCode() == null ||
													addr.getLocality() == null || // City
													addr.getAdminArea() == null // State
													) {
												showToast("The address is not valid.");
												return;
											}
												
											String sAddress = "";
											// Make sure the address is local to
											// US
											String countryCode = addr.getCountryCode();
											if (countryCode != null && ! countryCode.equalsIgnoreCase("US")) {
												showToast("Please select address in US.");
												return;
											} else {
												sAddress = MyApp.formatAddress(addr);
											}
											SharedPreferences prefs = getSharedPreferences(
													MyApp.PREFS_NAME, 0);
											SharedPreferences.Editor editor = prefs
													.edit();
											editor.putString("address",
													sAddress);
											editor.commit();
											mStreetAddress = sAddress;
											trustAddress = true;
											submitNewReportData();
										}
									});
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							showToast("There is a network problem.");
						}
					}
				}.run();
			}
			else {
				showToast("Unable to validate address.");
			}
			return; // Don't (potentially) double submit the report data; see below.
		}

		if (!valid) {
			showToast(msg);
		}
		// Only if valid.
		else {
			mStreetAddress = sAddress;

			submitNewReportData();
		}
	}

	private void submitNewReportData() {
		URL url = null;
		try {
			url = new URL("http://sfsuswe.com/~s13g01/sweeper/web_services/add_issue.php");
		} catch (MalformedURLException e) {
			Log.d("MalformedURLException", e.toString());
		}

		// Package up form data into a list.
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
		nameValuePairs.add(new BasicNameValuePair("category", Integer.toString(mCategorySelectedPosition)));
		nameValuePairs.add(new BasicNameValuePair("street_address",	mStreetAddress));
		nameValuePairs.add(new BasicNameValuePair("description", mDescription));
		nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(mLatLng.latitude)));
		nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(mLatLng.longitude)));
		nameValuePairs.add(new BasicNameValuePair(MyApp.API_KEY, "KHF4KH6498GFHJ3J37XBNSHD"));
		
		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		nameValuePairs.add(new BasicNameValuePair("user_token", prefs.getString(MyApp.USER_TOKEN_KEY, null)));

		// Start background task.
		SubmitReportTask task = new SubmitReportTask(NewReportActivity.this, null, nameValuePairs);
		task.setDelegate(this);
		task.execute(url);
	}
	
	// -------------------------------------------------------------------------
	private void clearForm() {

		spinnerCategory.setSelection(0);
		etAddress.setText("");
		etAddress.setHint("Address");
		etDescription.setText("");
		etDescription.setHint("Enter Description (required)");
		ivPicture.setImageResource(R.drawable.camera_logo_gray_70);
		
		prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("category", "");
		editor.putString("address", "");
		editor.putString("latitude", "");
		editor.putString("longitude", "");
		editor.putString("description", "");
		editor.putBoolean("isPic", false);
		editor.commit();
		
		// Delete image file.
		File file = new File(getApplicationContext().getFilesDir(),
				MyApp.USER_IMAGE_FILENAME);
		file.delete();
	}

	// -------------------------------------------------------------------------
	private void goHome() {
		Intent i = new Intent(NewReportActivity.this, HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	// -------------------------------------------------------------------------
	private void stopProgDialog() {
		progDialog.dismiss();
	}

	
	
	// ----------------------------------------------------------------------
	// To which activity LoginActivity should return is in the 'activity'.

	private void showDialogBox(final String activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle("Alert")
				.setMessage(
						"To use this feature, you need to be logged in. \n"
								+ "Would you like to login now?")
				.setIcon(android.R.drawable.ic_dialog_alert);

		AlertDialog alertDialog = builder.create();

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i = new Intent(NewReportActivity.this,
								LoginActivity.class);
						i.putExtra("Activity", activity);
						startActivity(i);
					}
				});

		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Just stay on this screen.
					}
				});
		alertDialog.setCancelable(false);
		alertDialog.show();
	}

	// -------------------------------------------------------------------------
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	// -----------------------------------------------
	// TextWatcher delegate methods
	@Override
    public void afterTextChanged(Editable s) {
    	trustAddress = false;
    }

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1,
			int arg2, int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {}

}
