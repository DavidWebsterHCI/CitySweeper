package edu.project.hoodwatch;

/*
 * This class main functionality is thread control.  Multiple threads are used to download images/gather data 
 * from the backend to display to the user.  This class allows for those threads to work in synchronization
 */

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class IssueAdapter extends ArrayAdapter<IssueData> implements DownloadImageTaskDelegate {
	private ArrayList<IssueData> dataObjects;
	private ThreadPoolExecutor executor;

	// ---------------------------------------------------------------------
	// constructor
	public IssueAdapter(Context context, int textViewResourceId,
			ArrayList<IssueData> objects) {
		super(context, textViewResourceId, objects);
		this.dataObjects = objects;
		
		// Creates a thread pool that reuses a fixed number of threads operating
		// off a shared unbounded queue; for image downloading.
		this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
	}

	// ---------------------------------------------------------------------
	// extracts the correct data from the data object and assigns this data to
	// the views in the row which is representing the data
	public View getView(int position, View convertView, ViewGroup parent) {

		ImageView ivThumbnail = null;
		if (convertView != null) {
			ivThumbnail = (ImageView) convertView.findViewById(R.id.image);
			// see if a task is running on this view
			ImageLoadTask tilt = (ImageLoadTask)ivThumbnail.getTag();
			if (tilt != null) {
				tilt.cancel(true); // kill the image load task
				convertView.setTag(null);
			}
			ivThumbnail.setImageBitmap(null);
		}
		
		// Assign the view we are converting to a local variable.
		View itemView = convertView;

		// Force not using convertView
		//itemView = null;
		// If the view is null, we have to inflate it.
		if (itemView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(R.layout.item, null);
		}

		// 'position' refers to the position of the current
		// object in the list. 
		IssueData iData = dataObjects.get(position);

		if (iData != null) {
			TextView tvCategory = (TextView) itemView.findViewById(R.id.category);
			TextView tvDate = (TextView) itemView.findViewById(R.id.date);
			TextView tvReportedBy = (TextView) itemView.findViewById(R.id.reported_by);
			TextView tvDescription = (TextView) itemView.findViewById(R.id.description);
			TextView tvAddress = (TextView) itemView.findViewById(R.id.address);
			ivThumbnail = (ImageView) itemView.findViewById(R.id.image);

			if (ivThumbnail != null) {
				//ivThumbnail.setImageResource(R.drawable.green_can);
				// At any point, at most 5 threads will be downloading images.
				ImageLoadTask ilt = new ImageLoadTask(ivThumbnail);
				ilt.setDelegate(this);
				
				String sUrl = iData.getThumbURL();
				String decodedURL;
				URL url;
				
				try {
					decodedURL = URLDecoder.decode(sUrl, "UTF-8");
					url = new URL (decodedURL);
					ivThumbnail.setTag(ilt); // attach the Task to the itemView
					ilt.executeOnExecutor(executor, url);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();	
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}	
			}
			
			if (tvReportedBy != null) {
				String s = "Reported by: ".concat(iData.getReportedBy().toUpperCase());
				tvReportedBy.setText(s);
			}
			if (tvAddress != null) {
				tvAddress.setText(iData.getAddress());
			}
			if (tvCategory != null) {
				tvCategory.setText(iData.getCategory());
			}
			if (tvDate != null) {
				tvDate.setText(iData.getDate());
			}
			if (tvDescription != null) {
				tvDescription.setText(iData.getDescription());
			}
		}

		// The view must be returned to our activity.
		return itemView;

	}

	// ---------------------------------------------------------------------
	@Override
	public void taskWillExecute() {
	}

	// ---------------------------------------------------------------------
	@Override
	public void taskDidExecute(Bitmap bitmap, ImageView imageView) {
		imageView.setTag(null);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		}
		// If there is no thumbnail image.
		else {
			imageView.setImageResource(R.drawable.green_can);
		}
	}

}
