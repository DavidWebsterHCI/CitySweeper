package edu.project.hoodwatch;

/* This map uses Google Maps Android API v2. See documentation here:
 * https://developers.google.com/maps/documentation/android/
 * 
 * Goal: This activity's goal is to gather the location of an environmental issue from the user.  
 * The user can navigate the map like a normal google map, drop a pin where they noticed the issue
 * and confirm the address selected.  This address with then be geocoded into a street address and 
 * passed back to the calling activity in both latlng and street address form.
 */	

import java.io.IOException;
import java.util.List;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



	
public class MapActivity extends FragmentActivity implements 
		LocationListener, // onLocationChanged, onStatusChanged, onProviderEnabled, onProviderDisabled
		Listener, // onGpsStatusChanged
		GoogleMap.OnInfoWindowClickListener, // onInfoWindowClick
		GoogleMap.OnMapClickListener // onMapClick
		{

	private static boolean isLocInitialized = false;
	private static String CLASS_NAME;
	private GoogleMap googleMap;
	private TextView label;
	private static final String MARKER_OPTION_TITLE = "Tap here to confirm this location";


	// -------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.map);
	
		// for test logs
		CLASS_NAME = getClass().getName();
		label = (TextView) findViewById(R.id.tv_location);  
	}

	// -------------------------------------------------------------------------
	@Override
	public void onLocationChanged(Location location) {
		// not used
	}

	// -------------------------------------------------------------------------
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(CLASS_NAME, "onProviderDisabled " + provider);
	}

	// -------------------------------------------------------------------------
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(CLASS_NAME, "onProviderEnabled " + provider);
	}

	// -------------------------------------------------------------------------
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(CLASS_NAME, "onStatusChanged");
		switch (status) {
		case LocationProvider.AVAILABLE:
			Log.d(CLASS_NAME, provider + " available");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			Log.d(CLASS_NAME, provider + " unavailable");
			break;
		case LocationProvider.OUT_OF_SERVICE:
			Log.d(CLASS_NAME, provider + " out of service");
			break;
		}
	}

	// ----------------------------------------------------------------------
	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			Log.d(CLASS_NAME, "GPS First Fix");
			break;
		case GpsStatus.GPS_EVENT_STARTED:
			Log.d(CLASS_NAME, "GPS Started");
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			Log.d(CLASS_NAME, "GPS Stopped");
			break;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			// don't log
			break;
		}
	}

	// -------------------------------------------------------------------------
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isLocInitialized = false;
	}

	// -------------------------------------------------------------------------
	@Override
	public void onResume() {
		super.onResume();

		// Find out if Google Play is available
		int gpStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		if (gpStatus != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(gpStatus,
					this, requestCode);
			dialog.show();
		} 
		// Google Play is good to go.
		else { 
			// Get the map and set the location.
			SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
			googleMap = fm.getMap();
			
			/* The My Location button appears in the top right corner of the
			 * screen only when the My Location layer is enabled.
			 */
			googleMap.setMyLocationEnabled(true);

			// Restore map state from saved shared preferences.
			SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
			String latitudeString = prefs.getString("latitude", "");
			String longitudeString = prefs.getString("longitude", "");
			if (!latitudeString.isEmpty() && !longitudeString.isEmpty()) {
				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.position(new LatLng(
						Double.parseDouble(latitudeString),
						Double.parseDouble(longitudeString)));
				markerOptions.title(MapActivity.MARKER_OPTION_TITLE);
				googleMap.addMarker(markerOptions).showInfoWindow();
			}

			// Getting LocationManager object from System Service LOCATION_SERVICE.
			LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider.
			Criteria criteria = new Criteria();

			// Getting the name of the best provider.
			String provider = locationManager.getBestProvider(criteria, true);

			// Getting current location? or
			// Getting last known location.
			Location location = locationManager.getLastKnownLocation(provider);

			// Initialize the map to users location, but only once (rotation
			// causing new life cycle calls shouldn't reset the camera.)
			if (!isLocInitialized) {
				isLocInitialized = true;
				LatLng latLng = null;

				// If no location for a marker is saved, initialize on the
				// user's location, otherwise initialize onto the marker location.
				if (latitudeString.isEmpty() || longitudeString.isEmpty()) {
					latLng = new LatLng(location.getLatitude(), location.getLongitude());
				} 
				// If last marker position was saved.
				else {
					latLng = new LatLng(
							Double.parseDouble(latitudeString),
							Double.parseDouble(longitudeString));
					label.setText(prefs.getString("address", ""));
				}
				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f));
			}

			boolean isOnGPS = locationManager.isProviderEnabled("gps");
			if (!isOnGPS) {
				showToast("GPS signal is not found.");
			}
			
			boolean isOnPassive = locationManager.isProviderEnabled("passive");
			if (!isOnPassive) {
				showToast("Passive signal not found.");
			}
			
			boolean isOnNetwork = locationManager.isProviderEnabled("network");
			if (!isOnNetwork) {
				showToast("Network signal is not found.");
			}

