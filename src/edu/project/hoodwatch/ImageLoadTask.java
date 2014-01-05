package edu.project.hoodwatch;

/*
 * Used in IssueActivity, LargeImageActivity, and IssueAdapter to perform pertinent functions
 */

import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

class ImageLoadTask extends AsyncTask<URL, Integer, Bitmap> {
	private DownloadImageTaskDelegate taskDelegate = null;
	ImageView imageView = null;
	Bitmap bitmap = null;
	
	// -----------------------------------------------------------------
	// Constructor used in IssueActivity and LargeImageActivity.
	public ImageLoadTask() {
		super();
	}
	
	// Constructor used in IssueAdapter.
	public ImageLoadTask(ImageView inImageView) {
		this();
		this.imageView = inImageView;
	}
	
	// -----------------------------------------------------------------
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// If not interested in pre- and post- operations
			// then don't call the delegate methods
			if (taskDelegate != null) {
				taskDelegate.taskWillExecute();
			}
		}

	// -----------------------------------------------------------------
	public void setDelegate(DownloadImageTaskDelegate inDelegate) {
		taskDelegate = inDelegate;
	}

	// -----------------------------------------------------------------
	//
	@Override
	protected Bitmap doInBackground(URL... url) {
		if (!isCancelled()) {
			try {
				bitmap = BitmapFactory.decodeStream(url[0].openStream());
			} catch (Exception e) {
				Log.d("ImageLoadTask", e.toString());
			}
		}
		return bitmap;
	}

	// ------------------------------------------------------------------
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		
		// If not interested in pre- and post- operations
		// then don't call the delegate methods
		if (taskDelegate != null) {
			taskDelegate.taskDidExecute(bitmap, imageView);
		}
		
	}

}