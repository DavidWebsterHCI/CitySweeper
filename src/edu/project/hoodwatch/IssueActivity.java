package edu.project.hoodwatch;

/*
 * Activity used to display a single issue (typically when a user clicks on an issue stublette from a list, 
 * it will take them to this: the full issue with a map/information etc.
 */
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; 
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class IssueActivity extends FragmentActivity 
//		implements DownloadImageTaskDelegate, OnClickListener { // use this line when buttons "Follow" and "Comment" will be implemented 
		implements DownloadImageTaskDelegate {
	private AsyncTask<URL, Integer, Bitmap> imageTask;
	private URL largeImageURL;
	private ImageView imageView;
	private static int INITIAL_ZOOM = 15;
	
	// ----------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.issue);
		
		// Set action bar to go home
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// to make sure that keyboard does not show up from start
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		imageView = (ImageView) findViewById(R.id.show_large_image);
		TextView thumb_label = (TextView) findViewById(R.id.thumb_label);
		
		// Unpack issue data.
		Bundle b = getIntent().getExtras();
		String issueJSONData = b.getString("IssueJSONData");
		// create an ArrayList of issue items from the string
		IssueData issue = new Gson().fromJson(issueJSONData, IssueData.class);
		
		Double dLatitude = Double.parseDouble(issue.getLatitude());
		Double dLongitude = Double.parseDouble(issue.getLongitude());

		GoogleMap map;
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		LatLng latLong = new LatLng(dLatitude, dLongitude);
		
		// Move the camera instantly to the location with a zoom of 15
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, INITIAL_ZOOM));
		
	    // Zoom in, animating the camera
	    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
	    
	    MarkerOptions m = new MarkerOptions().position(latLong);
		map.addMarker(m);

		String thumbImageURL = issue.getThumbURL();
		String largeIMageURL = issue.getImage_url();
		String decodedThumpURL;
		String decodedLargeImageURL;
		URL thumbURL;
		// Decode image url's.
		try {
			decodedThumpURL = URLDecoder.decode(thumbImageURL, "UTF-8");
			decodedLargeImageURL = URLDecoder.decode(largeIMageURL, "UTF-8");
			thumbURL = new URL (decodedThumpURL);
			largeImageURL = new URL (decodedLargeImageURL);
			if (largeImageURL != null && !decodedLargeImageURL.endsWith("/large_issue_default.jpg")) {
				ImageLoadTask ilt = new ImageLoadTask();
				ilt.setDelegate(this);
				imageTask = ilt.execute(thumbURL);
				imageView.setClickable(true);
			} else {
				imageView.setVisibility(View.GONE);
				thumb_label.setVisibility(View.GONE);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}	

		// Display text fields.

		TextView category = (TextView) findViewById(R.id.category);
		if (category != null) {
			category.setText(issue.getCategory());
		}
		TextView date = (TextView) findViewById(R.id.date);
		if (date != null) {
			date.setText(issue.getDate());
		}
		TextView reported_by = (TextView) findViewById(R.id.reported_by);
		if (reported_by != null) {
			String s = "Reported by: ".concat(issue.getReportedBy().toUpperCase());
			reported_by.setText(s);
		}
		TextView address = (TextView) findViewById(R.id.address);
		if (address != null) {
			address.setText(issue.getAddress());
		}
		TextView description = (TextView) findViewById(R.id.textdescription);
		if (description != null) {
			description.setText(issue.getDescription());
		}
		
		// Do not remove this code!
		
//		TextView tvNumFollow = (TextView) findViewById(R.id.num_follows);
//		String numValue = "0";
//		if (tvNumFollow != null) {
//			String sNumFollow = tvNumFollow.getText().toString();
//			String dataNumFollow = issue.getNum_follows();
//			if (dataNumFollow != null) {
//				numValue = dataNumFollow;
//			}
//			tvNumFollow.setText(sNumFollow.concat(numValue));
//		}
//
//		TextView tvNumComments = (TextView) findViewById(R.id.num_comments);
//		numValue = "0";
//		if (tvNumComments != null) {
//			String sNumCommented = tvNumComments.getText().toString();
//			String dataNumCommented = issue.getNum_comments();
//			if (dataNumCommented != null) {
//				numValue = dataNumCommented;
//			}
//			tvNumComments.setText(sNumCommented.concat(numValue));
//		}	
	}
	
	// -------------------------------------------------------------------
	// When the thumb image is clicked.
	public void thumbClick(View v) {
		Intent i = new Intent(this, LargeImageActivity.class);
		i.putExtra("URL", largeImageURL);
		startActivity(i);
	}

	// -------------------------------------------------------------------
	// Do not remove this code!
	// When buttons 'follow' or 'comment' are clicked.
//	@Override
//	public void onClick(View v) {
//		
//		if (v.getId() == R.id.follow) {
//			Toast.makeText(this, "Follow", Toast.LENGTH_SHORT).show();
//		} 
//		// if R.id.comment
//		else {
//			Toast.makeText(this, "Comment", Toast.LENGTH_SHORT).show();
//		}
//	}

	// -------------------------------------------------------------------
	// If user exits activity before image was loaded.
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (imageTask != null) {
			imageTask.cancel(true);
		}
	}

	// ------------------------------------------------------------------
	// This code deals with the menu options.
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
			Toast.makeText(this, "Help for an issue", Toast.LENGTH_SHORT).show();
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

	// ----------------------------------------------------------------------
	@Override
	public void taskWillExecute() {
		// nothing to do.
	}

	// ----------------------------------------------------------------------
	@Override
	public void taskDidExecute(Bitmap bitmap, ImageView iv) {
		imageView.setImageBitmap(bitmap);
	}
	
}