//			if(location!=null){
//				onLocationChanged(location);
//			}

			googleMap.setOnInfoWindowClickListener(this);
			googleMap.setOnMapClickListener(this);
					
			// Update the location found every second
			locationManager.requestLocationUpdates(provider, 1000, 0, this);
		}
	}
	
	// -------------------------------------------------------------------------
	// To prevent depletion of battery power?

	@Override
	protected void onPause() {
		super.onPause();
		googleMap.setMyLocationEnabled(false);
	}

	// -------------------------------------------------------------------------
	@Override
	public void onMapClick(LatLng latLng) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.title(MapActivity.MARKER_OPTION_TITLE);
	
		// Store the marker in a static variable, so it can be recreated
		// on configuration change (rotation)
		SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("latitude", Double.toString(latLng.latitude));
		editor.putString("longitude", Double.toString(latLng.longitude));
		editor.commit();

	
		// Deletes last taped position marker and then place the new one.
		googleMap.clear();
		googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.addMarker(markerOptions).showInfoWindow();
		
		final Geocoder geocoder = new Geocoder(getApplicationContext());
		final LatLng location = latLng;

		if (Geocoder.isPresent()) {
			final StringBuilder sbAddress = new StringBuilder();

			/* The geocoding or reverse geocoding should not be done on the UI
			 * thread as it may involve server access, and thus might cause the
			 * system to display an Application Not Responding (ANR) dialog to
			 * the user. The work has to be done in a separate thread.
			 */
			
			new Runnable() {
				@Override
				public void run() { // this is run on background thread
					try {
						final List<Address> addresses = geocoder
								.getFromLocation(location.latitude,
										location.longitude, 1);
						MapActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() { // this is run back on main UI thread
								String sAddress = "";
								// Make sure the address is local to US
								if (!addresses.get(0).getCountryCode().equalsIgnoreCase("US")) {
									showToast("Please select address in US.");
								} else {
									if (addresses.size() > 0) {
										Address address = addresses.get(0);
										sAddress = MyApp.formatAddress(address);
									}
								}
								// Save tapped location to shared preferences.
								SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, 0);
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString("address", sAddress);
								editor.commit();
								label.setText(sAddress);

								isLocInitialized = false;
							}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.run();
		}
	}

	// -------------------------------------------------------------------------
	// When a user clicks on a marker's info window with
	// "Tap here to confirm this location".
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		// Find the closest address to the user's marker
		// and return it to NewReportActivity

		LatLng location = marker.getPosition();

		SharedPreferences prefs = getSharedPreferences(MyApp.PREFS_NAME, MODE_PRIVATE);
		Intent resultIntent = new Intent();
		resultIntent.putExtra("Address", prefs.getString("address", ""));
		resultIntent.putExtra("Latitude", location.latitude);
		resultIntent.putExtra("Longitude", location.longitude);

		setResult(MapActivity.RESULT_OK, resultIntent);
		isLocInitialized = false;
		finish();
	}

	// -------------------------------------------------------------------------
	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
}
