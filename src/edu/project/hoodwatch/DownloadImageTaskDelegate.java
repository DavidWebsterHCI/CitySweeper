package edu.project.hoodwatch;

/*
 * An interface for image downloading from back-end server
 */

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface DownloadImageTaskDelegate {
	
	public void taskWillExecute();
	
	public void taskDidExecute(Bitmap bitmap, ImageView imageView);

}

