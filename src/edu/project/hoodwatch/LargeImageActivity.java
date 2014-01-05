package edu.project.hoodwatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
	
/*
 * Goals: to retain image on rotation; to download image in the background
 * task only once. Comments: We can not use onSaveInstanceState(Bundle
 * outState) because the AsynTask is not serializable and the Bitmap is too
 * big to be Parcelable. So the AsyncTask is just a static variable and the
 * Bitmap is saved to a file and referenced by URI.
 * 
 * Activities offer the onSaveInstanceState handler to persist data
 * associated with UI state across sessions. It's designed specifically to
 * persist UI state should an Activity be terminated by the run time, either
 * in an effort to free resources for foreground applications or to
 * accommodate restarts caused by hardware configuration changes. 
 * If an Activity is closed by the user (by pressing the Back button), or
 * programmatically with a call to finish, the instance state bundle will
 * not be passed in to onCreate or onRestoreInstanceState when the Activity
 * is next created. Data that should be persisted across user sessions
 * should be stored using Shared Preferences.
 */
public class LargeImageActivity extends Activity implements DownloadImageTaskDelegate {
	private ProgressDialog progDialog;
	
	// -------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.large_issue_image);

		// Set action bar to go home   
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		ImageView view = (ImageView) findViewById(R.id.large_image);
		
		Context context = getApplicationContext();
		File imageFile = new File(context.getFilesDir(), MyApp.LARGE_IMAGE_FILENAME);
		if (savedInstanceState != null) {
			if (MyApp.imageTask != null) {
				((ImageLoadTask)MyApp.imageTask).setDelegate(this);
				showProgDialog();
			}
			// See note in taskDidExecute about using setImageURI().
			if (imageFile.exists()) {
				view.setImageURI(Uri.fromFile(imageFile));
			}
		} 
		// When activity is first loaded.
		else {
			Bundle extras = getIntent().getExtras();

			if (imageFile.exists()) {
				imageFile.delete();
			}
			URL largeImageURL = (URL) extras.get("URL");
			ImageLoadTask ilt = new ImageLoadTask();
			ilt.setDelegate(this);
			MyApp.imageTask = ilt.execute(largeImageURL);
		}
	}

	// -------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.large_image_menu, menu);
		return true;
	}

	// -------------------------------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
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

	// -------------------------------------------------------------------
	// if user exits activity before image was loaded
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopProgDialog();
	}

	// -------------------------------------------------------------------
	@Override
	public void taskWillExecute() {
		showProgDialog();
	}

	// -------------------------------------------------------------------
	@Override
	public void taskDidExecute(Bitmap inBitmap, ImageView imageView) {
		stopProgDialog();
		
		// if some problems getting image
		if (inBitmap == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle("Alert")
					.setMessage("Unable to download image. \n Try again later.")
					.setIcon(android.R.drawable.ic_dialog_alert);

			AlertDialog alertDialog = builder.create();

			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(LargeImageActivity.this,
									HomeActivity.class));
						}
					});

			alertDialog.setCancelable(false);
			alertDialog.show();
		}
		else {
			ImageView iv = (ImageView) findViewById(R.id.large_image);
			Context context = getApplicationContext();
			try {
				// Save bitmap to file to avoid FAILED BINDER TRANSACTION errors
				// due to Android's low Binder transaction buffer size.
				// Instead of setImageBitmap with a bitmap object, just
				// setImageUri to the saved bitmap file.
				FileOutputStream ostr = context.openFileOutput(MyApp.LARGE_IMAGE_FILENAME, MODE_PRIVATE);
				inBitmap.compress(Bitmap.CompressFormat.PNG, 100, ostr);
				ostr.flush();
				ostr.close();
				File imageFile = new File(context.getFilesDir(), MyApp.LARGE_IMAGE_FILENAME);
				iv.setImageURI(Uri.fromFile(imageFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		MyApp.imageTask = null;
	}

	// ------------------------------------------------------------------
	// Shows progress dialog.
	void showProgDialog() {
		if (progDialog == null) {
			progDialog = ProgressDialog.show(this, "Progress", "Loading image",
					true, false);
		}
	}

	// ------------------------------------------------------------------
	// Hides an existing progress dialog.
	void stopProgDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
			progDialog = null;
		}
	}

}
